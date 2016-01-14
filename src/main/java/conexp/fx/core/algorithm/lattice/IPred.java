/**
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.algorithm.lattice;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;

import conexp.fx.core.collections.Collections3;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
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
        final List<BitSet> intents =
            new ArrayList<BitSet>(Lists.transform(lattice.rowHeads(), new Function<Concept<G, M>, BitSet>() {

          public final BitSet apply(final Concept<G, M> concept) {
            return lattice.context.colHeads().subBitSet(concept.intent());
          }
        }));
        updateProgress(0.2d, 1d);
        final Iterator<BitSet> intentIterator = intents.iterator();
        final List<BitSet> borderIntents = new ArrayList<BitSet>(intents.size());
        final Map<BitSet, BitSet> faceAccumulation = new HashMap<BitSet, BitSet>(intents.size(), 1f);
        for (BitSet intent : intents)
          faceAccumulation.put(intent, new BitSet(bits));
        if (intentIterator.hasNext())
          borderIntents.add(intentIterator.next());
        final double total = intents.size();
        double actual = 0d;
        while (intentIterator.hasNext()) {
          actual++;
          updateProgress(0.3d + 0.7d * (actual / total), 1d);
          updateMessage("Computing Neighborhood: " + (int) actual + " of " + (int) total + " Concepts...");
          final BitSet intent = intentIterator.next();
          final List<BitSet> candidateIntents = new ArrayList<BitSet>(borderIntents.size());
          for (BitSet borderIntent : borderIntents) {
            final BitSet candidateIntent = ((BitSet) intent.clone());
            candidateIntent.and(borderIntent);
            candidateIntents.add(candidateIntent);
          }
          for (BitSet candidateIntent : candidateIntents) {
            final BitSet candidateFace = faceAccumulation.get(candidateIntent);
            final BitSet intentFace = ((BitSet) intent.clone());
            try {
              intentFace.and(candidateFace);
            } catch (Exception e) {
              System.err.println("intentFace: " + intentFace);
              System.err.println("candidateFace: " + candidateFace);
              e.printStackTrace();
            }
            if (intentFace.isEmpty()) {
              lattice._add((int) actual, intents.indexOf(candidateIntent));
              final BitSet face = ((BitSet) intent.clone());
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

  public static final <G, M> TimeTask<Void> neighborhoodP(final FCADataset<G, M> dataset) {
    return new TimeTask<Void>(dataset, "iPred (parallel)") {

      protected final Void call() {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        updateMessage("Computing Concept Neighborhood...");
        dataset.lattice.empty();
        final int bits = dataset.lattice.context.colHeads().size();
        final List<BitSet> intents = dataset.lattice
            .rowHeads()
            .parallelStream()
            .map(concept -> dataset.lattice.context.colHeads().subBitSet(concept.intent()))
            .collect(Collectors.toList());
        updateProgress(0.2d, 1d);
        final Iterator<BitSet> intentIterator = intents.iterator();
        final List<BitSet> borderIntents = new ArrayList<BitSet>(intents.size());
        final Map<BitSet, BitSet> faceAccumulation =
            intents.parallelStream().collect(Collectors.toMap(intent -> intent, intent -> new BitSet(bits)));
        if (intentIterator.hasNext())
          borderIntents.add(intentIterator.next());
        final double total = intents.size();
        final AtomicDouble actual = new AtomicDouble(0d);
        while (intentIterator.hasNext()) {
          if (this.isCancelled())
            break;
          actual.set(actual.get() + 1d);
          updateProgress(0.3d + 0.7d * (actual.get() / total), 1d);
          updateMessage("Computing Neighborhood: " + (int) actual.get() + " of " + (int) total + " Concepts...");
          final BitSet intent = intentIterator.next();
          Set<BitSet> toBeRemoved = Collections3.newConcurrentHashSet();
          borderIntents.parallelStream().map(borderIntent -> {
            final BitSet candidateIntent = ((BitSet) intent.clone());
            candidateIntent.and(borderIntent);
            return candidateIntent;
          }).forEach(candidateIntent -> {
            final BitSet candidateFace = faceAccumulation.get(candidateIntent);
            final BitSet intentFace = ((BitSet) intent.clone());
            intentFace.and(candidateFace);
            if (intentFace.isEmpty()) {
              final int index = intents.indexOf(candidateIntent);
              synchronized (dataset.lattice) {
                dataset.lattice._add((int) actual.get(), index);
              }
              final BitSet face = ((BitSet) intent.clone());
              face.andNot(candidateIntent);
              candidateFace.or(face);
              toBeRemoved.add(candidateIntent);
            }
          });
          borderIntents.removeAll(toBeRemoved);
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
}
