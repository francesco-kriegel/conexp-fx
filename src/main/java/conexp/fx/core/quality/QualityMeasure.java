package conexp.fx.core.quality;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javafx.geometry.Point3D;

import com.google.common.base.Function;

import conexp.fx.core.layout.ConceptLayout;

public abstract class QualityMeasure<G, M, V> implements Function<ConceptLayout<G, M>, V>
{

  private final Map<Map<M, Point3D>, V> cache = new ConcurrentHashMap<Map<M, Point3D>, V>();

  public final V apply(final ConceptLayout<G, M> layout)
  {
    V v = cache.get(layout._seeds);
    if (v == null) {
      v = compute(layout);
      cache.put(layout._seeds, v);
    }
    return v;
  }

  protected abstract V compute(ConceptLayout<G, M> layout);
}
