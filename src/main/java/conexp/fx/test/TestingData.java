package conexp.fx.test;

import java.util.HashMap;
import java.util.Map;

import conexp.fx.core.context.MatrixContext;

public abstract class TestingData<G, M, X> {

  private final Map<MatrixContext<G, M>, X> data = new HashMap<MatrixContext<G, M>, X>();

  public TestingData() {
    super();
  }

  public final Map<MatrixContext<G, M>, X> data() {
    return data;
  }

}
