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

import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.Sets;

public class ELReasonerTest {

  @Test
  public void test() {
    final ELConceptDescription ab1 = new ELConceptDescription(Sets.newHashSet(IRI.create("A"), IRI.create("B")), Sets.newHashSet());
    final ELConceptDescription a1 = new ELConceptDescription(Sets.newHashSet(IRI.create("A")), Sets.newHashSet());
    final ELConceptDescription b1 = new ELConceptDescription(Sets.newHashSet(IRI.create("B")), Sets.newHashSet());

    final ELConceptDescription ab2 = new ELConceptDescription(Sets.newHashSet(IRI.create("A"), IRI.create("B")), Sets.newHashSet());
    final ELConceptDescription a2 = new ELConceptDescription(Sets.newHashSet(IRI.create("A")), Sets.newHashSet());
    final ELConceptDescription b2 = new ELConceptDescription(Sets.newHashSet(IRI.create("B")), Sets.newHashSet());

    Assert.assertTrue(ELReasoner.isSubsumedBy(ab1, a1));
    Assert.assertTrue(ELReasoner.isSubsumedBy(ab1, b1));
    Assert.assertTrue(ELReasoner.isSubsumedBy(ab1, ab1));
    Assert.assertTrue(ELReasoner.isSubsumedBy(ab1, ab2));
    Assert.assertFalse(ELReasoner.isSubsumedBy(a1, ab1));
    Assert.assertFalse(ELReasoner.isSubsumedBy(b1, ab1));
  }

}
