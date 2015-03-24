package conexp.fx.experiment;

import java.io.File;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import conexp.fx.core.dl.ELInterpretation;
import conexp.fx.core.dl.ELTBox;

public class SimpsonsExample {

  public static final void main(String[] args) throws OWLOntologyCreationException {
    final ELInterpretation i = ELInterpretation.fromTriples(new File("test.nt"), null, "type");
    final ELTBox tBox = i.computeTBoxBase(1, null);
    tBox.getGCIs().forEach(System.out::println);
  }

}
