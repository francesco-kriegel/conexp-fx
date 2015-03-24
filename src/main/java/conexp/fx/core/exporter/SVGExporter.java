package conexp.fx.core.exporter;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
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
import conexp.fx.core.layout.ConceptLayout;


public class SVGExporter<G, M> {

  public static <G, M> void export(
      MatrixContext<G, M> context,
      Map<Integer, Integer> domainPermutation,
      Map<Integer, Integer> codomainPermutation,
      ConceptLayout<G, M> layout,
      boolean exportArrows,
      boolean exportLabels,
      File file) {
    try {
      if (!file.exists()) {
        if (!file.getParentFile().exists())
          file.mkdirs();
        file.createNewFile();
      }
      Document doc =
          toSVGDocument(
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
      ConceptLayout<G, M> layout,
      boolean exportArrows,
      boolean exportLabels) {
    final double width = 100d * layout.getCurrentBoundingBox().getWidth();
    final double minX = 100d * layout.getCurrentBoundingBox().getMinX();
    final double height = 100d * layout.getCurrentBoundingBox().getHeight();
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
          final int x1 =
              (int) (100d * layout.positions.get(layout.lattice.rowHeads().get(i)).getX() - minX + (border / 2));
          final int y1 = (int) (100d * layout.positions.get(layout.lattice.rowHeads().get(i)).getY() + (border / 2));
          final int x2 =
              (int) (100d * layout.positions.get(layout.lattice.rowHeads().get(j)).getX() - minX + (border / 2));
          final int y2 = (int) (100d * layout.positions.get(layout.lattice.rowHeads().get(j)).getY() + (border / 2));
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
      final double x = 100d * layout.positions.get(conceptNode).getX();
      final double y = 100d * layout.positions.get(conceptNode).getY();
      Element circle = doc.createElementNS(svgNS, "circle");
      circle.setAttributeNS(null, "cx", String.valueOf((int) (x - minX) + (border / 2)));
      circle.setAttributeNS(null, "cy", String.valueOf((int) y + (border / 2)));
      circle.setAttributeNS(null, "r", String.valueOf(circleSize));
      svgRoot.appendChild(circle);
    }
    if (exportLabels)
      for (int i = 0; i < layout.lattice.rowHeads().size(); i++) {
        Concept<G, M> conceptNode = layout.lattice.rowHeads().get(i);
        String objLabels =
            layout.lattice
                .objectLabels(conceptNode)
                .toString()
                .substring(1, layout.lattice.objectLabels(conceptNode).toString().length() - 1);
        String attLabels =
            layout.lattice
                .attributeLabels(conceptNode)
                .toString()
                .substring(1, layout.lattice.attributeLabels(conceptNode).toString().length() - 1);
        final int x = (int) (100d * layout.positions.get(layout.lattice.rowHeads().get(i)).getX() - minX);
        final int y = (int) (100d * layout.positions.get(layout.lattice.rowHeads().get(i)).getY());
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
