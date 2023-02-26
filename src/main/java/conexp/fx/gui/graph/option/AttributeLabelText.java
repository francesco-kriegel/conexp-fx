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
