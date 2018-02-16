package conexp.fx.gui.assistent;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.InsetsBuilder;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.HBoxBuilder;
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

import com.google.common.base.Function;

import conexp.fx.gui.util.ColorScheme;

@SuppressWarnings("deprecation")
public abstract class Assistent<T> extends AssistentPage<T> {

  protected final int                              width                     = 500;
  protected final int                              height                    = 700;
  protected final Stage                            stage                     = StageBuilder.create().build();
  protected final BorderPane                       pane                      = BorderPaneBuilder.create().build();
  protected final Text                             title                     = TextBuilder
                                                                                 .create()
                                                                                 .font(
                                                                                     FontBuilder
                                                                                         .create()
                                                                                         .size(20)
                                                                                         .build())
                                                                                 .build();
  protected final Text                             text                      = TextBuilder
                                                                                 .create()
                                                                                 .font(
                                                                                     FontBuilder
                                                                                         .create()
                                                                                         .size(16)
                                                                                         .build())
                                                                                 .wrappingWidth(width - 50)
                                                                                 .build();
  protected final Map<String, AssistentPage<?>>    availablePages            =
                                                                                 new ConcurrentHashMap<String, AssistentPage<?>>();
  protected final ObjectProperty<AssistentPage<?>> currentPage               =
                                                                                 new SimpleObjectProperty<AssistentPage<?>>(
                                                                                     this);
  protected final ObjectProperty<Object>           currentResultProperty     = new SimpleObjectProperty<Object>();
  protected final StringProperty                   currentNextPageIdProperty = new SimpleStringProperty();
  protected final ListProperty<AssistentPage<?>>   previousPages             =
                                                                                 new SimpleListProperty<AssistentPage<?>>(
                                                                                     FXCollections
                                                                                         .<AssistentPage<?>> observableArrayList());
  protected final Stage                            owner;

  public Assistent(
      Stage owner,
      String stageTitle,
      String title,
      String text,
      Node content,
      Function<T, String> nextPageIdFunction) {
    super(title, text, content, nextPageIdFunction);
    this.owner = owner;
    this.stage.setTitle(stageTitle);
    this.stage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.getCode().equals(KeyCode.ESCAPE))
        stage.close();
    });
  }

  protected void initialize() {
    contentProperty.set(createInitialNode());
    if (owner != null)
      stage.initOwner(owner);
    stage.initStyle(StageStyle.UTILITY);
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setScene(SceneBuilder.create().width(width).height(height).root(pane).build());
    createPages();
    createTop();
    createBottom();
    this.title.textProperty().bind(new StringBinding() {

      {
        super.bind(currentPage);
      }

      @Override
      protected String computeValue() {
        return currentPage.get().titleProperty.get();
      }
    });
    this.text.textProperty().bind(new StringBinding() {

      {
        super.bind(currentPage);
      }

      @Override
      protected String computeValue() {
        return currentPage.get().textProperty.get();
      }
    });
    pane.centerProperty().bind(new ObjectBinding<Node>() {

      {
        super.bind(currentPage);
      }

      @Override
      protected Node computeValue() {
        return currentPage.get().contentProperty.get();
      }
    });
    currentPage.set(this);
    currentResultProperty.bind(currentPage.get().resultProperty);
    currentNextPageIdProperty.bind(currentPage.get().nextPageIdBinding);
  }

  public void showAndWait() {
    stage.showAndWait();
  }

  protected abstract Node createInitialNode();

  protected abstract void createPages();

  protected final void next() {
    currentPage.get().onNext();
    previousPages.add(currentPage.get());
    if (currentPage.get().nextPageIdBinding.get() != null) {
      currentPage.set(availablePages.get(currentPage.get().nextPageIdBinding.get()));
      currentResultProperty.bind(currentPage.get().resultProperty);
      currentNextPageIdProperty.bind(currentPage.get().nextPageIdBinding);
    } else
      stage.close();
  }

  protected final void previous() {
    currentPage.set(previousPages.remove(previousPages.size() - 1));
    currentResultProperty.bind(currentPage.get().resultProperty);
    currentNextPageIdProperty.bind(currentPage.get().nextPageIdBinding);
  }

  protected final void cancel() {
    stage.close();
  }

  protected final void createTop() {
    final Rectangle background =
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
                        StopBuilder.create().color(ColorScheme.JAVA_FX.getColor(4)).offset(0).build(),
                        StopBuilder.create().color(Color.WHITE).offset(1).build())
                    .build())
            .height(80)
            .width(width)
            .build();
    pane.setTop(StackPaneBuilder
        .create()
        .children(
            background,
            VBoxBuilder
                .create()
                .alignment(Pos.TOP_LEFT)
                .padding(InsetsBuilder.create().top(10).left(10).build())
                .children(title, text)
                .build())
        .build());
  }

  protected final void createBottom() {
    final Rectangle background =
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
    final Button cancel =
        ButtonBuilder.create().text("Cancel").minHeight(20).minWidth(100).onAction(new EventHandler<ActionEvent>() {

          @Override
          public void handle(ActionEvent event) {
            cancel();
          }
        }).build();
    final Button previous =
        ButtonBuilder.create().text("< Previous").minHeight(20).minWidth(100).onAction(new EventHandler<ActionEvent>() {

          @Override
          public void handle(ActionEvent event) {
            previous();
          }
        }).build();
    previous.disableProperty().bind(new BooleanBinding() {

      {
        super.bind(previousPages);
      }

      @Override
      protected boolean computeValue() {
        return previousPages.isEmpty();
      }
    });
    final Button next =
        ButtonBuilder.create().text("Next >").minHeight(20).minWidth(100).onAction(new EventHandler<ActionEvent>() {

          @Override
          public void handle(ActionEvent event) {
            next();
          }
        }).build();
    next.disableProperty().bind(new BooleanBinding() {

      {
        super.bind(currentResultProperty);
      }

      @Override
      protected boolean computeValue() {
        return currentResultProperty.isNull().get();
      }
    });
    next.textProperty().bind(new StringBinding() {

      {
        super.bind(currentResultProperty, currentNextPageIdProperty);
      }

      @Override
      protected String computeValue() {
        if (currentResultProperty.isNotNull().get() && currentNextPageIdProperty.isNull().get())
          return "Finish";
        else
          return "Next >";
      }
    });
    pane.setBottom(StackPaneBuilder
        .create()
        .children(
            background,
            HBoxBuilder
                .create()
                .alignment(Pos.CENTER_RIGHT)
                .spacing(10)
                .padding(InsetsBuilder.create().right(30).build())
                .children(cancel, previous, next)
                .build())
        .build());
  }

}
