package conexp.fx.core.context.probabilistic

import java.util.HashMap
import java.util.HashSet

import scala.collection.JavaConverters._
import scala.collection.mutable.Set

import conexp.fx.core.algorithm.nextclosures.NextClosures2Bit
import conexp.fx.core.algorithm.nextclosures.NextClosures2C
import conexp.fx.core.context.Context
import conexp.fx.core.context.Implication
import conexp.fx.core.context.MatrixContext
import conexp.fx.core.math.ClosureOperator

class PContext[G, M, W] {

  val worlds = new HashSet[W]
  val probabilities = new HashMap[W, Double]
  val contexts = new HashMap[W, Context[G, M]]

  def addWorld(w: W, p: Double, cxt: Context[G, M]) {
    worlds add w
    probabilities put (w, p)
    contexts put (w, cxt)
  }

  def contains(g: G, m: PAttribute[M]): Boolean = m match {
    case PlainAttribute(m) =>
      worlds.asScala.foldLeft(true)((v, w) => v && contexts.get(w).row(g).contains(m))
    case ProbabilisticAttribute(r, p, ms) => r match {
      case Relation.LNEQ => incidenceProbability(g, ms) < p
      case Relation.LEQ  => incidenceProbability(g, ms) <= p
      case Relation.EQ   => incidenceProbability(g, ms) == p
      case Relation.GEQ  => incidenceProbability(g, ms) >= p
      case Relation.GNEQ => incidenceProbability(g, ms) > p
    }
  }

  def incidenceProbability(g: G, B: java.util.Set[M]): Double = {
    var p = 0d
    for (w <- worlds.asScala)
      if (contexts.get(w).row(g).containsAll(B))
        p += probabilities.get(w)
    p
  }

  def objects: Set[G] = new HashSet[G]((contexts asScala)((worlds iterator) next) rowHeads) asScala

  def extension(attribute: PAttribute[M]): Set[G] = objects.filter(contains(_, attribute))

  def extension(attributes: Set[PAttribute[M]]): Set[G] = attributes.map(extension).fold(objects)(intersection)

  def intersection[T](s: Set[T], t: Set[T]): Set[T] = s intersect t

  def models(implication: Implication[G, PAttribute[M]]): Boolean = {
    extension((implication getPremise) asScala) subsetOf extension((implication getConclusion) asScala)
  }

  def axiomatize: (Set[Implication[G, PAttribute[M]]], Set[Implication[(G, W), M]], Set[Implication[G, PAttribute[M]]]) = {
    val acresult = NextClosures2Bit.bitCompute(almostCertainScaling)
    val intents = acresult.first().asScala.map(_.getIntent)
    var values = Map[Set[M], Set[Double]]()
    for (intent <- intents) {
      values += ((intent.asScala, Set[Double]()))
      for (g <- objects) {
        val p = incidenceProbability(g, intent)
        values(intent.asScala) += p
      }
    }
    for (intent <- intents) {
      val sortedValues = values(intent.asScala).toList.sortWith(_ <= _)
      var pairs: Set[(Double, Double)] = Set()
      sortedValues.tail.foldLeft((0d, sortedValues.head))((last: (Double, Double), next: Double) => {
        val current = (last._2, next)
        pairs += current
        current
      })
      pairs map ((pair: (Double, Double)) => {
        new Implication[G, PAttribute[M]](new ProbabilisticAttribute[M](Relation.GNEQ, pair._1, intent), new ProbabilisticAttribute[M](Relation.GEQ, pair._2, intent))
      })
    }
    val valueImplications = null
    val almostCertainImplications = acresult.second().asScala
    val probabilisticImplications = null

    (valueImplications, almostCertainImplications, probabilisticImplications)
  }

  def probabilisticImplicationalBase: java.util.Set[Implication[G, PAttribute[M]]] = {
    (NextClosures2C compute (probabilisticScaling, ClosureOperator.fromImplications(trivialImplications))) second
    //    (NextClosures2Bit bitCompute probabilisticScaling) second
  }

  def trivialImplications: java.util.Set[Implication[G, PAttribute[M]]] = {
    val avail = probabilisticAttributes
    val imps = new HashSet[Implication[G, PAttribute[M]]]
//    val patts = new HashSet[ProbabilisticAttribute[M]]
//    patts addAll probabilisticAttributes
//    val ms = new HashSet[M](contexts.get(worlds.iterator().next()).colHeads())
//    for (plainatt <- (ms asScala).subsets() if (!plainatt.isEmpty))
//      for (probatt <- patts asScala) {
//        val prem = new HashSet[PAttribute[M]]
//        val conc = new HashSet[PAttribute[M]]
//        for (m <- plainatt) prem add new PlainAttribute(m)
//        prem add probatt
//        val unionatts = new HashSet[M]
//        unionatts addAll (plainatt asJava)
//        unionatts addAll (probatt.ms)
//        val newprobatt = new ProbabilisticAttribute[M](probatt.p, unionatts)
//        if (avail contains newprobatt) {
//          conc add newprobatt
//          if (!((unionatts asScala) subsetOf (probatt.ms asScala)))
//            imps.add(new conexp.fx.core.context.Implication[G, PAttribute[M]](prem, conc))
//        }
//        if (((probatt.ms asScala) subsetOf plainatt)) {
//          val t = new HashSet[PAttribute[M]]
//          t add probatt
//          imps.add(new conexp.fx.core.context.Implication[G, PAttribute[M]](new HashSet[PAttribute[M]]((plainatt map (new PlainAttribute[M](_))) asJava), t))
//        }
//      }
//    for (probatt1 <- patts asScala; probatt2 <- patts asScala) {
//      val r = probatt1.p + probatt2.p - 1d;
//      if (r > 0d && !((probatt1.ms asScala) subsetOf (probatt2.ms asScala)) && !((probatt2.ms asScala) subsetOf (probatt1.ms asScala))) {
//        val unionatts = new HashSet[M]
//        unionatts addAll probatt1.ms
//        unionatts addAll probatt2.ms
//        val prem = new HashSet[PAttribute[M]]
//        val conc = new HashSet[PAttribute[M]]
//        prem add probatt1
//        prem add probatt2
//        val unionprobatt = new ProbabilisticAttribute[M](r, unionatts)
//        if (avail contains unionprobatt) {
//          conc add unionprobatt
//          imps.add(new conexp.fx.core.context.Implication[G, PAttribute[M]](prem, conc))
//        }
//      }
//      if (probatt2.p <= probatt1.p && ((probatt2.ms asScala) subsetOf (probatt1.ms asScala)) && !(probatt2 equals probatt1))
//        imps.add(new conexp.fx.core.context.Implication[G, PAttribute[M]](probatt1, probatt2))
//    }
    imps
  }

  implicit def toImplication(premise: HashSet[PAttribute[M]], conclusion: HashSet[PAttribute[M]]): Implication[G, PAttribute[M]] = {
    new conexp.fx.core.context.Implication[G, PAttribute[M]](premise, conclusion)
  }

  def minimize(set: java.util.Set[PAttribute[M]], implications: java.util.Set[Implication[G, PAttribute[M]]]): Set[PAttribute[M]] = {
    val min = Set[PAttribute[M]]()
    for (x <- (set asScala)) min add x
    for (implication <- implications asScala)
      if ((implication.getPremise asScala) subsetOf min)
        min --= (implication.getConclusion asScala)
    min
  }

  def probabilisticScaling: Context[G, PAttribute[M]] = {
    val probabilisticScaling = new MatrixContext[G, PAttribute[M]](false)
    val G = new HashSet(contexts.get(worlds.iterator().next()).rowHeads()).asScala
    val acxt = contexts.get(worlds.iterator().next())
    probabilisticScaling.rowHeads().addAll(acxt.rowHeads())
    for (m <- new HashSet(acxt.colHeads()).asScala) {
      val plainAttribute = new PlainAttribute[M](m)
      (probabilisticScaling colHeads) add plainAttribute
      for (g <- G)
        if (contains(g, plainAttribute))
          probabilisticScaling add (g, plainAttribute)
    }
    for (probabilisticAttribute <- probabilisticAttributes asScala) {
      (probabilisticScaling colHeads) add probabilisticAttribute
      for (g <- G)
        if (contains(g, probabilisticAttribute))
          probabilisticScaling add (g, probabilisticAttribute)
    }
    probabilisticScaling
  }

  def probabilisticAttributes: HashSet[ProbabilisticAttribute[M]] = {
    val probabilisticAttributes = new HashSet[ProbabilisticAttribute[M]]
    val G = new HashSet(contexts.get(worlds.iterator().next()).rowHeads()).asScala
    val cxt = almostCertainScaling
    val concepts = (NextClosures2Bit bitCompute cxt).first()
    val intents = concepts.asScala.map(_ getIntent)
    for (intent <- intents; g <- G) {
      val probability = incidenceProbability(g, intent)
      if (probability > 0d && probability < 0.9999999999999999d && !intent.isEmpty)
        probabilisticAttributes add new ProbabilisticAttribute[M](Relation.GEQ, probability, intent)
    }
    probabilisticAttributes
  }

  def almostCertainScaling: Context[(G, W), M] = {
    val cxt = new MatrixContext[(G, W), M](false)
    for (w <- worlds asScala) {
      val wcxt = contexts get w
      for (g <- new HashSet[G](wcxt.rowHeads()) asScala) {
        cxt.rowHeads().add((g, w))
        for (m <- new HashSet[M](wcxt.colHeads()) asScala) {
          cxt.colHeads().add(m)
          if (wcxt.contains(g, m)) cxt.add((g, w), m)
        }
      }
    }
    cxt
  }

  override def toString: String = {
    val sb = new StringBuilder
    for (w <- worlds.asScala) {
      sb append ("World: " + w + "\r\n")
      sb append ("Probability: " + (probabilities asScala)(w) + "\r\n")
      sb append ((contexts asScala)(w) + "\r\n")
    }
    sb toString
  }

}