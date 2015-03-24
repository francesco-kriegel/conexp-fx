package conexp.fx.gui.util;

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


import javafx.animation.Transition;
import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public final class NumberPropertyTransition
  extends Transition
{
  private final Property<Number> property;
  private final Number           source;
  private final Number           target;

  public NumberPropertyTransition(final Duration duration, final Property<Number> property, final Number target)
  {
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
      final EventHandler<ActionEvent> onFinished)
  {
    this(duration, property, target);
    this.setOnFinished(onFinished);
  }

  protected final void interpolate(final double frac)
  {
    property.setValue(source.doubleValue() + frac * (target.doubleValue() - source.doubleValue()));
  }
}
