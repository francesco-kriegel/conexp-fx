package conexp.fx.core.exporter;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.layout.ConceptLayout;

public class HTMLExporter<G, M> {

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
      BufferedWriter outputWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
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
    buffer
        .append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\r\n\t http://www.w3.org/TR/html4/strict.dtd\">\r\n");
    buffer.append("<html>\r\n");
    buffer.append("<head>\r\n");
    buffer.append("<title>" + name + "</title>\r\n");
    buffer.append("</head>\r\n");
    buffer.append("<body>\r\n");
    buffer.append("<table border=\"1\">\r\n");
    buffer.append("<tr>\r\n");
    buffer.append("<th></th>\r\n");
    for (int codomainIndex = 0; codomainIndex < formalContext.colHeads().size(); codomainIndex++) {
      int permIndex =
          codomainPermutation == null || !codomainPermutation.containsKey(codomainIndex) ? codomainIndex
              : codomainPermutation.get(codomainIndex);
      final M attribute = formalContext.colHeads().get(permIndex);
      buffer.append("<th>" + (exportLabels ? attribute : "") + "</th>\r\n");
    }
    buffer.append("</tr>\r\n");
    for (int domainIndex = 0; domainIndex < formalContext.rowHeads().size(); domainIndex++) {
      buffer.append("<tr>\r\n");
      final int dpermIndex =
          domainPermutation == null || !domainPermutation.containsKey(domainIndex) ? domainIndex : domainPermutation
              .get(domainIndex);
      final G object = formalContext.rowHeads().get(dpermIndex);
      buffer.append("<th>" + (exportLabels ? object : "") + "</th>\r\n");
      for (int codomainIndex = 0; codomainIndex < formalContext.colHeads().size(); codomainIndex++) {
        int cpermIndex =
            codomainPermutation == null || !codomainPermutation.containsKey(codomainIndex) ? codomainIndex
                : codomainPermutation.get(codomainIndex);
        final M attribute = formalContext.colHeads().get(cpermIndex);
        if (formalContext.contains(object, attribute))
          buffer.append("<td>X</td>\r\n");
        else if (exportArrows) {
          final boolean isDownArrow = formalContext.DownArrows.contains(object, attribute);
          final boolean isUpArrow = formalContext.UpArrows.contains(object, attribute);
          if (isDownArrow) {
            if (isUpArrow)
              buffer.append("<td>b</td>\r\n");
            else
              buffer.append("<td>d</td>\r\n");
          } else {
            if (isUpArrow)
              buffer.append("<td>u</td>\r\n");
            else
              buffer.append("<td>.</td>\r\n");
          }
        } else
          buffer.append("<td>.</td>\r\n");
      }
      buffer.append("</tr>\r\n");
    }
    buffer.append("</table>\r\n");
    buffer.append("</body>\r\n");
    buffer.append("</html>\r\n");
    return buffer;
  }
}
