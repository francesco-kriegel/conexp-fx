package conexp.fx.gui.dialog;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

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

public abstract class SimpleDialog {

  protected final Stage      stage;
  protected final Scene      scene;
  protected final BorderPane pane;

  protected SimpleDialog(final String title, final Window parent) {
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
