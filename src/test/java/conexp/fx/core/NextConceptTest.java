package conexp.fx.core;

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
