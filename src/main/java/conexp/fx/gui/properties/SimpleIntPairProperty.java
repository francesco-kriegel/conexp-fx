package conexp.fx.gui.properties;

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


import conexp.fx.core.collections.pair.IntPair;
import javafx.beans.property.SimpleObjectProperty;

public class SimpleIntPairProperty extends SimpleObjectProperty<IntPair> {

  public SimpleIntPairProperty(IntPair initialValue) {
    super(initialValue);
  }

  public SimpleIntPairProperty(final int x, final int y) {
    this(IntPair.valueOf(x, y));
  }

  public SimpleIntPairProperty() {
    this(IntPair.zero());
  }

  public void set(final int x, final int y) {
    super.set(IntPair.valueOf(x, y));
  }

//  public final void set(final IntPair coordinates) {
//    super.set(coordinates.clone());
//  }

  public SimpleIntPairProperty add(final int x, final int y) {
    set(super.get().plus(x, y));
    return this;
  }

  public SimpleIntPairProperty add(final IntPair coordinates) {
    set(super.get().plus(coordinates));
    return this;
  }

  public SimpleIntPairProperty subtract(final int x, final int y) {
    set(super.get().minus(x, y));
    return this;
  }

  public SimpleIntPairProperty subtract(final IntPair coordinates) {
    set(super.get().minus(coordinates));
    return this;
  }

}
