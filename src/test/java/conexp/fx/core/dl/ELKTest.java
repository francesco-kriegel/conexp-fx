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

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class ELKTest {

  public static void main(String[] args) throws OWLOntologyCreationException {
    final OWLDataFactory df = OWLManager.getOWLDataFactory();
    final OWLClassExpression concept1 = df.getOWLClass(IRI.create("class1"));
    final OWLClassExpression concept2 = df.getOWLClass(IRI.create("class2"));
    final OWLOntology ontology = createOntology();
    System.out.println(ELReasoner.isSubsumedBy(concept1, concept2, ontology));
  }

  private static final boolean testSubsumption() throws OWLOntologyCreationException {
    final OWLOntologyManager om = OWLManager.createOWLOntologyManager();
    final OWLDataFactory df = OWLManager.getOWLDataFactory();
    final OWLOntology ontology = createOntology();
    final OWLClass c1 = df.getOWLClass(IRI.create("dummy1"));
    final OWLClass c2 = df.getOWLClass(IRI.create("dummy2"));
    final OWLClassExpression concept1 = df.getOWLClass(IRI.create("class1"));
    final OWLClassExpression concept2 = df.getOWLClass(IRI.create("class2"));
    final OWLSubClassOfAxiom ax1 = df.getOWLSubClassOfAxiom(concept1, c1);
    final OWLSubClassOfAxiom _ax1 = df.getOWLSubClassOfAxiom(c1, concept1);
    final OWLSubClassOfAxiom ax2 = df.getOWLSubClassOfAxiom(c2, concept2);
    final OWLSubClassOfAxiom _ax2 = df.getOWLSubClassOfAxiom(concept2, c2);
    om.applyChange(new AddAxiom(ontology, ax1));
    om.applyChange(new AddAxiom(ontology, ax2));
    om.applyChange(new AddAxiom(ontology, _ax1));
    om.applyChange(new AddAxiom(ontology, _ax2));
    System.out.println(ontology);

    final OWLReasoner elk = new ElkReasonerFactory().createReasoner(ontology);
    elk.flush();
    elk.precomputeInferences(InferenceType.CLASS_HIERARCHY);
    final boolean result = elk.getSubClasses(c2, false).containsEntity(c1);
    elk.dispose();

    System.out.println(result);
    System.out.println();
    return result;
  }

  private static final OWLOntology createOntology() throws OWLOntologyCreationException {
    final OWLOntologyManager om = OWLManager.createOWLOntologyManager();
    final OWLDataFactory df = OWLManager.getOWLDataFactory();
    final OWLOntology ontology = om.createOntology();
    final OWLClassExpression concept1 = df.getOWLClass(IRI.create("class1"));
    final OWLClassExpression concept2 = df.getOWLClass(IRI.create("class2"));
    final OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(concept1, concept2);
    om.applyChange(new AddAxiom(ontology, ax));
    return ontology;
  }

}
