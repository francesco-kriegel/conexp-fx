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

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.Sets;

public class OWLInterpretationTest2 {

  @Test
  public void testE_GetSuccessorSetsER() {
    long time1 = 0;
    long time2 = 0;
    long start;
    for (int n = 0; n < 10; n++) {
      final OWLInterpretation j = OWLInterpretationTest.randomInterpretation(
          20,
          10,
          10,
          0.15f);
      j.updateSuccessorSets();
      for (IRI roleName : j.getSignature().getRoleNames()) {
        for (Set<IRI> individuals : Sets.powerSet(j.getDomain()))
          if (!individuals.isEmpty()&& individuals.size()==5) {
            start = System.currentTimeMillis();
            final Set<Set<IRI>> expected = j.getSuccessorSetsER(
                individuals,
                roleName);
            time1 += System.currentTimeMillis() - start;
            start = System.currentTimeMillis();
            final Set<Set<IRI>> actual = j.getSuccessorSetsER2(
                individuals,
                roleName);
            time2 += System.currentTimeMillis() - start;
            try {
              Assert.assertEquals(
                  expected,
                  actual);
            } catch (AssertionError e) {
              System.out.println(expected);
              System.out.println(actual);
              throw e;
            }
          }
      }
    }
    System.out.println(" powerset method processing time: " + time1 + " ms");
    System.out.println("optimized method processing time: " + time2 + " ms");
  }

}
