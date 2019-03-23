package conexp.fx.core.dl

import scala.language.implicitConversions
import conexp.fx.core.dl.ELConceptDescriptionsScala._

object ELNeighborhoodTest2 {

  def main(args: Array[String]): Unit = {
    val c = Top() & "foo" & 1 :: ("A" & "X") & "B" and "C" & 2 :: "Y" & Top() & 3 :: 4 :: 5 :: 6 :: "bar"
    //    val c = 1::(2 and 3 and 4)
    println(c)
    println(c.size)
    println(c.roleDepth)
  }

}