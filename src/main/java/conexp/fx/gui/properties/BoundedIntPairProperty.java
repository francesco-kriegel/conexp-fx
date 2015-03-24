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
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class BoundedIntPairProperty extends SimpleIntPairProperty {

  private final ObjectBinding<IntPair> lowerBound;
  private final ObjectBinding<IntPair> upperBound;

  public BoundedIntPairProperty(
      IntPair initialValue,
      ObjectBinding<IntPair> lowerBound,
      ObjectBinding<IntPair> upperBound) throws RuntimeException {
    super(initialValue);
    if (lowerBound.get().x() > upperBound.get().x() || lowerBound.get().y() > upperBound.get().y()
        || initialValue.x() < lowerBound.get().x() || initialValue.x() > upperBound.get().x()
        || initialValue.y() < lowerBound.get().y() || initialValue.y() > upperBound.get().y())
      throw new RuntimeException("lower bound is not smaller than upper bound, or initial value is not between them!");
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    lowerBound.addListener(new ChangeListener<IntPair>() {

      @Override
      public void changed(ObservableValue<? extends IntPair> observable, IntPair oldValue, IntPair newValue) {
        if (getValue().x() < newValue.x() || getValue().y() < newValue.y())
          setValue(truncate(getValue()));
      }
    });
    upperBound.addListener(new ChangeListener<IntPair>() {

      @Override
      public void changed(ObservableValue<? extends IntPair> observable, IntPair oldValue, IntPair newValue) {
        if (getValue().x() > newValue.x() || getValue().y() > newValue.y())
          setValue(truncate(getValue()));
      }
    });
  }

  private final IntPair truncate(final IntPair value) {
    value.setX(Math.min(Math.max(value.x(), lowerBound.get().x()), upperBound.get().x()));
    value.setY(Math.min(Math.max(value.y(), lowerBound.get().y()), upperBound.get().y()));
    return value;
  }

  @Override
  public void set(int x, int y) {
    set(truncate(IntPair.valueOf(x, y)));
  }

  @Override
  public void set(IntPair v) {
    super.set(truncate(v));
  }

  @Override
  public void setValue(IntPair v) {
    set(truncate(v));
  }
}
