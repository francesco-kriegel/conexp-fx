package conexp.fx.core.importer;

import java.io.BufferedReader;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ujmp.core.booleanmatrix.BooleanMatrix;

import conexp.fx.core.context.MatrixContext;

public class CXTImporter {

  public static final MatrixContext<String, String> read(final File file) throws IOException {
    final MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    read(cxt, file);
    return cxt;
  }

  public static final void read(final MatrixContext<String, String> context, final File file) throws IOException {
    if (!context.id.isBound())
      context.id.set(file.getName());
    final BufferedReader reader = new BufferedReader(new FileReader(file));
    final Iterator<String> lineIterator = reader.lines().iterator();
    final String[] firstLines = new String[5];
    int i = 0;
    while (lineIterator.hasNext() && i < 5) {
      firstLines[i++] = lineIterator.next();
    }
    final int rows = Integer.valueOf(firstLines[2]);
    final int cols = Integer.valueOf(firstLines[3]);
    final List<String> objs = new ArrayList<String>(rows);
    final List<String> atts = new ArrayList<String>(cols);

    while (lineIterator.hasNext() && i < 5 + rows) {
      objs.add(lineIterator.next());
      i++;
    }
    while (lineIterator.hasNext() && i < 5 + rows + cols) {
      atts.add(lineIterator.next());
      i++;
    }

    context.rowHeads().addAll(objs);
    context.colHeads().addAll(atts);

    final BooleanMatrix mat = context.matrix();// BooleanMatrix2D.factory.zeros(rows, cols);

    while (lineIterator.hasNext() && i < 5 + rows + cols + rows) {
      final char[] line = lineIterator.next().toCharArray();
      final int row = i - 5 - rows - cols;
      for (int col = 0; col < cols; col++)
        mat.setBoolean(line[col] == 'X' || line[col] == 'x', row, col);
      i++;
    }
    reader.close();
  }
}
