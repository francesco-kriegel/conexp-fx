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
