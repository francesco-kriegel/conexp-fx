package conexp.fx.gui.graph.option;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.core.context.Context;

public enum AttributeLabelText {
  NONE("None") {

    public <G, M> String get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept) {
      return null;
    }
  },
  ATTRIBUTE_LABELS("Attribute Labels") {

    public <G, M> String get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept) {
      return null;
    }
  },
  SEED_LABELS("Attribute Seed Labels") {

    public <G, M> String get(Context<G, M> context, ConceptLattice<G, M> lattice, Concept<G, M> concept) {
      return null;
    }
  },
  INTENT_SIZE("Intent Size") {

    public <G, M> String get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept) {
      return concept.intent().size() + "";
    }
  },
  INTENT_PERCENTAGE("Intent Ratio") {

    public <G, M> String get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept) {
      return (int) Math.rint(100d * ((double) concept.intent().size()) / ((double) lattice.context.colHeads().size()))
          + "%";
    }
  },
  ATTRIBUTE_LABELS_SIZE("Attribute Labels Size") {

    public <G, M> String get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept) {
      return lattice.attributeLabels(concept).size() + "";
    }
  },
  ATTRIBUTE_LABELS_PERCENTAGE("Attribute Labels Ratio") {

    public <G, M> String get(
        final Context<G, M> context,
        final ConceptLattice<G, M> lattice,
        final Concept<G, M> concept) {
      return (int) Math.rint(100d * ((double) lattice.attributeLabels(concept).size())
          / ((double) lattice.context.colHeads().size()))
          + "%";
    }
  };

  private final String name;

  private AttributeLabelText(final String name) {
    this.name = name;
  }

  public abstract <G, M> String get(
      final Context<G, M> context,
      final ConceptLattice<G, M> lattice,
      final Concept<G, M> concept);

  public final String toString() {
    return name;
  }
}
