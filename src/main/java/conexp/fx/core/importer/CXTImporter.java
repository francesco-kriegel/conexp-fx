package conexp.fx.core.importer;

import java.io.BufferedReader;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2023 Francesco Kriegel
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
