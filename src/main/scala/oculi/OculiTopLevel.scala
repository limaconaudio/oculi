package oculi

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import vexriscv.{plugin, _}
import vexriscv.demo.{SimpleBus, _}
import vexriscv.plugin._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class OculiTopLevel extends Component {

}

object OculiTopLevelVerilog {
  def main(args: Array[String]) {
    SpinalVerilog(new OculiTopLevel)
  }
}
