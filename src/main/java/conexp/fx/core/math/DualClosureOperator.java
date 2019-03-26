package conexp.fx.core.math;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;

import conexp.fx.core.dl.ELConceptDescription;
import conexp.fx.core.dl.ELInterpretation2;
import conexp.fx.core.dl.ELTBox;

@FunctionalInterface
public interface DualClosureOperator<T extends LatticeElement<T>> extends Function<T, T> {

  @Override
  public default T apply(final T element) {
    return closure(element);
  }

  public T closure(T element);

  public default boolean close(final T element) {
    return !element.inf(closure(element));
  }

  public default boolean isClosed(final T element) {
    return element.equivalent(closure(element));
  }

  @SafeVarargs
  public static <T extends LatticeElement<T>> DualClosureOperator<T>
      infimum(final DualClosureOperator<T>... closureOperators) {
    return infimum(Arrays.asList(closureOperators));
  }

  public static <T extends LatticeElement<T>> DualClosureOperator<T>
      infimum(final Iterable<DualClosureOperator<T>> closureOperators) {
    return element -> {
      final Iterator<DualClosureOperator<T>> it = closureOperators.iterator();
      if (!it.hasNext())
        return element.smallest();
      else {
        final T closure = it.next().closure(element);
        while (it.hasNext())
          closure.sup(it.next().closure(element));
        return closure;
      }
    };
  }

  @SafeVarargs
  public static <T extends LatticeElement<T>> DualClosureOperator<T>
      supremum(final DualClosureOperator<T>... closureOperators) {
    return supremum(Arrays.asList(closureOperators));
  }

  public static <T extends LatticeElement<T>> DualClosureOperator<T>
      supremum(final Iterable<DualClosureOperator<T>> closureOperators) {
    return element -> {
      final Iterator<DualClosureOperator<T>> it = closureOperators.iterator();
      if (!it.hasNext())
        return element;
      else {
        final T closure = closureOperators.iterator().next().closure(element);
        boolean changed = true;
        while (changed) {
          changed = false;
          for (DualClosureOperator<T> clop : closureOperators)
            changed |= closure.inf(clop.closure(closure));
        }
        return closure;
      }
    };
  }

  public static <I> DualClosureOperator<ELConceptDescription>
      fromInterpretation(final ELInterpretation2<I> i, final int d) {
    if (d < 0)
      throw new IllegalArgumentException();
    else
      return c -> {
        if (c.roleDepth() > d)
          throw new IllegalArgumentException();
        else
          return i.getMostSpecificConceptDescription(i.getExtension(c), d);
      };
  }

  public static DualClosureOperator<ELConceptDescription> fromTBox(final ELTBox t, final int d) {
    if (d < 0)
      throw new IllegalArgumentException();
    else
      return c -> {
        if (c.roleDepth() > d)
          throw new IllegalArgumentException();
        else
          return t.getMostSpecificConsequence(c, d);
      };
  }

}
