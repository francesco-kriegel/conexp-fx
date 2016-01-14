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


public enum GraphTransformation
{
  GRAPH_3D("3D Graph"),
  GRAPH_2D("2D Graph"),
  XY("XY Chart"),
  POLAR("Polar Chart"),
  CIRCULAR("Circular Chart");
  private final String name;

  private GraphTransformation(final String name)
  {
    this.name = name;
  }

  public final String toString()
  {
    return name;
  }
}
