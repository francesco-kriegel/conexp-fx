package conexp.fx.experiment.conelk;

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
