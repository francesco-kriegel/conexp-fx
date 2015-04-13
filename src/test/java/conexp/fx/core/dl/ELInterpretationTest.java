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

import junit.framework.Assert;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

import conexp.fx.core.collections.pair.Pair;

public class ELInterpretationTest {

  private final Signature        signature;
  private final ELInterpretation i;

  public ELInterpretationTest() {
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
    i = new ELInterpretation(signature);
    i.addConceptNameAssertion(
        "A",
        "d");
    i.addConceptNameAssertion(
        "A",
        "e");
    i.addConceptNameAssertion(
        "B",
        "d");
    i.addConceptNameAssertion(
        "B",
        "f");
    i.addConceptNameAssertion(
        "C",
        "f");
    i.addRoleNameAssertion(
        "r",
        "d",
        "e");
    i.addRoleNameAssertion(
        "r",
        "d",
        "f");
    i.addRoleNameAssertion(
        "r",
        "e",
        "f");
    i.addRoleNameAssertion(
        "s",
        "d",
        "e");
    i.updateSuccessorSets();
  }

  @Test
  public void testGetMostSpecificConcept() {
    final ELConceptDescription expected_d0 = new ELConceptDescription();
    expected_d0.getConceptNames().addAll(
        Collections2.transform(
            Sets.newHashSet(
                "A",
                "B"),
            IRI::create));
    Assert.assertEquals(
        expected_d0,
        i.getMostSpecificConcept(
            IRI.create("d"),
            0,
            0));
    final ELConceptDescription expected_e0 = new ELConceptDescription();
    expected_e0.getConceptNames().addAll(
        Collections2.transform(
            Sets.newHashSet("A"),
            IRI::create));
    Assert.assertEquals(
        expected_e0,
        i.getMostSpecificConcept(
            IRI.create("e"),
            0,
            0));
    final ELConceptDescription expected_f0 = new ELConceptDescription();
    expected_f0.getConceptNames().addAll(
        Collections2.transform(
            Sets.newHashSet(
                "B",
                "C"),
            IRI::create));
    Assert.assertEquals(
        expected_f0,
        i.getMostSpecificConcept(
            IRI.create("f"),
            0,
            0));
    final ELConceptDescription expected_d1 = expected_d0.clone();
    expected_d1.getExistentialRestrictions().add(
        Pair.of(
            IRI.create("r"),
            expected_e0));
    expected_d1.getExistentialRestrictions().add(
        Pair.of(
            IRI.create("r"),
            expected_f0));
    expected_d1.getExistentialRestrictions().add(
        Pair.of(
            IRI.create("s"),
            expected_e0));
    Assert.assertEquals(
        expected_d1,
        i.getMostSpecificConcept(
            IRI.create("d"),
            0,
            1));
    final ELConceptDescription expected_e1 = expected_e0.clone();
    expected_e1.getExistentialRestrictions().add(
        Pair.of(
            IRI.create("r"),
            expected_f0));
    Assert.assertEquals(
        expected_e1,
        i.getMostSpecificConcept(
            IRI.create("e"),
            0,
            1));
    Assert.assertEquals(
        expected_f0,
        i.getMostSpecificConcept(
            IRI.create("f"),
            0,
            1));
    final ELConceptDescription expected_d2 = expected_d0.clone();
    expected_d2.getExistentialRestrictions().add(
        Pair.of(
            IRI.create("r"),
            expected_e1));
    expected_d2.getExistentialRestrictions().add(
        Pair.of(
            IRI.create("r"),
            expected_f0));
    expected_d2.getExistentialRestrictions().add(
        Pair.of(
            IRI.create("s"),
            expected_e1));
    for (int roleDepth = 2; roleDepth < 10; roleDepth++) {
      Assert.assertEquals(
          expected_d2,
          i.getMostSpecificConcept(
              IRI.create("d"),
              roleDepth,
              0));
      Assert.assertEquals(
          expected_e1,
          i.getMostSpecificConcept(
              IRI.create("e"),
              roleDepth,
              0));
      Assert.assertEquals(
          expected_f0,
          i.getMostSpecificConcept(
              IRI.create("f"),
              roleDepth,
              0));
    }
  }

  @Test
  public void testIsInstanceOf() {
    final ELConceptDescription c = new ELConceptDescription();
    c.getConceptNames().add(
        IRI.create("A"));
    Assert.assertTrue(i.isInstanceOf(
        IRI.create("d"),
        c));
    Assert.assertTrue(i.isInstanceOf(
        IRI.create("e"),
        c));
    Assert.assertFalse(i.isInstanceOf(
        IRI.create("f"),
        c));
    c.getExistentialRestrictions().add(
        Pair.of(
            IRI.create("r"),
            c.clone()));
    Assert.assertTrue(i.isInstanceOf(
        IRI.create("d"),
        c));
  }

}
