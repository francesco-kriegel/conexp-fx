package conexp.fx.gui.exploration;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import conexp.fx.core.algorithm.exploration.CounterExample;
import conexp.fx.core.context.Implication;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.util.IdGenerator;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.dialog.ErrorDialog;
import conexp.fx.gui.dialog.FXDialog;

public class CounterExampleDialog extends FXDialog<CounterExample<String, String>> {

  private final MatrixContext<String, String> context;
  private final Implication<String, String>   implication;

//  private final MatrixContext<String, String> counterExampleContext;

  public CounterExampleDialog(
      final MatrixContext<String, String> context,
      final Implication<String, String> implication) {
    super(
        ConExpFX.instance.primaryStage,
        Style.QUESTION,
        "Attribute Exploration",
        "Is the formal implication " + implication + " correct? If no, then please provide a counterexample.",
        null);
    this.context = context;
    this.implication = implication;
//    this.counterExampleContext =
//        new MatrixContext<String, String>(
//            SetLists.create("Counterexample" + IdGenerator.getNextId()),
//            context.colHeads(),
//            false);
    this.setCenterNode(createCenterNode());
  }

  private final Node createCenterNode() {
    final BorderPane pane = new BorderPane();
//    final Text implicationText = new Text(implication.toString());
//    implicationText.setWrappingWidth(300);
//    implicationText.setFont(Font.font(12));
//    pane.setTop(implicationText);

    final Label objectLabel = new Label("Counterexample-Name: ");
    final TextField objectTextField = new TextField("Counterexample" + IdGenerator.getNextId(context));
    final HBox objectBox = new HBox(objectLabel, objectTextField);
    final ObservableList<String> selectedAttributes = FXCollections.observableArrayList();
    final ObservableValue<CounterExample<String, String>> counterExample = Bindings.createObjectBinding(
        () -> new CounterExample<String, String>(objectTextField.getText(), selectedAttributes),
        objectTextField.textProperty(),
        selectedAttributes);
    counterExample.addListener((__, ___, newValue) -> CounterExampleDialog.this.value = newValue);
    selectedAttributes.addAll(implication.getPremise());
    final VBox checkBoxes = new VBox();
    for (String m : context.colHeads()) {
      final CheckBox checkBox = new CheckBox(m);
      if (implication.getPremise().contains(m)) {
        checkBox.setSelected(true);
        checkBox.setDisable(true);
      } else {
        checkBox.selectedProperty().addListener((__, ___, isSelected) -> {
          if (isSelected)
            selectedAttributes.add(checkBox.getText());
          else
            selectedAttributes.remove(checkBox.getText());
        });
      }
      checkBoxes.getChildren().add(checkBox);
    }
    counterExample.addListener((__, ___, newValue) -> {
      final boolean illegal = selectedAttributes.containsAll(implication.getPremise())
          && selectedAttributes.containsAll(implication.getConclusion());
      if (illegal) {
        checkBoxes.getChildren().stream().map(child -> (CheckBox) child).forEach(
            checkBox -> checkBox.setSelected(implication.getPremise().contains(checkBox.getText())));
        new ErrorDialog(
            ConExpFX.instance.primaryStage,
            new IllegalArgumentException(
                "An illegal counterexample was entered. The counterexample must not have all attributes in the implication's conclusion."))
                    .showAndWait();
      }
    });
    pane.setCenter(checkBoxes);
    pane.setTop(objectBox);
    return pane;
//  pane.setMinWidth(600);
//    final MatrixContextWidget<String, String> matrixContextWidget =
//        new MatrixContextWidget<String, String>(null, false, counterExampleContext);
//    matrixContextWidget.highlightToggleButton.setSelected(true);
//    pane.setCenter(matrixContextWidget);
//    pane.setMinHeight(500);
//    pane.minHeightProperty().bind(
//        new DoubleBinding() {
//
//          {
//            bind(matrixContextWidget.height);
//          }
//
//          @Override
//          protected double computeValue() {
////            System.out.println(matrixContextWidget.height.intValue());
//            return matrixContextWidget.height.doubleValue() + implicationText.getBoundsInLocal().getHeight();
//          }
//        });
//    counterExampleContext.addEventHandler(
//        event -> {
//          CounterExampleDialog.this.value = getCounterExample();
//        },
//        RelationEvent.ANY);
  }

//  private final CounterExample<String, String> getCounterExample() {
//    final String object = counterExampleContext.rowHeads().get(
//        0);
//    final Set<String> attributes = counterExampleContext.row(object);
//    return new CounterExample<String, String>(object, attributes);
//  }

}
