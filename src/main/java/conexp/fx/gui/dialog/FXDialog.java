package conexp.fx.gui.dialog;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2023 Francesco Kriegel
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

import java.util.LinkedList;
import java.util.List;

import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.InsetsBuilder;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.DropShadowBuilder;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradientBuilder;
import javafx.scene.paint.StopBuilder;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.text.FontBuilder;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.StageStyle;

public class FXDialog<T> {

  public enum Answer {

    OK,
    CANCEL,
    YES,
    NO,
    UNKNOWN;

  }

  public enum Style {

    INFO,
    WARN,
    ERROR,
    QUESTION;

  }

  private final int            width;
  private final Stage          stage        = StageBuilder.create().build();
  protected final BorderPane   pane         = BorderPaneBuilder.create().build();
  private final Text           text;
  private final FXDialog.Style style;
  private Answer               result       = Answer.UNKNOWN;
  protected T                  value        = null;
  private StackPane            topPane;
  private StackPane            bottomPane;
  private Rectangle            topBackground;
  private Rectangle            bottomBackground;
  private final Button         okButton     = ButtonBuilder
      .create()
      .text("OK")
      .minHeight(20)
      .minWidth(100)
      .effect(new DropShadow())
      .onAction(new EventHandler<ActionEvent>() {

                                                  @Override
                                                  public void handle(ActionEvent event) {
                                                    result = Answer.OK;
                                                    stage.close();
                                                  }
                                                })
      .build();
  private final Button         cancelButton = ButtonBuilder
      .create()
      .text("Cancel")
      .minHeight(20)
      .minWidth(100)
      .effect(new DropShadow())
      .onAction(new EventHandler<ActionEvent>() {

                                                  @Override
                                                  public void handle(ActionEvent event) {
                                                    result = Answer.CANCEL;
                                                    stage.close();
                                                  }
                                                })
      .build();
  private final Button         yesButton    = ButtonBuilder
      .create()
      .text("Yes")
      .minHeight(20)
      .minWidth(100)
      .effect(new DropShadow())
      .onAction(new EventHandler<ActionEvent>() {

                                                  @Override
                                                  public void handle(ActionEvent event) {
                                                    result = Answer.YES;
                                                    stage.close();
                                                  }
                                                })
      .build();
  private final Button         noButton     = ButtonBuilder
      .create()
      .text("No")
      .minHeight(20)
      .minWidth(100)
      .effect(new DropShadow())
      .onAction(new EventHandler<ActionEvent>() {

                                                  @Override
                                                  public void handle(ActionEvent event) {
                                                    result = Answer.NO;
                                                    stage.close();
                                                  }
                                                })
      .build();

  public FXDialog(
      final Stage primaryStage,
      final FXDialog.Style style,
      final String title,
      final String message,
      final Node optionalCenterNode) {
    this(primaryStage, style, title, message, optionalCenterNode, 500);
  }

  public FXDialog(
      final Stage primaryStage,
      final FXDialog.Style style,
      final String title,
      final String message,
      final Node optionalCenterNode,
      final int width) {
    super();
    this.width = width;
    this.style = style;
    this.text = TextBuilder
        .create()
        .effect(
            DropShadowBuilder.create().radius(1).blurType(BlurType.GAUSSIAN).color(Color.LIGHTGREY).spread(1).build())
        .font(FontBuilder.create().size(16).build())
        .wrappingWidth(width - 50)
        .build();
    stage.initOwner(primaryStage);
    stage.initStyle(StageStyle.UTILITY);
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setTitle(title);
    stage.setResizable(false);
    stage.setScene(SceneBuilder.create().width(width).root(pane).build());
    text.setText(message);
    createTop();
    if (optionalCenterNode != null)
      pane.setCenter(optionalCenterNode);
    createBottom();
  }

  public final void setCenterNode(final Node centerNode) {
    pane.setCenter(centerNode);
  }

  public final static class Return<T> {

    private final Answer answer;
    private final T      value;

    private Return(final Answer answer, final T value) {
      super();
      this.answer = answer;
      this.value = value;
    }

    public final Answer result() {
      return answer;
    }

    public final T value() {
      return value;
    }
  }

  public final Return<T> showAndWait() {
    stage.showAndWait();
    bindHeight();
    return new Return<T>(result, value);
  }

  private final void bindHeight() {
    topBackground.heightProperty().bind(new DoubleBinding() {

      {
        super.bind(text.layoutBoundsProperty());
      }

      @Override
      protected double computeValue() {
        return text.getLayoutBounds().getHeight() + 20;
      }
    });
    final DoubleBinding height = new DoubleBinding() {

      {
        super.bind(topBackground.heightProperty(), bottomBackground.heightProperty());
        if (pane.getCenter() != null && pane.getCenter() instanceof Pane) {
          super.bind(((Pane) pane.getCenter()).minHeightProperty());
        }
      }

      @Override
      protected double computeValue() {
        return topBackground.heightProperty().get() + bottomBackground.heightProperty().get()
            + ((pane.getCenter() != null && pane.getCenter() instanceof Pane)
                ? ((Pane) pane.getCenter()).minHeightProperty().get() : 0d);
      }
    };
    pane.minHeightProperty().bind(height);
    pane.maxHeightProperty().bind(height);
    stage.minHeightProperty().bind(pane.heightProperty());
    stage.maxHeightProperty().bind(pane.heightProperty());
  }

  private final void createTop() {
    topBackground = RectangleBuilder.create().fill(Color.WHITE).width(width).build();
    topPane = StackPaneBuilder
        .create()
        .children(
            topBackground,
            VBoxBuilder
                .create()
                .alignment(Pos.TOP_LEFT)
                .spacing(10)
                .padding(InsetsBuilder.create().left(10).top(10).right(10).bottom(10).build())
                .children(text)
                .build())
        .build();
    if (text.getText().trim() != "")
      pane.setTop(topPane);
  }

  private final void createBottom() {
    bottomBackground = RectangleBuilder
        .create()
        .fill(
            LinearGradientBuilder
                .create()
                .startX(0)
                .startY(0)
                .endX(0)
                .endY(1)
                .cycleMethod(CycleMethod.NO_CYCLE)
                .proportional(true)
                .stops(
                    StopBuilder.create().color(Color.LIGHTGRAY).offset(1).build(),
                    StopBuilder.create().color(Color.WHITE).offset(0).build())
                .build())
        .height(50)
        .width(width)
        .build();
    final List<Button> buttons = new LinkedList<Button>();
    switch (style) {
    case INFO:
    case ERROR:
      buttons.add(okButton);
      break;
    case WARN:
      buttons.add(okButton);
      buttons.add(cancelButton);
      break;
    case QUESTION:
      buttons.add(yesButton);
      buttons.add(noButton);
      buttons.add(cancelButton);
      break;
    }
    bottomPane = StackPaneBuilder
        .create()
        .children(
            bottomBackground,
            HBoxBuilder
                .create()
                .alignment(Pos.CENTER_RIGHT)
                .spacing(10)
                .padding(InsetsBuilder.create().right(30).build())
                .children(buttons)
                .build())
        .build();
    pane.setBottom(bottomPane);
  }

}
