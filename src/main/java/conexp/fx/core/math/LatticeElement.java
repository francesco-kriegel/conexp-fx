package conexp.fx.core.math;

public interface LatticeElement<E> extends PartialComparable<E> {
  
  public E greatest();
  
  public E smallest();

  public E infimum(E e);

  public E supremum(E e);

  public boolean inf(E e);

  public boolean sup(E e);

}
