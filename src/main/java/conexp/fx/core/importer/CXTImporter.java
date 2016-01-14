package conexp.fx.core.importer;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.io.File;
import java.util.Iterator;

import org.ujmp.core.booleanmatrix.BooleanMatrix;
import org.ujmp.core.exceptions.MatrixException;

import conexp.fx.core.collections.setlist.HashSetArrayList;
import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.util.IterableFile;

public class CXTImporter {

  public static final MatrixContext<String, String> read(final File file) throws Exception {
    final MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    read(cxt, file);
    return cxt;
  }

  public static final void read(final MatrixContext<String, String> context, final File file) throws Exception {
    try {
      if (!context.id.isBound())
        context.id.set(file.getName());
      final Iterator<String> lineIterator = new IterableFile(file).iterator();
      final String[] firstLines = new String[5];
      int i = 0;
      while (lineIterator.hasNext() && i < 5) {
        firstLines[i++] = lineIterator.next();
      }
      final int rows = Integer.valueOf(firstLines[2]);
      final int cols = Integer.valueOf(firstLines[3]);
      final SetList<String> objs = new HashSetArrayList<String>();
      final SetList<String> atts = new HashSetArrayList<String>();

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
    } catch (NumberFormatException | MatrixException e) {
      throw new Exception("Could not read formal context from " + file, e);
    }
  }
}
