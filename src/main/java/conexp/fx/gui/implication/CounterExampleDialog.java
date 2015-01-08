package conexp.fx.gui.implication;

import java.util.Set;

import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextBuilder;
import javafx.stage.Window;
import conexp.fx.core.builder.Request;
import conexp.fx.core.builder.Requests;
import conexp.fx.core.util.IdGenerator;
import conexp.fx.gui.FCAInstance;
import conexp.fx.gui.context.MatrixContextWidget;
import de.tudresden.inf.tcs.fcalib.Implication;

public class CounterExampleDialog<G, M> extends Dialog {

  private final FCAInstance<String, String> tab;
  private final Button                      addButton;
  private final FCAInstance<Object, Object> cExTab;

  public CounterExampleDialog(final FCAInstance<String, String> tab2, final Implication<M> imp, final Window parent) {
    super("Attribute Exploration: Provide CounterExample", parent);
    this.tab = tab2;
    this.addButton = ButtonBuilder.create().text("Add").minWidth(100).onAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent event) {
        add();
      }
    }).build();
    HBox buttons = new HBox();
    buttons.setPadding(new Insets(10));
    buttons.setSpacing(4);
    buttons.setAlignment(Pos.CENTER_RIGHT);
    buttons.getChildren().add(addButton);
    pane.setBottom(buttons);
    pane.setTop(TextBuilder.create().text(imp.toString()).wrappingWidth(300).build());
    Request request = new Requests.New.NewContext(0, 0);// 1, tab.fca.context.colHeads().size());
    cExTab = new FCAInstance<Object, Object>(tab2.cfx, request);
    cExTab.context.rowHeads().add("counterexample " + IdGenerator.getNextId());
    cExTab.context.colHeads().addAll(tab2.context.colHeads());
    final MatrixContextWidget<Object, Object> matrixContextWidget =
        new MatrixContextWidget<Object, Object>(cExTab, true);
    matrixContextWidget.highlightToggleButton.setSelected(true);
    pane.setCenter(matrixContextWidget);
//    pane.setMinHeight(500);
    pane.minHeightProperty().bind(new DoubleBinding() {

      {
        bind(matrixContextWidget.height);
      }

      @Override
      protected double computeValue() {
        System.out.println(matrixContextWidget.height.intValue());
        return matrixContextWidget.height.doubleValue() + 100;
      }
    });
    pane.setMinWidth(600);
  }

  private final void add() {
    final Object counterObject = cExTab.context.rowHeads().get(0);
    final Set<Object> counterExample = cExTab.context.row(counterObject);
    // TODO: mechanism for type safety needed!
//    tab.fca.context.rowHeads().add(counterObject);
//    tab.fca.context.row(counterObject).addAll(counterExample);
  }

  @Override
  protected void onClose() {}
}
