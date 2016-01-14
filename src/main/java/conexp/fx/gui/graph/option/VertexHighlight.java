package conexp.fx.gui.graph.option;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


public enum VertexHighlight {
  NONE("None"),
  CONCEPT("Only Concept"),
  UPPER_NEIGHBORS("Upper Neighbors"),
  LOWER_NEIGHBORS("Lower Neighbors"),
  NEIGHBORS("Neighbors"),
  FILTER("Filter"),
  IDEAL("Ideal"),
  FILTER_IDEAL("Filter & Ideal");

  private final String name;

  private VertexHighlight(final String name) {
    this.name = name;
  }

  @Override
  public final String toString() {
    return name;
  }
}
