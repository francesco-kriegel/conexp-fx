package conexp.fx.core.importer;

import java.io.BufferedReader;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2019 Francesco Kriegel
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
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import conexp.fx.core.context.MatrixContext;

public final class CSVImporter {

  public static final List<String[]> getTuples(final File file) {
    final CSVImporter csvImporter = new CSVImporter(file);
    csvImporter.readFile();
    return csvImporter.getTuples();
  }

  public static final List<String[]> getTuples(final File file, final String delimiter) {
    final CSVImporter csvImporter = new CSVImporter(file, delimiter);
    csvImporter.readFile();
    return csvImporter.getTuples();
  }

  public static final void importContext(
      final File file,
      final MatrixContext<String, String> context,
      final String delimiter) throws Exception {
    final CSVImporter csvImporter = new CSVImporter(file, delimiter);
    csvImporter.readFile();
    csvImporter.toContext2(context);
  }

  private final File           file;
  private final String         delimiter;
  private final List<String[]> tuples;

  public CSVImporter(final File file) {
    this(file, ",");
  }

  public CSVImporter(final File file, final String delimiter) {
    super();
    this.file = file;
    this.delimiter = delimiter;
    this.tuples = new LinkedList<String[]>();
  }

  public final void readFile() {
    try {
      final BufferedReader reader = new BufferedReader(new FileReader(file));
      final Iterator<String> it = reader.lines().iterator();
      while (it.hasNext()) {
        final String[] tuple = it.next().split(delimiter);
        if (tuple.length > 0)
          tuples.add(tuple);
      }
      reader.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public final List<String[]> getTuples() {
    return tuples;
  }

  public final void toContext2(final MatrixContext<String, String> context) {
    for (String[] row : tuples)
      context.add(row[0], row[1]);
  }

  public final void toContext(final MatrixContext<String, String> context) throws Exception {
    final String[][] array = tuples.toArray(new String[][] {});
    final int rows = array.length - 1;
    final int cols = array[0].length - 2;
    final List<String> objects = new LinkedList<String>();
    final List<String> attributes = new LinkedList<String>();
    for (int r = 1; r < rows + 1; r++)
      objects.add("(" + array[r][0] + "," + array[r][1] + ")");
    final String[] header = array[0];
    for (int c = 2; c < cols + 2; c++) {
      if (attributes.contains(header[c]))
        throw new Exception("Duplicate attribute name.");
      attributes.add(header[c]);
    }
    context.rowHeads().addAll(objects);
    context.colHeads().addAll(attributes);
    for (int r = 1; r < rows + 1; r++)
      for (int c = 2; c < cols + 2; c++)
        if (Integer.valueOf(array[r][c].trim()).equals(1))
          context.matrix().setBoolean(true, r - 1, c - 2);
  }

  public final MatrixContext<String, String> toContext() throws Exception {
    MatrixContext<String, String> context = new MatrixContext<String, String>(false);
    toContext(context);
    return context;
  }
}
