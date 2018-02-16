package conexp.fx.core.exporter;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.layout.AdditiveConceptLayout;

public class PNGExporter<G, M> {

  public static <G, M> void export(
      MatrixContext<G, M> context,
      Map<Integer, Integer> domainPermutation,
      Map<Integer, Integer> codomainPermutation,
      AdditiveConceptLayout<G, M> layout,
      boolean exportArrows,
      boolean exportLabels,
      File file) {
    try {
      if (!file.exists()) {
        if (!file.getParentFile().exists())
          file.mkdirs();
        file.createNewFile();
      }
      BufferedImage img = toBufferedImage(
          file.getName(),
          context,
          domainPermutation,
          codomainPermutation,
          layout,
          exportArrows,
          exportLabels);
      ImageIO.write(img, "png", file);
    } catch (IOException e) {
      System.err.println("Unable to create or write GraphicsBuffer to file " + file);
      e.printStackTrace();
    }
  }

  private static <G, M> BufferedImage toBufferedImage(
      String name,
      MatrixContext<G, M> formalContext,
      Map<Integer, Integer> domainPermutation,
      Map<Integer, Integer> codomainPermutation,
      AdditiveConceptLayout<G, M> layout,
      boolean exportArrows,
      boolean exportLabels) {
    final double width = 100d * layout.getCurrentBoundingBox(false, false).getWidth();
    final double minX = 100d * layout.getCurrentBoundingBox(false, false).getMinX();
    final double height = 100d * layout.getCurrentBoundingBox(false, false).getHeight();
    // final double unit = Math.min(160 / width, 230 / height);
    final int border = 100;
    final int circleSize = 20;
    final int textOffset = 0;
    BufferedImage img = new BufferedImage(((int) width) + border, ((int) height) + border, BufferedImage.TYPE_INT_RGB);
    Graphics2D gfx = img.createGraphics();
    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    gfx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    gfx.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    gfx.setBackground(Color.WHITE);
    gfx.setColor(Color.WHITE);
    gfx.fillRect(0, 0, (int) width + border, (int) height + border);
    gfx.setColor(Color.BLACK);
    for (int i = 0; i < layout.lattice.rowHeads().size(); i++) {
      Concept<G, M> conceptNode = layout.lattice.rowHeads().get(i);
      final double x = 100d * layout.getPosition(conceptNode).getValue().getX();
      final double y = 100d * layout.getPosition(conceptNode).getValue().getY();
      gfx.fillOval(
          (int) (x - minX) - (circleSize / 2) + (border / 2),
          (int) y - (circleSize / 2) + (border / 2),
          circleSize,
          circleSize);
    }
    for (int i = 0; i < layout.lattice.rowHeads().size(); i++) {
      for (int j = 0; j < layout.lattice.rowHeads().size(); j++) {
        if (layout.lattice._contains(i, j)) {
          final int x1 = (int) (100d * layout.getPosition(layout.lattice.rowHeads().get(i)).getValue().getX()
              - minX + (border / 2));
          final int y1 = (int) (100d * layout.getPosition(layout.lattice.rowHeads().get(i)).getValue().getY()
              + (border / 2));
          final int x2 = (int) (100d * layout.getPosition(layout.lattice.rowHeads().get(j)).getValue().getX()
              - minX + (border / 2));
          final int y2 = (int) (100d * layout.getPosition(layout.lattice.rowHeads().get(j)).getValue().getY()
              + (border / 2));
          gfx.drawLine(x1, y1, x2, y2);
        }
      }
    }
    if (exportLabels)
      for (int i = 0; i < layout.lattice.rowHeads().size(); i++) {
        Concept<G, M> conceptNode = layout.lattice.rowHeads().get(i);
        String objLabels = layout.lattice.objectLabels(conceptNode).toString().substring(
            1,
            layout.lattice.objectLabels(conceptNode).toString().length() - 1);
        String attLabels = layout.lattice.attributeLabels(conceptNode).toString().substring(
            1,
            layout.lattice.attributeLabels(conceptNode).toString().length() - 1);
        final int x =
            (int) (100d * layout.getPosition(layout.lattice.rowHeads().get(i)).getValue().getX() - minX);
        final int y = (int) (100d * layout.getPosition(layout.lattice.rowHeads().get(i)).getValue().getY());
        final int owidth = gfx.getFontMetrics().stringWidth(objLabels);
        final int awidth = gfx.getFontMetrics().stringWidth(attLabels);
        final int theight = gfx.getFontMetrics().getHeight();
        gfx.drawString(
            objLabels,
            x + (border / 2) - (owidth / 2),
            y + (circleSize + textOffset) + (border / 2) + (theight / 2));
        gfx.drawString(attLabels, x + (border / 2) - (awidth / 2), y - (circleSize + textOffset) + (border / 2));
      }
    gfx.dispose();
    return img;
  }
}
