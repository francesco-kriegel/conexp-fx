package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX - Graphical User Interface
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
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


import javafx.scene.Scene;
import javafx.scene.control.LabelBuilder;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.StageStyle;

public final class TextWidthCalculator {

  private final static Text  text  = TextBuilder.create().opacity(0).build();
  private final static Stage stage = StageBuilder
                                       .create()
                                       .opacity(0)
                                       .style(StageStyle.UTILITY)
                                       .scene(new Scene(LabelBuilder.create().graphic(text).build()))
                                       .build();

  public final static double getMaximalTextWidth(final int textSize, final Iterable<String> strings) {
    return getMaximalTextWidth("-fx-font-size: " + textSize + ";", strings);
  }

  public final static double getMaximalTextWidth(final String style, final Iterable<String> strings) {
    text.setStyle(style);
    double maxWidth = 0;
    for (final String string : strings)
      maxWidth = Math.max(maxWidth, getTextWidth(string));
    return maxWidth;
  }

  private final static double getTextWidth(final String string) {
    text.setText(string);
    stage.show();
    final double width = text.getLayoutBounds().getWidth();
    stage.hide();
    return width;
  }

}
