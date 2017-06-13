package conexp.fx.core.context.probabilistic

import java.io.File
import java.util.HashSet

import scala.collection.JavaConverters._

import conexp.fx.core.context.Implication
import conexp.fx.core.importer.CXT3Importer

object Main1 extends App {

  override def main(args: Array[String]): Unit = {
    val pcxt = CXT3Importer.read(new File("/Users/francesco/workspace-Scala/conexp-fx/example.cxt3"))
    println(pcxt)
    println(pcxt.almostCertainScaling)
    for (patt <- pcxt.probabilisticAttributes.asScala)
      println(patt)
    println(pcxt.probabilisticScaling)
    for (timp <- pcxt.trivialImplications asScala)
      println((pcxt models timp) + " :: " + timp)
    println()
    val trivialImplications = pcxt.trivialImplications
    def printImplication(implication: Implication[String, PAttribute[String]]) = {
      val prem = pcxt.minimize(implication.getPremise, trivialImplications)
      var conc: scala.collection.mutable.Set[PAttribute[String]] = (new HashSet[PAttribute[String]]).asScala
      if (implication.getSupport().size() > 0)
        conc = pcxt.minimize(implication.getConclusion, trivialImplications)
      println((pcxt models implication) + " :: " + implication.getSupport.size() + " :: " + new Implication[String, PAttribute[String]](prem asJava, conc asJava))
      //      println((pcxt models implication) + " :: " + implication.getSupport.size() + " :: " + implication)
    }
    val base = pcxt.probabilisticImplicationalBase.asScala
    for (pimp <- base)
      printImplication(pimp)
    println()
    println(base forall (pcxt models))
  }

}