package conexp.fx.gui.exploration;

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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import conexp.fx.core.algorithm.exploration.CounterExample;
import conexp.fx.core.algorithm.exploration.Expert;
import conexp.fx.core.collections.relation.RelationEvent;
import conexp.fx.core.collections.relation.RelationEventHandler;
import conexp.fx.core.context.Context;
import conexp.fx.core.context.Implication;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.context.MatrixContext.AutomaticMode;
import conexp.fx.core.util.IdGenerator;
import conexp.fx.gui.context.MatrixContextWidget;
import conexp.fx.gui.dialog.ErrorDialog;
import conexp.fx.gui.util.Platform2;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class HumanExpertP<M> implements Expert<String, M> {

  private final class Question {

    private final String                                          object;
    private final Implication<String, M>                          implication;

    private final CountDownLatch                                  cdl        = new CountDownLatch(1);
    private final AtomicReference<Set<CounterExample<String, M>>> ref        = new AtomicReference<>();

    private final HBox                                            buttonsBox = new HBox();
    private final RelationEventHandler<Implication<String, M>, M> eventHandler;

    private Question(final Implication<String, M> implication) {
      super();
      this.object = "Counter-Example " + IdGenerator.getNextId(cxt);
      this.implication = implication;
      final Button acceptButton = new Button("accept");
      final Button declineButton = new Button("decline");
      acceptButton.maxHeightProperty().bind(counterExamplesWidget.rowHeaderPane.rowHeight);
      acceptButton.minHeightProperty().bind(counterExamplesWidget.rowHeaderPane.rowHeight);
      acceptButton.prefHeightProperty().bind(counterExamplesWidget.rowHeaderPane.rowHeight);
      declineButton.maxHeightProperty().bind(counterExamplesWidget.rowHeaderPane.rowHeight);
      declineButton.minHeightProperty().bind(counterExamplesWidget.rowHeaderPane.rowHeight);
      declineButton.prefHeightProperty().bind(counterExamplesWidget.rowHeaderPane.rowHeight);
      acceptButton.setMaxWidth(60);
      acceptButton.setMinWidth(60);
      acceptButton.setPrefWidth(60);
      declineButton.setMaxWidth(60);
      declineButton.setMinWidth(60);
      declineButton.setPrefWidth(60);
      acceptButton.setStyle("-fx-padding:0; -fx-background-radius: 5 0 0 5, 5 0 0 5, 4 0 0 4, 3 0 0 3;");
      declineButton.setStyle("-fx-padding:0; -fx-background-radius: 0 5 5 0, 0 5 5 0, 0 4 4 0, 0 3 3 0;");
      acceptButton.setOnAction(__ -> {
        ref.set(Collections.emptySet());
        cancel();
      });
      declineButton.setOnAction(__ -> {
        cancel();
      });
      buttonsBox.getChildren().addAll(acceptButton, declineButton);
      synchronized (counterExamples.rowHeads()) {
        counterExamples.rowHeads().add(implication);
        counterExamples.row(implication).addAll(implication.getPremise());
      }
      ref.set(Collections.singleton(new CounterExample<String, M>(object, implication.getPremise())));
      final AtomicBoolean blocked = new AtomicBoolean(false);
      eventHandler = __ -> {
        if (blocked.get() || !counterExamples.rowHeads().contains(implication))
          return;
        if (!counterExamples.row(implication).containsAll(implication.getPremise())
            || counterExamples.row(implication).containsAll(implication.getConclusion())) {
          blocked.set(true);
          counterExamples.row(implication).clear();
          counterExamples.row(implication).addAll(implication.getPremise());
          new ErrorDialog(
              stage,
              "Invalid Counterexample",
              "A counterexample must contain all premise attributes, but must not contain all conclusion attributes.")
                  .showAndWait();
          blocked.set(false);
        }
        ref.set(Collections.singleton(new CounterExample<String, M>(object, counterExamples.row(implication))));
      };
      counterExamples.addEventHandler(eventHandler, RelationEvent.ENTRIES);
      counterExamplesWidget.addRowDecoration(implication, buttonsBox);
    }

    private final void cancel() {
      synchronized (counterExamples.rowHeads()) {
        counterExamples.removeEventHandler(RelationEvent.ENTRIES, eventHandler);
        counterExamples.rowHeads().remove(implication);
        pendingQuestions.remove(this);
      }
      cdl.countDown();
    }

  }

  private Stage                                          stage;
  private final Map<Implication<String, M>, Question>    pendingQuestions = new ConcurrentHashMap<>();
  private boolean                                        isCancelled      = false;

  private final Context<String, M>                       cxt;
  private MatrixContext<Implication<String, M>, M>       counterExamples;
  private MatrixContextWidget<Implication<String, M>, M> counterExamplesWidget;

  public HumanExpertP(final Context<String, M> cxt) {
    super();
    this.cxt = cxt;
  }

  public final void init() {
    this.stage = new Stage(StageStyle.UTILITY);
//    this.stage.setResizable(false);
    this.stage.setTitle("Parallel Attribute Exploration");
    this.stage.setWidth(800);
    this.stage.setHeight(500);
    this.counterExamples = new MatrixContext<>(false, AutomaticMode.NONE);
    this.counterExamples.colHeads().addAll(cxt.colHeads());
    this.counterExamplesWidget = new MatrixContextWidget<>(null, true, false, this.counterExamples);
//    this.counterExamplesWidget.rowHeaderPane.highlight.set(true);
//    this.counterExamplesWidget.colHeaderPane.highlight.set(true);
//    this.counterExamplesWidget.contextPane.highlight.set(true);
    this.stage.setScene(
        new Scene(
            new BorderPane(
                this.counterExamplesWidget,
                new Label("Please decide whether the following implications are valid."),
                null,
                null,
                null)));
    this.stage.setOnHiding(__ -> {
      this.isCancelled = true;
      for (Question question : pendingQuestions.values()) {
        question.ref.set(Collections.emptySet());
        question.cancel();
      }
    });
  }

  public final void show() {
    stage.show();
  }

  public final void hide() {
    stage.hide();
  }

  @Override
  public Set<CounterExample<String, M>> getCounterExamples(final Implication<String, M> implication)
      throws InterruptedException {
    Platform2.runOnFXThreadAndWaitTryCatch(() -> pendingQuestions.put(implication, new Question(implication)));
//    return new Future<Set<CounterExample<String, M>>>() {
//
//      private 
    final Question question = pendingQuestions.get(implication);
//      private boolean        isCancelled = false;
//
//      @Override
//      public boolean cancel(boolean mayInterruptIfRunning) {
//        if (question.cdl.getCount() == 0)
//          return false;
//        isCancelled = true;
//        question.cancel();
//        return true;
//      }
//
//      @Override
//      public boolean isCancelled() {
//        return isCancelled;
//      }
//
//      @Override
//      public boolean isDone() {
//        return question.cdl.getCount() == 0;
//      }
//
//      @Override
//      public Set<CounterExample<String, M>> get() throws InterruptedException, ExecutionException {
    question.cdl.await();
    if (isCancelled)
      throw new InterruptedException();
    return question.ref.get();
//      }
//
//      @Override
//      public Set<CounterExample<String, M>> get(long timeout, TimeUnit unit)
//          throws InterruptedException, ExecutionException, TimeoutException {
//        if (question.cdl.await(timeout, unit))
//          return question.ref.get();
//        else
//          throw new TimeoutException();
//      }
//    };
  }

}
