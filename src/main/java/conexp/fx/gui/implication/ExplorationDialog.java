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
    super("Attribute Exploration", tab2.cfx.primaryStage);
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
