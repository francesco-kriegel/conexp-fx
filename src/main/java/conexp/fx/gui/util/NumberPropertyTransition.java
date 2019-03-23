package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2019 Francesco Kriegel
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
