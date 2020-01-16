package conexp.fx.gui.graph.option;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2020 Francesco Kriegel
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


import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.core.context.Context;

public enum VertexRadius
{
  NONE("None")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      return 0d;
    }
  },
  TINY("Tiny")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      return 3d;
    }
  },
  SMALL("Small")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      return 4d;
    }
  },
  NORMAL("Normal")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      return 5d;
    }
  },
  LARGE("Large")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      return 7d;
    }
  },
  HUGE("Huge")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      return 10d;
    }
  },
  EXTENT("Extent Size")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      return 3d + 17d * Math.sqrt(((double) concept.extent().size()) / ((double) context.rowHeads().size()));
    }
  },
  INTENT("Intent Size")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      return 3d + 17d * Math.sqrt(((double) concept.intent().size()) / ((double) context.colHeads().size()));
    }
  },
  OBJECT_LABELS("Object Labels Size")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      return 3d + 17d * Math.sqrt(((double) lattice.objectLabels(concept).size())
          / ((double) context.rowHeads().size()));
    }
  },
  ATTRIBUTE_LABELS("Attribute Labels Size")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      return 3d + 17d * Math.sqrt(((double) lattice.attributeLabels(concept).size())
          / ((double) context.colHeads().size()));
    }
  },
  EXTENT_STABILITY("Extent Stability")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      if (concept.intent().size() > 10d)
        return 5d;
      return 3d + 17d * (double) Sets.filter(Sets.powerSet(concept.intent()), new Predicate<Set<M>>()
        {
          public boolean apply(Set<M> intentSubset)
          {
            return context.colAnd(intentSubset).equals(concept.extent());
          }
        }).size() / (double) (1 << concept.intent().size());
    }
  },
  INTENT_STABILITY("Intent Stability")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept)
    {
      if (concept.extent().size() > 10d)
        return 5d;
      return 3d + 17d * (double) Sets.filter(Sets.powerSet(concept.extent()), new Predicate<Set<G>>()
        {
          public boolean apply(Set<G> extentSubset)
          {
            return context.rowAnd(extentSubset).equals(concept.intent());
          }
        }).size() / (double) (1 << concept.extent().size());
    }
  };
  private final String name;

  private VertexRadius(final String name)
  {
    this.name = name;
  }

  public abstract <G, M> double get(
      final Context<G, M> context,
      final ConceptLattice<G, M> lattice,
      final Concept<G, M> concept);

  public final String toString()
  {
    return name;
  }
}
