package conexp.fx.core.dl;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.enums.ValueType;

import conexp.fx.core.collections.pair.Pair;

public class Matrix3D<G, M, W> {

  protected final Matrix        mat;
  private int                   xs = 0;
  private int                   ys = 0;
  private int                   zs = 0;
  private final Map<G, Integer> x1;
  private final Map<M, Integer> y1;
  private final Map<W, Integer> z1;
  private final Map<Integer, G> x2;
  private final Map<Integer, M> y2;
  private final Map<Integer, W> z2;

  public Matrix3D(final int x, final int y, final int z) {
    super();
    mat = MatrixFactory.zeros(ValueType.BOOLEAN, x, y, z);
    x1 = new HashMap<G, Integer>(x);
    y1 = new HashMap<M, Integer>(y);
    z1 = new HashMap<W, Integer>(z);
    x2 = new HashMap<Integer, G>(x);
    y2 = new HashMap<Integer, M>(y);
    z2 = new HashMap<Integer, W>(z);
  }

  public final void addG(final G g) {
    if (x1.keySet().contains(g))
      return;
    x1.put(g, xs);
    x2.put(xs, g);
    xs++;
  }

  public final void addM(final M m) {
    if (y1.keySet().contains(m))
      return;
    y1.put(m, ys);
    y2.put(ys, m);
    ys++;
  }

  public final void addW(final W w) {
    if (z1.keySet().contains(w))
      return;
    z1.put(w, zs);
    z2.put(zs, w);
    zs++;
  }

  public final Set<G> getGs() {
    return x1.keySet();
  }

  public final Set<M> getMs() {
    return y1.keySet();
  }

  public final Set<W> getWs() {
    return z1.keySet();
  }

  public final void add(final G g, final M m, final W w) throws NullPointerException {
    mat.setAsBoolean(true, x1.get(g), y1.get(m), z1.get(w));
  }

  public final boolean get(final G g, final M m, final W w) throws NullPointerException {
    return mat.getAsBoolean(x1.get(g), y1.get(m), z1.get(w));
  }

  public final Stream<G> row(final M m, final W w) {
    final int y = y1.get(m);
    final int z = z1.get(w);
    return x1.keySet().stream().filter(g -> mat.getAsBoolean(x1.get(g), y, z));
  }

  public final Stream<M> col(final G g, final W w) {
    final int x = x1.get(g);
    final int z = z1.get(w);
    return y1.keySet().stream().filter(m -> mat.getAsBoolean(x, y1.get(m), z));
  }

  public final Stream<W> cut(final G g, final M m) {
    final int x = x1.get(g);
    final int y = y1.get(m);
    return z1.keySet().stream().filter(w -> mat.getAsBoolean(x, y, z1.get(w)));
  }

  public final Stream<W> cut(final Stream<G> gs, final M m) {
    final Set<G> _gs = gs.collect(Collectors.toSet());
    final int y = y1.get(m);
    return z1.keySet().stream().filter(w -> _gs.stream().allMatch(g -> mat.getAsBoolean(x1.get(g), y, z1.get(w))));
  }

  public final Set<Pair<G, W>> getPairsGW(final M m) {
    final Set<Pair<G, W>> pairs = new HashSet<Pair<G, W>>();
    for (G g : getGs())
      for (W w : getWs())
        if (get(g, m, w))
          pairs.add(new Pair<G, W>(g, w));
    return pairs;
  }

}