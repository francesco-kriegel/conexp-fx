package conexp.fx.gui.graph.option;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
