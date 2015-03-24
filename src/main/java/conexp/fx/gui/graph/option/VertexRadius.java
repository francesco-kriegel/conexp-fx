package conexp.fx.gui.graph.option;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
