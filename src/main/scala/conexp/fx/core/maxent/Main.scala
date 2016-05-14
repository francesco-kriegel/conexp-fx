package conexp.fx.core.maxent

import org.ujmp.core.doublematrix.impl.DefaultDenseDoubleMatrix2D
import org.ujmp.core.doublematrix.DoubleMatrix
import org.ujmp.core.calculation.Calculation.Ret

object Main extends App {

  override def main(args: Array[String]): Unit = {
    val A: DoubleMatrix = "1,2,2;3,2,1"
    val b: DoubleMatrix = "1;5;4"
    A
    for (E <- A.eig()) println(E)
    val x = A.solve(b)
    println(x)
  }

  implicit def fromString(s: String): DoubleMatrix = {
    val rows = s.split(";")
    val is = rows size
    val js = rows map (_.split(",")) map (_.length) reduceLeft (_ max _)
    val mat = new DefaultDenseDoubleMatrix2D(is, js)
    var i = 0
    for (row <- rows) {
      var j = 0
      for (entry <- row.split(",")) {
        mat.setDouble(entry.toDouble, i, j)
        j += 1
      }
      i += 1
    }
    mat
  }
}