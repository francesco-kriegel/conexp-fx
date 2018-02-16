package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
