package conexp.fx.core.util;

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
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class OWLMinimizer {

  public static final Reasoner r = initializeReasoner();

  private static final Reasoner initializeReasoner() {
    try {
      return new Reasoner(OWLManager.createOWLOntologyManager().createOntology());
    } catch (OWLOntologyCreationException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static final boolean isSubsumedBy(final OWLClassExpression c1, final OWLClassExpression c2) {
    return r.isEntailed(OWLManager.getOWLDataFactory().getOWLSubClassOfAxiom(
        c1,
        c2));
  }

  public static final boolean subsumes(final OWLClassExpression c1, final OWLClassExpression c2) {
    return isSubsumedBy(
        c2,
        c1);
  }

  public static final <T> Set<T> filterMinimal(final Set<T> elements, BiPredicate<T, T> predicate) {
    return elements.stream().filter(
        x -> !elements.stream().anyMatch(
            y -> !x.equals(y) && predicate.test(
                x,
                y))).collect(
        Collectors.toSet());
  }

  public static final <T> Set<T> filterMinimalParallel(final Set<T> elements, BiPredicate<T, T> predicate) {
    return elements.parallelStream().filter(
        x -> !elements.parallelStream().anyMatch(
            y -> !x.equals(y) && predicate.test(
                x,
                y))).collect(
        Collectors.toSet());
  }

  public static final OWLClassExpression minimizeConjunction(final OWLClassExpression classExpression) {
    if (classExpression instanceof OWLObjectIntersectionOf) {
      final OWLObjectIntersectionOf c = (OWLObjectIntersectionOf) classExpression;
      return OWLManager.getOWLDataFactory().getOWLObjectIntersectionOf(
          filterMinimal(
              c.getOperands(),
              OWLMinimizer::subsumes));
    } else if (classExpression instanceof OWLObjectComplementOf) {
      final OWLObjectComplementOf c = (OWLObjectComplementOf) classExpression;
      return OWLManager.getOWLDataFactory().getOWLObjectComplementOf(
          minimizeConjunction(c.getOperand()));
    } else if (classExpression instanceof OWLObjectSomeValuesFrom) {
      final OWLObjectSomeValuesFrom c = (OWLObjectSomeValuesFrom) classExpression;
      return OWLManager.getOWLDataFactory().getOWLObjectSomeValuesFrom(
          c.getProperty(),
          minimizeConjunction(c.getFiller()));
    } else if (classExpression instanceof OWLObjectAllValuesFrom) {
      final OWLObjectAllValuesFrom c = (OWLObjectAllValuesFrom) classExpression;
      return OWLManager.getOWLDataFactory().getOWLObjectSomeValuesFrom(
          c.getProperty(),
          minimizeConjunction(c.getFiller()));
    } else if (classExpression instanceof OWLObjectMinCardinality) {
      final OWLObjectMinCardinality c = (OWLObjectMinCardinality) classExpression;
      return OWLManager.getOWLDataFactory().getOWLObjectMinCardinality(
          c.getCardinality(),
          c.getProperty(),
          minimizeConjunction(c.getFiller()));
    } else if (classExpression instanceof OWLObjectMaxCardinality) {
      final OWLObjectMaxCardinality c = (OWLObjectMaxCardinality) classExpression;
      return OWLManager.getOWLDataFactory().getOWLObjectMaxCardinality(
          c.getCardinality(),
          c.getProperty(),
          minimizeConjunction(c.getFiller()));
    } else if (classExpression instanceof OWLObjectExactCardinality) {
      final OWLObjectExactCardinality c = (OWLObjectExactCardinality) classExpression;
      return OWLManager.getOWLDataFactory().getOWLObjectExactCardinality(
          c.getCardinality(),
          c.getProperty(),
          minimizeConjunction(c.getFiller()));
    }
    return classExpression;
  }
}
