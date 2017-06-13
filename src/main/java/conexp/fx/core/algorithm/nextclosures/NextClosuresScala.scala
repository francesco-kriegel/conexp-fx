package conexp.fx.core.algorithm.nextclosures

import scala.collection.mutable.BitSet
import scala.collection.Set
import org.ujmp.core.booleanmatrix.BooleanMatrix
import org.ujmp.core.calculation.Calculation.Ret
import conexp.fx.core.context.MatrixContext

object NextClosuresScala {

  def foo(): Int = 42

  def bar[G, M](mcxt: MatrixContext[G, M]): Int = 23

  def bax1(): Unit = {}

  def bax2[G](y: java.util.Set[G]): Unit = {}

  def bax3(y: java.util.Set[java.lang.Integer]): Unit = {}

  def bax4(y: java.util.Set[java.lang.Integer]): java.util.Set[java.lang.Integer] = y

  def bax5(x: MatrixContext[java.lang.String, java.lang.String], y: java.util.Set[java.lang.Integer]): java.util.Set[java.lang.Integer] = y

  def baz[G, M](mcxt: MatrixContext[G, M], javaResult: java.util.Set[BitImpl]): java.util.Set[BitImpl] = javaResult

  //    exe: ExecutorService,
  //    conceptConsumer: Consumer[Concept[G, M]],
  //    implicationConsumer: Consumer[Implication[G, M]],
  //    updateStatus: Consumer[String],
  //    updateProgress: Consumer[Double],
  //    isCancelled: Supplier[Boolean],
  def compute[G, M](
    mcxt: MatrixContext[G, M],
    javaResult: java.util.Set[BitImpl],
    javaIntents: java.util.Set[BitSet]): (java.util.Set[BitImpl], java.util.Set[BitSet]) = {
    val (implications, intents) = bitCompute(mcxt)
    implications.par foreach javaResult.add
    intents.par foreach javaIntents.add
    (javaResult, javaIntents)
  }

  def bitCompute[G, M](mcxt: MatrixContext[G, M]): (Set[BitImpl], Set[BitSet]) = {
    //    val mcxt = cxt.asInstanceOf[MatrixContext[G, M]]
    //    val r = new Result[G, M]()
    //    val candidates = new scala.collection.mutable.HashSet[BitSet]() with scala.collection.mutable.SynchronizedSet[BitSet] + new BitSet()
    val candidates = new scala.collection.mutable.HashMap[Integer, scala.collection.mutable.Set[BitSet]]() with scala.collection.mutable.SynchronizedMap[Integer, scala.collection.mutable.Set[BitSet]]
    def addCandidate(size: Integer, candidate: BitSet) =
      candidates.getOrElseUpdate(size, new scala.collection.mutable.HashSet[BitSet]() with scala.collection.mutable.SynchronizedSet[BitSet]).add(candidate)
    addCandidate(0, new BitSet())
    val intents = new scala.collection.mutable.HashSet[BitSet]() with scala.collection.mutable.SynchronizedSet[BitSet]
    val implications = new scala.collection.mutable.HashSet[BitImpl]() with scala.collection.mutable.SynchronizedSet[BitImpl]
    def bitClosure1(set: BitSet): BitSet = {
      val cl = set.clone()
      var changed = false
      do {
        changed = false
        implications.par.foreach(impl =>
          if ((impl.premise.size != cl.size) && (impl.premise subsetOf cl) && !(impl.conclusion subsetOf cl)) {
            cl ++= impl.conclusion
            changed = true
          })
      } while (changed)
      cl
    }
    def bitClosure2(set: BitSet): BitSet = {
      val cl = set.clone()
      var changed = false
      do {
        changed = false
        implications.par.foreach(impl =>
          if ((impl.premise.size != cl.size) && (impl.premise subsetOf cl))
            changed |= impl.conclusion.map(cl.add).reduce(_ | _))
      } while (changed)
      cl
    }
    def andRow1(mat: BooleanMatrix, is: BitSet): BitSet = {
      val r = new BitSet()
      val it = is.iterator
      if (it.hasNext) {
        val x = mat.selectRows(Ret.NEW, it.next())
        while (it.hasNext)
          x.and(Ret.ORIG, mat.selectRows(Ret.LINK, it.next()))
        for (i <- 0 to mat.getColumnCount().toInt - 1 if (x.getAsBoolean(0, i)))
          r += i
        r
      } else {
        for (i <- 0 to mat.getColumnCount.toInt - 1)
          r += i
        r
      }
    }
    def andCol1(mat: BooleanMatrix, js: BitSet): BitSet = {
      val r = new BitSet()
      val jt = js.iterator
      if (jt.hasNext) {
        val y = mat.selectColumns(Ret.NEW, jt.next())
        while (jt.hasNext)
          y.and(Ret.ORIG, mat.selectColumns(Ret.LINK, jt.next()))
        for (j <- 0 to mat.getRowCount().toInt - 1 if (y.getAsBoolean(j, 0)))
          r += j
        r
      } else {
        for (j <- 0 to mat.getRowCount().toInt - 1)
          r += j
        r
      }
    }
    def andRow2(mat: BooleanMatrix)(is: BitSet): BitSet = {
      is.foldLeft({
        val d = new BitSet
        d ++= (0 to mat.getColumnCount.toInt - 1)
        d
      })((d, i) => {
        d retain (0 to mat.getColumnCount.toInt - 1).filter(mat.getBoolean(i, _)).contains
        d
      })
    }
    def andCol2(mat: BooleanMatrix)(js: BitSet): BitSet = {
      js.foldLeft({
        val c = new BitSet
        c ++= (0 to mat.getRowCount.toInt - 1)
        c
      })((c, j) => {
        c retain (0 to mat.getRowCount.toInt - 1).filter(i => mat.getBoolean(i, j)).contains
        c
      })
    }
    def rowAnd1(is: BitSet) = andRow1(mcxt.matrix(), is)
    def colAnd1(js: BitSet) = andCol1(mcxt.matrix(), js)
    def rowAnd2 = andRow2(mcxt.matrix())_
    def colAnd2 = andCol2(mcxt.matrix())_
    (0 to mcxt.colHeads().size() - 1).seq.foreach(i =>
      candidates.get(i) map (_.par.foreach(cand => {
        val clos = bitClosure2(cand)
        if (i == clos.size) {
          val int = rowAnd1(colAnd1(cand))
          if (i != int.size)
            implications add new BitImpl(cand, int -- cand)
          if (!intents(int)) {
            intents += int
            (0 to mcxt.colHeads().size() - 1).par.filter(!int(_)).map(int + _).foreach(addCandidate(int.size + 1, _))
          }
        } else
          addCandidate(clos.size, clos)
        //            candidates -= cand
      })))
    (implications, intents)
  }

  def pseudoIntents[G, M](cxt: MatrixContext[G, M]): Set[(Set[M], Set[M])] = {
    bitCompute(cxt)._1.map(i => (i.premise() map cxt.colHeads().get, i.conclusion() map cxt.colHeads().get))
  }
}