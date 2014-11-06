/**
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.algorithm.lattice;

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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.gui.task.BlockingTask;

public final class IPred<G, M> {

  public static final <G, M> BlockingTask neighborhood(final ConceptLattice<G, M> lattice) {
    return new BlockingTask("iPred") {

      protected final void _call() {
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
        updateMessage("Pushing Changes...");
        lattice.pushAllChangedEvent();
      }
    };
  }
}
