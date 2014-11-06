package conexp.fx.gui.implication;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.text.Text;
import conexp.fx.gui.FCAInstance;
import de.tudresden.inf.tcs.fcalib.Implication;

public class ExplorationDialog<G, M> extends Dialog {

  private final FCAInstance<String, String> tab;
  private final Button                      yesButton;
  private final Button                      noButton;
  private final Button                      cancelButton;
  private int                               result = 0;
  private final Implication<M>              imp;

  @Override
  protected final void onClose() {
    result = -1;
  }

  public ExplorationDialog(final FCAInstance<String, String> tab2, final Implication<M> imp) {
    super("Attribute Exploration", tab2.conExpFX.primaryStage);
    this.tab = tab2;
    this.imp = imp;
    pane.setPadding(new Insets(10));
    final Text text = new Text(imp.toString());
    text.setWrappingWidth(300);
    pane.setTop(text);
    yesButton = ButtonBuilder.create().text("Yes").minWidth(100).onAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent event) {
        showNextImplication();
      }
    }).build();
    noButton = ButtonBuilder.create().text("No").minWidth(100).onAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent event) {
        askCounterExample();
      }
    }).build();
    cancelButton = ButtonBuilder.create().text("Cancel").minWidth(100).onAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent event) {
        result = -1;
        stage.close();
      }
    }).build();
    final HBox buttons = HBoxBuilder.create().children(yesButton, noButton, cancelButton).build();
    buttons.setSpacing(5);
    pane.setBottom(buttons);
  }

  public final int showAndReturn() {
    stage.showAndWait();
    return result;
  }

  public final void showNextImplication() {
    stage.close();
  }

  public final void askCounterExample() {
    new CounterExampleDialog<G, M>(tab, imp, stage).showAndWait();
  }
}
