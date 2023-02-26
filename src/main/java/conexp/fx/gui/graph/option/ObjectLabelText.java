package conexp.fx.gui.graph.option;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2022 Francesco Kriegel
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


import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.core.context.Context;

public enum ObjectLabelText
{
  NONE("None")
  {
    public <G, M> String get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      return null;
    }
  },
  OBJECT_LABELS("Object Labels")
  {
    public <G, M> String get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      return null;
    }
  },
  EXTENT_SIZE("Extent Size")
  {
    public <G, M> String get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      return concept.extent().size() + "";
    }
  },
  EXTENT_PERCENTAGE("Extent Ratio")
  {
    public <G, M> String get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      return (int) Math.rint(100d * ((double) concept.extent().size()) / ((double) context.rowHeads().size())) + "%";
    }
  },
  OBJECT_LABELS_SIZE("Object Labels Size")
  {
    public <G, M> String get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      return lattice.objectLabels(concept).size() + "";
    }
  },
  OBJECT_LABELS_PERCENTAGE("Object Labels Ratio")
  {
    public <G, M> String get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      return (int) Math.rint(100d * ((double) lattice.objectLabels(concept).size())
          / ((double) lattice.context.rowHeads().size()))
          + "%";
    }
  };
  private final String name;

  private ObjectLabelText(final String name)
  {
    this.name = name;
  }

  public abstract <G, M> String get(
      final Context<G, M> context,
      final ConceptLattice<G, M> lattice,
      final Concept<G, M> concept);

  public final String toString()
  {
    return name;
  }
}
