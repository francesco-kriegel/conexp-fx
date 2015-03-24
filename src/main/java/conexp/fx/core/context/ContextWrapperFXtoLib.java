package conexp.fx.core.context;

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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import de.tudresden.inf.tcs.fcaapi.Expert;
import de.tudresden.inf.tcs.fcaapi.FCAImplication;
import de.tudresden.inf.tcs.fcaapi.FCAObject;
import de.tudresden.inf.tcs.fcaapi.exception.IllegalAttributeException;
import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcaapi.utils.IndexedSet;
import de.tudresden.inf.tcs.fcalib.AbstractContext;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.FullObjectDescription;

public final class ContextWrapperFXtoLib<G, M> extends AbstractContext<M, G, FCAObject<M, G>> {

  private final MatrixContext<G, M>     cxt;
  private Expert<M, G, FCAObject<M, G>> expert;

  public ContextWrapperFXtoLib(final MatrixContext<G, M> cxt) {
    super();
    this.cxt = cxt;
    this.getAttributes().addAll(cxt.colHeads());
  }

  @Override
  public FCAObject<M, G> getObject(G id) {
    return new FullObject<M, G>(id, cxt.row(id));
  }

  @Override
  public FCAObject<M, G> getObjectAtIndex(int index) {
    return getObject(cxt.rowHeads().get(index));
  }

  @Override
  public boolean objectHasAttribute(FCAObject<M, G> object, M attribute) {
    return cxt.contains(object.getIdentifier(), attribute);
  }

  @Override
  public Set<FCAImplication<M>> getDuquenneGuiguesBase() {
    return null;
  }

  @Override
  public Expert<M, G, FCAObject<M, G>> getExpert() {
    return expert;
  }

  @Override
  public void setExpert(Expert<M, G, FCAObject<M, G>> e) {
    expert = e;
  }

  @Override
  public IndexedSet<FCAObject<M, G>> getObjects() {
    return new IndexedSet<FCAObject<M, G>>() {

      @Override
      public int size() {
        return cxt.rowHeads().size();
      }

      @Override
      public boolean isEmpty() {
        return cxt.rowHeads().isEmpty();
      }

      @Override
      public boolean contains(Object o) {
        if (o instanceof FCAObject)
          return cxt.rowHeads().contains(((FCAObject) o).getIdentifier());
        return cxt.rowHeads().contains(o);
      }

      @Override
      public Iterator<FCAObject<M, G>> iterator() {
        return Iterators.transform(cxt.rowHeads().iterator(), new Function<G, FCAObject<M, G>>() {

          @Override
          public FCAObject<M, G> apply(G object) {
            return getObject(object);
          }
        });
      }

      @Override
      public Object[] toArray() {
        return cxt.rowHeads().toArray();
      }

      @Override
      public <T> T[] toArray(T[] a) {
        return cxt.rowHeads().toArray(a);
      }

      @Override
      public boolean add(FCAObject<M, G> e) {
        return cxt.rowHeads().add(e.getIdentifier())
            && cxt.row(e.getIdentifier()).addAll(((FullObjectDescription<M>) e.getDescription()).getAttributes());
      }

      @Override
      public boolean remove(Object o) {
        if (o instanceof FCAObject)
          return cxt.rowHeads().remove(((FCAObject) o).getIdentifier());
        return cxt.rowHeads().remove(o);
      }

      @Override
      public boolean containsAll(Collection<?> c) {
        boolean b = false;
        for (Object o : c)
          b |= contains(o);
        return b;
      }

      @Override
      public boolean addAll(Collection<? extends FCAObject<M, G>> c) {
        boolean b = false;
        for (FCAObject<M, G> o : c)
          b |= add(o);
        return b;
      }

      @Override
      public boolean retainAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public boolean removeAll(Collection<?> c) {
        boolean b = false;
        for (Object o : c)
          b |= remove(o);
        return b;
      }

      @Override
      public void clear() {
        cxt.rowHeads().clear();
      }

      @Override
      public int getIndexOf(FCAObject<M, G> e) {
        return cxt.rowHeads().indexOf(e.getIdentifier());
      }

      @Override
      public FCAObject<M, G> getElementAt(int index) throws IndexOutOfBoundsException {
        return getObjectAtIndex(index);
      }

      @Override
      public void changeOrder() {}
    };
  }

  @Override
  public boolean addObject(FCAObject<M, G> e) throws IllegalObjectException {
    return cxt.rowHeads().add(e.getIdentifier())
        && cxt.row(e.getIdentifier()).addAll(((FullObjectDescription<M>) e.getDescription()).getAttributes());
  }

  @Override
  public boolean removeObject(G id) throws IllegalObjectException {
    return cxt.rowHeads().remove(id);
  }

  @Override
  public boolean removeObject(FCAObject<M, G> object) throws IllegalObjectException {
    return cxt.rowHeads().remove(object.getIdentifier());
  }

  @Override
  public boolean addAttributeToObject(M attribute, G id) throws IllegalAttributeException, IllegalObjectException {
    return cxt.add(id, attribute);
  }

  @Override
  public Set<M> doublePrime(Set<M> x) {
    return cxt.intent(x);
  }

  @Override
  public Set<FCAImplication<M>> getStemBase() {
    return null;
  }

  @Override
  public boolean refutes(FCAImplication<M> imp) {
    return false;
  }

  @Override
  public boolean isCounterExampleValid(FCAObject<M, G> counterExample, FCAImplication<M> imp) {
    return false;
  }

  @Override
  protected boolean followsFromBackgroundKnowledge(FCAImplication<M> implication) {
    return false;
  }

}
