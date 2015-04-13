package conexp.fx.core.dl;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class ELTBox {

  private final Set<ELConceptInclusion> gcis;

  public ELTBox() {
    super();
    this.gcis = new HashSet<ELConceptInclusion>();
  }

  public final Set<ELConceptInclusion> getGCIs() {
    return gcis;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof ELTBox))
      return false;
    final ELTBox other = (ELTBox) obj;
    return this.gcis.equals(other.gcis);
  }

  public final OWLOntology toOWLOntology() {
    try {
      final OWLOntologyManager om = OWLManager.createOWLOntologyManager();
      final OWLDataFactory df = om.getOWLDataFactory();
      final OWLOntology ontology = om.createOntology();
      gcis.parallelStream().forEach(
          gci -> om.applyChange(new AddAxiom(ontology, df.getOWLSubClassOfAxiom(gci
              .getSubsumee()
              .minimize()
              .toOWLClassExpression(), gci.getSubsumer().minimize().toOWLClassExpression()))));
      return ontology;
    } catch (OWLOntologyCreationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int hashCode() {
    return 23 * gcis.hashCode() + 99;
  }

  @Override
  public String toString() {
    return "EL-TBox " + gcis.toString();
  }

}
