package conexp.fx.core.importer

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.ArrayList
import java.util.HashMap

import scala.collection.JavaConverters._

import conexp.fx.core.context.probabilistic.PContext
import conexp.fx.core.context.MatrixContext

object CXT3Importer {

  def read(file: File): PContext[String, String, String] = {
    val pcxt = new PContext[String, String, String]
    val probs = new HashMap[String, Double]

    val lines = Files.lines(file.toPath()).iterator()

    val firstLines = new Array[String](6)
    var i: Int = 0
    while (lines.hasNext() && i < 6) {
      firstLines(i) = lines.next()
      i += 1
    }
    if (firstLines(0).trim() != "B3p") throw new IOException()
    val rows: Int = firstLines(2).toInt
    val cols: Int = firstLines(3).toInt
    val dims: Int = firstLines(4).toInt

    val objects = new ArrayList[String](rows)
    val attributes = new ArrayList[String](cols)
    val worlds = new ArrayList[String](dims)

    while (lines.hasNext() && i < 6 + rows) {
      objects add lines.next()
      i += 1
    }
    while (lines.hasNext() && i < 6 + rows + cols) {
      attributes add lines.next()
      i += 1
    }
    while (lines.hasNext() && i < 6 + rows + cols + dims) {
      val line = lines.next().split("=")
      var p = 0d
      if (line(1) contains "/") {
        val v = line(1).split("/")
        p = v(0).toDouble / v(1).toDouble
      } else {
        p = line(1).toDouble
      }
      probs put (line(0), p)
      worlds add line(0)
      i += 1
    }

    var j: Int = 1
    for (w <- worlds asScala) {
      val wcxt = new MatrixContext[String, String](false)
      wcxt.rowHeads().addAll(objects)
      wcxt.colHeads().addAll(attributes)
      val matrix = (wcxt matrix)
      while (lines.hasNext() && i < 6 + rows + cols + dims + j * rows) {
        val line = lines.next().toCharArray()
        val row = i - (6 + rows + cols + dims + (j - 1) * rows)
        for (col <- 0 to cols - 1)
          matrix.setBoolean(line(col).toLower == 'x', row, col)
        i += 1
      }
      pcxt.addWorld(w, (probs asScala)(w), wcxt)
      j += 1
    }

    pcxt
  }

}