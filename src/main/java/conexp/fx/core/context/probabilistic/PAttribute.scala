package conexp.fx.core.context.probabilistic

import java.util.Set
import conexp.fx.core.context.probabilistic.Relation._
import conexp.fx.core.util.UnicodeSymbols

abstract class PAttribute[M]

case class PlainAttribute[M](m: M) extends PAttribute[M] {
  override def toString: String = m.toString

  override def equals(that: Any): Boolean = {
    that.isInstanceOf[PlainAttribute[M]] && m.equals(that.asInstanceOf[PlainAttribute[M]].m)
  }
}

case class ProbabilisticAttribute[M](r: Relation, p: Double, ms: Set[M]) extends PAttribute[M] {
  override def toString: String = r match {
    case Relation.LNEQ => "P<" + p + "." + ms
    case Relation.LEQ  => "P" + UnicodeSymbols.LEQ + p + "." + ms
    case Relation.EQ   => "P=" + p + "." + ms
    case Relation.GEQ  => "P" + UnicodeSymbols.GEQ + p + "." + ms
    case Relation.GNEQ => "P>" + p + "." + ms
  }

  override def equals(that: Any): Boolean = {
    that.isInstanceOf[ProbabilisticAttribute[M]] && p.equals(that.asInstanceOf[ProbabilisticAttribute[M]].p) && ms.equals(that.asInstanceOf[ProbabilisticAttribute[M]].ms)
  }
}