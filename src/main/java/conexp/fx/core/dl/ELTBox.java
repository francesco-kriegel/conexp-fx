package conexp.fx.core.dl;

import java.util.Collections;
import java.util.HashMap;

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

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import conexp.fx.core.collections.relation.MatrixRelation;

public class ELTBox {

  private static final IRI              NOTHING = OWLManager.getOWLDataFactory().getOWLNothing().getIRI();

  private final Set<ELConceptInclusion> conceptInclusions;

  public ELTBox() {
    super();
    this.conceptInclusions = new HashSet<>();
  }

  public final Signature getSignature() {
    final Signature sigma = new Signature(IRI.generateDocumentIRI());
    for (ELConceptInclusion ci : conceptInclusions) {
      sigma.getConceptNames().addAll(ci.getSubsumee().getConceptNamesInSignature().collect(Collectors.toSet()));
      sigma.getConceptNames().addAll(ci.getSubsumer().getConceptNamesInSignature().collect(Collectors.toSet()));
      sigma.getRoleNames().addAll(ci.getSubsumee().getRoleNamesInSignature().collect(Collectors.toSet()));
      sigma.getRoleNames().addAll(ci.getSubsumer().getRoleNamesInSignature().collect(Collectors.toSet()));
    }
    return sigma;
  }

  public final Set<ELConceptInclusion> getConceptInclusions() {
    return conceptInclusions;
  }

  private final class CanonicalModelBuilder {

    private final ELConceptDescription                    C;
    private final ELInterpretation2<ELConceptDescription> canmod;

    private CanonicalModelBuilder(final ELConceptDescription C) {
      super();
      this.C = C;
      this.canmod = new ELInterpretation2<>();
    }

    private final void insert(final ELConceptDescription D) {
      insert(D, D);
    }

    private final void insert(final ELConceptDescription X, final ELConceptDescription D) {
      for (IRI A : D.getConceptNames())
        canmod.getConceptNameExtensionMatrix().add(X, A);
      for (Entry<IRI, ELConceptDescription> rE : D.getExistentialRestrictions().entries()) {
        canmod.getRoleNameExtensionMatrix(rE.getKey()).add(X, rE.getValue());
        insert(rE.getValue());
      }
    }

    private final ELInterpretation2<ELConceptDescription> buildAndGet() {
      insert(C);
      boolean changed = true;
      while (changed) {
        changed = false;
        for (ELConceptInclusion ci : conceptInclusions)
          for (ELConceptDescription o : new HashSet<>(canmod.getDomain())) {
            final ELConceptDescription char1 =
                canmod.getMostSpecificConceptDescription(Collections.singleton(o), ci.getSubsumee().roleDepth());
            final ELConceptDescription char2 =
                canmod.getMostSpecificConceptDescription(Collections.singleton(o), ci.getSubsumer().roleDepth());
            if (char1.isSubsumedBy(ci.getSubsumee()) && !char2.isSubsumedBy(ci.getSubsumer())) {
              insert(o, ci.getSubsumer());
              changed = true;
            }
          }
      }
      changed = true;
      while (changed) {
        changed = false;
        for (ELConceptDescription o : canmod.getDomain())
          if (canmod.getConceptNameExtensionMatrix().contains(o, NOTHING))
            for (MatrixRelation<ELConceptDescription, ELConceptDescription> r : canmod
                .getRoleNameExtensionMatrixMap()
                .values())
              if (r.colHeads().contains(o))
                for (ELConceptDescription p : r.col(o))
                  changed |= canmod.add(p, NOTHING);
      }
      for (ELConceptDescription o : canmod.getDomain())
        if (canmod.getConceptNameExtensionMatrix().contains(o, NOTHING)) {
          canmod.getConceptNameExtensionMatrix().row(o).retainAll(Collections.singleton(NOTHING));
          for (MatrixRelation<ELConceptDescription, ELConceptDescription> r : canmod
              .getRoleNameExtensionMatrixMap()
              .values())
            if (r.rowHeads().contains(o))
              r.row(o).clear();
        }
      return this.canmod;
    }

  }

  public final ELInterpretation2<ELConceptDescription> getCanonicalModel(final ELConceptDescription C) {
    return new CanonicalModelBuilder(C).buildAndGet();
  }

  public final ELConceptDescription getMostSpecificConsequence(final ELConceptDescription C, final int roleDepth) {
    return getCanonicalModel(C).getMostSpecificConceptDescription(Collections.singleton(C), roleDepth);
  }

  private final class CanonicalModelBuilderLutz {

    private final ELConceptDescription                    C;
    private final ELInterpretation2<ELConceptDescription> canmod;
    private final Set<Entry<IRI, ELConceptDescription>>   exsubT;
    private final Set<ELConceptDescription>               domain;
    private final Set<IRI>                                conceptNames = new HashSet<>();
    private final Set<IRI>                                roleNames    = new HashSet<>();

    private CanonicalModelBuilderLutz(final ELConceptDescription C) {
      super();
      this.C = C;
      this.canmod = new ELInterpretation2<>();
      this.exsubT = new HashSet<>();
      this.domain = new HashSet<>();
    }

    private final void populate_exsubT(final ELConceptDescription X) {
      exsubT.addAll(X.getExistentialRestrictions().entries());
      for (ELConceptDescription Y : X.getExistentialRestrictions().values())
        populate_exsubT(Y);
    }

    private final void populate_domain(final ELConceptDescription X) {
      domain.addAll(X.getExistentialRestrictions().values());
      for (ELConceptDescription Y : X.getExistentialRestrictions().values())
        populate_domain(Y);
    }

    private final void populate_signature(final ELConceptDescription X) {
      conceptNames.addAll(X.getConceptNames());
      roleNames.addAll(X.getExistentialRestrictions().keys());
      for (ELConceptDescription Y : X.getExistentialRestrictions().values())
        populate_signature(Y);
    }

    private final ELInterpretation2<ELConceptDescription> buildAndGet() {
      for (ELConceptInclusion ci : conceptInclusions) {
        populate_exsubT(ci.getSubsumee());
        populate_exsubT(ci.getSubsumer());
      }
      domain.add(C);
      populate_domain(C);
      for (Entry<IRI, ELConceptDescription> e : exsubT)
        domain.add(e.getValue());
      populate_signature(C);
      for (ELConceptInclusion ci : conceptInclusions) {
        populate_signature(ci.getSubsumee());
        populate_signature(ci.getSubsumer());
      }
      for (ELConceptDescription D : domain) {
        for (IRI A : conceptNames)
          if (ELReasoner.isSubsumedBy(D, ELConceptDescription.conceptName(A), ELTBox.this))
            canmod.getConceptNameExtensionMatrix().add(D, A);
        for (ELConceptDescription E : domain)
          for (IRI r : roleNames) {
            final HashMap<IRI, ELConceptDescription> hashMap = new HashMap<>();
            hashMap.put(r, E);
            final Entry<IRI, ELConceptDescription> rE = hashMap.entrySet().iterator().next();
            if ((exsubT.contains(rE)
                && ELReasoner.isSubsumedBy(D, ELConceptDescription.existentialRestriction(rE), ELTBox.this))
                || D.getExistentialRestrictions().containsEntry(r, E))
              canmod.getRoleNameExtensionMatrix(r).add(D, E);
          }
      }
      return canmod;
    }

  }

  public final ELInterpretation2<ELConceptDescription> getCanonicalModelLutz(final ELConceptDescription C) {
    return new CanonicalModelBuilderLutz(C).buildAndGet();
  }

  public final ELConceptDescription getMostSpecificConsequenceLutz(final ELConceptDescription C, final int roleDepth) {
    return getCanonicalModelLutz(C).getMostSpecificConceptDescription(Collections.singleton(C), roleDepth);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof ELTBox))
      return false;
    final ELTBox other = (ELTBox) obj;
    return this.conceptInclusions.equals(other.conceptInclusions);
  }

  public final OWLOntology toOWLOntology() {
    try {
      final OWLOntologyManager om = OWLManager.createOWLOntologyManager();
      final OWLDataFactory df = om.getOWLDataFactory();
      final OWLOntology ontology = om.createOntology();
      conceptInclusions
          .parallelStream()
          .forEach(
              gci -> om
                  .applyChange(
                      new AddAxiom(
                          ontology,
                          df
                              .getOWLSubClassOfAxiom(
                                  gci.getSubsumee().clone().reduce().toOWLClassExpression(),
                                  gci.getSubsumer().clone().reduce().toOWLClassExpression()))));
      return ontology;
    } catch (OWLOntologyCreationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int hashCode() {
    return 23 * conceptInclusions.hashCode() + 99;
  }

  @Override
  public String toString() {
    return "EL-TBox " + conceptInclusions.toString();
  }

}
