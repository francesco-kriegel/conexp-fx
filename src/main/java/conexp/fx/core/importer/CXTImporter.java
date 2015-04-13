/*
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.importer;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.ujmp.core.booleanmatrix.BooleanMatrix;

import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.util.IterableFile;

public class CXTImporter {

  public static void importt(MatrixContext<String, String> context, File file) {
    importt(context, new IterableFile(file));
  }

  public static void importt(MatrixContext<String, String> context, String content) {
    importt(context, Arrays.asList(content.split("\\r?\\n")));
  }

  public static void importt(MatrixContext<String, String> context, Iterable<String> lines) {
    int linenumber = 0;
    int objectCount = 0;
    int attributeCount = 0;
    List<String> objects = new LinkedList<String>();
    List<String> attributes = new LinkedList<String>();
    BooleanMatrix matrix = null;
    for (String line : lines) {
      if (line != null && !line.startsWith("EOF")) {
        if (linenumber == 2) {
          objectCount = Integer.parseInt(line);
          System.out.println(objectCount + " objects");
        }
        if (linenumber == 3) {
          attributeCount = Integer.parseInt(line);
          System.out.println(attributeCount + " attributes");
        }
        if (linenumber >= 5 && linenumber < 5 + objectCount)
          objects.add(line);
        if (linenumber == 5 + objectCount)
          context.rowHeads().addAll(objects);
        if (linenumber >= 5 + objectCount && linenumber < 5 + objectCount + attributeCount)
          attributes.add(line);
        if (linenumber == 5 + objectCount + attributeCount) {
          context.colHeads().addAll(attributes);
          matrix = context.matrix();
        }
        if (linenumber >= 5 + objectCount + attributeCount
            && linenumber < 5 + objectCount + attributeCount + objectCount) {
          int row = linenumber - (5 + objectCount + attributeCount);
          int column = 0;
          for (char c : line.toCharArray()) {
            if (c == 'X')
              matrix.setBoolean(true, row, column);
            column++;
          }
        }
        linenumber++;
      }
    }
    context.pushAllChangedEvent();
  }
}
