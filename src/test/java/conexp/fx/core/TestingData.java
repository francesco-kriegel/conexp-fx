package conexp.fx.core;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

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
