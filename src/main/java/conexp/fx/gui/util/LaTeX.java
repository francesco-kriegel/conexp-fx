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

import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;

import conexp.fx.gui.ConExpFX;

public class LaTeX {

  public final static Image toFXImage(final String string, final float height) {
    if (string == null)
      return new Image(ConExpFX.class.getResourceAsStream("image/16x16/warning.png"));
    try {
      final BufferedImage texImage =
          (BufferedImage) new TeXFormula("\\sf\\mbox{" + string.replace("%", "\\%") + "}").createBufferedImage(
              TeXConstants.STYLE_DISPLAY,
              height,
              java.awt.Color.BLACK,
              java.awt.Color.WHITE);
      final BufferedImage TeXImage =
          new BufferedImage(texImage.getWidth(), texImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
      final Graphics2D graphics = TeXImage.createGraphics();
      graphics.drawImage(
          Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(texImage.getSource(), new RGBImageFilter() {

            public final int filterRGB(final int x, final int y, final int rgb) {
              if (0xFFEEEEEE < rgb)
                return 0x00FFFFFF;
              return rgb;
            }
          })),
          0,
          0,
          null);
      graphics.dispose();
      return SwingFXUtils.toFXImage(TeXImage, null);
    } catch (ParseException e) {
      System.err.println(string);
      e.printStackTrace();
      return null;
    }
  }

  public final static ObjectBinding<Image> toFXImageBinding(final ObservableValue<String> string, final float height) {
    return new ObjectBinding<Image>() {

      {
        bind(string);
      }

      @Override
      protected final Image computeValue() {
        return LaTeX.toFXImage(string.getValue(), height);
      }
    };
  }

  public final static ObjectBinding<Image> toFXImageBinding(
      final ObservableValue<String> string,
      final ObservableValue<Number> height) {
    return new ObjectBinding<Image>() {

      {
        bind(string, height);
      }

      @Override
      protected final Image computeValue() {
        return LaTeX.toFXImage(string.getValue(), height.getValue().floatValue());
      }
    };
  }

}
