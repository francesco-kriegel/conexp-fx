/*
 * @author Francesco Kriegel (Francesco.Kriegel@tu-dresden.de)
 */
package conexp.fx.core.algorithm.nextclosure;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.implication.Implication;
import conexp.fx.core.implication.ImplicationSet;
import conexp.fx.gui.task.BlockingTask;

public final class NextImplication<G, M> implements Iterable<Implication<G, M>> {

  public static final <G, M> BlockingTask implications(
      final MatrixContext<G, M> context,
      final List<Implication<G, M>> implications) {
    return new BlockingTask("NextImplication") {

      protected final void _call() {
        updateMessage("Computing Formal Implications...");
        updateProgress(
            0.05d,
            1d);
        double currentImplicationNumber = 0;
        // final double maximalImplicationNumber = 1;
        updateProgress(
            0.1d,
            1d);
        final Iterator<Implication<G, M>> iterator = new NextImplication<G, M>(context).iterator();
        updateProgress(
            0.2d,
            1d);
        // final boolean observable = implications instanceof
        // ObservableList;
        while (iterator.hasNext()) {
          final Implication<G, M> next = iterator.next();
          if (Platform.isFxApplicationThread()) {
            implications.add(next);
          } else {
            Platform.runLater(new Runnable() {

              public void run() {
                implications.add(next);
              }
            });
          }
//          if (!next.getPremise().equals(next.getConclusion())) {
//            final HashSet<M> premise = new HashSet<M>();
//            premise.addAll(next.getPremise());
//            final HashSet<M> conclusion = new HashSet<M>();
//            conclusion.addAll(next.getConclusion());
//            conclusion.removeAll(next.getPremise());
//            // if (observable)
//            // Platform.runLater(new Runnable() {
//            //
//            // @Override
//            // public void run() {
//            // implications.add(new Implication<G,M>(premise,
//            // conclusion));
//            // }
//            // });
//            // else
//            implications.add(new Implication<G,M>(premise, conclusion));
//          }
          currentImplicationNumber++;
          // updateProgress(0.2d + 0.7d * (currentImplicationNumber /
          // maximalImplicationNumber), 1d);
          updateMessage("computing implications: " + currentImplicationNumber + "...");
        }
        updateProgress(
            0.9d,
            1d);
      }
    };

  }

  private final MatrixContext<G, M> context;

  public NextImplication(final MatrixContext<G, M> context) {
    super();
    this.context = context;
  }

  public final Iterator<Implication<G, M>> iterator() {
    final MatrixContext<G, M> selection = context.selection;
    selection.pushAllChangedEvent();
    // final MatrixContext<Set<Integer>, Set<Integer>> reduced =
    // selection._reduced.clone();
    return new UnmodifiableIterator<Implication<G, M>>() {

      private final int         cols             = selection.colHeads().size();
      private Set<M>            nextPseudoIntent = Collections.<M> emptySet();
      private ImplicationSet<M> implications     = new ImplicationSet<M>();

      public final boolean hasNext() {
        return nextPseudoIntent != null;
      }

      // TODO: Check for null values on start...
      public final Implication<G, M> next() {
        Set<M> pseudoIntent = nextPseudoIntent();
        Set<M> intent = selection.intent(pseudoIntent);
        while (pseudoIntent != null && intent.equals(pseudoIntent)) {
          pseudoIntent = nextPseudoIntent();
          intent = selection.intent(pseudoIntent);
        }
        if (pseudoIntent == null)
          return null;
        final HashSet<M> premise = Sets.newHashSet(pseudoIntent);
        final HashSet<M> conclusion = Sets.newHashSet(intent);
        conclusion.removeAll(premise);
        final Implication<G, M> implication = new Implication<G, M>(premise, conclusion);
        implications.add(implication);
        return implication;
      }

      private Set<M> nextPseudoIntent() {
        final Set<M> pseudoIntent = nextPseudoIntent;
        if (pseudoIntent == null)
          return null;
        if (pseudoIntent.size() == cols)
          nextPseudoIntent = null;
        else
          nextPseudoIntent = nextClosure(pseudoIntent);
        return pseudoIntent;
      }

      private final Set<M> nextClosure(final Set<M> attributes) {
        final Set<M> closure = new HashSet<M>(attributes);

        final int cols = selection.colHeads().size();
        for (int col = cols - 1; col >= 0; col--) {
          final int index = col;
          final M m = selection.colHeads().get(
              col);
          if (closure.contains(m))
            closure.remove(m);
          else {
            final Set<M> clos = implications.closure(Sets.union(
                closure,
                Collections.singleton(m)));
            final Predicate<M> pred = new Predicate<M>() {

              public final boolean apply(final M m) {
                return selection.colHeads().indexOf(
                    m) < index;
              }
            };
            if (Sets.filter(
                Sets.difference(
                    clos,
                    closure),
                pred).isEmpty())
              return clos;
          }
        }
        return null;
      }
    };
  }
}
