package conexp.fx.core.dl

import scala.language.implicitConversions
import conexp.fx.core.util.UnicodeSymbols

object ELConceptDescriptionsScala {

  implicit def toConceptName[C, R](conceptName: C): ConceptName[C, R] = {
    ConceptName(conceptName)
  }

  implicit def retypeConceptName[C, R, S](conceptName: ConceptName[C, R]): ConceptName[C, S] = {
    ConceptName(conceptName.conceptName)
  }

  def exists[C, R](roleName: R, conceptDescription: ELConceptDescriptionScala[C, R]): ExistentialRestriction[C, R] = {
    ExistentialRestriction(roleName, conceptDescription)
  }
}

abstract class ELConceptDescriptionScala[C, R] {

  def and(that: ELConceptDescriptionScala[C, R]): Conjunction[C, R] = {
    that match {
      case Conjunction(cs) => Conjunction(Set(this) ++ cs)
      case default         => Conjunction(Set(this, that))
    }
  }

  def &(that: ELConceptDescriptionScala[C, R]): Conjunction[C, R] = and(that)

  def ::(roleName: R): ExistentialRestriction[C, R] = {
    ExistentialRestriction(roleName, this)
  }

  def <=(that: ELConceptDescriptionScala[C, R]): Boolean = {
    false
  }

  def reduce {}

  def roleDepth: Int

  def size: Int

  def toString: String

}

case class Top[C, R]() extends ELConceptDescriptionScala[C, R] {

  override def toString = UnicodeSymbols.TOP

  override def roleDepth = 0

  override def size = 1

}

case class Bot[C, R]() extends ELConceptDescriptionScala[C, R] {

  override def toString = UnicodeSymbols.BOT

  override def roleDepth = 0

  override def size = 1

}

case class ConceptName[C, R](conceptName: C) extends ELConceptDescriptionScala[C, R] {

  override def toString = conceptName.toString

  override def roleDepth = 0

  override def size = 1

}

case class ExistentialRestriction[C, R](roleName: R, conceptDescription: ELConceptDescriptionScala[C, R]) extends ELConceptDescriptionScala[C, R] {

  override def toString = UnicodeSymbols.EXISTS + roleName.toString + "." + conceptDescription.toString

  override def roleDepth = 1 + conceptDescription.roleDepth

  override def size = 1 + conceptDescription.size

}

case class Conjunction[C, R](conceptDescriptions: Set[ELConceptDescriptionScala[C, R]]) extends ELConceptDescriptionScala[C, R] {

  override def and(that: ELConceptDescriptionScala[C, R]): Conjunction[C, R] = {
    that match {
      case Conjunction(cs) => Conjunction(conceptDescriptions ++ cs)
      case default         => Conjunction(conceptDescriptions ++ Set(that))
    }
  }

  override def toString = conceptDescriptions.mkString("(", UnicodeSymbols.SQCAP, ")")

  override def roleDepth = conceptDescriptions.map(_.roleDepth).max

  override def size = conceptDescriptions.foldLeft(-1)(_ + _.size + 1)

}