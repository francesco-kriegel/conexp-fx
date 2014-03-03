/*
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.algorithm.nextclosure;

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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;

import org.ujmp.core.collections.BitSetSet;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.setlist.SetLists;
import conexp.fx.core.concurrent.BlockingTask;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter;
import de.tudresden.inf.tcs.fcalib.Implication;

public final class NextImplication2<G, M> implements Iterable<Implication<M>> {

  public static final <G, M> BlockingTask implications(
      final MatrixContext<G, M> context,
      final List<Implication<M>> implications) {
    return new BlockingTask("NextImplication") {

      protected final void _call() {
        updateMessage("Computing Formal Implications...");
        updateProgress(0.05d, 1d);
        double currentImplicationNumber = 0;
        // final double maximalImplicationNumber = 1;
        updateProgress(0.1d, 1d);
        final Iterator<Implication<M>> iterator = new NextImplication2<G, M>(context).iterator();
        updateProgress(0.2d, 1d);
        // final boolean observable = implications instanceof
        // ObservableList;
        while (iterator.hasNext()) {
          final Implication<M> next = iterator.next();
          if (Platform.isFxApplicationThread()) {
            implications.add(next);
          } else {
            Platform.runLater(new Runnable() {

              @Override
              public void run() {
                implications.add(next);
              }
            });
          }
//            if (!next.getPremise().equals(next.getConclusion())) {
//              final HashSet<M> premise = new HashSet<M>();
//              premise.addAll(next.getPremise());
//              final HashSet<M> conclusion = new HashSet<M>();
//              conclusion.addAll(next.getConclusion());
//              conclusion.removeAll(next.getPremise());
//              // if (observable)
//              // Platform.runLater(new Runnable() {
//              //
//              // @Override
//              // public void run() {
//              // implications.add(new Implication<M>(premise,
//              // conclusion));
//              // }
//              // });
//              // else
//              implications.add(new Implication<M>(premise, conclusion));
//            }
          currentImplicationNumber++;
          // updateProgress(0.2d + 0.7d * (currentImplicationNumber /
          // maximalImplicationNumber), 1d);
          updateMessage("computing implications: " + currentImplicationNumber + "...");
        }
        updateProgress(0.9d, 1d);
      }
    };

  }

  private final MatrixContext<G, M> context;

  public NextImplication2(final MatrixContext<G, M> context) {
    super();
    this.context = context;
  }

  private interface HullOperator<M> {

    public Collection<Integer> closure(final Collection<M> iterable);
  }

  private final static class BitImplication {

    private final BitSetSet premise;
    private final BitSetSet conclusion;

    private BitImplication(BitSetSet premise, BitSetSet conclusion) {
      super();
      this.premise = premise;
      this.conclusion = conclusion;
    }

  }

  public final Iterator<Implication<M>> iterator() {
    final MatrixContext<Set<Integer>, Set<Integer>> cleaned = context.selection._cleaned.clone();
    final UnmodifiableIterator<BitImplication> it = new UnmodifiableIterator<BitImplication>() {

      private final int                   cols   = cleaned.colHeads().size();
      private BitSetSet                   _P     = Collections3.newBitSetSet(cleaned._colAnd(SetLists.integers(cols)));
      private final Set<BitImplication>   impls  = new HashSet<BitImplication>();
      private final HullOperator<Integer> hullOp = new HullOperator<Integer>() {

                                                   @Override
                                                   public final Collection<Integer> closure(
                                                       final Collection<Integer> set) {
                                                     for (BitImplication impl : impls)
                                                       if (set.containsAll(impl.premise))
                                                         set.addAll(impl.conclusion);
                                                     return set;
                                                   }
                                                 };

      public final boolean hasNext() {
        return _P != null;
      }

      public final BitImplication next() {
        BitSetSet _nextPseudoIntent;
        BitImplication bitImplication;
        do {
          _nextPseudoIntent = _P;
          bitImplication = toBitImplication(_nextPseudoIntent);
          _PPlus();
        } while (_P != null && bitImplication.conclusion.isEmpty());
        impls.add(bitImplication);
        return bitImplication;
      }

      private final BitImplication toBitImplication(final BitSetSet pseudoIntent) {
        final BitSetSet conclusion = Collections3.newBitSetSet(cleaned._intent(pseudoIntent));
        conclusion.removeAll(pseudoIntent);
        return new BitImplication(pseudoIntent, conclusion);
      }

      private final void _PPlus() {
        BitSetSet _PPlus;
        for (int _m = cols - 1; _m > -1; --_m)
          if (!_P.contains(_m)) {
            _PPlus = _PPlusM(_m);
            if (_PisLexicSmallerM(_PPlus, _m)) {
              _P = _PPlus;
              return;
            }
          }
        _P = null;
      }

      private final BitSetSet _PPlusM(final int _m) {
        return Collections3.newBitSetSet(hullOp.closure(Sets.newHashSet(Collections3.iterable(Iterators.concat(
            Iterators.filter(_P.iterator(), Collections3.isSmaller(_m)),
            Iterators.singletonIterator(_m))))));
      }

      private final boolean _PisLexicSmallerM(final BitSetSet _B, final int _m) {
        for (int _n : _B)
          if (_n == _m)
            break;
          else if (!_P.contains(_n))
            return false;
        return true;
      }
    };
    final Function<BitImplication, Implication<M>> f = new Function<BitImplication, Implication<M>>() {

      public final Implication<M> apply(final BitImplication _implication) {
        final Collection<M> p =
            context.selection.colHeads().getAll(
                Collections3.union(Collections2.transform(
                    _implication.premise,
                    new Function<Integer, Collection<Integer>>() {

                      @Override
                      public final Collection<Integer> apply(final Integer index) {
                        return cleaned.colHeads().get(index);
                      }
                    })),
                false);
        final Collection<M> c =
            context.selection.colHeads().getAll(
                Collections3.union(Collections2.transform(
                    _implication.conclusion,
                    new Function<Integer, Collection<Integer>>() {

                      @Override
                      public final Collection<Integer> apply(final Integer index) {
                        return cleaned.colHeads().get(index);
                      }
                    })),
                false);
        return new Implication<M>(Sets.newHashSet(p), Sets.newHashSet(c));
      }
    };
    return Iterators.transform(it, f);
  }
}
