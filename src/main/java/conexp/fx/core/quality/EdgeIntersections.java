package conexp.fx.core.quality;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
