/**
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.math;

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


public interface PartialComparable<E> extends Comparable<E> {

  public boolean smaller(E e);

  public boolean greater(E e);

  public boolean smallerEq(E e);

  public boolean greaterEq(E e);

  public boolean uncomparable(E e);
}
