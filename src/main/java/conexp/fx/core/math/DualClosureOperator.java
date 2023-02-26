package conexp.fx.core.math;

/*-
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2022 Francesco Kriegel
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;

//import conexp.fx.core.dl.ELConceptDescription;
//import conexp.fx.core.dl.ELInterpretation2;
//import conexp.fx.core.dl.ELTBox;

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

}
