package conexp.fx.core.exporter;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2020 Francesco Kriegel
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


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.layout.AdditiveConceptLayout;
import javafx.geometry.Point3D;

public class CFXExporter<G, M> {

  public static <G, M> void export(
      MatrixContext<G, M> context,
      Map<Integer, Integer> domainPermutation,
      Map<Integer, Integer> codomainPermutation,
      AdditiveConceptLayout<G, M> layout,
      File file) {
    Document xml = new Document("");
    final Element domainEl = xml.appendElement("domain");
//    domainEl.attr("type", formalContext.getDomainType() == null ? "unknown" : formalContext.getDomainType().toString());
    domainEl.attr("size", String.valueOf(context.rowHeads().size()));
    for (int domainIndex = 0; domainIndex < context.rowHeads().size(); domainIndex++) {
      final int permIndex =
          domainPermutation == null || !domainPermutation.containsKey(domainIndex) ? domainIndex : domainPermutation
              .get(domainIndex);
      final G object = context.rowHeads().get(permIndex);
      final Element objectEl = domainEl.appendElement("object");
      objectEl.attr("index", String.valueOf(permIndex));
      objectEl.attr("name", object.toString());
      objectEl.attr("selected", "true");// String.valueOf(formalContext.getSelectedObjects().contains(object)));
    }
    final Element codomainEl = xml.appendElement("codomain");
//    codomainEl.attr("type", formalContext.getCodomainType() == null ? "unknown" : formalContext
//        .getCodomainType()
//        .toString());
    codomainEl.attr("size", String.valueOf(context.colHeads().size()));
    for (int codomainIndex = 0; codomainIndex < context.colHeads().size(); codomainIndex++) {
      int permIndex =
          codomainPermutation == null || !codomainPermutation.containsKey(codomainIndex) ? codomainIndex
              : codomainPermutation.get(codomainIndex);
      final M attribute = context.colHeads().get(permIndex);
      final Element attributeEl = codomainEl.appendElement("attribute");
      attributeEl.attr("index", String.valueOf(permIndex));
      attributeEl.attr("name", attribute.toString());
      attributeEl.attr("selected", String.valueOf(context.selectedAttributes().contains(attribute)));
    }
    final Element contextEl = xml.appendElement("context");
    for (int domainIndex = 0; domainIndex < context.rowHeads().size(); domainIndex++) {
      final int dpermIndex =
          domainPermutation == null || !domainPermutation.containsKey(domainIndex) ? domainIndex : domainPermutation
              .get(domainIndex);
      final G object = context.rowHeads().get(dpermIndex);
      for (int codomainIndex = 0; codomainIndex < context.colHeads().size(); codomainIndex++) {
        int cpermIndex =
            codomainPermutation == null || !codomainPermutation.containsKey(codomainIndex) ? codomainIndex
                : codomainPermutation.get(codomainIndex);
        final M attribute = context.colHeads().get(cpermIndex);
        if (context._contains(dpermIndex, cpermIndex)) {
          final Element incidenceEl = contextEl.appendElement("incidence");
          incidenceEl.attr("object", object.toString());
          incidenceEl.attr("attribute", attribute.toString());
        } else {
          final boolean upArrow = context.UpArrows.contains(object, attribute);
          final boolean downArrow = context.DownArrows.contains(object, attribute);
          if (upArrow && downArrow) {
            final Element arrowsEl = contextEl.appendElement("both-arrows");
            arrowsEl.attr("object", object.toString());
            arrowsEl.attr("attribute", attribute.toString());
          } else if (upArrow) {
            final Element arrowsEl = contextEl.appendElement("up-arrow");
            arrowsEl.attr("object", object.toString());
            arrowsEl.attr("attribute", attribute.toString());
          } else if (downArrow) {
            final Element arrowsEl = contextEl.appendElement("down-arrow");
            arrowsEl.attr("object", object.toString());
            arrowsEl.attr("attribute", attribute.toString());
          }
        }
      }
    }
    final Element latticeEl = xml.appendElement("lattice");
    for (Entry<M, Point3D> attributeSeed : layout.seedsM.entrySet()) {
      final Element seedEl = latticeEl.appendElement("attribute-seed");
      seedEl.attr("attribute", attributeSeed.getKey().toString());
      seedEl.attr("x", String.valueOf(attributeSeed.getValue().getX()));
      seedEl.attr("y", String.valueOf(attributeSeed.getValue().getY()));
      seedEl.attr("z", String.valueOf(attributeSeed.getValue().getZ()));
    }
    try {
      if (!file.exists()) {
        if (!file.getParentFile().exists())
          file.mkdirs();
        file.createNewFile();
      }
      final FileWriter fileWriter = new FileWriter(file);
      final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
      bufferedWriter.write(xml.toString());
      bufferedWriter.close();
      fileWriter.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.err.println("Unable to write CFX-File to " + file.toString());
      e.printStackTrace();
    }
  }
}
