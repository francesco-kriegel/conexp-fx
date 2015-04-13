package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public final class TransitionTimer
  extends Transition
{
  public TransitionTimer(final Duration duration, final EventHandler<ActionEvent> onExecution)
  {
    super();
    this.setCycleDuration(duration);
    this.setOnFinished(onExecution);
  }

  protected final void interpolate(final double frac)
  {}
}
