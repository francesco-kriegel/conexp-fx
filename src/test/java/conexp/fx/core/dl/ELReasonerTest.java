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
