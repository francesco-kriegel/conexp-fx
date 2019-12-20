package conexp.fx.core.dl

import scala.language.implicitConversions
import conexp.fx.core.util.UnicodeSymbols
import com.google.common.collect.Sets
import scala.collection.JavaConverters._
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicIntegerArray
import java.util.concurrent.atomic.AtomicReferenceArray

object ELsippConceptDescription {

  implicit def toConceptName[I, C, R](conceptName: C): ConceptName[I, C, R] = {
    ConceptName(conceptName)
  }

  implicit def retypeConceptName[I, J, C, R, S](conceptName: ConceptName[I, C, R]): ConceptName[J, C, S] = {
    ConceptName(conceptName.conceptName)
  }

  implicit def toIndividualName[I, C, R](individualName: I): IndividualName[I, C, R] = {
    IndividualName(individualName)
  }

  implicit def retypeIndividualName[I, C, D, R, S](individualName: IndividualName[I, C, R]): IndividualName[I, D, S] = {
    IndividualName(individualName.individualName)
  }

  def exists[I, C, R](roleName: R, conceptDescription: ELsippConceptDescription[I, C, R]): ExistentialRestriction[I, C, R] = {
    ExistentialRestriction(roleName, conceptDescription)
  }
}

abstract class ELsippConceptDescription[I, C, R] {

  def and(that: ELsippConceptDescription[I, C, R]): ELsippConceptDescription[I, C, R] = Conjunction(this, that)

  def &(that: ELsippConceptDescription[I, C, R]): ELsippConceptDescription[I, C, R] = and(that)

  def ::(roleName: R): ExistentialRestriction[I, C, R] = {
    ExistentialRestriction(roleName, this)
  }

  def <=(that: ELsippConceptDescription[I, C, R]): Boolean = {
    false
  }

  def reduce {}

  def roleDepth: Int
  def size: Int
  def toString: String

}

case class TopConceptDescription[I, C, R]() extends ELsippConceptDescription[I, C, R] {

  override def and(that: ELsippConceptDescription[I, C, R]): ELsippConceptDescription[I, C, R] = that

  override def toString = UnicodeSymbols.TOP
  override def roleDepth = 0
  override def size = 1

}

case class BottomConceptDescription[I, C, R]() extends ELsippConceptDescription[I, C, R] {

  override def and(that: ELsippConceptDescription[I, C, R]): BottomConceptDescription[I, C, R] = this

  override def toString = UnicodeSymbols.BOT
  override def roleDepth = 0
  override def size = 1

}

case class ConceptName[I, C, R](conceptName: C) extends ELsippConceptDescription[I, C, R] {

  override def toString = conceptName.toString
  override def roleDepth = 0
  override def size = 1

}

case class IndividualName[I, C, R](individualName: I) extends ELsippConceptDescription[I, C, R] {

  val individualNameReference: AtomicReference[I] = new AtomicReference(individualName)
  
  override def toString = individualName.toString
  override def roleDepth = 0
  override def size = 1

}

case class ExistentialRestriction[I, C, R](roleName: R, conceptDescription: ELsippConceptDescription[I, C, R]) extends ELsippConceptDescription[I, C, R] {

  val roleNameReference: AtomicReference[R] = new AtomicReference(roleName)
  val conceptDescriptionReference: AtomicReference[ELsippConceptDescription[I, C, R]] = new AtomicReference(conceptDescription)
  
  override def toString = UnicodeSymbols.EXISTS + roleName.toString + "." + conceptDescription.toString
  override def roleDepth = 1 + conceptDescription.roleDepth
  override def size = 1 + conceptDescription.size

}

case class Conjunction[I, C, R](conceptDescriptions: ELsippConceptDescription[I, C, R]*) extends ELsippConceptDescription[I, C, R] {

  val conjuncts: scala.collection.mutable.Set[ELsippConceptDescription[I, C, R]] = Sets.newConcurrentHashSet().asScala
  def flatten(conceptDescription: ELsippConceptDescription[I, C, R]) {
    conceptDescription match {
      case conjunction: Conjunction[I, C, R] ⇒ flatten(conjunction)
      case default                           ⇒ conjuncts add conceptDescription
    }
  }
  conceptDescriptions.foreach(flatten)

  override def toString = conceptDescriptions.mkString("(", UnicodeSymbols.SQCAP, ")")
  override def roleDepth = conceptDescriptions.map(_.roleDepth).max
  override def size = conceptDescriptions.foldLeft(-1)(_ + _.size + 1)

}

case class PointedInterpretation[I, C, R, J](val interpretation: ELInterpretation2[J], val element: J) extends ELsippConceptDescription[I, C, R] {

  val foo: AtomicReferenceArray[Boolean] = ???
  val elementReference: AtomicReference[J] = new AtomicReference(element)
  
  override def toString = ""
  override def roleDepth = 0
  override def size = 0

}