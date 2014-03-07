package conexp.fx.core.quality;

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


import java.util.Map.Entry;

import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.context.Concept;
import conexp.fx.core.layout.ConceptLayout;
import conexp.fx.core.math.Points;
import javafx.geometry.Point3D;

public final class ConflictDistance<G, M>
  extends QualityMeasure<G, M, Pair<Concept<G, M>, Double>>
{
  public ConflictDistance()
  {
    super();
  }

  protected final Pair<Concept<G, M>, Double> compute(final ConceptLayout<G, M> layout)
  {
    double num = 0d;
    double minC = 1d;
    double sumC = 0d;
    double minL = Double.MAX_VALUE;
    double sumL = 0d;
    final Concept<G, M> bottom = layout.lattice.context.selection.bottomConcept();
    Concept<G, M> faulty = bottom;
    for (Entry<Concept<G, M>, Point3D> entry : layout.positions.entrySet()) {
      if (!entry.getKey().equals(bottom)) {
        final Point3D p = entry.getValue();
        final Concept<G, M> c = entry.getKey();
        for (Pair<Concept<G, M>, Concept<G, M>> edge : layout.lattice)
          if (!edge.x().equals(bottom) && !c.equals(edge.x()) && !c.equals(edge.y())) {
            final Point3D q1 = layout.positions.get(edge.x());
            final Point3D q2 = layout.positions.get(edge.y());
            final double length = q1.distance(q2);
            final double conflictDistance = Math.min(Points.pointSegmentDistance(p, q1, q2) / length, 0.5d) * 2d;
            num++;
            minL = Math.min(minL, length);
            sumL += length;
            sumC += conflictDistance;
            if (conflictDistance < minC) {
              minC = conflictDistance;
              faulty = c;
            }
          }
      }
    }
    final double avgC = num == 0d ? 1d : (sumC / num);
    final double avgL = num == 0d ? 1d : (sumL / num);
    return Pair.of(faulty, Math.pow(minC * Math.pow(minC / avgC, 0.5d) * Math.pow(minL / avgL, 0.25d), 0.125d));
  }
}
