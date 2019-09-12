package conexp.fx.core.dl

import scala.collection.mutable.HashSet

class ELsippOntology[I, C, R]() extends HashSet[ELsippAxiom[I, C, R]] {

  def normalize(): (scala.collection.mutable.Set[ELsippConceptInclusion[I, C, R]], scala.collection.mutable.HashSet[ELsippRangeRestriction[I, C, R]], scala.collection.mutable.HashSet[ELsippRoleInclusion[I, C, R]]) = {
    val cis = new HashSet[ELsippConceptInclusion[I, C, R]]
    val rrs = new HashSet[ELsippRangeRestriction[I, C, R]]
    val ris = new HashSet[ELsippRoleInclusion[I, C, R]]
    this.foreach(axiom ⇒ {
      axiom match {
        case ci: ELsippConceptInclusion[I, C, R]   ⇒ cis += ci
        case ce: ELsippConceptEquivalence[I, C, R] ⇒ cis ++= ce.toConceptInclusions()
        case cd: ELsippConceptDefinition[I, C, R]  ⇒ cis ++= cd.toConceptInclusions()
        case ca: ELsippConceptAssertion[I, C, R]   ⇒ cis += ca.toConceptInclusion()
        case ra: ELsippRoleAssertion[I, C, R]      ⇒ cis += ra.toConceptInclusion()
        case dr: ELsippDomainRestriction[I, C, R]  ⇒ cis += dr.toConceptInclusion()
        case rr: ELsippRangeRestriction[I, C, R]   ⇒ rrs += rr
        case ri: ELsippRoleInclusion[I, C, R]      ⇒ ris += ri
      }
    })
    (cis, rrs, ris)
  }

}

abstract class ELsippAxiom[I, C, R] {

}

case class ELsippConceptInclusion[I, C, R](
  val subsumee: ELsippConceptDescription[I, C, R],
  val subsumer: ELsippConceptDescription[I, C, R])
  extends ELsippAxiom[I, C, R] {

}

case class ELsippConceptEquivalence[I, C, R](
  val conceptDescription1: ELsippConceptDescription[I, C, R],
  val conceptDescription2: ELsippConceptDescription[I, C, R])
  extends ELsippAxiom[I, C, R] {

  def toConceptInclusions() =
    Set(ELsippConceptInclusion[I, C, R](conceptDescription1, conceptDescription2), ELsippConceptInclusion[I, C, R](conceptDescription2, conceptDescription1))

}

case class ELsippConceptDefinition[I, C, R](
  val conceptName:        C,
  val conceptDescription: ELsippConceptDescription[I, C, R])
  extends ELsippAxiom[I, C, R] {

  def toConceptInclusions() =
    Set(ELsippConceptInclusion[I, C, R](ConceptName(conceptName), conceptDescription), ELsippConceptInclusion[I, C, R](conceptDescription, ConceptName(conceptName)))

}

case class ELsippConceptAssertion[I, C, R](
  val individualName:     I,
  val conceptDescription: ELsippConceptDescription[I, C, R])
  extends ELsippAxiom[I, C, R] {

  def toConceptInclusion() =
    ELsippConceptInclusion[I, C, R](IndividualName(individualName), conceptDescription)

}

case class ELsippRoleAssertion[I, C, R](
  val individualName1: I,
  val roleName:        R,
  val individualName2: I)
  extends ELsippAxiom[I, C, R] {

  def toConceptInclusion() =
    ELsippConceptInclusion[I, C, R](IndividualName(individualName1), ExistentialRestriction(roleName, IndividualName(individualName2)))

}

case class ELsippDomainRestriction[I, C, R](
  val roleName:           R,
  val conceptDescription: ELsippConceptDescription[I, C, R])
  extends ELsippAxiom[I, C, R] {

  def toConceptInclusion() =
    ELsippConceptInclusion[I, C, R](ExistentialRestriction(roleName, Top()), conceptDescription)

}

case class ELsippRangeRestriction[I, C, R](
  val roleName:           R,
  val conceptDescription: ELsippConceptDescription[I, C, R])
  extends ELsippAxiom[I, C, R] {

}

class ELsippRoleChain[R](roleNames: R*) {}

case class ELsippRoleInclusion[I, C, R](
  val premise:    ELsippRoleChain[R],
  val conclusion: R)
  extends ELsippAxiom[I, C, R] {

}