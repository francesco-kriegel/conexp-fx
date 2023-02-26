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
