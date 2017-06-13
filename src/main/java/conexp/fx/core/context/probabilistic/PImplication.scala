package conexp.fx.core.context.probabilistic

import conexp.fx.core.context.Implication

class PImplication[G, M] extends Implication[G, ProbabilisticAttribute[M]] {

  override def isTrivial: Boolean = {
    false
  }

}