package conexp.fx.gui.notification;

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

import java.awt.Window.Type;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;

import conexp.fx.gui.util.CoordinateUtil;
import javafx.animation.FadeTransitionBuilder;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransitionBuilder;
import javafx.animation.ScaleTransitionBuilder;
import javafx.animation.Transition;
import javafx.animation.TranslateTransitionBuilder;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.util.Duration;

public class Notification {

  public enum TransitionType {
    TRANSLATE,
    FADE;
  }

  private static final Duration DEFAULT_DURATION = Duration.seconds(10);

  protected final JDialog       dialog           = new JDialog();
  protected final JFXPanel      panel            = new JFXPanel();
  private final Timer           timer            = new Timer();
  private boolean               isDisposed       = false;
  private Transition            showTransition;
  private Transition            hideTransition;

  public Notification(
      final Pane contentPane,
      final Pos position,
      final int xShift,
      final int yShift,
      final Notification.TransitionType type) {
    this(contentPane, position, xShift, yShift, type, DEFAULT_DURATION);
  }

  public Notification(
      final Pane contentPane,
      final Pos position,
      final int xShift,
      final int yShift,
      final Notification.TransitionType type,
      final Duration duration) {
    this();
    setContentPane(contentPane, position, xShift, yShift, type, duration);
  }

  protected Notification() {
    super();
  }

  public void show() {
    dialog.setVisible(true);
    showTransition.play();
  }

  public void hideAndDispose() {
    if (!isDisposed) {
      timer.cancel();
      isDisposed = true;
      hideTransition.play();
    }
  }

  protected final void setContentPane(
      final Pane contentPane,
      final Pos position,
      final int xShift,
      final int yShift,
      final Notification.TransitionType type) {
    setContentPane(contentPane, position, xShift, yShift, type, DEFAULT_DURATION);
  }

  protected final void setContentPane(
      final Pane contentPane,
      final Pos position,
      final int xShift,
      final int yShift,
      final Notification.TransitionType type,
      final Duration duration) {
    final int width = (int) contentPane.getMaxWidth();
    final int height = (int) contentPane.getMaxHeight();
    final Scene scene = new Scene(
        StackPaneBuilder.create().alignment(CoordinateUtil.contraryPosition(position)).children(contentPane).build(),
        Color.TRANSPARENT);
    panel.setScene(scene);
    panel.setSize(width + xShift, height + yShift);
    dialog.add(panel);
    dialog.setType(Type.POPUP);
    dialog.setUndecorated(true);
    dialog.setAlwaysOnTop(true);
    dialog.setResizable(false);
    dialog.setBackground(new java.awt.Color(0, 0, 0, 0));
    dialog.setLocation(getDialogX(position, width, xShift), getDialogY(position, height, yShift));
    dialog.setSize(width + xShift, height + yShift);
    createTransitions(contentPane, position, xShift, yShift, type);
    if (!duration.isIndefinite() && !duration.isUnknown()) {
      timer.schedule(new TimerTask() {

        @Override
        public void run() {
          hideAndDispose();
        }
      }, (long) duration.toMillis());
    }
  }

  private final void createTransitions(
      final Pane contentPane,
      final Pos position,
      final int xShift,
      final int yShift,
      final Notification.TransitionType type) {
    switch (type) {
    case FADE:
      createFadeTransition(contentPane);
      break;
    case TRANSLATE:
      createTranslateTransition(contentPane, position, xShift, yShift);
      break;
    }
  }

  private final void createFadeTransition(final Pane contentPane) {
    contentPane.setOpacity(0);
    showTransition = ParallelTransitionBuilder
        .create()
        .children(
            FadeTransitionBuilder
                .create()
                .node(contentPane)
                .fromValue(0)
                .toValue(1)
                .interpolator(Interpolator.EASE_OUT)
                .duration(Duration.millis(700))
                .build(),
            ScaleTransitionBuilder
                .create()
                .node(contentPane)
                .fromX(0)
                .fromY(0)
                .toX(1)
                .toY(1)
                .interpolator(Interpolator.EASE_OUT)
                .duration(Duration.millis(700))
                .build())
        .build();
    hideTransition = ParallelTransitionBuilder
        .create()
        .children(
            FadeTransitionBuilder
                .create()
                .node(contentPane)
                .fromValue(1)
                .toValue(0)
                .interpolator(Interpolator.EASE_IN)
                .duration(Duration.millis(700))
                .build(),
            ScaleTransitionBuilder
                .create()
                .node(contentPane)
                .fromX(1)
                .fromY(1)
                .toX(0)
                .toY(0)
                .interpolator(Interpolator.EASE_IN)
                .duration(Duration.millis(700))
                .build())
        .onFinished(new EventHandler<ActionEvent>() {

          @Override
          public void handle(ActionEvent event) {
            dialog.setVisible(false);
            dialog.dispose();
          }
        })
        .build();
  }

  private final void
      createTranslateTransition(final Pane contentPane, final Pos position, final int xShift, final int yShift) {
    int translateX = 0;
    switch (position) {
    case BOTTOM_RIGHT:
    case CENTER_RIGHT:
    case TOP_RIGHT:
      translateX = (int) contentPane.getMaxWidth() + xShift;
      break;
    case BOTTOM_LEFT:
    case CENTER_LEFT:
    case TOP_LEFT:
      translateX = (int) -(contentPane.getMaxWidth() + xShift);
      break;
    default:
    }
    int translateY = 0;
    switch (position) {
    case BOTTOM_RIGHT:
    case BOTTOM_CENTER:
    case BOTTOM_LEFT:
      translateY = (int) contentPane.getMaxHeight() + yShift;
      break;
    case TOP_RIGHT:
    case TOP_CENTER:
    case TOP_LEFT:
      translateY = (int) -(contentPane.getMaxHeight() + yShift);
      break;
    default:
    }
    contentPane.setTranslateX(translateX);
    contentPane.setTranslateY(translateY);
    showTransition = TranslateTransitionBuilder
        .create()
        .node(contentPane)
        .byX(-translateX)
        .byY(-translateY)
        .interpolator(Interpolator.EASE_OUT)
        .duration(Duration.millis(700))
        .build();
    hideTransition = TranslateTransitionBuilder
        .create()
        .node(contentPane)
        .byX(translateX)
        .byY(translateY)
        .interpolator(Interpolator.EASE_IN)
        .duration(Duration.millis(700))
        .onFinished(new EventHandler<ActionEvent>() {

          @Override
          public void handle(ActionEvent event) {
            dialog.setVisible(false);
            dialog.dispose();
          }
        })
        .build();
  }

  private final int getDialogX(final Pos position, final int width, final int shift) {
    switch (position) {
    case BOTTOM_RIGHT:
    case CENTER_RIGHT:
    case TOP_RIGHT:
      return (int) Screen.getPrimary().getBounds().getWidth() - width - shift;
    case BOTTOM_CENTER:
    case CENTER:
    case TOP_CENTER:
      return (int) (Screen.getPrimary().getBounds().getWidth() - width) / 2;
    case BOTTOM_LEFT:
    case CENTER_LEFT:
    case TOP_LEFT:
    default:
      return 0;
    }
  }

  private final int getDialogY(final Pos position, final int height, final int shift) {
    switch (position) {
    case BOTTOM_LEFT:
    case BOTTOM_CENTER:
    case BOTTOM_RIGHT:
      return (int) Screen.getPrimary().getBounds().getHeight() - height - shift;
    case CENTER_LEFT:
    case CENTER:
    case CENTER_RIGHT:
      return (int) (Screen.getPrimary().getBounds().getHeight() - height) / 2;
    case TOP_LEFT:
    case TOP_CENTER:
    case TOP_RIGHT:
    default:
      return 0;
    }
  }

}
