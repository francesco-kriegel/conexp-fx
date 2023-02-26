/*
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.algorithm.nextclosure;

/*
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

import conexp.fx.core.collections.BitSetFX;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.setlist.SetLists;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.gui.task.TimeTask;

public final class NextConcept<G, M> implements Iterable<Concept<G, M>> {

  public static final <G, M> TimeTask<Void> concepts(final ConceptLattice<G, M> lattice) {
    return new TimeTask<Void>("NextConcept") {

      private final Comparator<Concept<G, M>> intentSizeComparator = new Comparator<Concept<G, M>>() {

        public final int compare(final Concept<G, M> c1, final Concept<G, M> c2) {
          return (int) Math.signum(c1.intent().size() - c2.intent().size());
        }
      };

      protected final Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        updateMessage("Computing Formal Concepts...");
        lattice.dispose();
        updateProgress(0.05d, 1d);
        double currentConceptNumber = 0;
        final double maximalConceptNumber =
            Math.pow(2d, (double) Math.min(lattice.context.rowHeads().size(), lattice.context.colHeads().size()));
        final HashSet<Concept<G, M>> hashSet = new HashSet<Concept<G, M>>();
        updateProgress(0.1d, 1d);
        final Iterator<Concept<G, M>> iterator = new NextConcept<G, M>(lattice.context).iterator();
        updateProgress(0.2d, 1d);
        while (iterator.hasNext()) {
          Concept<G, M> concept = iterator.next();
          hashSet.add(concept);
          currentConceptNumber++;
          updateProgress(0.2d + 0.5d * (currentConceptNumber / maximalConceptNumber), 1d);
          updateMessage("computing concepts: " + currentConceptNumber + "...");
        }
        updateProgress(0.7d, 1d);
        final ArrayList<Concept<G, M>> concepts = new ArrayList<Concept<G, M>>(hashSet);
        updateProgress(0.75d, 1d);
        updateMessage("sorting " + currentConceptNumber + " concepts...");
        Collections.sort(concepts, intentSizeComparator);
        updateProgress(0.9d, 1d);
        lattice.rowHeads().addAll(concepts);
        updateMessage("Pushing Changes...");
//        lattice.pushAllChangedEvent();
        updateProgress(1d, 1d);
        return null;
      }
    };
  }

  private final MatrixContext<G, M> context;

  public NextConcept(final MatrixContext<G, M> context) {
    super();
    this.context = context;
  }

  private interface HullOperator<M> {

    public Collection<Integer> closure(final Iterable<M> iterable);
  }

  public final Iterator<Concept<G, M>> iterator() {
    final MatrixContext<G, M> selection = context.selection;
//    selection.pushAllChangedEvent();
    // maybe drop this for huge contexts
    // or encapsulate in own class or blocking task
//    final MatrixContext<Set<Integer>, Set<Integer>> reduced;
//    switch (MatrixContext.AutomaticMode.fromSize(selection.rowHeads().size(), selection.colHeads().size())){
//    	case REDUCE:
//    		reduced = selection._reduced.clone();
//    		break;
//    	case CLEAN:
//    	case NONE:
//    	default:
//    reduced = selection._cleaned.clone();
//       	 	break;
//    }
    final MatrixContext<Set<Integer>, Set<Integer>> reduced = selection._reduced.clone();
    final HullOperator<Integer> hullOp = new HullOperator<Integer>() {

      public Collection<Integer> closure(Iterable<Integer> set) {
        return reduced._extent(set);
      }
    };
    return Iterators.transform(new UnmodifiableIterator<BitSetFX>() {

      private final int rows = reduced.rowHeads().size();
      private BitSetFX _A = new BitSetFX(reduced._colAnd(SetLists.integers(reduced.colHeads().size())));

      public final boolean hasNext() {
        return _A != null;
      }

      public final BitSetFX next() {
        final BitSetFX _nextExtent = _A;
        _APlus();
        return _nextExtent;
      }

      private final void _APlus() {
        BitSetFX _APlus;
        for (int _g = rows - 1; _g > -1; --_g)
          if (!_A.contains(_g)) {
            _APlus = _APlusG(_g);
            if (_AisLexicSmallerG(_APlus, _g)) {
              _A = _APlus;
              return;
            }
          }
        _A = null;
      }

      private final BitSetFX _APlusG(final int _g) {
        return new BitSetFX(
            hullOp.closure(
                Collections3.iterable(
                    Iterators.concat(
                        Iterators.filter(_A.iterator(), Collections3.isSmaller(_g)),
                        Iterators.singletonIterator(_g)))));
      }

      private final boolean _AisLexicSmallerG(final BitSetFX _B, final int _g) {
        for (int _h : _B)
          if (_h == _g)
            break;
          else if (!_A.contains(_h))
            return false;
        return true;
      }
    }, new Function<BitSetFX, Concept<G, M>>() {

      public final Concept<G, M> apply(final BitSetFX _extent) {
        return new Concept<G, M>(
            selection.rowHeads().getAll(
                selection._colAnd(
                    Collections3.iterable(
                        Iterators.concat(
                            Iterators.transform(
                                reduced.colHeads().getAll(reduced._rowAnd(_extent), true).iterator(),
                                Collections3.<Integer> setToIterator())))),
                true),
            selection.colHeads().getAll(
                selection._rowAnd(
                    Collections3.iterable(
                        Iterators.concat(
                            Iterators.transform(
                                reduced.rowHeads().getAll(_extent, true).iterator(),
                                Collections3.<Integer> setToIterator())))),
                true));
      }
    });
  }
}
