package conexp.fx.core.importer;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.util.IterableFile;

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
    final Iterator<String> it = new IterableFile(file).iterator();
    while (it.hasNext()) {
      final String[] tuple = it.next().split(delimiter);
      if (tuple.length > 0)
        tuples.add(tuple);
    }
  }

  public final List<String[]> getTuples() {
    return tuples;
  }

  public final MatrixContext<String, String> toContext() throws Exception {
    MatrixContext<String, String> context = new MatrixContext<String, String>(false);
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
    return context;
  }
}
