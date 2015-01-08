package conexp.fx.experiment.conelk;

import java.io.File;

import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import conexp.fx.core.importer.CSVImporter;

public abstract class TupleFile {

  public final File   file;
  public final String delimiter;

  public TupleFile(final File file) {
    this(file, ",");
  }

  public TupleFile(final File file, final String delimiter) {
    super();
    this.file = file;
    this.delimiter = delimiter;
  }

  public abstract Iterable<Statement> convert(String[] tuple);

  public void addTo(final RepositoryConnection connection) throws RepositoryException {
    for (String[] tuple : CSVImporter.getTuples(file, delimiter))
      connection.add(convert(tuple));
  }

}
