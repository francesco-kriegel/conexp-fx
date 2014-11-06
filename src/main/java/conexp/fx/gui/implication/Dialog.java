package conexp.fx.gui.implication;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public abstract class Dialog {

  protected final Stage      stage;
  protected final Scene      scene;
  protected final BorderPane pane;

  protected Dialog(final String title, final Window parent) {
    super();
    this.pane = new BorderPane();
    this.scene = SceneBuilder.create().root(pane).build();
    this.stage = StageBuilder.create().title(title).style(StageStyle.UTILITY).scene(scene).build();
    this.stage.initModality(Modality.WINDOW_MODAL);
    this.stage.initOwner(parent);
    this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

      @Override
      public final void handle(final WindowEvent event) {
        onClose();
      }
    });
  }

  protected abstract void onClose();

  public void show() {
    stage.show();
  }

  public void showAndWait() {
    stage.showAndWait();
  }
}
