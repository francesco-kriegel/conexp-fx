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
