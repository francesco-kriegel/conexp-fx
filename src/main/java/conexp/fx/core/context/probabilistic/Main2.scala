package conexp.fx.core.context.probabilistic

import scala.math.Numeric

object Main2 extends App {

  override def main(args: Array[String]): Unit = {
    val w: Set[Any] = Set("foo", "bar", "baz")
    val x: Set[Any] = Set(1, 2, 3)
    val y: Set[Any] = Set("A", "B", "C", "D")
    val z: Set[Any] = Set(true, false)
    for (X <- Set(w, x, y, z).subsets) {
      println("***")
      val cp = PropositionalFormulae.cartesianProduct(X)
      val exp = X.foldLeft(1)((x: Int, y: Set[Any]) => x * y.size)
      println(exp + "=" + cp.size + "  " + (exp == cp.size))
      for (foo <- cp)
        println(foo)
    }

    val h = Not(And[Int](Set(Not[Int](Not[Int](Var[Int](1))))))
    println(h)
    println(PropositionalFormulae.flattenSingletonAndsAndOrs(h))
    println(h map (PropositionalFormulae flattenSingletonAndsAndOrs))
    println(PropositionalFormulae.removeDoubleNegations(h))
    println(h map (PropositionalFormulae flattenSingletonAndsAndOrs) map (PropositionalFormulae removeDoubleNegations))

    val f: PropositionalFormula[Int] =
      And[Int](Set(
        Or[Int](Set(
          Var[Int](1),
          Var[Int](2),
          Var[Int](3),
          Var[Int](1))),
        Or[Int](Set(
          Var[Int](4),
          Var[Int](2),
          Not[Int](Not[Int](Var[Int](7))),
          Var[Int](1))),
        Var(23)))
    val notf = new Not(f)
    println(f)
    println(f map (PropositionalFormulae flattenSingletonAndsAndOrs) map (PropositionalFormulae removeDoubleNegations))
    println(f.conjunctiveNormalForm)
    println(f.disjunctiveNormalForm)
    println(f.fold(0, 100, (v: Int) => -v, (vs: Set[Int]) => vs.foldLeft(0)((x: Int, y: Int) => x max y), (vs: Set[Int]) => vs.foldLeft(100)((x: Int, y: Int) => x min y)))
    val valuation = ((v: Int) => { if (v % 2 == 1) false else true })
    println(f hasModel valuation)
    println((f mapVar[Boolean] valuation).fold(false, true, (v: Boolean) => !v, (vs: Set[Boolean]) => vs.foldLeft(true)((x: Boolean, y: Boolean) => x && y), (vs: Set[Boolean]) => vs.foldLeft(false)((x: Boolean, y: Boolean) => x || y)))
    println()
    println()
    println(notf)
    println(notf.conjunctiveNormalForm)
    println(notf.disjunctiveNormalForm)
    println(notf.flatten.fold(0, 100, (v: Int) => v, (vs: Set[Int]) => vs.foldLeft(100)((x: Int, y: Int) => x min y), (vs: Set[Int]) => vs.foldLeft(0)((x: Int, y: Int) => x max y)))
    println(notf.cnf.flatten.fold(0, 100, (v: Int) => v, (vs: Set[Int]) => vs.foldLeft(100)((x: Int, y: Int) => x min y), (vs: Set[Int]) => vs.foldLeft(0)((x: Int, y: Int) => x max y)))
    println(notf.dnf.flatten.fold(0, 100, (v: Int) => v, (vs: Set[Int]) => vs.foldLeft(100)((x: Int, y: Int) => x min y), (vs: Set[Int]) => vs.foldLeft(0)((x: Int, y: Int) => x max y)))
    println(And[Int](Set(notf)).disjunctiveNormalForm)
  }
}