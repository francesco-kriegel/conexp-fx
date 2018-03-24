package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
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


import conexp.fx.gui.ConExpFX;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

public class SearchBox
  extends Region
{
  public final TextField textBox;
  public final Button    clearButton;

  public SearchBox()
  {
    super();
    this.getStylesheets().add(ConExpFX.class.getResource("style/SearchBox.css").toExternalForm());
    this.setId("SearchBox");
    this.getStyleClass().add("search-box");
    this.setMinHeight(24);
    this.setPrefSize(200, 24);
    this.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
    this.clearButton = new Button();
    clearButton.setVisible(false);
    clearButton.setOnAction(new EventHandler<ActionEvent>()
      {
        public final void handle(final ActionEvent actionEvent)
        {
          textBox.setText("");
          textBox.requestFocus();
        }
      });
    this.textBox = new TextField();
    textBox.setPromptText("Search");
    textBox.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>()
      {
        public final void handle(final MouseEvent event)
        {
          textBox.requestFocus();
        }
      });
    textBox.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>()
      {
        @SuppressWarnings("incomplete-switch")
        public final void handle(final KeyEvent event)
        {
          switch (event.getCode()) {
            case ESCAPE:
              textBox.setText("");
              textBox.requestFocus();
              break;
          }
        }
      });
    textBox.textProperty().addListener(new ChangeListener<String>()
      {
        public void changed(
            final ObservableValue<? extends String> observable,
            final String oldValue,
            final String newValue)
        {
          clearButton.setVisible(textBox.getText().length() != 0);
        }
      });
    this.getChildren().addAll(textBox, clearButton);
  }

  protected void layoutChildren()
  {
    textBox.resize(getWidth(), getHeight());
    clearButton.resizeRelocate(getWidth() - 18, 6, 12, 13);
  }
}
