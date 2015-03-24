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

import conexp.fx.core.collections.pair.Pair;

public class ELLeastCommonSubsumerTest {

  @Test
  public void test() {
    final ELConceptDescription c1 = new ELConceptDescription(Sets.newHashSet(IRI.create("A"), IRI.create("B")), Sets.newHashSet());
    final ELConceptDescription c2 = new ELConceptDescription(Sets.newHashSet(IRI.create("A")), Sets.newHashSet());
    final ELConceptDescription c3 = new ELConceptDescription(Sets.newHashSet(IRI.create("B")), Sets.newHashSet());

    Assert.assertEquals(ELConceptDescription.top(), ELLeastCommonSubsumer._of(c1, c2, c3));
    Assert.assertEquals(c2, ELLeastCommonSubsumer._of(c1, c2));
    Assert.assertEquals(c3, ELLeastCommonSubsumer._of(c1, c3));

    final ELConceptDescription d1 =
        new ELConceptDescription(Sets.newHashSet(IRI.create("A"), IRI.create("B")), Sets.newHashSet(
            Pair.of(IRI.create("r"), c1),
            Pair.of(IRI.create("s"), c2)));
    final ELConceptDescription d2 =
        new ELConceptDescription(
            Sets.newHashSet(IRI.create("C"), IRI.create("B")),
            Sets.newHashSet(Pair.of(IRI.create("r"), c3)));
    final ELConceptDescription d3 =
        new ELConceptDescription(Sets.newHashSet(IRI.create("B")), Sets.newHashSet(Pair.of(IRI.create("r"), c3)));
    
    // computing lcs of
    //           (A and B       and ex r (A and B) and ex s A) 
    // and       (      B and C and ex r        B            )
    // expecting (      B       and ex r        B            )
    Assert.assertEquals(d3, ELLeastCommonSubsumer._of(d1, d2));

  }
}
