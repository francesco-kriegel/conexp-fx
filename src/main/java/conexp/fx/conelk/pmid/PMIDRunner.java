package conexp.fx.conelk.pmid;

import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import conexp.fx.conelk.TBoxer;

public class PMIDRunner {

  public static final void main(String[] args) throws OWLOntologyCreationException {
    Map<String, OWLOntology> tBoxes = new TBoxer(new PMIDDataset()).getAllTBoxes();
    for (Entry<String, OWLOntology> entry : tBoxes.entrySet()) {
      System.out.println(entry.getValue());
    }
  }

}
