package conexp.fx.core.dl;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
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
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class ELTBox {

  private final Set<ELConceptInclusion> gcis;

  public ELTBox() {
    super();
    this.gcis = new HashSet<ELConceptInclusion>();
  }

  public final Set<ELConceptInclusion> getGCIs() {
    return gcis;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof ELTBox))
      return false;
    final ELTBox other = (ELTBox) obj;
    return this.gcis.equals(other.gcis);
  }

  public final OWLOntology toOWLOntology() {
    try {
      final OWLOntologyManager om = OWLManager.createOWLOntologyManager();
      final OWLDataFactory df = om.getOWLDataFactory();
      final OWLOntology ontology = om.createOntology();
      gcis.parallelStream().forEach(
          gci -> om.applyChange(
              new AddAxiom(
                  ontology,
                  df.getOWLSubClassOfAxiom(
                      gci.getSubsumee().clone().reduce().toOWLClassExpression(),
                      gci.getSubsumer().clone().reduce().toOWLClassExpression()))));
      return ontology;
    } catch (OWLOntologyCreationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int hashCode() {
    return 23 * gcis.hashCode() + 99;
  }

  @Override
  public String toString() {
    return "EL-TBox " + gcis.toString();
  }

}
