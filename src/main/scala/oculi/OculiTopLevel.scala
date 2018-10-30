package oculi

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import vexriscv.{plugin, _}
import vexriscv.demo.{SimpleBus, _}
import vexriscv.plugin._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object OculiCore {

  def main(args: Array[String]): Unit = {
    SpinalVerilog(OculiCore)
  }
  
  
 def oculiCore() = new VexRiscv(
    config = VexRiscvConfig(
      plugins = List(
        
        new IBusSimplePlugin(
          resetVector = 0x80000000l,
          cmdForkOnSecondStage = false,
          cmdForkPersistence = true,
          prediction = NONE,
          catchAccessFault = false,
          compressedGen = false,
          injectorStage = true,
          rspHoldValue = false
        ),
        
        new DBusSimplePlugin(
          catchAddressMisaligned = true,
          catchAccessFault = false
        ),
        
        new DecoderSimplePlugin(
          catchIllegalInstruction = false
        ),
        
        new RegFilePlugin(
          regFileReadyKind = plugin.SYNC,
          zeroBoot = false
        ),
        
        new MulDivIterativePlugin(
          genMul = true,
          genDiv = true,
          mulUnrollFactor = 1,
          divUnrollFactor = 1
        ),
		
		new MacPlugin(SFixMacPluginConfig.default), // Q16.16 and Q32.32 acc
        
        new IntAluPlugin,
        
        new SrcPlugin(
          separatedAddSub = true,
          executeInsertion = false,
          decodeAddSub = false
        ),
        
        new FullBarrelShifterPlugin(
          earlyInjection = false
        ),
        
        new HazardSimplePlugin(
          bypassExecute = true,
          bypassMemory = true,
          bypassWriteBack = true,
          bypassWriteBackBuffer = true
        ),
        
        new BranchPlugin(
          earlyBranch = false,
          catchAddressMisaligned = true
        )
        
        new YamlPlugin("cpu0.yaml")
        
      )
    )
  )
  
  SpinalConfig(mergeAsyncProcess = false).generateVerilog(oculiCore())
  
}
