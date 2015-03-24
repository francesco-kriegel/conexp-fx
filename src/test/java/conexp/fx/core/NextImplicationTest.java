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

public class NextImplicationTest extends TestBenchmark {

  private static ImplicationTest<Integer, Integer> ord;
  private static ImplicationTest<Integer, Integer> nom;
  private static ImplicationTest<Integer, Integer> cnom;
  private static ImplicationTest<String, String>   small;
  private static ImplicationTest<String, String>   scales;
  private static ImplicationTest<String, String>   daniel;

  @BeforeClass
  public static void createImplicationTests() {
    final long start = System.currentTimeMillis();
    ord = TestContexts.ordinalScaleImplicationTest(3, 10);
    nom = TestContexts.nominalScaleImplicationTest(3, 10);
    cnom = TestContexts.contraNominalScaleImplicationTest(0, 10);
    small = TestContexts.smallImplicationTest();
    scales = TestContexts.scalesImplicationTest();
    daniel = TestContexts.danielsImplicationTest();
    System.out.println("Creation of ImplicationTests took " + (System.currentTimeMillis() - start) + " ms");
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
  public void testContranominalScales() {
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

//  @Test
  public void testAgainstDanielsSamples() {
    daniel.run();
  }

  @AfterClass
  public static void deleteImplicationTests() {
    ord = null;
    nom = null;
    cnom = null;
    small = null;
    scales = null;
    daniel = null;
  }

}
