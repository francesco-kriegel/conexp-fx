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
