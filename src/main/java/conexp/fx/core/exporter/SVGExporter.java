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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.layout.AdditiveConceptLayout;

public class SVGExporter<G, M> {

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
      Document doc = toSVGDocument(
          file.getName(),
          context,
          domainPermutation,
          codomainPermutation,
          layout,
          exportArrows,
          exportLabels);
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      Result output = new StreamResult(new FileOutputStream(file));
      Source input = new DOMSource(doc);
      transformer.transform(input, output);
    } catch (TransformerFactoryConfigurationError | TransformerException | IOException e) {
      System.err.println("Unable to create or write SVGDocument to file " + file);
      e.printStackTrace();
    }
  }

  private static <G, M> Document toSVGDocument(
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
    final int circleSize = 16;
    final int textOffset = 12;
    DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
    String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    Document doc = impl.createDocument(svgNS, "svg", null);
    Element svgRoot = doc.getDocumentElement();
    svgRoot.setAttributeNS(null, "width", String.valueOf((int) width + border));
    svgRoot.setAttributeNS(null, "height", String.valueOf((int) height + border));
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
          Element line = doc.createElementNS(svgNS, "line");
          line.setAttributeNS(null, "x1", String.valueOf(x1));
          line.setAttributeNS(null, "y1", String.valueOf(y1));
          line.setAttributeNS(null, "x2", String.valueOf(x2));
          line.setAttributeNS(null, "y2", String.valueOf(y2));
          line.setAttributeNS(null, "stroke-width", "4");
          line.setAttributeNS(null, "stroke", "grey");
          svgRoot.appendChild(line);
        }
      }
    }
    for (int i = 0; i < layout.lattice.rowHeads().size(); i++) {
      Concept<G, M> conceptNode = layout.lattice.rowHeads().get(i);
      final double x = 100d * layout.getPosition(conceptNode).getValue().getX();
      final double y = 100d * layout.getPosition(conceptNode).getValue().getY();
      Element circle = doc.createElementNS(svgNS, "circle");
      circle.setAttributeNS(null, "cx", String.valueOf((int) (x - minX) + (border / 2)));
      circle.setAttributeNS(null, "cy", String.valueOf((int) y + (border / 2)));
      circle.setAttributeNS(null, "r", String.valueOf(circleSize));
      svgRoot.appendChild(circle);
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
        final int ox = x + (border / 2);
        final int oy = y + (circleSize + textOffset) + (border / 2) + 10;
        final int ax = x + (border / 2);
        final int ay = y - (circleSize + textOffset) + (border / 2);
        Element objText = doc.createElementNS(svgNS, "text");
        objText.setAttributeNS(null, "x", String.valueOf(ox));
        objText.setAttributeNS(null, "y", String.valueOf(oy));
        objText.setAttributeNS(null, "text-anchor", "middle");
        objText.setTextContent(objLabels);
        svgRoot.appendChild(objText);
        Element attText = doc.createElementNS(svgNS, "text");
        attText.setAttributeNS(null, "x", String.valueOf(ax));
        attText.setAttributeNS(null, "y", String.valueOf(ay));
        attText.setAttributeNS(null, "text-anchor", "middle");
        attText.setTextContent(attLabels);
        svgRoot.appendChild(attText);
      }
    return doc;
  }
}
