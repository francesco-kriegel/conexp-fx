package conexp.fx.gui.properties;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import conexp.fx.core.collections.IntPair;
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
