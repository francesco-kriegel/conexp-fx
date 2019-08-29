package conexp.fx.core.dl;

import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2019 Francesco Kriegel
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

import conexp.fx.core.util.UnicodeSymbols;

public class ELConceptInclusion {

  private static final OWLDataFactory df = OWLManager.getOWLDataFactory();

  public static final ELConceptInclusion parse(final String subsumeeExpression, final String subsumerExpression) {
    return new ELConceptInclusion(
        ELConceptDescription.parse(subsumeeExpression),
        ELConceptDescription.parse(subsumerExpression));
  }

  private final ELConceptDescription subsumee;
  private final ELConceptDescription subsumer;

  public ELConceptInclusion(final ELConceptDescription subsumee, final ELConceptDescription subsumer) {
    super();
    this.subsumee = subsumee;
    this.subsumer = subsumer;
  }

  public final Signature getSignature() {
    final Signature sigma = new Signature(IRI.generateDocumentIRI());
    sigma.getConceptNames().addAll(subsumee.getConceptNamesInSignature().collect(Collectors.toSet()));
    sigma.getConceptNames().addAll(subsumer.getConceptNamesInSignature().collect(Collectors.toSet()));
    sigma.getRoleNames().addAll(subsumee.getRoleNamesInSignature().collect(Collectors.toSet()));
    sigma.getRoleNames().addAll(subsumer.getRoleNamesInSignature().collect(Collectors.toSet()));
    return sigma;
  }

  public final ELConceptDescription getSubsumee() {
    return subsumee;
  }

  public final ELConceptDescription getSubsumer() {
    return subsumer;
  }

  public final boolean isTautological() {
    return subsumee.isSubsumedBy(subsumer);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof ELConceptInclusion))
      return false;
    final ELConceptInclusion other = (ELConceptInclusion) obj;
    return this.subsumee.equals(other.subsumee) && this.subsumer.equals(other.subsumee);
  }

  @Override
  public int hashCode() {
    return 5 * subsumee.hashCode() + 7 * subsumer.hashCode();
  }

  @Override
  public String toString() {
    return subsumee.toString() + " " + UnicodeSymbols.SQSUBSETEQ + " " + subsumer.toString();
  }

  public OWLSubClassOfAxiom toOWLSubClassOfAxiom() {
    return df.getOWLSubClassOfAxiom(subsumee.toOWLClassExpression(), subsumer.toOWLClassExpression());
  }

}
