package conexp.fx.core.algorithm.nextclosures

import scala.collection._

import conexp.fx.core.context.MatrixContext
import conexp.fx.core.util.UnicodeSymbols

object LTLClosures {

  type TemporalContext[G, M] = MatrixContext[G, (M, Int)]
  type LTLContext[G, M] = MatrixContext[G, LTLformula[M]]
  type Trace[M] = Set[(M, Int)]

  def temporalContext[G, M](data: (G, M, Int)*): TemporalContext[G, M] = {
    val tcxt = new MatrixContext[G, (M, Int)](false)
    data.foreach({ case (g, m, t) => tcxt.add(g, (m, t)) })
    tcxt
  }

  def ltlize[G, M](tcxt: TemporalContext[G, M], t0: Int): LTLContext[G, M] = {
    val ltlcxt = new LTLContext[G, M](false)
    ltlcxt.rowHeads().addAll(tcxt.rowHeads())
    var tmax = 0
    val ct = tcxt.colHeads().iterator()
    while (ct.hasNext()) {
      val (m, t) = ct.next()
      tmax = tmax.max(t)
      ltlcxt.colHeads().add(new LTLliteral(m))
      ltlcxt.colHeads().add(new LTLnext(new LTLliteral(m)))
      ltlcxt.colHeads().add(new LTLsometimes(new LTLliteral(m)))
      ltlcxt.colHeads().add(new LTLalways(new LTLliteral(m)))
      val ct2 = tcxt.colHeads().iterator()
      while (ct2.hasNext()) {
        val (m2, t2) = ct2.next()
        ltlcxt.colHeads().add(new LTLuntil(new LTLliteral(m), new LTLliteral(m2)))
      }
    }
    def models = ltlmodels(tcxt, tmax)_
    val ƒt = ltlcxt.colHeads().iterator()
    while (ƒt.hasNext()) {
      val ƒ = ƒt.next()
      val gt = ltlcxt.rowHeads().iterator()
      while (gt.hasNext()) {
        val g = gt.next()
        if (models(g, t0, ƒ)) ltlcxt.addFast(g, ƒ)
      }
    }
    ltlcxt
  }

  def ltlmodels[G, M](tcxt: TemporalContext[G, M], tmax: Int)(g: G, t: Int, ƒ: LTLformula[M]): Boolean = {
    ƒ match {
      case LTLtrue => true
      case LTLfalse => false
      case LTLliteral(m) => tcxt.contains(g, (m, t))
      case LTLconjunction(conjuncts) => conjuncts.forall(ltlmodels(tcxt, tmax)(g, t, _))
      case LTLimplication(premise, conclusion) => !ltlmodels(tcxt, tmax)(g, t, premise) || ltlmodels(tcxt, tmax)(g, t, conclusion)
      case LTLnext(formula) => ltlmodels(tcxt, tmax)(g, t + 1, formula)
      case LTLsometimes(formula) => (t to tmax).exists(ltlmodels(tcxt, tmax)(g, _, formula))
      case LTLalways(formula) => (t to tmax).forall(ltlmodels(tcxt, tmax)(g, _, formula))
      case LTLuntil(guard, future) =>
        (t to tmax).exists(v =>
          ltlmodels(tcxt, tmax)(g, v, future)
            && (t until v).forall(ltlmodels(tcxt, tmax)(g, _, guard)))
    }
  }

  abstract class LTLformula[+M] {
    override def toString() = this match {
      case LTLtrue => UnicodeSymbols.TOP
      case LTLfalse => UnicodeSymbols.BOT
      case LTLliteral(variable) => variable.toString()
      case LTLconjunction(conjuncts) => {
        if (conjuncts.isEmpty)
          UnicodeSymbols.TOP
        else if (conjuncts.size == 1)
          conjuncts.head.toString()
        else {
          val s = new StringBuilder()
          val it = conjuncts.iterator
          s append "( "
          s append it.next()
          while (it.hasNext)
            s append " " + UnicodeSymbols.WEDGE + " " + it.next()
          s append " )"
          s.toString()
        }
      }
      case LTLimplication(premise, conclusion) => "( " + premise + " " + UnicodeSymbols.TO + " " + conclusion + " )"
      case LTLnext(formula) => UnicodeSymbols.NEXT + formula
      case LTLsometimes(formula) => UnicodeSymbols.SOMETIMES + formula
      case LTLalways(formula) => UnicodeSymbols.ALWAYS + formula
      case LTLuntil(guard, future) => {
        if (guard.toString() == UnicodeSymbols.TOP)
          UnicodeSymbols.SOMETIMES + future
        else
          "( " + guard + " " + UnicodeSymbols.UNTIL + " " + future + " )"
      }
    }
  }
  case object LTLtrue extends LTLformula
  case object LTLfalse extends LTLformula
  case class LTLliteral[M](variable: M) extends LTLformula[M]
  case class LTLconjunction[M](conjuncts: Iterable[LTLformula[M]]) extends LTLformula[M] {
    def this(conjuncts: LTLformula[M]*) { this(conjuncts.toIterable) }
  }
  case class LTLimplication[M](premise: LTLformula[M], conclusion: LTLformula[M]) extends LTLformula[M]
  case class LTLnext[M](formula: LTLformula[M]) extends LTLformula[M]
  case class LTLsometimes[M](formula: LTLformula[M]) extends LTLformula[M]
  case class LTLalways[M](formula: LTLformula[M]) extends LTLformula[M]
  case class LTLuntil[M](guard: LTLformula[M], future: LTLformula[M]) extends LTLformula[M]

  def toLTLformula[M](trace: Trace[M], t0: Int, depth: Int): LTLformula[M] = toLTLformula(trace, t0, t0, depth)

  def toLTLformula[M](trace: Trace[M], t0: Int, t1: Int, depth: Int): LTLformula[M] = {
    if (t0 > t1)
      error("start timepoint must not be greater than end timepoint")
    else if (t0 == t1)
      new LTLconjunction[M](
        trace.filter(_._2 == t0).map(_._1).map(new LTLliteral[M](_))
          ++
          (if (depth > 0)
            trace.map(_._2).filter(_ > t0).map(t => new LTLuntil(
            toLTLformula(trace, t0, t, depth - 1),
            toLTLformula(trace, t, depth - 1)))
          else Set.empty))
    else
      new LTLconjunction[M](
        trace.filter({ case (_, s) => t0 <= s && s < t1 }).map(_._1)
          .filter(m => (t0 until t1).forall(s => trace contains (m, s)))
          .map(new LTLliteral[M](_))
          ++ (if (depth > 0)
            Set.empty
          else Set.empty))
  }

  def getObjectTrace[G, M](cxt: MatrixContext[G, (M, Int)], obj: G): Trace[M] = {
    val trace = new mutable.HashSet[(M, Int)]()
    val it = cxt.row(obj).iterator()
    while (it.hasNext())
      trace += it.next()
    trace
  }

  def main(args: Array[String]): Unit = {
    val traffic_lights = temporalContext[Int, String](
      (1, "r", 0), (1, "r", 1), (1, "r", 2), (1, "y", 3), (1, "g", 4), (1, "y", 5), (1, "r", 6), (1, "r", 7), (1, "r", 8),
      (2, "g", 0), (2, "y", 1), (2, "r", 2), (2, "r", 3), (2, "r", 4), (2, "r", 5), (2, "r", 6), (2, "y", 7), (2, "g", 8))
    println(print(toLTLformula(getObjectTrace[Int, String](traffic_lights, 1), 0, 1)))
    println(print(toLTLformula(getObjectTrace[Int, String](traffic_lights, 2), 0, 1)))
    println()
    //    val psint = NextClosuresScala.pseudoIntents(traffic_lights)
    val implications = NextClosuresScala.pseudoIntents(traffic_lights)
    val ltl_theory = new mutable.HashSet[LTLformula[String]]()
    implications map {
      case (premise, conclusion) => {
        println(premise + " " + UnicodeSymbols.TO + " " + conclusion)
        val i = new LTLimplication[String](toLTLformula(premise, 0, 1), toLTLformula(premise ++ conclusion, 0, 1))
        println(i)
        println()
      }
    } // foreach { println }

    val ltlcxt = ltlize(traffic_lights, 0)
    val ltlimplications = NextClosuresScala.pseudoIntents(ltlcxt)
    ltlimplications filter {
      case (p, _) => !ltlcxt.colAnd(toJavaSet(p)).isEmpty()
    } map {
      case (p, c) => new LTLimplication[String](new LTLconjunction[String](p), new LTLconjunction[String](c))
    } foreach { println }

  }

  def toJavaSet[T](set: Set[T]): java.util.Set[T] = {
    val jset = new java.util.HashSet[T]()
    set.seq.foreach(jset.add(_))
    jset
  }

  def fromJavaSet[T](jset: java.util.Set[T]): Set[T] = {
    val set = new mutable.HashSet[T]()
    val jt = jset.iterator()
    while (jt.hasNext())
      set.add(jt.next())
    set
  }

}