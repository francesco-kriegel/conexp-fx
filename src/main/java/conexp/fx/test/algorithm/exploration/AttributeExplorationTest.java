package conexp.fx.test.algorithm.exploration;

import org.junit.Test;

import conexp.fx.core.algorithm.exploration.AttributeExploration;
import conexp.fx.core.algorithm.exploration.NoExpert;
import conexp.fx.core.builder.Requests;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.test.TestContexts;

public class AttributeExplorationTest {

  @Test
  public void testStartExploration() {
    final MatrixContext<Integer, Integer> context = TestContexts.fromRequest(new Requests.Scale.NominalScaleFromInt(7));
    System.out.println(context.matrix());
    final AttributeExploration<Integer, Integer> exploration =
        new AttributeExploration<Integer, Integer>(context, new NoExpert<Integer, Integer>());
    System.out.println("starting exploration...");
    exploration.startExploration();
    System.out.println("done.");
  }
}
