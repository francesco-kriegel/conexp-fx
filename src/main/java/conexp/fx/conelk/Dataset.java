package conexp.fx.conelk;

import java.util.Map;

import org.semanticweb.owlapi.model.OWLOntology;

public abstract class Dataset {

  public abstract String getName();

  public abstract Map<String, OWLOntology> getData();

}
