package conexp.fx.core.context.probabilistic

import scala.Array._
import scala.collection.JavaConverters._
import scala.language.implicitConversions

import org.apache.commons.math4.optim.MaxIter
import org.apache.commons.math4.optim.linear.LinearConstraint
import org.apache.commons.math4.optim.linear.LinearConstraintSet
import org.apache.commons.math4.optim.linear.LinearObjectiveFunction
import org.apache.commons.math4.optim.linear.NonNegativeConstraint
import org.apache.commons.math4.optim.linear.Relationship
import org.apache.commons.math4.optim.linear.SimplexSolver
import org.apache.commons.math4.optim.nonlinear.scalar.GoalType

import conexp.fx.core.context.probabilistic.PropositionalFormulae._
import conexp.fx.core.context.probabilistic.Relation._

/**
 * a class for representing weight formula of the form "sum(ts) r c"
 */
class WeightFormula[M](val ts: Set[WeightTerm[M]], val r: Relation, val c: Double) {
  def this(t: WeightTerm[M], r: Relation, c: Double) = {
    this(Set(t), r, c)
  }
}

/**
 * a class for representing weight terms of the form "a * w(f)"
 */
class WeightTerm[M](val a: Double, val f: PropositionalFormula[M])

trait ProbabilisticPropositionalTheory[M] extends Set[PropositionalFormula[WeightFormula[M]]] {

  def entails(i: PropositionalFormula[WeightFormula[M]]): Boolean = {
    val th = And(Set[PropositionalFormula[WeightFormula[M]]](And(this), Not(i))).disjunctiveNormalForm
    !th.asInstanceOf[Or[WeightFormula[M]]].fs.exists((f: PropositionalFormula[WeightFormula[M]]) => ProbabilisticPropositionalFormulae isSatisfiable f)
  }

  def |= = entails _

  def toFormula: PropositionalFormula[WeightFormula[M]] = And(this)

}

object ProbabilisticPropositionalFormulae {

  implicit def toTheory[M](pfs: Set[PropositionalFormula[WeightFormula[M]]]): ProbabilisticPropositionalTheory[M] = {
    pfs.asInstanceOf[ProbabilisticPropositionalTheory[M]]
  }

  implicit def toProbabilisticPropositionalTheory[G, M](pimps: Set[PImplication[G, M]]): ProbabilisticPropositionalTheory[M] = {
    pimps map toProbabilisticPropositionalFormula
  }

  implicit def toProbabilisticPropositionalFormula[G, M](pimp: PImplication[G, M]): PropositionalFormula[WeightFormula[M]] = {
    val premise = pimp.getPremise.asScala.map((patt: ProbabilisticAttribute[M]) => {
      val fs = patt.ms.asScala.foldLeft(Set[PropositionalFormula[M]]())((curr: Set[PropositionalFormula[M]], m: M) => curr + Var(m))
      val t = new WeightTerm[M](1d, And(fs))
      new WeightFormula[M](t, Relation.GEQ, patt.p)
    }).map(Var(_).asInstanceOf[PropositionalFormula[WeightFormula[M]]])
    val conclusion = pimp.getConclusion.asScala.map((patt: ProbabilisticAttribute[M]) => {
      val fs = patt.ms.asScala.foldLeft(Set[PropositionalFormula[M]]())((curr: Set[PropositionalFormula[M]], m: M) => curr + Var(m))
      val t = new WeightTerm[M](1d, And(fs))
      new WeightFormula[M](t, Relation.GEQ, patt.p)
    }).map(Var(_).asInstanceOf[PropositionalFormula[WeightFormula[M]]])
    Implication(And(premise), And(conclusion))
  }

  implicit def toImmutableSet[T](set: scala.collection.mutable.Set[T]): scala.collection.immutable.Set[T] = {
    scala.collection.immutable.Set(set.toSeq: _*)
  }

  /**
   * tests whether a conjunction of possibly negated atomic weight formulae is satisfiable
   */
  def isSatisfiable[M](f: PropositionalFormula[WeightFormula[M]]): Boolean = {
    var ms: Set[M] = Set()
    f visit ((g: PropositionalFormula[WeightFormula[M]]) => g match {
      case Var(wf) => {
        wf.ts.foreach(_.f visit ((k: PropositionalFormula[M]) => k match {
          case Var(m) => { ms += m }
        }))
      }
    })
    val worlds = ms subsets
    def worldToValuation: Set[M] => (M => Boolean) = { (world: Set[M]) => { (m: M) => world contains m } }
    def worldToFormula: Set[M] => And[M] = { (world: Set[M]) => And(world.map(Var(_)) ++ (ms -- world).map(Var(_).map(Not(_)))) }
    val f0 = (f map ((g: PropositionalFormula[WeightFormula[M]]) => g match {
      case Var(wf) => {
        val ands = wf.ts.flatMap((t: WeightTerm[M]) =>
          worlds.filter({
            (world: Set[M]) =>
              {
                t.f.hasModel(worldToValuation(world))
              }
          }).map({
            (world: Set[M]) =>
              {
                new WeightTerm(t.a, worldToFormula(world))
              }
          }))
        Var(new WeightFormula(ands, wf.r, wf.c))
      }
      case default => g
    })).mapVar(merge)
    f0 match {
      case And(wfs) => {
        hasSolution(worlds.size, linearConstraintSet(worlds.map(worldToFormula).toSet, wfs.map((pwf: PropositionalFormula[WeightFormula[M]]) => pwf match {
          case Var(wf) => wf
          case default => throw new RuntimeException
        })))
      }
      case default => throw new RuntimeException
    }
  }

  def linearConstraintSet[M](worlds: Set[And[M]], wfs: Set[WeightFormula[M]]): LinearConstraintSet = {
    var constraints: List[LinearConstraint] = List()
    var enumeration: Map[And[M], Int] = Map()
    var k: Int = 0
    for (world <- worlds) {
      enumeration += ((world, k))
    }
    k += 1
    val A1: Array[Double] = fill(worlds.size)(1d)
    constraints ::= new LinearConstraint(A1, Relationship.EQ, 1d)
    for (world <- worlds) {
      val An: Array[Double] = fill(worlds.size)(0d)
      An(enumeration(world)) = 1d
      constraints ::= new LinearConstraint(An, Relationship.GEQ, 0d)
    }
    for (wf <- wfs) {
      val Am: Array[Double] = fill(worlds.size)(0d)
      for (wt <- wf.ts)
        Am(enumeration(wt.f.asInstanceOf[And[M]])) = wt.a
      wf.r match {
        case Relation.LNEQ => constraints ::= new LinearConstraint(Am, Relationship.LEQ, wf.c - Double.MinPositiveValue)
        case Relation.LEQ  => constraints ::= new LinearConstraint(Am, Relationship.LEQ, wf.c)
        case Relation.EQ   => constraints ::= new LinearConstraint(Am, Relationship.EQ, wf.c)
        case Relation.GEQ  => constraints ::= new LinearConstraint(Am, Relationship.GEQ, wf.c)
        case Relation.GNEQ => constraints ::= new LinearConstraint(Am, Relationship.GEQ, wf.c + Double.MinPositiveValue)
      }
    }
    new LinearConstraintSet(constraints.asJava)
  }

  def hasSolution[M](numvar: Int, lcs: LinearConstraintSet): Boolean = {
    val c: Array[Double] = fill(numvar)(1d)
    val f: LinearObjectiveFunction = new LinearObjectiveFunction(c, 0)
    val solver: SimplexSolver = new SimplexSolver
    try {
      solver.optimize(new MaxIter(0), f, lcs, GoalType.MAXIMIZE, new NonNegativeConstraint(true))
      true
    } catch {
      case e: RuntimeException => false
    }
  }

  def merge[M](f: WeightFormula[M]): WeightFormula[M] = {
    val ts = f.ts.groupBy((t: WeightTerm[M]) => t.f).mapValues((us: Set[WeightTerm[M]]) => us.foldLeft(0d)((a: Double, t: WeightTerm[M]) => a + t.a)).map((fa: (PropositionalFormula[M], Double)) => new WeightTerm[M](fa._2, fa._1)).toSet
    new WeightFormula[M](ts, f.r, f.c)
  }

}
