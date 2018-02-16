/*
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.algorithm.nextclosure;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

import conexp.fx.core.collections.BitSetFX;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.setlist.SetLists;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.gui.task.TimeTask;
import de.tudresden.inf.tcs.fcalib.Implication;
import javafx.application.Platform;

public final class NextImplication<G, M> implements Iterable<Implication<M>> {

  public static final <G, M> TimeTask<Void>
      implications(final MatrixContext<G, M> context, final List<Implication<M>> implications) {
    return new TimeTask<Void>("NextImplication") {

      protected final Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        updateMessage("Computing Formal Implications...");
        updateProgress(0.05d, 1d);
        double currentImplicationNumber = 0;
        // final double maximalImplicationNumber = 1;
        updateProgress(0.1d, 1d);
        final Iterator<Implication<M>> iterator = new NextImplication<G, M>(context).iterator();
        updateProgress(0.2d, 1d);
        // final boolean observable = implications instanceof
        // ObservableList;
        while (iterator.hasNext()) {
          final Implication<M> next = iterator.next();
          if (Platform.isFxApplicationThread()) {
            implications.add(next);
          } else {
            Platform.runLater(new Runnable() {

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
        updateProgress(1d, 1d);
        return null;
      }
    };

  }

  private final MatrixContext<G, M> context;

  public NextImplication(final MatrixContext<G, M> context) {
    super();
    this.context = context;
  }

  private interface HullOperator<M> {

    public Collection<Integer> closure(final Collection<M> iterable);
  }

  private final static class BitImplication {

    private final BitSetFX premise;
    private final BitSetFX conclusion;

    private BitImplication(BitSetFX premise, BitSetFX conclusion) {
      super();
      this.premise = premise;
      this.conclusion = conclusion;
    }

  }

  public final Iterator<Implication<M>> iterator() {
    final MatrixContext<Set<Integer>, Set<Integer>> cleaned = context.selection._cleaned.clone();
    final UnmodifiableIterator<BitImplication> it = new UnmodifiableIterator<BitImplication>() {

      private final int                   cols   = cleaned.colHeads().size();
      private BitSetFX                    _P     = new BitSetFX(cleaned._colAnd(SetLists.integers(cols)));
      private final Set<BitImplication>   impls  = new HashSet<BitImplication>();
      private final HullOperator<Integer> hullOp = new HullOperator<Integer>() {

                                                   public final Collection<Integer>
                                                       closure(final Collection<Integer> set) {
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
        BitSetFX _nextPseudoIntent;
        BitImplication bitImplication;
        do {
          _nextPseudoIntent = _P;
          bitImplication = toBitImplication(_nextPseudoIntent);
          _PPlus();
        } while (_P != null && bitImplication.conclusion.isEmpty());
        impls.add(bitImplication);
        return bitImplication;
      }

      private final BitImplication toBitImplication(final BitSetFX pseudoIntent) {
        final BitSetFX conclusion = new BitSetFX(cleaned._intent(pseudoIntent));
        conclusion.removeAll(pseudoIntent);
        return new BitImplication(pseudoIntent, conclusion);
      }

      private final void _PPlus() {
        BitSetFX _PPlus;
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

      private final BitSetFX _PPlusM(final int _m) {
        return new BitSetFX(
            hullOp.closure(
                Sets.newHashSet(
                    Collections3.iterable(
                        Iterators.concat(
                            Iterators.filter(_P.iterator(), Collections3.isSmaller(_m)),
                            Iterators.singletonIterator(_m))))));
      }

      private final boolean _PisLexicSmallerM(final BitSetFX _B, final int _m) {
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
        final Collection<M> p = context.selection.colHeads().getAll(
            Collections3
                .union(Collections2.transform(_implication.premise, new Function<Integer, Collection<Integer>>() {

                  @Override
                  public final Collection<Integer> apply(final Integer index) {
                    return cleaned.colHeads().get(index);
                  }
                })),
            false);
        final Collection<M> c = context.selection.colHeads().getAll(
            Collections3
                .union(Collections2.transform(_implication.conclusion, new Function<Integer, Collection<Integer>>() {

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
