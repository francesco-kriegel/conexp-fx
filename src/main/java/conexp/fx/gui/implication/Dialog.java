package conexp.fx.gui.implication;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
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
