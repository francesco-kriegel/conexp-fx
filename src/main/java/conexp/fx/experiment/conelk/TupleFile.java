package conexp.fx.experiment.conelk;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
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
