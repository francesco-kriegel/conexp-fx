package conexp.fx.conelk.pmid;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import conexp.fx.conelk.Dataset;
import conexp.fx.core.util.IterableFile;

public class PMIDDataset extends Dataset {

  private static final OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
  private static final OWLDataFactory     owlDataFactory  = ontologyManager.getOWLDataFactory();

  public PMIDDataset() {}

  @Override
  public final String getName() {
    return "PMID";
  }

  @Override
  public final Map<String, OWLOntology> getData() {
    try {
      final String basepath = "/Users/francesco/Downloads/Data4GCI.v3";
      final Map<String, OWLOntology> ontologies = new HashMap<String, OWLOntology>();
      final File folder = new File(basepath);
      for (File file : folder.listFiles(new FileFilter() {

        @Override
        public boolean accept(File file) {
          return !file.isDirectory();
        }
      })) {
        final IterableFile iterableFile = new IterableFile(file);
        final OWLOntology onto = ontologyManager.createOntology();
        ontologies.put(file.getName(), onto);
        for (String line : iterableFile) {
          String _line = line.trim();
          if (!_line.isEmpty()) {
            _line = _line.substring(0, _line.length() - 1);
            final String[] triple = _line.split("\\ ");
            if (triple.length == 3) {
              for (int i = 0; i < 3; i++)
                triple[i] = triple[i].substring(1, triple[i].length() - 1);
              if (triple[1].trim().equals("isa"))
                addClassAssertion(onto, triple[2], triple[0]);
              else
                addRoleAssertion(onto, triple[1], triple[0], triple[2]);
            }
          }
        }
      }
      return ontologies;
    } catch (OWLOntologyCreationException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static final void addClassAssertion(final OWLOntology ontology, final String clazz, final String individual) {
    final OWLClassAssertionAxiom ax =
        owlDataFactory.getOWLClassAssertionAxiom(
            owlDataFactory.getOWLClass(IRI.create("", clazz)),
            owlDataFactory.getOWLNamedIndividual(IRI.create("", individual)));
//    System.out.println(ax);
    ontologyManager.applyChange(new AddAxiom(ontology, ax));
  }

  private static final void addRoleAssertion(
      final OWLOntology ontology,
      final String role,
      final String individual1,
      final String individual2) {
    final OWLObjectPropertyAssertionAxiom ax =
        owlDataFactory.getOWLObjectPropertyAssertionAxiom(
            owlDataFactory.getOWLObjectProperty(IRI.create("", role)),
            owlDataFactory.getOWLNamedIndividual(IRI.create("", individual1)),
            owlDataFactory.getOWLNamedIndividual(IRI.create("", individual2)));
//    System.out.println(ax);
    ontologyManager.applyChange(new AddAxiom(ontology, ax));
  }

  private static final void addSubclassOfAssertion(
      final OWLOntology ontology,
      final OWLClassExpression subsumee,
      final OWLClassExpression subsumer) {
    ontologyManager.applyChange(new AddAxiom(ontology, owlDataFactory.getOWLSubClassOfAxiom(subsumee, subsumer)));
  }

}
