package oculi

/*
 * RISCV MacPlugin
 * 
 * Copyright (c) 2018 Limacon Audio Limited
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import spinal.core._
import vexriscv.plugin.Plugin
import vexriscv.{DecoderService, Stageable, VexRiscv}

// Parameterization class for the MacPlugin
case class MacPluginConfig( accumulatorPeak : Int,              //Peak value storable by the acumulator (31 => 2^31-1
                            accumulatorResolution : Int,        //Resolution of the accumulator (32 => 2^-32)
                            accumulatorReadPeak : Int,          //Rescale of the accumulator when the cpu read it
                            accumulatorReadResolution : Int,
                            leftPeak : Int,                     //MAC left opperand fixed point paramaters
                            leftResolution : Int,
                            rightPeak : Int,                    //MAC right opperand fixed point paramaters
                            rightResolution : Int){
  
  //Define some "typedef" to easily instanciate fixed points later
  
  def leftType() = SFix(leftPeak exp, leftResolution exp)
  def rightType() = SFix(rightPeak exp, rightResolution exp)
  def mulResultType() = SFix(rightPeak + leftPeak + 1 exp, (1 + leftPeak - leftResolution) + (1 +rightPeak - rightResolution) bits)
  def accumulatorType() = SFix(accumulatorPeak exp, accumulatorResolution exp)
  def accumulatorReadType() = SFix(accumulatorReadPeak exp, accumulatorReadResolution exp)
  
}

object MacPluginConfig{
  // Default parametrization object for the MacPlugin
  // MAC operands are Q16,16 - Accumulator is Q32.32
  // Accumulator read by the CPU is Q16.16
  def default = MacPluginConfig(
    accumulatorPeak = 31,
    accumulatorResolution = -32,
    accumulatorReadPeak = 16,
    accumulatorReadResolution = -16,
    leftPeak = 15,
    leftResolution = -16,
    rightPeak = 15,
    rightResolution = -16
  )
}

//Implement two instruction.
//
//MAC_ACCUMULATOR_ACCESS to read the content of the accumulator and clear it
//Instruction encoding :
//0000000----------000-----0001011
//                    |RD |
//
//MAC_PUSH to multiply the two opperands and accumulate them into the accumulator
//Instruction encoding :
//0000001----------000-----0001011
//       |RS2||RS1|
//
//Note :  RS1, RS2, RD positions follow the RISC-V spec and are common for all instruction of the ISA

class MacPlugin(config : MacPluginConfig) extends Plugin[VexRiscv]{
  import config._
  
  //Define some CPU-pipelinable signal deinitions
  object IS_MAC_ACCUMULATOR_ACCESS_INSTRUCTION extends Stageable(Bool) 
  object IS_MAC_PUSH_INSTRUCTION extends Stageable(Bool)
  object MUL_RESULT extends Stageable(mulResultType())

  //Callback to setup the plugin and ask for different services
  override def setup(pipeline: VexRiscv): Unit = {
    import pipeline.config._

    //Retrieve the DecoderService instance
    val decoderService = pipeline.service(classOf[DecoderService])

    //Specify the instruction decoding which should be applied when the instruction match the 'key' parttern
    //MAC_ACCUMULATOR_ACCESS instruction encoding =>
    decoderService.addDefault(IS_MAC_ACCUMULATOR_ACCESS_INSTRUCTION, False)
    decoderService.add(
      key = M"0000000----------000-----0001011", //custom-0 instruction

      //Decoding specification when the 'key' pattern is recognized in the instruction
      List(
        IS_MAC_ACCUMULATOR_ACCESS_INSTRUCTION   -> True,
        REGFILE_WRITE_VALID         -> True, //Enable the register file write
        BYPASSABLE_EXECUTE_STAGE    -> True, //Notify the hazard management unit that the instruction result is already accessible in the EXECUTE stage (Bypass ready)
        BYPASSABLE_MEMORY_STAGE     -> True  //Same as above but for the memory stage
      )
    )

    //MAC_PUSH instruction encoding =>
    decoderService.addDefault(IS_MAC_PUSH_INSTRUCTION, False)
    decoderService.add(
      key = M"0000001----------000-----0001011", //custom-0 instruction

      //Decoding specification when the 'key' pattern is recognized in the instruction
      List(
        IS_MAC_PUSH_INSTRUCTION   -> True,
        RS1_USE -> True,  //Enable the register file source 1 reading
        RS2_USE -> True
      )
    )
  }

  override def build(pipeline: VexRiscv): Unit = {
    import pipeline._
    import pipeline.config._

    //Define some stuff into the global scope of the pipeline (not mandatory, but allow to give a names to signals defined into it)
    val global = pipeline plug new Area {
      val accumulator = Reg(accumulatorType())
    }

    //Add some logic into the execute stage of the cpu
    execute plug new Area {
      import execute._  //import execute._ alow to write    input(xxx)  in place of execute.input(xxx)

      //Here all the calculation is done by using the SFix arithetic.
      val mulLeft = leftType()
      val mulRight = rightType()

      mulLeft.raw := input(RS1).asSInt.resized //Capture the register file source 1 and assign it into the raw data of the mulLeft SFix
      mulRight.raw := input(RS2).asSInt.resized

      //Insert the MUL_RESULT value into the pipeline.
      //The multiplication is done in a single cycle, which could be an issue from a FMAX perspective.
      insert(MUL_RESULT) := mulLeft*mulRight

      //Reformat the accumulator value into the CPU readable one
      val accumulatorRead = accumulatorReadType()
      accumulatorRead := global.accumulator.truncated

      when(input(IS_MAC_ACCUMULATOR_ACCESS_INSTRUCTION)) { //When the current instruction in the execute stage is a MAC_ACCUMULATOR_ACCESS one
        //Then override the REGFILE_WRITE_DATA value with the accumulator one.
        //It will be written back in the register file in the writeback cpu stage
        input(REGFILE_WRITE_DATA) := accumulatorRead.asBits.resized

        //Logic to halt the CPU when a accumulator hazzard is present
        when(arbitration.isValid) { //When the current instruction is valid (so realy existing)
          //Track hazard on the accumulator value
          when(memory.arbitration.isValid    && (memory.input(IS_MAC_PUSH_INSTRUCTION)    || memory.input(IS_MAC_ACCUMULATOR_ACCESS_INSTRUCTION))
            || writeBack.arbitration.isValid && (writeBack.input(IS_MAC_PUSH_INSTRUCTION) || writeBack.input(IS_MAC_ACCUMULATOR_ACCESS_INSTRUCTION))){
            arbitration.haltItself := True //Order to halt the execute stage for the current cycle
          }
        }
      }
    }

    memory plug new Area {
      //memory stage only used as a retiming one for the multiplication, so, nothing to do here, the autopipelining of Stageable value will do everything
    }

    writeBack plug new Area {
      import writeBack._

      //When there is a MAC_PUSH instruction present in the writeback stage
      when(input(IS_MAC_PUSH_INSTRUCTION)) {
        when(arbitration.isFiring) { //When the instruction is currentrly fireing (which mean "There is a real instruction in the stage, and nothing stop it/unschedule it)
          global.accumulator := global.accumulator + input(MUL_RESULT)
        }
      }

      //When there is a MAC_ACCUMULATOR_ACCESS instruction
      when(input(IS_MAC_ACCUMULATOR_ACCESS_INSTRUCTION)) {
        when(arbitration.isFiring) { //and it realy happend
          //Clear the accumulator.
          //The reason why it is done in the writeBack stage in place of the execute one, is because in the writeback stage,
          //nothing can unschedule the instruction in the writeback stage except it self. Things which can unschedule instruction in other stages are in general jumps and exception.
          global.accumulator := 0
        }
      }
    }
  }
}
