package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import javafx.animation.Transition;
import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public final class NumberPropertyTransition extends Transition {

  private final Property<Number> property;
  private final Number           source;
  private final Number           target;

  public NumberPropertyTransition(final Duration duration, final Property<Number> property, final Number target) {
    super();
    this.setCycleDuration(duration);
    this.property = property;
    this.source = property.getValue();
    this.target = target;
  }

  public NumberPropertyTransition(
      final Duration duration,
      final Property<Number> property,
      final Number target,
      final EventHandler<ActionEvent> onFinished) {
    this(duration, property, target);
    this.setOnFinished(onFinished);
  }

  protected final void interpolate(final double frac) {
    property.setValue(source.doubleValue() + frac * (target.doubleValue() - source.doubleValue()));
  }
}
