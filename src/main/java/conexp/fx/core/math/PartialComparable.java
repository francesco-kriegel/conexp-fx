/**
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.math;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

public interface PartialComparable<E> extends Comparable<E> {

  public boolean smaller(E e);

  public boolean greater(E e);

  public boolean smallerEq(E e);

  public boolean greaterEq(E e);

  public boolean uncomparable(E e);
}
