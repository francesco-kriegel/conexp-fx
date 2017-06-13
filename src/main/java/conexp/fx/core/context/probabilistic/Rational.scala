package conexp.fx.core.context.probabilistic

class Rational(n: BigInt, d: BigInt) { // extends Numeric[Rational] {

  def toDouble: Double = n.toDouble / d.toDouble

  override def toString: String = n + "/" + d

}

object RationalConversions {
  def fromDouble(x: Double, precision: Int): Rational = {
    val (n, d) = makeRelativePrime(x * scala.math.pow(10d, precision.toDouble), scala.math.pow(10d, precision.toDouble))
    new Rational(n, d)
  }

  def makeRelativePrime(k: BigInt, l: BigInt): (BigInt, BigInt) = {
    val gcd = k gcd l
    (k / gcd, l / gcd)
  }

  implicit def toBigInt(x: Double): BigInt = {
    BigDecimal(x).setScale(0, BigDecimal.RoundingMode.HALF_UP).toBigInt()
  }

}