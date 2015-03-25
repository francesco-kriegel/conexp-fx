package conexp.fx.core.dl;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    signature.addConceptNames("A", "B", "C");
    signature.addRoleNames("r", "s");
    signature.addIndividualNames("d", "e", "f");
    i = new ELInterpretation(signature);
    i.addConceptNameAssertion("A", "d");
    i.addConceptNameAssertion("A", "e");
    i.addConceptNameAssertion("B", "d");
    i.addConceptNameAssertion("B", "f");
    i.addConceptNameAssertion("C", "f");
    i.addRoleNameAssertion("r", "d", "e");
    i.addRoleNameAssertion("r", "d", "f");
    i.addRoleNameAssertion("r", "e", "f");
    i.addRoleNameAssertion("s", "d", "e");
    i.updateSuccessorSets();
  }

  @Test
  public void testGetMostSpecificConcept() {
    final ELConceptDescription expected_d0 = new ELConceptDescription();
    expected_d0.getConceptNames().addAll(Collections2.transform(Sets.newHashSet("A", "B"), IRI::create));
    Assert.assertEquals(expected_d0, i.getMostSpecificConcept(IRI.create("d"), 0));
    final ELConceptDescription expected_e0 = new ELConceptDescription();
    expected_e0.getConceptNames().addAll(Collections2.transform(Sets.newHashSet("A"), IRI::create));
    Assert.assertEquals(expected_e0, i.getMostSpecificConcept(IRI.create("e"), 0));
    final ELConceptDescription expected_f0 = new ELConceptDescription();
    expected_f0.getConceptNames().addAll(Collections2.transform(Sets.newHashSet("B", "C"), IRI::create));
    Assert.assertEquals(expected_f0, i.getMostSpecificConcept(IRI.create("f"), 0));
    final ELConceptDescription expected_d1 = expected_d0.clone();
    expected_d1.getExistentialRestrictions().add(Pair.of(IRI.create("r"), expected_e0));
    expected_d1.getExistentialRestrictions().add(Pair.of(IRI.create("r"), expected_f0));
    expected_d1.getExistentialRestrictions().add(Pair.of(IRI.create("s"), expected_e0));
    Assert.assertEquals(expected_d1, i.getMostSpecificConcept(IRI.create("d"), 1));
    final ELConceptDescription expected_e1 = expected_e0.clone();
    expected_e1.getExistentialRestrictions().add(Pair.of(IRI.create("r"), expected_f0));
    Assert.assertEquals(expected_e1, i.getMostSpecificConcept(IRI.create("e"), 1));
    Assert.assertEquals(expected_f0, i.getMostSpecificConcept(IRI.create("f"), 1));
    final ELConceptDescription expected_d2 = expected_d0.clone();
    expected_d2.getExistentialRestrictions().add(Pair.of(IRI.create("r"), expected_e1));
    expected_d2.getExistentialRestrictions().add(Pair.of(IRI.create("r"), expected_f0));
    expected_d2.getExistentialRestrictions().add(Pair.of(IRI.create("s"), expected_e1));
    for (int roleDepth = 2; roleDepth < 10; roleDepth++) {
      Assert.assertEquals(expected_d2, i.getMostSpecificConcept(IRI.create("d"), roleDepth));
      Assert.assertEquals(expected_e1, i.getMostSpecificConcept(IRI.create("e"), roleDepth));
      Assert.assertEquals(expected_f0, i.getMostSpecificConcept(IRI.create("f"), roleDepth));
    }
  }

  @Test
  public void testIsInstanceOf() {
    final ELConceptDescription c = new ELConceptDescription();
    c.getConceptNames().add(IRI.create("A"));
    Assert.assertTrue(i.isInstanceOf(IRI.create("d"), c));
    Assert.assertTrue(i.isInstanceOf(IRI.create("e"), c));
    Assert.assertFalse(i.isInstanceOf(IRI.create("f"), c));
    c.getExistentialRestrictions().add(Pair.of(IRI.create("r"), c.clone()));
    Assert.assertTrue(i.isInstanceOf(IRI.create("d"), c));
  }

}