/**
 * @author Francesco.Kriegel@gmx.de
 */
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
import java.util.Set;

import conexp.fx.core.collections.relation.Relation;

public interface Context<G, M> extends Relation<G, M> {

  public Set<G> extent(Collection<?> objects);

  public Set<M> intent(Collection<?> attributes);

  public Set<G> extent(Object... objects);

  public Set<M> intent(Object... attributes);

  public Relation<G, G> objectQuasiOrder();

  public Relation<M, M> attributeQuasiOrder();

  public MatrixContext<G, M> clone();
}
