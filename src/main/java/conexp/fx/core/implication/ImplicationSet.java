package conexp.fx.core.implication;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.tudresden.inf.tcs.fcaapi.ClosureOperator;
import de.tudresden.inf.tcs.fcalib.Implication;

public class ImplicationSet<M> extends HashSet<Implication<M>> implements ClosureOperator<M> {

  private static final long serialVersionUID = -3657067383501935730L;

  public ImplicationSet() {
    super();
  }

  public Set<M> closure(final Set<M> attributes) {
    final Set<M> closure = new HashSet<M>(attributes);
    final Map<Implication<M>, Integer> count = new HashMap<Implication<M>, Integer>();
    final Multimap<M, Implication<M>> list = HashMultimap.<M, Implication<M>> create();
    for (Implication<M> impl : this) {
      final int psize = impl.getPremise().size();
      count.put(impl, psize);
      if (psize == 0)
        closure.addAll(impl.getConclusion());
      else
        for (M im : impl.getPremise())
          list.put(im, impl);
    }
    List<M> update = new LinkedList<M>(attributes);
    while (!update.isEmpty()) {
      final M m = update.remove(0);
      for (Implication<M> impl : list.get(m)) {
        final int newCount = count.get(impl) - 1;
        count.put(impl, newCount);
        if (newCount == 0) {
          final Set<M> add = new HashSet<M>(Sets.difference(impl.getConclusion(), closure));
          closure.addAll(add);
          update.addAll(add);
        }
      }
    }
    return closure;
  }

//  @Override
//  public Set<M> closure(Set<M> s) {
//    final Set<M> closure = new HashSet<M>(s);
//    boolean stable = false;
//    do {
//      stable = true;
//      final Iterator<Implication<M>> implIt = this.iterator();
//      while (implIt.hasNext()) {
//        final Implication<M> impl = implIt.next();
//        if (closure.containsAll(impl.getPremise())) {
//          closure.addAll(impl.getConclusion());
//          stable = false;
//          implIt.remove();
//        }
//      }
//    } while (!stable);
//    return closure;
//  }

  @Override
  public boolean isClosed(Set<M> s) {
    return closure(s).equals(s);
  }

  @Override
  public Set<Set<M>> allClosures() {
    throw new UnsupportedOperationException();
  }

}
