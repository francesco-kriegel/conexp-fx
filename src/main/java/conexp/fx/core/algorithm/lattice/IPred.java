/**
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.algorithm.lattice;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2019 Francesco Kriegel
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;

import conexp.fx.core.collections.BitSetFX;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.gui.dataset.FCADataset;
import conexp.fx.gui.task.TimeTask;

public final class IPred<G, M> {

  public static final <G, M> TimeTask<Void> neighborhood(final String id, final ConceptLattice<G, M> lattice) {
    return new TimeTask<Void>(id + " - iPred") {

      protected final Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        updateMessage("Computing Concept Neighborhood...");
        lattice.empty();
        final int bits = lattice.context.colHeads().size();
        final List<BitSetFX> intents =
            new ArrayList<BitSetFX>(Lists.transform(lattice.rowHeads(), new Function<Concept<G, M>, BitSetFX>() {

          public final BitSetFX apply(final Concept<G, M> concept) {
            return lattice.context.colHeads().subBitSet(concept.intent());
          }
        }));
        updateProgress(0.2d, 1d);
        final Iterator<BitSetFX> intentIterator = intents.iterator();
        final List<BitSetFX> borderIntents = new ArrayList<BitSetFX>(intents.size());
        final Map<BitSetFX, BitSetFX> faceAccumulation = new HashMap<BitSetFX, BitSetFX>(intents.size(), 1f);
        for (BitSetFX intent : intents)
          faceAccumulation.put(intent, new BitSetFX(bits));
        if (intentIterator.hasNext())
          borderIntents.add(intentIterator.next());
        final double total = intents.size();
        double actual = 0d;
        while (intentIterator.hasNext()) {
          actual++;
          updateProgress(0.3d + 0.7d * (actual / total), 1d);
          updateMessage("Computing Neighborhood: " + (int) actual + " of " + (int) total + " Concepts...");
          final BitSetFX intent = intentIterator.next();
          final List<BitSetFX> candidateIntents = new ArrayList<BitSetFX>(borderIntents.size());
          for (BitSetFX borderIntent : borderIntents) {
            final BitSetFX candidateIntent = ((BitSetFX) intent.clone());
            candidateIntent.and(borderIntent);
            candidateIntents.add(candidateIntent);
          }
          for (BitSetFX candidateIntent : candidateIntents) {
            final BitSetFX candidateFace = faceAccumulation.get(candidateIntent);
            final BitSetFX intentFace = ((BitSetFX) intent.clone());
            try {
              intentFace.and(candidateFace);
            } catch (Exception e) {
              System.err.println("intentFace: " + intentFace);
              System.err.println("candidateFace: " + candidateFace);
              e.printStackTrace();
            }
            if (intentFace.isEmpty()) {
              lattice._add((int) actual, intents.indexOf(candidateIntent));
              final BitSetFX face = ((BitSetFX) intent.clone());
              face.andNot(candidateIntent);
              candidateFace.or(face);
              borderIntents.remove(candidateIntent);
            }
          }
          borderIntents.add(intent);
        }
//        updateMessage(
//            "Pushing Changes...");
//        lattice.pushAllChangedEvent();
        updateProgress(1d, 1d);
        return null;
      }
    };
  }

  public static final <G, M> ConceptLattice<G, M>
      getConceptLattice(final MatrixContext<G, M> cxt, final Set<Concept<G, M>> concepts) {
    final ConceptLattice<G, M> lattice = new ConceptLattice<G, M>(cxt);
    lattice.rowHeads().addAll(
        concepts
            .parallelStream()
            .sorted((c1, c2) -> (int) Math.signum(c1.extent().size() - c2.extent().size()))
            .collect(Collectors.toList()));
    populateConceptLattice(lattice, __ -> {} , __ -> {} , () -> false);
    return lattice;
  }

  public static final <G, M> void populateConceptLattice(
      final ConceptLattice<G, M> lattice,
      final Consumer<String> messageConsumer,
      final Consumer<Double> statusConsumer,
      final Supplier<Boolean> isCancelled) {
    statusConsumer.accept(0d);
    if (isCancelled.get())
      return;
    messageConsumer.accept("Computing Concept Neighborhood...");
    lattice.empty();
    final int bits = lattice.context.colHeads().size();
    final List<BitSetFX> intents = lattice
        .rowHeads()
        .parallelStream()
        .map(concept -> lattice.context.colHeads().subBitSet(concept.intent()))
        .collect(Collectors.toList());
    statusConsumer.accept(0.2d);
    final Iterator<BitSetFX> intentIterator = intents.iterator();
    final List<BitSetFX> borderIntents = new ArrayList<BitSetFX>(intents.size());
    final Map<BitSetFX, BitSetFX> faceAccumulation =
        intents.parallelStream().collect(Collectors.toMap(intent -> intent, intent -> new BitSetFX(bits)));
    if (intentIterator.hasNext())
      borderIntents.add(intentIterator.next());
    final double total = intents.size();
    final AtomicDouble actual = new AtomicDouble(0d);
    while (intentIterator.hasNext()) {
      if (isCancelled.get())
        break;
      actual.set(actual.get() + 1d);
      statusConsumer.accept(0.3d + 0.7d * (actual.get() / total));
      messageConsumer.accept("Computing Neighborhood: " + (int) actual.get() + " of " + (int) total + " Concepts...");
      final BitSetFX intent = intentIterator.next();
      Set<BitSetFX> toBeRemoved = Collections3.newConcurrentHashSet();
      borderIntents.parallelStream().map(borderIntent -> {
        final BitSetFX candidateIntent = ((BitSetFX) intent.clone());
        candidateIntent.and(borderIntent);
        return candidateIntent;
      }).forEach(candidateIntent -> {
        final BitSetFX candidateFace = faceAccumulation.get(candidateIntent);
        final BitSetFX intentFace = ((BitSetFX) intent.clone());
        intentFace.and(candidateFace);
        if (intentFace.isEmpty()) {
          final int index = intents.indexOf(candidateIntent);
          synchronized (lattice) {
            lattice._add((int) actual.get(), index);
          }
          final BitSetFX face = ((BitSetFX) intent.clone());
          face.andNot(candidateIntent);
          candidateFace.or(face);
          toBeRemoved.add(candidateIntent);
        }
      });
      borderIntents.removeAll(toBeRemoved);
      borderIntents.add(intent);
    }
//    messageConsumer.accept(
//        "Pushing Changes...");
//    lattice.pushAllChangedEvent();
    statusConsumer.accept(1d);
  }

  public static final <G, M> TimeTask<Void> neighborhoodP(final FCADataset<G, M> dataset) {
    return new TimeTask<Void>(dataset, "iPred (parallel)") {

      protected final Void call() {
        populateConceptLattice(dataset.lattice, m -> updateMessage(m), p -> updateProgress(p, 1d), this::isCancelled);
        return null;
//        updateProgress(0d, 1d);
//        if (isCancelled())
//          return null;
//        updateMessage("Computing Concept Neighborhood...");
//        dataset.lattice.empty();
//        final int bits = dataset.lattice.context.colHeads().size();
//        final List<BitSet> intents = dataset.lattice
//            .rowHeads()
//            .parallelStream()
//            .map(concept -> dataset.lattice.context.colHeads().subBitSet(concept.intent()))
//            .collect(Collectors.toList());
//        updateProgress(0.2d, 1d);
//        final Iterator<BitSet> intentIterator = intents.iterator();
//        final List<BitSet> borderIntents = new ArrayList<BitSet>(intents.size());
//        final Map<BitSet, BitSet> faceAccumulation =
//            intents.parallelStream().collect(Collectors.toMap(intent -> intent, intent -> new BitSet(bits)));
//        if (intentIterator.hasNext())
//          borderIntents.add(intentIterator.next());
//        final double total = intents.size();
//        final AtomicDouble actual = new AtomicDouble(0d);
//        while (intentIterator.hasNext()) {
//          if (this.isCancelled())
//            break;
//          actual.set(actual.get() + 1d);
//          updateProgress(0.3d + 0.7d * (actual.get() / total), 1d);
//          updateMessage("Computing Neighborhood: " + (int) actual.get() + " of " + (int) total + " Concepts...");
//          final BitSet intent = intentIterator.next();
//          Set<BitSet> toBeRemoved = Collections3.newConcurrentHashSet();
//          borderIntents.parallelStream().map(borderIntent -> {
//            final BitSet candidateIntent = ((BitSet) intent.clone());
//            candidateIntent.and(borderIntent);
//            return candidateIntent;
//          }).forEach(candidateIntent -> {
//            final BitSet candidateFace = faceAccumulation.get(candidateIntent);
//            final BitSet intentFace = ((BitSet) intent.clone());
//            intentFace.and(candidateFace);
//            if (intentFace.isEmpty()) {
//              final int index = intents.indexOf(candidateIntent);
//              synchronized (dataset.lattice) {
//                dataset.lattice._add((int) actual.get(), index);
//              }
//              final BitSet face = ((BitSet) intent.clone());
//              face.andNot(candidateIntent);
//              candidateFace.or(face);
//              toBeRemoved.add(candidateIntent);
//            }
//          });
//          borderIntents.removeAll(toBeRemoved);
//          borderIntents.add(intent);
//        }
////        updateMessage(
////            "Pushing Changes...");
////        lattice.pushAllChangedEvent();
//        updateProgress(1d, 1d);
//        return null;
      }
    };
  }
}
