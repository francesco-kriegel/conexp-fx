package conexp.fx.core.exporter;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2019 Francesco Kriegel
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


import java.io.File;
import java.util.Map;

import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.layout.AdditiveConceptLayout;


public class PDFExporter<G, M> {

  public static <G, M> void export(
      MatrixContext<G, M> context,
      Map<Integer, Integer> domainPermutation,
      Map<Integer, Integer> codomainPermutation,
      AdditiveConceptLayout<G, M> layout,
      boolean exportArrows,
      boolean exportLabels,
      File file) {
//    try {
//      if (!file.exists()) {
//        if (!file.getParentFile().exists())
//          file.mkdirs();
//        file.createNewFile();
//      }
//      final double width = 100d * layout.getCurrentBoundingBox().getWidth();
//      final double minX = 100d * layout.getCurrentBoundingBox().getMinX();
//      final double height = 100d * layout.getCurrentBoundingBox().getHeight();
//      // final double unit = Math.min(160 / width, 230 / height);
//      final int border = 100;
//      final int circleSize = 20;
//      final int textOffset = 5;
//      final Document doc = new Document(PageSize.A4);
//      FileOutputStream outputStream = new FileOutputStream(file);
//      PdfWriter writer = PdfWriter.getInstance(doc, outputStream);
//      doc.open();
//      PdfContentByte content = writer.getDirectContent();
//      final float dw = doc.getPageSize().getWidth();
//      final float dh = doc.getPageSize().getHeight();
//      final float cw = (float) width + border;
//      final float ch = (float) height + border;
//      final float wr = dw / cw;
//      final float hr = dh / ch;
//      final float f = (wr > hr ? hr : wr);
//      Graphics2D gfx = content.createGraphics(dw, dh);
//      gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//      gfx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//      gfx.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//      gfx.setBackground(Color.WHITE);
//      gfx.setColor(Color.WHITE);
//      gfx.fillRect(0, 0, (int) width + border, (int) height + border);
//      gfx.setColor(Color.BLACK);
//      for (int i = 0; i < layout.lattice.rowHeads().size(); i++) {
//        Concept<G, M> conceptNode = layout.lattice.rowHeads().get(i);
//        final double x = 100d * layout.positions.get(conceptNode).getX();
//        final double y = 100d * layout.positions.get(conceptNode).getY();
//        gfx.fillOval((int) (f * ((x - minX) - (3 * circleSize / 4) + (border / 2))), (int) (f * (y
//            - (3 * circleSize / 4) + (border / 2))), circleSize, circleSize);
//      }
//      for (int i = 0; i < layout.lattice.rowHeads().size(); i++) {
//        for (int j = 0; j < layout.lattice.rowHeads().size(); j++) {
//          if (layout.lattice._contains(i, j)) {
//            final int x1 =
//                (int) (f * (100d * layout.positions.get(layout.lattice.rowHeads().get(i)).getX() - minX + (border / 2)));
//            final int y1 =
//                (int) (f * (100d * layout.positions.get(layout.lattice.rowHeads().get(i)).getY() + (border / 2)));
//            final int x2 =
//                (int) (f * (100d * layout.positions.get(layout.lattice.rowHeads().get(j)).getX() - minX + (border / 2)));
//            final int y2 =
//                (int) (f * (100d * layout.positions.get(layout.lattice.rowHeads().get(j)).getY() + (border / 2)));
//            gfx.drawLine(x1, y1, x2, y2);
//          }
//        }
//      }
//      if (exportLabels)
//        for (int i = 0; i < layout.lattice.rowHeads().size(); i++) {
//          Concept<G, M> conceptNode = layout.lattice.rowHeads().get(i);
//          String objLabels =
//              layout.lattice
//                  .objectLabels(conceptNode)
//                  .toString()
//                  .substring(1, layout.lattice.objectLabels(conceptNode).toString().length() - 1);
//          String attLabels =
//              layout.lattice
//                  .attributeLabels(conceptNode)
//                  .toString()
//                  .substring(1, layout.lattice.attributeLabels(conceptNode).toString().length() - 1);
//          final int owidth = (int) (f * gfx.getFontMetrics().stringWidth(objLabels));
//          final int awidth = (int) (f * gfx.getFontMetrics().stringWidth(attLabels));
//          final int theight = gfx.getFontMetrics().getHeight();
//          final int x =
//              (int) (f * (100d * layout.positions.get(layout.lattice.rowHeads().get(i)).getX() - minX + (border / 2)));
//          final int ay =
//              (int) (f * (100d * layout.positions.get(layout.lattice.rowHeads().get(i)).getY()
//                  - (circleSize + textOffset) + (border / 2)));
//          final int oy =
//              (int) (f * (100d * layout.positions.get(layout.lattice.rowHeads().get(i)).getY()
//                  + (circleSize + textOffset) + (border / 2) + (theight / 2)));
//          gfx.drawString(objLabels, x - (owidth / 2), oy);
//          gfx.drawString(attLabels, x - (awidth / 2), ay);
//        }
//      gfx.dispose();
//      doc.close();
//      outputStream.flush();
//      outputStream.close();
//    } catch (IOException | DocumentException e) {
//      System.err.println("Unable to create or write PDFDocument to file " + file);
//      e.printStackTrace();
//    }
  }
}
