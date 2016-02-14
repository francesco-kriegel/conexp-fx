package conexp.fx.scala

import scala.collection.mutable.BitSet

class BitImpl(premise: BitSet = new BitSet(), conclusion: BitSet = new BitSet()) {
  def premise(): BitSet = premise
  def conclusion(): BitSet = conclusion

  override def toString(): String = premise.toString() + " --> " + conclusion.toString()

  override def equals(that: Any): Boolean = that.isInstanceOf[BitImpl] && {
    val other = that.asInstanceOf[BitImpl]
    (this.premise() equals other.premise()) && (this.conclusion() equals other.conclusion())
  }

  override def hashCode(): Int = 5 + 7 * this.premise().hashCode() + 11 * this.conclusion().hashCode()
}