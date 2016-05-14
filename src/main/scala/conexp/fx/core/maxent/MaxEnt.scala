package conexp.fx.core.maxent

import java.util.ArrayList
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.asScalaSet
import scala.collection.JavaConversions.bufferAsJavaList
import scala.collection.JavaConversions.mutableSetAsJavaSet
import org.ujmp.core.doublematrix.impl.DefaultSparseDoubleMatrix
import com.google.common.collect.Sets
import conexp.fx.core.algorithm.lattice.IPred
import conexp.fx.core.algorithm.nextclosures.NextClosures2Bit
import conexp.fx.core.context.Context
import conexp.fx.core.context.Implication
import org.ujmp.core.doublematrix.DoubleMatrix
import org.ujmp.core.doublematrix.impl.DefaultDenseDoubleMatrix2D

class MaxEnt[G, M] {

  def entropy(cxt: Context[G, M]): Double = {
    def confidence(p: java.util.Set[M], c: java.util.Set[M]): Double = cxt.colAnd(p ++ c).size() / cxt.colAnd(p).size()
    var entropy = 0d
    Sets.powerSet(cxt.colHeads()) foreach { p =>
      Sets.powerSet(cxt.colHeads()) foreach { c =>
        entropy += xpluslogx(confidence(p, c))
      }
    }
    -entropy
  }

  def xpluslogx(x: Double) = x * Math.log(x)

  abstract class Weight[M] extends Function[Implication[G, M], Double]

  def generateMatEntModel(cxt: Context[G, M]): Weight[M] = {
    val mcxt = cxt.toMatrixContext()
    val x = NextClosures2Bit.bitBitCompute(cxt)
    val concepts = x.first()
    val implications = new ArrayList(x.second())
    val lat = IPred.getConceptLattice(mcxt, concepts)
    val partialImplications = new ArrayList(lat.luxenburgerBase(0, false))

    val intents = new ArrayList(concepts map (_.getIntent))
    val pseudoIntents = new ArrayList(implications map (_.getPremise))

    val rows = implications.size() + partialImplications.size()
    val cols = implications.size() + concepts.size()

    // rows are indexed with implications and partial implications
    // columns are indexed with pseudo-intents and intents
    val A = new DefaultSparseDoubleMatrix(rows, cols)

    for (i <- implications) {
      A.setDouble(1, implications.indexOf(i), pseudoIntents.indexOf(i.getPremise))
      A.setDouble(-1, implications.indexOf(i), pseudoIntents.size() + intents.indexOf(i.getConclusion))
    }
    for (i <- partialImplications) {
      A.setDouble(1, implications.size() + partialImplications.indexOf(i), pseudoIntents.size() + intents.indexOf(i.getPremise))
      A.setDouble(-i.getConfidence, implications.size() + partialImplications.indexOf(i), pseudoIntents.size() + intents.indexOf(i.getConclusion))
    }

    null
  }

}