package conexp.fx.core.dl

import java.io.File

import scala.collection.JavaConverters.asScalaSetConverter
import scala.collection.JavaConverters.mutableSetAsJavaSetConverter
import scala.collection.JavaConverters.setAsJavaSetConverter

import org.semanticweb.owlapi.model.IRI

import conexp.fx.core.collections.Pair
import conexp.fx.core.collections.setlist.HashSetArrayList
import conexp.fx.core.context.MatrixContext
import conexp.fx.core.exporter.CXTExporter
import conexp.fx.core.util.UnicodeSymbols
import conexp.fx.core.util.Meter
import java.util.HashSet

object ELSubsumptionContextGenerator {

  def main(args: Array[String]): Unit = {
    val timer = Meter.newMilliStopWatch()
    val signature = new Signature(IRI create "")
    signature addConceptNames (IRI create "A", IRI create "B")
    signature addRoleNames (IRI create "r")
    val roleDepth = 2
    val cxt = make(signature.getConceptNames().asInstanceOf[java.util.Set[IRI]].asScala, signature.getRoleNames().asInstanceOf[java.util.Set[IRI]].asScala, roleDepth)
    val file = new File("EL_" + signature.getConceptNames.size + "_" + signature.getRoleNames.size + "_" + roleDepth + ".cxt")
    println("Writing context to file " + file)
    CXTExporter.export(cxt, file)
    println("Time taken: " + timer.measureAndFormat())
  }

  def make(conceptNames: scala.collection.mutable.Set[IRI], roleNames: scala.collection.mutable.Set[IRI], roleDepth: Int): MatrixContext[ELConceptDescription, ELConceptDescription] = {
    val cs = allConcepts(conceptNames, roleNames, roleDepth)
    val concepts = new HashSetArrayList[ELConceptDescription]()
    concepts.addAll(cs.asJava)
    concepts.sort((c, d) => {
      if (c == d)
        0
      else if (c isEquivalentTo d)
        0
      else if (c.roleDepth < d.roleDepth)
        -1
      else if (c.roleDepth > d.roleDepth)
        1
      else if (c.size < d.size)
        -1
      else if (c.size > d.size)
        1
      else 0
    })
    val cxt = new MatrixContext[ELConceptDescription, ELConceptDescription](concepts, concepts, false)
    var i = 1
    val n = concepts.size() * concepts.size()
    for {
      c1 <- cs
      c2 <- cs
    } yield {
      if (i % 1024 == 0)
        println(i + "/" + n + "   ")
      i += 1
      if (c1 == c2 || (c2 subsumes c1))
        cxt addFastSilent (c1, c2)
    }
    cxt
  }

  def numberOfConcepts(cs: Int, rs: Int, rd: Int): Long = {
    if (rd < 0) throw new IllegalArgumentException
    else if (rd == 0) Math.pow(2d, cs.toDouble).toLong
    else Math.pow(2d, cs.toDouble + rs.toDouble * numberOfConcepts(cs, rs, rd - 1).toDouble).toLong
  }

  def allConcepts(conceptNames: scala.collection.mutable.Set[IRI], roleNames: scala.collection.mutable.Set[IRI], roleDepth: Int): Set[ELConceptDescription] = {
    if (roleDepth < 0) throw new IllegalArgumentException
    else if (roleDepth == 0) {
      conceptNames
        .subsets()
        .map(as => as.map(iri => ELConceptDescription.conceptName(iri)))
        .map(cs => ELConceptDescription.conjunction(cs.asJava))
        .toSet
    } else {
      var i = 1
      val n = numberOfConcepts(conceptNames.size, roleNames.size, roleDepth)
      allConcepts(conceptNames, roleNames, roleDepth - 1)
        .map(c => roleNames.map(r => ELConceptDescription.existentialRestriction(r, c.clone())))
        .flatten
        .union(conceptNames.map((A => ELConceptDescription.conceptName(A))))
        .subsets()
        .map(cs => {
          var c = ELConceptDescription.conjunction(cs.asJava)
          val str = c.toString()
          c.reduce
          if (i % 16384 == 0)
            println(i + "/" + n)
          i += 1
          //          println(c + " " + UnicodeSymbols.EQUIV + " " + str)
          c
        })
        .toSet
    }
  }

}