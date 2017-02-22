package conexp.fx.core.dl;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class ELReasoner {

  public static final boolean subsumes(final OWLClassExpression concept1, final OWLClassExpression concept2) {
    return isSubsumedBy(concept2, concept1);
  }

  public static final boolean subsumes(final ELConceptDescription concept1, final ELConceptDescription concept2) {
    return isSubsumedBy(concept2, concept1);
  }

  public static final boolean isSubsumedBy(final OWLClassExpression concept1, final OWLClassExpression concept2) {
    return isSubsumedBy(ELConceptDescription.of(concept1), ELConceptDescription.of(concept2));
  }

  /**
   * @param concept1
   * @param concept2
   * @return true, iff concept1 is subsumed by concept2 (w.r.t. empty TBox)
   */
  public static final boolean isSubsumedBy(final ELConceptDescription concept1, final ELConceptDescription concept2) {
    if (concept1.isBot())
      return true;
    if (concept2.isTop())
      return true;
    if (!concept1.getConceptNames().containsAll(concept2.getConceptNames()))
      return false;
    return concept2
        .getExistentialRestrictions()
        .parallelStream()
        .allMatch(
            existentialRestriction2 -> {
              return concept1
                  .getExistentialRestrictions()
                  .parallelStream()
                  .anyMatch(
                      existentialRestriction1 -> {
                        return existentialRestriction2.x().equals(existentialRestriction1.x())
                            && isSubsumedBy(existentialRestriction2.y(), existentialRestriction1.y());
                      });
            });
  }

  public static final boolean isSubsumedBy(final ELConceptDescription concept1, final ELConceptDescription concept2, final ELTBox tBox) {
    return isSubsumedBy(concept1.toOWLClassExpression(), concept2.toOWLClassExpression(), tBox.toOWLOntology());
  }

  private static int dummy = 1618;

  private static final int nextDummy() {
    return dummy++;
  }

  public static final boolean isSubsumedBy(
      final OWLClassExpression concept1,
      final OWLClassExpression concept2,
      final OWLOntology ontology) {
    final OWLOntologyManager om = OWLManager.createOWLOntologyManager();
    final OWLDataFactory df = OWLManager.getOWLDataFactory();

    final OWLClass c1 = df.getOWLClass(IRI.create("dummy" + nextDummy()));
    final OWLClass c2 = df.getOWLClass(IRI.create("dummy" + nextDummy()));
    final OWLSubClassOfAxiom ax1 = df.getOWLSubClassOfAxiom(concept1, c1);
    final OWLSubClassOfAxiom _ax1 = df.getOWLSubClassOfAxiom(c1, concept1);
    final OWLSubClassOfAxiom ax2 = df.getOWLSubClassOfAxiom(c2, concept2);
    final OWLSubClassOfAxiom _ax2 = df.getOWLSubClassOfAxiom(concept2, c2);
    om.applyChange(new AddAxiom(ontology, ax1));
    om.applyChange(new AddAxiom(ontology, ax2));
    om.applyChange(new AddAxiom(ontology, _ax1));
    om.applyChange(new AddAxiom(ontology, _ax2));

    final OWLReasoner elk = new ElkReasonerFactory().createReasoner(ontology);
    elk.flush();
    elk.precomputeInferences(InferenceType.CLASS_HIERARCHY);

    final boolean result =
        elk.getEquivalentClasses(c2).contains(c1) || elk.getSubClasses(c2, false).getFlattened().contains(c1);
    elk.dispose();
    om.applyChange(new RemoveAxiom(ontology, ax1));
    om.applyChange(new RemoveAxiom(ontology, ax2));
    om.applyChange(new RemoveAxiom(ontology, _ax1));
    om.applyChange(new RemoveAxiom(ontology, _ax2));
    return result;
//    return elk.isEntailed(df.getOWLSubClassOfAxiom(concept1, concept2));
  }

  public static final boolean isSubsumedBy(
      final ELConceptDescription concept1,
      final ELConceptDescription concept2,
      final OWLOntology ontology) {
    return isSubsumedBy(concept1.toOWLClassExpression(), concept2.toOWLClassExpression(), ontology);
  }

}
