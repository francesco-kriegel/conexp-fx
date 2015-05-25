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

import java.util.Collections;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.google.common.collect.Sets;

import conexp.fx.core.util.OWLMinimizer;
import conexp.fx.core.util.OWLtoString;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OWLInterpretationTest {

  private final Signature         signature;
  private final OWLInterpretation i;
  private final OWLDataFactory    df = OWLManager.getOWLDataFactory();
  private final OWLReasoner       reasoner;

  public OWLInterpretationTest() throws OWLOntologyCreationException {
    super();
    signature = new Signature(null);
    signature.addConceptNames(
        "A",
        "B",
        "C");
    signature.addRoleNames(
        "r",
        "s");
    signature.addIndividualNames(
        "d",
        "e",
        "f");
    i = new OWLInterpretation(signature);
    i.addConceptNameAssertion(
        "A",
        "d");
    i.addConceptNameAssertion(
        "A",
        "e");
    i.addConceptNameAssertion(
        "B",
        "e");
    i.addConceptNameAssertion(
        "C",
        "f");
    i.addRoleNameAssertion(
        "r",
        "d",
        "d");
    i.addRoleNameAssertion(
        "r",
        "d",
        "e");
    i.addRoleNameAssertion(
        "s",
        "e",
        "f");
    i.addRoleNameAssertion(
        "s",
        "f",
        "d");
    final OWLOntologyManager oman = OWLManager.createOWLOntologyManager();
    final OWLOntology onto = oman.createOntology();
    reasoner = new Reasoner(onto);
  }

  @Test
  public void testA_IsInstanceOf() {
    final OWLClassExpression c1 = df.getOWLObjectHasSelf(df.getOWLObjectProperty(IRI.create("r")));
    Assert.assertTrue(i.isInstanceOf(
        IRI.create("d"),
        c1));
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("e"),
        c1));
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("f"),
        c1));
    final OWLClassExpression c2 = df.getOWLObjectSomeValuesFrom(
        df.getOWLObjectProperty(IRI.create("s")),
        c1);
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("d"),
        c2));
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("e"),
        c2));
    Assert.assertTrue(i.isInstanceOf(
        IRI.create("f"),
        c2));
    final OWLClassExpression c3 = df.getOWLObjectAllValuesFrom(
        df.getOWLObjectProperty(IRI.create("s")),
        df.getOWLClass(IRI.create("C")));
    Assert.assertTrue(i.isInstanceOf(
        IRI.create("d"),
        c3));
    Assert.assertTrue(i.isInstanceOf(
        IRI.create("e"),
        c3));
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("f"),
        c3));
    final OWLClassExpression c4 = df.getOWLObjectIntersectionOf(
        df.getOWLClass(IRI.create("A")),
        df.getOWLClass(IRI.create("B")));
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("d"),
        c4));
    Assert.assertTrue(i.isInstanceOf(
        IRI.create("e"),
        c4));
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("f"),
        c4));
    final OWLClassExpression c5 = df.getOWLObjectMinCardinality(
        2,
        df.getOWLObjectProperty(IRI.create("r")),
        df.getOWLClass(IRI.create("A")));
    Assert.assertTrue(i.isInstanceOf(
        IRI.create("d"),
        c5));
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("e"),
        c5));
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("f"),
        c5));
    final OWLClassExpression c6 = df.getOWLObjectMaxCardinality(
        1,
        df.getOWLObjectProperty(IRI.create("s")),
        df.getOWLClass(IRI.create("C")));
    Assert.assertTrue(i.isInstanceOf(
        IRI.create("d"),
        c6));
    Assert.assertTrue(i.isInstanceOf(
        IRI.create("e"),
        c6));
    Assert.assertTrue(i.isInstanceOf(
        IRI.create("f"),
        c6));
    final OWLClassExpression c7 = df.getOWLObjectExactCardinality(
        2,
        df.getOWLObjectProperty(IRI.create("r")),
        df.getOWLClass(IRI.create("A")));
    Assert.assertTrue(i.isInstanceOf(
        IRI.create("d"),
        c7));
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("e"),
        c7));
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("f"),
        c7));
    final OWLClassExpression c8 = df.getOWLObjectMinCardinality(
        1,
        df.getOWLObjectProperty(IRI.create("r")),
        df.getOWLClass(IRI.create("A")));
    Assert.assertTrue(i.isInstanceOf(
        IRI.create("d"),
        c8));
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("e"),
        c8));
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("f"),
        c8));
    final OWLClassExpression c9 = df.getOWLObjectMinCardinality(
        3,
        df.getOWLObjectProperty(IRI.create("r")),
        df.getOWLClass(IRI.create("A")));
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("d"),
        c9));
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("e"),
        c9));
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("f"),
        c9));
  }

  @Test
  public void testB_GetMostSpecificConcept() {

    final OWLClassExpression mmsc1 = i.getMostSpecificConceptALQ(
        IRI.create("d"),
        0,
        0);
    final OWLClassExpression expected1 = df.getOWLClass(IRI.create("A"));
    Assert.assertEquals(
        expected1,
        mmsc1);

    final OWLClassExpression mmsc2 = i.getMostSpecificConceptALQ(
        IRI.create("d"),
        0,
        0,
        Constructor.PRIMITIVE_NEGATION);
    final OWLClassExpression expected2 = df.getOWLObjectIntersectionOf(
        df.getOWLClass(IRI.create("A")),
        df.getOWLObjectComplementOf(df.getOWLClass(IRI.create("B"))),
        df.getOWLObjectComplementOf(df.getOWLClass(IRI.create("C"))));
    Assert.assertEquals(
        expected2,
        mmsc2);

    final OWLClassExpression mmsc3 = i.getMostSpecificConceptALQ(
        IRI.create("d"),
        1,
        0,
        Constructor.EXISTENTIAL_SELF_RESTRICTION);
    final OWLClassExpression expected3 = df.getOWLObjectIntersectionOf(
        df.getOWLClass(IRI.create("A")),
        df.getOWLObjectHasSelf(df.getOWLObjectProperty(IRI.create("r"))));
    Assert.assertEquals(
        expected3,
        mmsc3);

    final OWLClassExpression mmsc4 = i.getMostSpecificConceptALQ(
        IRI.create("d"),
        1,
        0,
        Constructor.EXISTENTIAL_RESTRICTION,
        Constructor.EXISTENTIAL_SELF_RESTRICTION);
    final OWLClassExpression expected4 = df.getOWLObjectIntersectionOf(
        df.getOWLClass(IRI.create("A")),
        df.getOWLObjectHasSelf(df.getOWLObjectProperty(IRI.create("r"))),
        df.getOWLObjectSomeValuesFrom(
            df.getOWLObjectProperty(IRI.create("r")),
            df.getOWLObjectIntersectionOf(
                df.getOWLClass(IRI.create("A")),
                df.getOWLClass(IRI.create("B")))));
    Assert.assertTrue(reasoner.isEntailed(df.getOWLEquivalentClassesAxiom(
        expected4,
        mmsc4)));

    final OWLClassExpression expected5 = df.getOWLObjectIntersectionOf(
        df.getOWLClass(IRI.create("A")),
        df.getOWLClass(IRI.create("B")),
//            df.getOWLObjectComplementOf(df.getOWLClass(IRI.create("C"))),
        df.getOWLObjectSomeValuesFrom(
            df.getOWLObjectProperty(IRI.create("s")),
            df.getOWLClass(IRI.create("C"))),
        df.getOWLObjectAllValuesFrom(
            df.getOWLObjectProperty(IRI.create("s")),
            df.getOWLClass(IRI.create("C"))),
        df.getOWLObjectAllValuesFrom(
            df.getOWLObjectProperty(IRI.create("r")),
            df.getOWLNothing()));
    final OWLClassExpression mmsc5 = i.getMostSpecificConceptALQ(
        IRI.create("e"),
        1,
        0,
        Constructor.EXISTENTIAL_RESTRICTION,
        Constructor.EXISTENTIAL_SELF_RESTRICTION,
        Constructor.VALUE_RESTRICTION);
    Assert.assertTrue(reasoner.isEntailed(df.getOWLEquivalentClassesAxiom(
        expected5,
        mmsc5)));

    final OWLClassExpression expected5a = df.getOWLObjectIntersectionOf(
        df.getOWLClass(IRI.create("A")),
        df.getOWLClass(IRI.create("B")),
        df.getOWLObjectComplementOf(df.getOWLClass(IRI.create("C"))),
        df.getOWLObjectSomeValuesFrom(
            df.getOWLObjectProperty(IRI.create("s")),
            df.getOWLObjectIntersectionOf(
                df.getOWLClass(IRI.create("C")),
                df.getOWLObjectComplementOf(df.getOWLClass(IRI.create("A"))),
                df.getOWLObjectComplementOf(df.getOWLClass(IRI.create("B"))))),
        df.getOWLObjectAllValuesFrom(
            df.getOWLObjectProperty(IRI.create("s")),
            df.getOWLObjectIntersectionOf(
                df.getOWLClass(IRI.create("C")),
                df.getOWLObjectComplementOf(df.getOWLClass(IRI.create("A"))),
                df.getOWLObjectComplementOf(df.getOWLClass(IRI.create("B"))))),
        df.getOWLObjectAllValuesFrom(
            df.getOWLObjectProperty(IRI.create("r")),
            df.getOWLNothing()));
    final OWLClassExpression mmsc5a = i.getMostSpecificConceptALQ(
        IRI.create("e"),
        1,
        0,
        Constructor.PRIMITIVE_NEGATION,
        Constructor.EXISTENTIAL_RESTRICTION,
        Constructor.EXISTENTIAL_SELF_RESTRICTION,
        Constructor.VALUE_RESTRICTION);
    Assert.assertTrue(reasoner.isEntailed(df.getOWLEquivalentClassesAxiom(
        expected5a,
        mmsc5a)));

    final OWLClassExpression mmsc6 = i.getMostSpecificConceptALQ(
        Sets.newHashSet(
            IRI.create("d"),
            IRI.create("e")),
        0,
        0,
        Constructor.PRIMITIVE_NEGATION);
    final OWLClassExpression expected6 = df.getOWLObjectIntersectionOf(
        df.getOWLClass(IRI.create("A")),
        df.getOWLObjectComplementOf(df.getOWLClass(IRI.create("C"))));
    Assert.assertTrue(reasoner.isEntailed(df.getOWLEquivalentClassesAxiom(
        expected6,
        mmsc6)));

    final OWLClassExpression expected7 = df.getOWLThing();
    final OWLClassExpression mmsc7 = i.getMostSpecificConceptALQ(
        Sets.newHashSet(
            IRI.create("e"),
            IRI.create("f")),
        0,
        0,
        Constructor.PRIMITIVE_NEGATION);
    Assert.assertTrue(reasoner.isEntailed(df.getOWLEquivalentClassesAxiom(
        expected7,
        mmsc7)));

    Assert.assertEquals(
        Collections.singleton(Sets.newHashSet(
            IRI.create("d"),
            IRI.create("f"))),
        i.getSuccessorSetsER(
            Sets.newHashSet(
                IRI.create("e"),
                IRI.create("f")),
            IRI.create("s")));
    Assert.assertEquals(
        Sets.newHashSet(
            IRI.create("d"),
            IRI.create("f")),
        i.getAllSuccessors(
            Sets.newHashSet(
                IRI.create("e"),
                IRI.create("f")),
            IRI.create("s")));
    final OWLClassExpression mmsc9 = i.getMostSpecificConceptALQ(
        Sets.newHashSet(
            IRI.create("d"),
            IRI.create("f")),
        0,
        0,
        Constructor.PRIMITIVE_NEGATION);
    final OWLClassExpression expected9 = df.getOWLObjectComplementOf(df.getOWLClass(IRI.create("B")));
    Assert.assertEquals(
        expected9,
        mmsc9);

    final OWLClassExpression expected8 = df.getOWLObjectIntersectionOf(
        df.getOWLObjectSomeValuesFrom(
            df.getOWLObjectProperty(IRI.create("s")),
            df.getOWLObjectComplementOf(df.getOWLClass(IRI.create("B")))),
        df.getOWLObjectAllValuesFrom(
            df.getOWLObjectProperty(IRI.create("s")),
            df.getOWLObjectComplementOf(df.getOWLClass(IRI.create("B")))),
        df.getOWLObjectAllValuesFrom(
            df.getOWLObjectProperty(IRI.create("r")),
            df.getOWLNothing()));
    final OWLClassExpression mmsc8 = i.getMostSpecificConceptALQ(
        Sets.newHashSet(
            IRI.create("e"),
            IRI.create("f")),
        1,
        0,
        Constructor.PRIMITIVE_NEGATION,
        Constructor.EXISTENTIAL_RESTRICTION,
        Constructor.VALUE_RESTRICTION,
        Constructor.EXISTENTIAL_SELF_RESTRICTION);
    Assert.assertTrue(reasoner.isEntailed(df.getOWLEquivalentClassesAxiom(
        expected8,
        mmsc8)));
  }

  @Test
  public void testC_GetMostSpecificConcept2() {
    final OWLClassExpression mmsc1 = i.getMostSpecificConceptALQ(
        IRI.create("d"),
        1,
        4,
        Constructor.PRIMITIVE_NEGATION,
        Constructor.EXISTENTIAL_RESTRICTION,
        Constructor.EXISTENTIAL_SELF_RESTRICTION,
        Constructor.VALUE_RESTRICTION,
        Constructor.QUALIFIED_AT_LEAST_RESTRICTION,
        Constructor.UNQUALIFIED_AT_MOST_RESTRICTION);
    System.out.println(OWLtoString.toString(mmsc1));
    System.out.println(OWLtoString.toString(OWLMinimizer.minimizeConjunction(mmsc1)));
    System.out.println();
    final OWLClassExpression mmsc2 = i.getMostSpecificConceptALQ(
        IRI.create("e"),
        1,
        4,
        Constructor.PRIMITIVE_NEGATION,
        Constructor.EXISTENTIAL_RESTRICTION,
        Constructor.EXISTENTIAL_SELF_RESTRICTION,
        Constructor.VALUE_RESTRICTION,
        Constructor.QUALIFIED_AT_LEAST_RESTRICTION,
        Constructor.UNQUALIFIED_AT_MOST_RESTRICTION);
    System.out.println(OWLtoString.toString(mmsc2));
    System.out.println(OWLtoString.toString(OWLMinimizer.minimizeConjunction(mmsc2)));
    System.out.println();
    final OWLClassExpression mmsc3 = i.getMostSpecificConceptALQ(
        IRI.create("f"),
        1,
        4,
        Constructor.PRIMITIVE_NEGATION,
        Constructor.EXISTENTIAL_RESTRICTION,
        Constructor.EXISTENTIAL_SELF_RESTRICTION,
        Constructor.VALUE_RESTRICTION,
        Constructor.QUALIFIED_AT_LEAST_RESTRICTION,
        Constructor.UNQUALIFIED_AT_MOST_RESTRICTION);
    System.out.println(OWLtoString.toString(mmsc3));
    System.out.println(OWLtoString.toString(OWLMinimizer.minimizeConjunction(mmsc3)));
    System.out.println();
    final OWLClassExpression mmsc4 = i.getMostSpecificConceptALQ(
        Sets.newHashSet(
            IRI.create("d"),
            IRI.create("e")),
        1,
        4,
        Constructor.PRIMITIVE_NEGATION,
        Constructor.EXISTENTIAL_RESTRICTION,
        Constructor.EXISTENTIAL_SELF_RESTRICTION,
        Constructor.VALUE_RESTRICTION,
        Constructor.QUALIFIED_AT_LEAST_RESTRICTION,
        Constructor.UNQUALIFIED_AT_MOST_RESTRICTION);
    System.out.println(OWLtoString.toString(mmsc4));
    System.out.println(OWLtoString.toString(OWLMinimizer.minimizeConjunction(mmsc4)));
    System.out.println();
    final OWLClassExpression mmsc5 = i.getMostSpecificConceptALQ(
        Sets.newHashSet(
            IRI.create("d"),
            IRI.create("f")),
        1,
        4,
        Constructor.PRIMITIVE_NEGATION,
        Constructor.EXISTENTIAL_RESTRICTION,
        Constructor.EXISTENTIAL_SELF_RESTRICTION,
        Constructor.VALUE_RESTRICTION,
        Constructor.QUALIFIED_AT_LEAST_RESTRICTION,
        Constructor.UNQUALIFIED_AT_MOST_RESTRICTION);
    System.out.println(OWLtoString.toString(mmsc5));
    System.out.println(OWLtoString.toString(OWLMinimizer.minimizeConjunction(mmsc5)));
    System.out.println();
    final OWLClassExpression mmsc6 = i.getMostSpecificConceptALQ(
        Sets.newHashSet(
            IRI.create("e"),
            IRI.create("f")),
        1,
        4,
        Constructor.PRIMITIVE_NEGATION,
        Constructor.EXISTENTIAL_RESTRICTION,
        Constructor.EXISTENTIAL_SELF_RESTRICTION,
        Constructor.VALUE_RESTRICTION,
        Constructor.QUALIFIED_AT_LEAST_RESTRICTION,
        Constructor.UNQUALIFIED_AT_MOST_RESTRICTION);
    System.out.println(OWLtoString.toString(mmsc6));
    System.out.println(OWLtoString.toString(OWLMinimizer.minimizeConjunction(mmsc6)));
    System.out.println();
    final OWLClassExpression mmsc7 = i.getMostSpecificConceptALQ(
        i.getDomain(),
        1,
        4,
        Constructor.PRIMITIVE_NEGATION,
        Constructor.EXISTENTIAL_RESTRICTION,
        Constructor.EXISTENTIAL_SELF_RESTRICTION,
        Constructor.VALUE_RESTRICTION,
        Constructor.QUALIFIED_AT_LEAST_RESTRICTION,
        Constructor.UNQUALIFIED_AT_MOST_RESTRICTION);
    System.out.println(OWLtoString.toString(mmsc7));
    System.out.println(OWLtoString.toString(OWLMinimizer.minimizeConjunction(mmsc7)));
    System.out.println();
  }

  @Test
  public void testD_constructRandomInterpretation() {
    final OWLInterpretation j;
    final OWLClassExpression mmsc;
    j = randomInterpretation(
        1000,
        50,
        20,
        0.01f);
    mmsc = j.getMostSpecificConceptALQ(
        IRI.create("d0"),
        1,
        4,
//        Constructor.PRIMITIVE_NEGATION,
        Constructor.EXISTENTIAL_RESTRICTION,
        Constructor.EXISTENTIAL_SELF_RESTRICTION,
        Constructor.VALUE_RESTRICTION
//        Constructor.QUALIFIED_AT_LEAST_RESTRICTION,
//        Constructor.QUALIFIED_AT_MOST_RESTRICTION
        );
    System.out.println(OWLtoString.toString(mmsc));
    System.out.println(OWLtoString.toString(OWLMinimizer.minimizeConjunction(mmsc)));
    System.out.println();
  }

  public static final OWLInterpretation randomInterpretation(
      final int individuals,
      final int conceptNames,
      final int roleNames,
      final float density) {
    final OWLInterpretation i = new OWLInterpretation((IRI) null);

    for (int j = 0; j < individuals; j++)
      i.getDomain().add(
          IRI.create("d" + j));
    for (int c = 0; c < conceptNames; c++)
      i.getSignature().getConceptNames().add(
          IRI.create("A" + c));
    for (int r = 0; r < roleNames; r++)
      i.getSignature().getRoleNames().add(
          IRI.create("r" + r));
    final Random rng = new Random();
    for (IRI d : i.getDomain()) {
      for (IRI A : i.getSignature().getConceptNames())
        if (rng.nextFloat() < density)
          i.addConceptNameAssertion(
              A,
              d);
      for (IRI e : i.getDomain())
        for (IRI r : i.getSignature().getRoleNames())
          if (rng.nextFloat() < density)
            i.addRoleNameAssertion(
                r,
                d,
                e);
    }
    return i;
  }

}
