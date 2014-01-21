package conexp.fx.core.importer;

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

import java.io.File;
import java.util.Iterator;

import org.ujmp.core.booleanmatrix.BooleanMatrix;

import conexp.fx.core.collections.setlist.HashSetArrayList;
import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.util.IterableFile;

public class CXTImporter2 {

  public static final void read(final MatrixContext<String, String> context, final File file) {
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
        mat.setBoolean(line[col] == 'X', row, col);
      i++;
    }
//    final MatrixContext<String, String> context = new MatrixContext<String, String>(objs, atts, mat, false);
//    System.out.println("done.");
//    return context;
  }
}
