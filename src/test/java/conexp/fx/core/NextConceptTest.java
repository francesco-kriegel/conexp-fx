package conexp.fx.core;

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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class NextConceptTest extends TestBenchmark {

  private static ConceptTest<Integer, Integer> ord;
  private static ConceptTest<Integer, Integer> nom;
  private static ConceptTest<Integer, Integer> cnom;
  private static ConceptTest<String, String>   small;
  private static ConceptTest<String, String>   scales;

  @BeforeClass
  public static final void createConceptTests() {
    final long start = System.currentTimeMillis();
    ord = TestContexts.ordinalScaleConceptTest(1, 32);// TODO:Check for 0
    nom = TestContexts.nominalScaleConceptTest(2, 32);// TODO:Check for 0 an d1
    cnom = TestContexts.contraNominalScaleConceptTest(0, 16);
    small = TestContexts.smallConceptTest();
    scales = TestContexts.scalesConceptTest();
    System.out.println("Creation of ConceptTests took " + (System.currentTimeMillis() - start) + " ms");
  }

  @Test
  public void testOrdinalScales() {
    ord.run();
  }

  @Test
  public void testNominalScales() {
    nom.run();
  }

  @Test
  public void testContraNominalScales() {
    cnom.run();
  }

  @Test
  public void testSmallContexts() {
    small.run();
  }

  @Test
  public void testScaleContexts() {
    scales.run();
  }

  @AfterClass
  public static void deleteConceptTests() {
    ord = null;
    nom = null;
    cnom = null;
    small = null;
    scales = null;
  }

}
