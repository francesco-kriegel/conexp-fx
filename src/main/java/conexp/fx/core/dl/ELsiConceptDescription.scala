package conexp.fx.core.dl

import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.concurrent.atomic.AtomicInteger

import org.semanticweb.owlapi.model.IRI

import conexp.fx.core.collections.relation.MatrixRelation
import conexp.fx.core.collections.setlist.SetList
import conexp.fx.core.collections.setlist.SetLists
import conexp.fx.core.math.BooleanMatrices
import conexp.fx.core.collections.relation.Relation
import com.google.common.collect.Sets

import scala.collection.JavaConverters._

object ELsiConceptDescription {

  val nextFreeInteger = new AtomicInteger(0)

  implicit def of(concept: ELConceptDescription): ELsiConceptDescription[Integer] = {
    val graph = new ELInterpretation2[Integer]()
    val n = new AtomicInteger(0)
    def insert(k: Integer, tree: ELConceptDescription) {
      graph.getConceptNameExtensionMatrix.rowHeads.add(k)
      tree.getConceptNames.forEach(graph.add(k, _))
      tree.getExistentialRestrictions.entries().forEach(entry ⇒ {
        val l = n.incrementAndGet()
        graph.add(k, entry.getKey, l)
        insert(l, entry.getValue)
      })
    }
    insert(0, concept)
    new ELsiConceptDescription(graph, 0)
  }

  implicit def of(graph: Map[String, ELConceptDescription], element: String): ELsiConceptDescription[IRI] = {
    if (!(graph.keySet contains element)) throw new IllegalArgumentException()
    val normalization = new scala.collection.mutable.HashMap[IRI, ELConceptDescription]
    graph.foreach(entry ⇒ normalization put ((IRI create entry._1), entry._2))
    var changed = true
    while (changed) {
      changed = false
      normalization.foreach(entry ⇒ {
        val variable = entry._1
        val definition = entry._2
        if (definition.getConceptNames contains variable) {
          definition.getConceptNames remove variable
          changed = true
        }
        normalization.keySet.foreach(wariable ⇒ {
          if (!(variable equals wariable) && (definition.getConceptNames contains wariable)) {
            definition.getConceptNames remove wariable
            val eefinition = normalization(wariable)
            definition.getConceptNames addAll eefinition.getConceptNames
            definition.getExistentialRestrictions putAll eefinition.getExistentialRestrictions
            changed = true
          }
        })
        definition.getExistentialRestrictions.forEach((role, filler) ⇒ {
          if (!(filler.getExistentialRestrictions.isEmpty() &&
            (filler.getConceptNames.size() == 1) &&
            (normalization.keySet contains filler.getConceptNames.iterator.next()))) {
            val xariable = IRI create ("urn:conexp-fx:variable:" + nextFreeInteger.getAndIncrement)
            val fefinition = filler.clone()
            normalization put (xariable, fefinition)
            filler.getConceptNames.clear()
            filler.getExistentialRestrictions.clear()
            filler.getConceptNames add xariable
            changed = true
          }
        })
      })
    }
    val interpretation = new ELInterpretation2[IRI]()
    normalization.foreach(entry ⇒ {
      val yariable = entry._1
      val gefinition = entry._2
      interpretation.getConceptNameExtensionMatrix.rowHeads() add yariable
      gefinition.getConceptNames.forEach(interpretation add (yariable, _))
      gefinition.getExistentialRestrictions.forEach((sole, giller) ⇒ {
        if (!giller.getExistentialRestrictions.isEmpty() ||
          !(giller.getConceptNames.size() == 1))
          throw new RuntimeException()
        val zariable = giller.getConceptNames.iterator().next()
        if (!(normalization.keySet contains zariable))
          throw new RuntimeException()
        interpretation.add(yariable, sole, zariable)
      })
    })
    new ELsiConceptDescription(interpretation, IRI create element)
  }

  def exists[I](role: IRI, filler: ELsiConceptDescription[I]): ELsiConceptDescription[Either[Unit, I]] = {
    val transformedRowHeads: SetList[Either[Unit, I]] = SetLists.transform(filler.interpretation.getConceptNameExtensionMatrix.rowHeads(), (i: I) ⇒ Right(i));
    val matrix = new MatrixRelation[Either[Unit, I], IRI](transformedRowHeads.clone(), filler.interpretation.getConceptNameExtensionMatrix.colHeads().clone(), BooleanMatrices.clone(filler.interpretation.getConceptNameExtensionMatrix.matrix()), false)
    val natrix = new HashMap[IRI, MatrixRelation[Either[Unit, I], Either[Unit, I]]]
    filler.interpretation.getRoleNameExtensionMatrixMap.keySet().forEach(role ⇒ {
      val transformedRowHeads: SetList[Either[Unit, I]] = SetLists.transform(filler.interpretation.getRoleNameExtensionMatrix(role).rowHeads(), (i: I) ⇒ Right(i));
      val transformedColHeads: SetList[Either[Unit, I]] = SetLists.transform(filler.interpretation.getRoleNameExtensionMatrix(role).colHeads(), (i: I) ⇒ Right(i));
      natrix.put(role, new MatrixRelation[Either[Unit, I], Either[Unit, I]](transformedRowHeads.clone(), transformedColHeads.clone(), BooleanMatrices.clone(filler.interpretation.getRoleNameExtensionMatrix(role).matrix()), false))
    })
    val graph: ELInterpretation2[Either[Unit, I]] = new ELInterpretation2(matrix, natrix)
    val root = Left(())
    graph add (root, role, Right(filler.element))
    new ELsiConceptDescription[Either[Unit, I]](graph, root)
  }

  def leastCommonSubsumer[I](concepts: ELsiConceptDescription[I]*): ELsiConceptDescription[java.util.List[I]] = {
    new ELsiConceptDescription[java.util.List[I]](
      ELInterpretation2 productOf concepts.toList.map(_.interpretation).asJava,
      concepts.toList.map(_.element).asJava)
  }

}

class ELsiConceptDescription[I](val interpretation: ELInterpretation2[I], val element: I) {

  override def equals(that: Any): Boolean = {
    that.isInstanceOf[ELsiConceptDescription[I]] &&
      (this.interpretation equals that.asInstanceOf[ELsiConceptDescription[I]].interpretation) &&
      (this.element equals that.asInstanceOf[ELsiConceptDescription[I]].element)
  }

  override def hashCode(): Int = {
    29 * interpretation.hashCode() + 31 * element.hashCode()
  }

  override def clone(): ELsiConceptDescription[I] =
    new ELsiConceptDescription(interpretation.clone, element)

  def rotate(targetElement: I): ELsiConceptDescription[I] =
    new ELsiConceptDescription(interpretation, targetElement)

  def approximate(roleDepth: Integer): ELConceptDescription = {
    if (roleDepth < 0) throw new IllegalArgumentException()
    val approximation = new ELConceptDescription
    if (interpretation.getConceptNameExtensionMatrix.rowHeads contains element)
      interpretation.getConceptNameExtensionMatrix.row(element).forEach(approximation.getConceptNames add _)
    if (roleDepth > 0)
      interpretation.getRoleNameExtensionMatrixMap.keySet().forEach(role ⇒
        if (interpretation.getRoleNameExtensionMatrix(role).rowHeads contains element)
          interpretation.getRoleNameExtensionMatrix(role).row(element).forEach(successor ⇒
          approximation.getExistentialRestrictions.put(role, rotate(successor).approximate(roleDepth - 1))))
    approximation
  }

  def reduce(): ELsiConceptDescription[java.util.Set[I]] = {
    val reduction = interpretation.reduce()
    val root = reduction.getDomain.stream().filter(_ contains element).findAny.get()
    new ELsiConceptDescription[java.util.Set[I]](reduction, root)
  }

  def subsumes[J](other: ELsiConceptDescription[J]): Boolean =
    checkExistenceOfSimulation(interpretation, element, other.interpretation, other.element, Set[Pair[I, J]]())

  def emptySetIfNull[T](set: java.util.Set[T]): java.util.Set[T] =
    if (set == null) Collections.emptySet() else set

  def guardedUnmodifiableRow[X, Y](relation: Relation[X, Y], x: X): java.util.Set[Y] = {
    if (relation.rowHeads() contains x)
      Collections.unmodifiableSet(new HashSet(relation row x));
    else
      Collections.emptySet()
  }

  def guardedUnmodifiableCol[X, Y](relation: Relation[X, Y], y: Y): java.util.Set[X] = {
    if (relation.colHeads() contains y)
      Collections.unmodifiableSet(new HashSet(relation col y))
    else
      Collections.emptySet()
  }

  private def checkExistenceOfSimulation[I, J](
    interpretation: ELInterpretation2[I], element: I,
    jnterpretation: ELInterpretation2[J], flement: J,
    partialSimulation: Set[Pair[I, J]]): Boolean = {
    if (partialSimulation contains Pair(element, flement))
      true
    else if (!(guardedUnmodifiableRow(jnterpretation.getConceptNameExtensionMatrix, flement) containsAll guardedUnmodifiableRow(interpretation.getConceptNameExtensionMatrix, element)))
      false
    else if (interpretation.getRoleNameExtensionMatrixMap.keySet().stream().allMatch(role ⇒
      guardedUnmodifiableRow(interpretation.getRoleNameExtensionMatrix(role), element).stream().allMatch(successor ⇒
        guardedUnmodifiableRow(jnterpretation.getRoleNameExtensionMatrix(role), flement).stream().anyMatch(duccessor ⇒
          checkExistenceOfSimulation(interpretation, successor, jnterpretation, duccessor, partialSimulation + Pair(element, flement))))))
      true
    else
      false
  }

  def isSubsumedBy[J](other: ELsiConceptDescription[J]) = other subsumes this
  def isEquivalentTo[J](other: ELsiConceptDescription[J]) = (this subsumes other) && (other subsumes this)
  def isMoreSpecificThan = isSubsumedBy _
  def isMoreGeneralThan = subsumes _

  def mostSpecificConsequence[J](tbox: Set[Pair[ELsiConceptDescription[J], ELsiConceptDescription[J]]]): ELsiConceptDescription[Either[I, ELsiConceptDescription[J]]] = {
    val transformedRowHeads: SetList[Either[I, ELsiConceptDescription[J]]] = SetLists.transform(interpretation.getConceptNameExtensionMatrix.rowHeads(), (i: I) ⇒ Left(i));
    val matrix = new MatrixRelation[Either[I, ELsiConceptDescription[J]], IRI](transformedRowHeads.clone(), interpretation.getConceptNameExtensionMatrix.colHeads().clone(), BooleanMatrices.clone(interpretation.getConceptNameExtensionMatrix.matrix()), false)
    val natrix = new HashMap[IRI, MatrixRelation[Either[I, ELsiConceptDescription[J]], Either[I, ELsiConceptDescription[J]]]]
    interpretation.getRoleNameExtensionMatrixMap.keySet().forEach(role ⇒ {
      val transformedRowHeads: SetList[Either[I, ELsiConceptDescription[J]]] = SetLists.transform(interpretation.getRoleNameExtensionMatrix(role).rowHeads(), (i: I) ⇒ Left(i));
      val transformedColHeads: SetList[Either[I, ELsiConceptDescription[J]]] = SetLists.transform(interpretation.getRoleNameExtensionMatrix(role).colHeads(), (i: I) ⇒ Left(i));
      natrix.put(role, new MatrixRelation[Either[I, ELsiConceptDescription[J]], Either[I, ELsiConceptDescription[J]]](transformedRowHeads.clone(), transformedColHeads.clone(), BooleanMatrices.clone(interpretation.getRoleNameExtensionMatrix(role).matrix()), false))
    })
    val graph: ELInterpretation2[Either[I, ELsiConceptDescription[J]]] = new ELInterpretation2(matrix, natrix)
    val mostSpecificConsequence = new ELsiConceptDescription(graph, Left(element))
    def insert(y: Either[I, ELsiConceptDescription[J]], c: ELsiConceptDescription[J]) {
      if (y.isLeft || !(mostSpecificConsequence.interpretation.getDomain contains y.right)) {
        c.interpretation.getConceptNameExtensionMatrix.row(c.element).forEach(a ⇒ mostSpecificConsequence.interpretation.getConceptNameExtensionMatrix.add(y, a))
        c.interpretation.getRoleNameExtensionMatrixMap.keySet().forEach(role ⇒
          c.interpretation.getRoleNameExtensionMatrix(role).row(c.element).forEach(successor ⇒ {
            val d = c.rotate(successor)
            val z = Right(d)
            mostSpecificConsequence.interpretation.getRoleNameExtensionMatrix(role).add(y, z)
            insert(z, d)
          }))
      }
    }
    var changed = true
    while (changed) {
      changed = false
      new HashSet(mostSpecificConsequence.interpretation.getDomain).forEach(x ⇒
        tbox.foreach(gci ⇒
          if ((mostSpecificConsequence.rotate(x) isSubsumedBy gci._1) && !(mostSpecificConsequence.rotate(x) isSubsumedBy gci._2)) {
            insert(x, gci._2)
            changed = true
          }))
    }
    mostSpecificConsequence
  }

  def subsumes_TBox[J, K](other: ELsiConceptDescription[J], tbox: Set[Pair[ELsiConceptDescription[K], ELsiConceptDescription[K]]]): Boolean = {
    this subsumes other.mostSpecificConsequence(tbox)
  }

  def isSubsumedBy_TBox[J, K](other: ELsiConceptDescription[J], tbox: Set[Pair[ELsiConceptDescription[K], ELsiConceptDescription[K]]]) = other subsumes_TBox (this, tbox)
  def isEquivalentTo_TBox[J, K](other: ELsiConceptDescription[J], tbox: Set[Pair[ELsiConceptDescription[K], ELsiConceptDescription[K]]]) = (this subsumes_TBox (other, tbox)) && (other subsumes_TBox (this, tbox))
  def isMoreSpecificThan_TBox = isSubsumedBy_TBox _
  def isMoreGeneralThan_TBox = subsumes_TBox _

}