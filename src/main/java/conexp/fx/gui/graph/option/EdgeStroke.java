package conexp.fx.gui.graph.option;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.core.context.Context;

public enum EdgeStroke
{
  NONE("None")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Pair<Concept<G, M>, Concept<G, M>> concepts)
    {
      return 0d;
    }
  },
  TINY("Tiny")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Pair<Concept<G, M>, Concept<G, M>> concepts)
    {
      return 0.5d;
    }
  },
  SMALL("Small")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Pair<Concept<G, M>, Concept<G, M>> concepts)
    {
      return 1d;
    }
  },
  NORMAL("Normal")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Pair<Concept<G, M>, Concept<G, M>> concepts)
    {
      return 2d;
    }
  },
  LARGE("Large")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Pair<Concept<G, M>, Concept<G, M>> concepts)
    {
      return 3d;
    }
  },
  HUGE("Huge")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Pair<Concept<G, M>, Concept<G, M>> concepts)
    {
      return 4d;
    }
  },
  EXTENT_DIFFERENCE("Extent Difference")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Pair<Concept<G, M>, Concept<G, M>> concepts)
    {
      return 1d + 6d * Math.sqrt(((double) concepts.second().extent().size() - (double) concepts
          .first()
          .extent()
          .size())
          / (double) context.rowHeads().size());
    }
  },
  INTENT_DIFFERENCE("Intent Difference")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Pair<Concept<G, M>, Concept<G, M>> concepts)
    {
      return 1d + 6d * Math.sqrt(((double) concepts.first().intent().size() - (double) concepts
          .second()
          .intent()
          .size())
          / (double) context.colHeads().size());
    }
  },
  EXTENT_RATIO("Extent Ratio")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Pair<Concept<G, M>, Concept<G, M>> concepts)
    {
      return 1d + 6d * (double) concepts.first().extent().size() / (double) concepts.second().extent().size();
    }
  },
  INTENT_RATIO("Intent Ratio")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Pair<Concept<G, M>, Concept<G, M>> concepts)
    {
      return 1d + 6d * (double) concepts.second().intent().size() / (double) concepts.first().intent().size();
    }
  },
  INVERSE_EXTENT_RATIO("Inverse Extent Ratio")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Pair<Concept<G, M>, Concept<G, M>> concepts)
    {
      return 1d + 6d * (1d - (double) concepts.first().extent().size() / (double) concepts.second().extent().size());
    }
  },
  INVERSE_INTENT_RATIO("Inverse Intent Ratio")
  {
    public <G, M> double get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Pair<Concept<G, M>, Concept<G, M>> concepts)
    {
      return 1d + 6d * (1d - (double) concepts.second().intent().size() / (double) concepts.first().intent().size());
    }
  };
  private final String name;

  public abstract <G, M> double get(
      final Context<G, M> context,
      final ConceptLattice<G, M> lattice,
      final Pair<Concept<G, M>, Concept<G, M>> concepts);

  private EdgeStroke(final String name)
  {
    this.name = name;
  }

  public final String toString()
  {
    return name;
  }
}
