package conexp.fx.gui.properties;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
