/*
 * @author Francesco.Kriegel@gmx.de
 */
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

import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.layout.ConceptLayout;

public class TeXExporter<G, M> {

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
      BufferedWriter outputWriter = new BufferedWriter(new FileWriter(file));
      StringBuffer buffer =
          toStringBuffer(
              file.getName(),
              context,
              domainPermutation,
              codomainPermutation,
              layout,
              exportArrows,
              exportLabels);
      outputWriter.append(buffer);
      outputWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static <G, M> StringBuffer toStringBuffer(
      String name,
      MatrixContext<G, M> formalContext,
      Map<Integer, Integer> domainPermutation,
      Map<Integer, Integer> codomainPermutation,
      ConceptLayout<G, M> layout,
      boolean exportArrows,
      boolean exportLabels) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("\\begin{cxt}\r\n\\cxtName{" + name + "}\r\n");
    for (int codomainIndex = 0; codomainIndex < formalContext.colHeads().size(); codomainIndex++) {
      int permIndex =
          codomainPermutation == null || !codomainPermutation.containsKey(codomainIndex) ? codomainIndex
              : codomainPermutation.get(codomainIndex);
      final M attribute = formalContext.colHeads().get(permIndex);
      buffer.append("\\atr{" + (exportLabels ? attribute : "") + "}\r\n");
    }
    for (int domainIndex = 0; domainIndex < formalContext.rowHeads().size(); domainIndex++) {
      final int dpermIndex =
          domainPermutation == null || !domainPermutation.containsKey(domainIndex) ? domainIndex : domainPermutation
              .get(domainIndex);
      final G object = formalContext.rowHeads().get(dpermIndex);
      String incidences = "";
      for (int codomainIndex = 0; codomainIndex < formalContext.colHeads().size(); codomainIndex++) {
        int cpermIndex =
            codomainPermutation == null || !codomainPermutation.containsKey(codomainIndex) ? codomainIndex
                : codomainPermutation.get(codomainIndex);
        final M attribute = formalContext.colHeads().get(cpermIndex);
        if (formalContext.contains(object, attribute)) {
          incidences += "X";
        } else if (exportArrows) {
          final boolean isDownArrow = formalContext.DownArrows.contains(object, attribute);
          final boolean isUpArrow = formalContext.UpArrows.contains(object, attribute);
          if (isDownArrow) {
            if (isUpArrow) {
              incidences += "b";
            } else {
              incidences += "d";
            }
          } else {
            if (isUpArrow) {
              incidences += "u";
            } else {
              incidences += ".";
            }
          }
        } else {
          incidences += ".";
        }
      }
      buffer.append("\\obj{" + incidences + "}{" + (exportLabels ? object : "") + "}\r\n");
    }
    buffer.append("\\end{cxt}\r\n");
    final double width = layout.getCurrentBoundingBox().getWidth();
    final double minX = layout.getCurrentBoundingBox().getMinX();
    final double height = layout.getCurrentBoundingBox().getHeight();
    final double unit = Math.min(160 / width, 230 / height);
    buffer.append("\\begin{diagram}{" + width + "}{" + height + "}\r\n");
    buffer.append("\\unitlength " + unit + "mm\r\n");
    buffer.append("\\CircleSize{10}\r\n");
    buffer.append("\\NodeThickness{1pt}\r\n");
    buffer.append("\\EdgeThickness{1pt}\r\n");
    for (int i = 0; i < layout.lattice.rowHeads().size(); i++) {
      Concept<G, M> concept = layout.lattice.rowHeads().get(i);
      final double x = layout.positions.get(concept).getX();
      final double y = layout.positions.get(concept).getY();
      buffer.append("\\Node{" + i + "}{" + (x - minX) + "}{" + (height - y) + "}\r\n");
    }
    for (int i = 0; i < layout.lattice.rowHeads().size(); i++) {
      for (int j = 0; j < layout.lattice.rowHeads().size(); j++) {
        if (layout.lattice._contains(i, j))
          buffer.append("\\Edge{" + i + "}{" + j + "}\r\n");
      }
    }
    if (exportLabels)
      for (int i = 0; i < layout.lattice.rowHeads().size(); i++) {
        Concept<G, M> concept = layout.lattice.rowHeads().get(i);
        String objLabels =
            layout.lattice
                .objectLabels(concept)
                .toString()
                .substring(1, layout.lattice.objectLabels(concept).toString().length() - 1);
        String attLabels =
            layout.lattice
                .attributeLabels(concept)
                .toString()
                .substring(1, layout.lattice.attributeLabels(concept).toString().length() - 1);
        buffer.append("\\centerObjbox{" + i + "}{0}{3}{" + objLabels + "}\r\n");
        buffer.append("\\centerAttbox{" + i + "}{0}{3}{" + attLabels + "}\r\n");
      }
    buffer.append("\\end{diagram}\r\n");
    return buffer;
  }
}
