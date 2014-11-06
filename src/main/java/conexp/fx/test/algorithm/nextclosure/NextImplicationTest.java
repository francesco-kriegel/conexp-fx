package conexp.fx.test.algorithm.nextclosure;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import conexp.fx.test.ImplicationTest;
import conexp.fx.test.TestBenchmark;
import conexp.fx.test.TestContexts;

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
