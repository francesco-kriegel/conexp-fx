/*
 * @author Francesco Kriegel (Francesco.Kriegel@tu-dresden.de)
 */
package conexp.fx.core.algorithm;

/*
 * #%L
 * Concept Explorer FX - Core
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.application.Platform;
import javafx.collections.ObservableList;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

import conexp.fx.core.concurrent.BlockingTask;
import conexp.fx.core.context.Implication;
import conexp.fx.core.context.MatrixContext;

public final class NextImplication<G, M> implements Iterable<Implication<M>> {

  public static final <G, M> BlockingTask implications(
      final MatrixContext<G, M> context,
      final List<Implication<M>> implications) {
    return new BlockingTask("NextImplication") {

      protected final void _call() {
        updateMessage("Computing Formal Implications...");
        updateProgress(0.05d, 1d);
        double currentImplicationNumber = 0;
//        final double maximalImplicationNumber = 1;
        updateProgress(0.1d, 1d);
        final Iterator<Implication<M>> iterator = new NextImplication<G, M>(context).iterator();
        updateProgress(0.2d, 1d);
//        final boolean observable = implications instanceof ObservableList;
        while (iterator.hasNext()) {
          final Implication<M> next = iterator.next();
          if (!next.premise.equals(next.conclusion)) {
            final HashSet<M> premise = new HashSet<M>();
            premise.addAll(next.premise);
            final HashSet<M> conclusion = new HashSet<M>();
            conclusion.addAll(next.conclusion);
            conclusion.removeAll(next.premise);
//            if (observable)
//              Platform.runLater(new Runnable() {
//
//                @Override
//                public void run() {
//                  implications.add(new Implication<M>(premise, conclusion));
//                }
//              });
//            else
              implications.add(new Implication<M>(premise, conclusion));
          }
          currentImplicationNumber++;
//          updateProgress(0.2d + 0.7d * (currentImplicationNumber / maximalImplicationNumber), 1d);
          updateMessage("computing implications: " + currentImplicationNumber + "...");
        }
        updateProgress(0.9d, 1d);
      }
    };
  }

  private final MatrixContext<G, M> context;

  public NextImplication(final MatrixContext<G, M> context) {
    super();
    this.context = context;
  }

  public final Iterator<Implication<M>> iterator() {
    final MatrixContext<G, M> selection = context.selection;
    selection.pushAllChangedEvent();
//    final MatrixContext<Set<Integer>, Set<Integer>> reduced = selection._reduced.clone();
    return new UnmodifiableIterator<Implication<M>>() {

      private final int           cols             = selection.colHeads().size();
      private Set<M>              nextPseudoIntent = Collections.<M> emptySet();
      private Set<Implication<M>> implications     = new HashSet<Implication<M>>();

      public final boolean hasNext() {
        return nextPseudoIntent != null;
      }

      public final Implication<M> next() {
        final Set<M> pseudoIntent = nextPseudoIntent;
        if (pseudoIntent.size() == cols)
          nextPseudoIntent = null;
        else
          nextPseudoIntent = nextClosure(pseudoIntent);
        final Implication<M> implication = new Implication<M>(pseudoIntent, selection.intent(pseudoIntent));
        implications.add(implication);
        return implication;
      }

      private final Set<M> closure(final Set<M> attributes) {
        final Set<Implication<M>> impls = new HashSet<Implication<M>>(implications);
        final Set<M> closure = new HashSet<M>(attributes);
        boolean stable = false;
        do {
          stable = true;
          final Iterator<Implication<M>> implIt = impls.iterator();
          while (implIt.hasNext()) {
            final Implication<M> impl = implIt.next();
            if (closure.containsAll(impl.premise)) {
              closure.addAll(impl.conclusion);
              stable = false;
              implIt.remove();
            }
          }
        } while (!stable);
        return closure;
      }

      private final Set<M> linClosure(final Set<M> attributes) {
        final Set<M> closure = new HashSet<M>(attributes);
        final Map<Implication<M>, Integer> count = new HashMap<Implication<M>, Integer>();
        final Multimap<M, Implication<M>> list = HashMultimap.<M, Implication<M>> create();
        for (Implication<M> impl : implications) {
          final int psize = impl.premise.size();
          count.put(impl, psize);
          if (psize == 0)
            closure.addAll(impl.conclusion);
          else
            for (M im : impl.premise)
              list.put(im, impl);
        }
        List<M> update = new LinkedList<M>(attributes);
        while (!update.isEmpty()) {
          final M m = update.remove(0);
          for (Implication<M> impl : list.get(m)) {
            final int newCount = count.get(impl) - 1;
            count.put(impl, newCount);
            if (newCount == 0) {
              final Set<M> add = new HashSet<M>(Sets.difference(impl.conclusion, closure));
              closure.addAll(add);
              update.addAll(add);
            }
          }
        }
        return closure;
      }

      private final Set<M> nextClosure(final Set<M> attributes) {
        final Set<M> closure = new HashSet<M>(attributes);
        final int cols = selection.colHeads().size();
        for (int col = cols - 1; col >= 0; col--) {
          final int index = col;
          final M m = selection.colHeads().get(col);
          if (closure.contains(m))
            closure.remove(m);
          else {
            final Set<M> clos = closure(Sets.union(closure, Collections.singleton(m)));
            final Predicate<M> pred = new Predicate<M>() {

              @Override
              public final boolean apply(final M m) {
                return selection.colHeads().indexOf(m) < index;
              }
            };
            if (Sets.filter(Sets.difference(clos, closure), pred).isEmpty())
              return clos;
          }
        }
        return null;
      }
//    private final void _APlus() {
//      BitSetSet _APlus;
//      for (int _g = rows - 1; _g > -1; --_g)
//        if (!_A.contains(_g)) {
//          _APlus = _APlusG(_g);
//          if (_AisLexicSmallerG(_APlus, _g)) {
//            _A = _APlus;
//            return;
//          }
//        }
//      _A = null;
//    }
//
//    private final BitSetSet _APlusG(final int _g) {
//      return Collections3.newBitSetSet(reduced._extent(Collections3.iterable(Iterators.concat(
//          Iterators.filter(_A.iterator(), Collections3.isSmaller(_g)),
//          Iterators.singletonIterator(_g)))));
//    }
//
//    private final boolean _AisLexicSmallerG(final BitSetSet _B, final int _g) {
//      for (int _h : _B)
//        if (_h == _g)
//          break;
//        else if (!_A.contains(_h))
//          return false;
//      return true;
//    }
    };
  }
}
