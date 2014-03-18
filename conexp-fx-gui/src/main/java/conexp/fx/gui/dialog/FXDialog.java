package conexp.fx.gui.dialog;

/*
 * #%L
 * Concept Explorer FX - Graphical User Interface
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
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

  public enum Result {

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

  private final int        width;
  private final Stage      stage        = StageBuilder.create().build();
  private final BorderPane pane         = BorderPaneBuilder.create().build();
  private final Text       text;
  private final Style      style;
  private Result           result       = Result.UNKNOWN;
  protected T              value        = null;
  private StackPane        topPane;
  private StackPane        bottomPane;
  private Rectangle        topBackground;
  private Rectangle        bottomBackground;
  private final Button     okButton     = ButtonBuilder
                                            .create()
                                            .text("OK")
                                            .minHeight(20)
                                            .minWidth(100)
                                            .effect(new DropShadow())
                                            .onAction(new EventHandler<ActionEvent>() {

                                              @Override
                                              public void handle(ActionEvent event) {
                                                result = Result.OK;
                                                stage.close();
                                              }
                                            })
                                            .build();
  private final Button     cancelButton = ButtonBuilder
                                            .create()
                                            .text("Cancel")
                                            .minHeight(20)
                                            .minWidth(100)
                                            .effect(new DropShadow())
                                            .onAction(new EventHandler<ActionEvent>() {

                                              @Override
                                              public void handle(ActionEvent event) {
                                                result = Result.CANCEL;
                                                stage.close();
                                              }
                                            })
                                            .build();
  private final Button     yesButton    = ButtonBuilder
                                            .create()
                                            .text("Yes")
                                            .minHeight(20)
                                            .minWidth(100)
                                            .effect(new DropShadow())
                                            .onAction(new EventHandler<ActionEvent>() {

                                              @Override
                                              public void handle(ActionEvent event) {
                                                result = Result.YES;
                                                stage.close();
                                              }
                                            })
                                            .build();
  private final Button     noButton     = ButtonBuilder
                                            .create()
                                            .text("No")
                                            .minHeight(20)
                                            .minWidth(100)
                                            .effect(new DropShadow())
                                            .onAction(new EventHandler<ActionEvent>() {

                                              @Override
                                              public void handle(ActionEvent event) {
                                                result = Result.NO;
                                                stage.close();
                                              }
                                            })
                                            .build();
  protected final Node     optionalCenterNode;

  public FXDialog(
      final Stage primaryStage,
      final Style style,
      final String title,
      final String message,
      final Node optionalCenterNode) {
    this(primaryStage, style, title, message, optionalCenterNode, 500);
  }

  public FXDialog(
      final Stage primaryStage,
      final Style style,
      final String title,
      final String message,
      final Node optionalCenterNode,
      final int width) {
    super();
    this.width = width;
    this.style = style;
    this.text =
        TextBuilder
            .create()
            .effect(
                DropShadowBuilder
                    .create()
                    .radius(1)
                    .blurType(BlurType.GAUSSIAN)
                    .color(Color.LIGHTGREY)
                    .spread(1)
                    .build())
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
    this.optionalCenterNode = optionalCenterNode;
    createTop();
    if (optionalCenterNode != null)
      pane.setCenter(optionalCenterNode);
    createBottom();
  }

  public final static class Return<T> {

    private final Result result;
    private final T      value;

    private Return(final Result result, final T value) {
      super();
      this.result = result;
      this.value = value;
    }

    public final Result result() {
      return result;
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
      }

      @Override
      protected double computeValue() {
        return topBackground.heightProperty().get() + bottomBackground.heightProperty().get();
      }
    };
    pane.minHeightProperty().bind(height);
    pane.maxHeightProperty().bind(height);
    stage.minHeightProperty().bind(pane.heightProperty());
    stage.maxHeightProperty().bind(pane.heightProperty());
  }

  private final void createTop() {
    topBackground = RectangleBuilder.create().fill(Color.WHITE).width(width).build();
    topPane =
        StackPaneBuilder
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
    bottomBackground =
        RectangleBuilder
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
      break;
    }
    bottomPane =
        StackPaneBuilder
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
