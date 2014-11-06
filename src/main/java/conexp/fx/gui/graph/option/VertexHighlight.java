package conexp.fx.gui.graph.option;

/*
 * #%L
 * Concept Explorer FX - Graphical User Interface
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


public enum VertexHighlight {
  NONE("None"),
  CONCEPT("Only Concept"),
  UPPER_NEIGHBORS("Upper Neighbors"),
  LOWER_NEIGHBORS("Lower Neighbors"),
  NEIGHBORS("Neighbors"),
  FILTER("Filter"),
  IDEAL("Ideal"),
  FILTER_IDEAL("Filter & Ideal");

  private final String name;

  private VertexHighlight(final String name) {
    this.name = name;
  }

  @Override
  public final String toString() {
    return name;
  }
}
