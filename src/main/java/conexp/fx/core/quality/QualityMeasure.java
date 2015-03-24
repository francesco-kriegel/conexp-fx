package conexp.fx.core.quality;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
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
