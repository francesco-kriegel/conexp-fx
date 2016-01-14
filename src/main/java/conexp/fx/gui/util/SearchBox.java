package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
