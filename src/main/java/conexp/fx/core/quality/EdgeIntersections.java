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


import conexp.fx.core.collections.ListIterators;
import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.context.Concept;
import conexp.fx.core.layout.ConceptLayout;
import conexp.fx.core.math.Points;

public final class EdgeIntersections<G, M> extends QualityMeasure<G, M, Integer>
{

  public EdgeIntersections()
  {
    super();
  }

  protected final Integer compute(final ConceptLayout<G, M> layout)
  {
    int intersections = 0;
    for (Pair<Pair<Concept<G, M>, Concept<G, M>>, Pair<Concept<G, M>, Concept<G, M>>> e : ListIterators
        .upperCartesianDiagonalStrict(layout.lattice))
      if (!e.first().x().equals(e.second().y()) && !e.first().y().equals(e.second().x()))
        if (Points.intersectingLineSegments(
            layout.positions.get(e.first().x()),
            layout.positions.get(e.first().y()),
            layout.positions.get(e.second().x()),
            layout.positions.get(e.second().y())))
          intersections++;
    return intersections;
  }
}
