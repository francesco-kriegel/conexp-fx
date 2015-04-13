package conexp.fx.gui.exploration;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Set;

import javafx.beans.binding.DoubleBinding;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import conexp.fx.core.algorithm.exploration.CounterExample;
import conexp.fx.core.collections.relation.RelationEvent;
import conexp.fx.core.collections.setlist.SetLists;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.implication.Implication;
import conexp.fx.core.util.IdGenerator;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.context.MatrixContextWidget;
import conexp.fx.gui.dialog.FXDialog;

public class CounterExampleDialog extends FXDialog<CounterExample<String, String>> {

  private final MatrixContext<String, String> context;
  private final Implication<String, String>   implication;
  private final MatrixContext<String, String> counterExampleContext;

  public CounterExampleDialog(final MatrixContext<String, String> context, final Implication<String, String> implication) {
    super(
        ConExpFX.instance.primaryStage,
        Style.QUESTION,
        "Attribute Exploration",
        "Is the following formal Implication correct? If no, then please provide a counterexample.",
        null);
    this.context = context;
    this.implication = implication;
    this.counterExampleContext =
        new MatrixContext<String, String>(
            SetLists.create("Counterexample" + IdGenerator.getNextId()),
            context.colHeads(),
            false);
    this.setCenterNode(createCenterNode());
  }

  private final Node createCenterNode() {
    final BorderPane pane = new BorderPane();
    final Text implicationText = TextBuilder.create().text(
        implication.toString()).wrappingWidth(
        300).build();
    pane.setTop(implicationText);
    final MatrixContextWidget<String, String> matrixContextWidget =
        new MatrixContextWidget<String, String>(null, false, counterExampleContext);
    matrixContextWidget.highlightToggleButton.setSelected(true);
    pane.setCenter(matrixContextWidget);
    pane.setMinHeight(500);
    pane.minHeightProperty().bind(
        new DoubleBinding() {

          {
            bind(matrixContextWidget.height);
          }

          @Override
          protected double computeValue() {
//            System.out.println(matrixContextWidget.height.intValue());
            return matrixContextWidget.height.doubleValue() + implicationText.getBoundsInLocal().getHeight();
          }
        });
    pane.setMinWidth(600);
    counterExampleContext.addEventHandler(
        event -> {
          CounterExampleDialog.this.value = getCounterExample();
        },
        RelationEvent.ANY);
    return pane;
  }

  private final CounterExample<String, String> getCounterExample() {
    final String object = counterExampleContext.rowHeads().get(
        0);
    final Set<String> attributes = counterExampleContext.row(object);
    return new CounterExample<String, String>(object, attributes);
  }

}
