package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2022 Francesco Kriegel
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

import javafx.scene.Scene;
import javafx.scene.control.LabelBuilder;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.StageStyle;

@SuppressWarnings("deprecation")
public final class TextWidthCalculator {

  private static final Text  text  = TextBuilder.create().opacity(0).build();
  private static final Stage stage = StageBuilder
                                       .create()
                                       .opacity(0)
                                       .style(StageStyle.UTILITY)
                                       .scene(new Scene(LabelBuilder.create().graphic(text).build()))
                                       .build();

  public static final double getMaximalTextWidth(final int textSize, final Iterable<String> strings) {
    return getMaximalTextWidth("-fx-font-size: " + textSize + ";", strings);
  }

  public static final double getMaximalTextWidth(final String style, final Iterable<String> strings) {
    text.setStyle(style);
    double maxWidth = 0;
    for (final String string : strings)
      maxWidth = Math.max(maxWidth, getTextWidth(string));
    return maxWidth;
  }

  public static synchronized final double getTextWidth(final String string) {
    text.setText(string);
    stage.show();
    final double width = text.getLayoutBounds().getWidth();
    stage.hide();
    return width;
  }

}
