package conexp.fx.core.algorithm.nextclosure;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

import conexp.fx.core.closureoperators.ClosureOperator;
import conexp.fx.core.collections.setlist.SetList;

public class NextClosure<T> implements Iterable<Set<T>> {

  private final SetList<T>         base;
  private final ClosureOperator<T> clop;

  public NextClosure(final SetList<T> base, final ClosureOperator<T> clop) {
    super();
    this.base = base;
    this.clop = clop;
  }

  public final Iterator<Set<T>> iterator() {

    return new UnmodifiableIterator<Set<T>>() {

      private final int n = base.size();
      private Set<T>    c = clop.closure(new HashSet<T>());

      public final boolean hasNext() {
        return c != null;
      }

      public final Set<T> next() {
        final Set<T> _nextExtent = c;
        _APlus();
        return _nextExtent;
      }

      private final void _APlus() {
        Set<T> _APlus;
        for (int i = n - 1; i > -1; --i) {
          final T e = base.get(i);
          if (!c.contains(e)) {
            _APlus = _APlusG(e);
            if (_AisLexicSmallerG(_APlus, e)) {
              c = _APlus;
              return;
            }
          }
        }
        c = null;
      }

      private final Set<T> _APlusG(final T e) {
        return clop.closure(Sets.union(Sets.filter(c, new Predicate<T>() {

          private final int i = base.indexOf(e);

          public final boolean apply(final T input) {
            return base.indexOf(input) < i;
          }
        }), Collections.singleton(e)));
      }

      private final boolean _AisLexicSmallerG(final Set<T> s, final T e) {
        for (T x : s)
          if (x == e)
            break;
          else if (!c.contains(x))
            return false;
        return true;
      }
    };
  }
}
