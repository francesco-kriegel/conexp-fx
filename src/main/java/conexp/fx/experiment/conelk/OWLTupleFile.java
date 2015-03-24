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

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import conexp.fx.core.importer.CSVImporter;

public abstract class OWLTupleFile {

  public final File   file;
  public final String delimiter;

  public OWLTupleFile(final File file) {
    this(file, ",");
  }

  public OWLTupleFile(final File file, final String delimiter) {
    super();
    this.file = file;
    this.delimiter = delimiter;
  }

  public abstract Iterable<OWLAxiom> convert(String[] tuple, OWLDataFactory owlDataFactory);

  public void addTo(final OWLOntologyManager ontologyManager, final OWLOntology ontology) {
    for (String[] tuple : CSVImporter.getTuples(file, delimiter))
      for (OWLAxiom axiom : convert(tuple, ontologyManager.getOWLDataFactory()))
        ontologyManager.addAxiom(ontology, axiom);
  }

}
