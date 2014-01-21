package conexp.fx.core.exporter;

/*
 * #%L
 * Concept Explorer FX - Core
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


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javafx.geometry.Point3D;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.layout.ConceptLayout;

public class CFXExporter<G, M> {

  public static <G, M> void export(
      MatrixContext<G, M> context,
      Map<Integer, Integer> domainPermutation,
      Map<Integer, Integer> codomainPermutation,
      ConceptLayout<G, M> layout,
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
    for (Entry<M, Point3D> attributeSeed : layout.seeds.entrySet()) {
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
