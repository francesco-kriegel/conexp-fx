package conexp.fx.core.dl;

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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import com.google.common.collect.Sets;

import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.Pair;

public class ELLeastCommonSubsumer {

  private static final OWLDataFactory df = OWLManager.getOWLDataFactory();

//  private static final Map<Set<ELConceptDescription>, ELConceptDescription> lcsCache =
//      new HashMap<Set<ELConceptDescription>, ELConceptDescription>();
//
//  protected static final ELConceptDescription
//      _of(final ELConceptDescription concept1, final ELConceptDescription concept2) {
//    final Set<ELConceptDescription> _concepts = Sets.newHashSet(concept1, concept2);
//    final ELConceptDescription _lcs = lcsCache.get(_concepts);
//    if (_lcs != null)
//      return _lcs.clone();
//    if (concept1.isBot())
//      return concept2;
//    if (concept2.isBot())
//      return concept1;
//    if (concept1.isTop())
//      return concept1;
//    if (concept2.isTop())
//      return concept2;
////    System.out.println("computing lcs of " + concept1 + " and " + concept2);
//    final Set<IRI> commonNames = new HashSet<IRI>();
//    commonNames.addAll(Sets.intersection(concept1.getConceptNames(), concept2.getConceptNames()));
//    final Set<Pair<IRI, ELConceptDescription>> commonRestrictions = new HashSet<Pair<IRI, ELConceptDescription>>();
//    for (Pair<IRI, ELConceptDescription> existentialRestriction1 : concept1.getExistentialRestrictions())
//      for (Pair<IRI, ELConceptDescription> existentialRestriction2 : concept2.getExistentialRestrictions())
//        if (existentialRestriction1.x().equals(existentialRestriction2.x())) {
//          commonRestrictions.add(
//              new Pair<IRI, ELConceptDescription>(
//                  existentialRestriction1.x(),
//                  _of(existentialRestriction1.y(), existentialRestriction2.y())));
//        }
//    final ELConceptDescription lcs = new ELConceptDescription(commonNames, commonRestrictions);
////    System.out.println("lcs( " + concept1 + " , " + concept2 + " ) = " + lcs);
//    lcsCache.put(_concepts, lcs);
//    return lcs.clone();
//  }
//
//  protected static final ELConceptDescription _of(ELConceptDescription... concepts) {
////    final Set<ELNormalForm> _concepts = Sets.newHashSet(concepts);
////    final ELNormalForm _lcs = lcsCache.get(_concepts);
////    if (_lcs != null)
////      return _lcs;
////    if (concepts.length == 0)
////      return new ELNormalForm();
////    if (concepts.length == 1)
////      return concepts[0];
////    if (concepts.length == 2)
////      return _of(concepts[0], concepts[1]);
//    return _of(Arrays.asList(concepts));
//  }
//
//  protected static final ELConceptDescription _of(Collection<ELConceptDescription> concepts) {
//    final Set<ELConceptDescription> _concepts = Sets.newHashSet(concepts);
//    final ELConceptDescription _lcs = lcsCache.get(_concepts);
//    if (_lcs != null)
//      return _lcs.clone();
//    if (concepts.isEmpty())
//      return new ELConceptDescription();
//    final Iterator<ELConceptDescription> it = concepts.iterator();
//    if (concepts.size() == 1)
//      return it.next();
//    if (concepts.size() == 2) {
//      final ELConceptDescription lcs = _of(it.next(), it.next());
//      lcsCache.put(_concepts, lcs);
//      return lcs;
//    }
//    ELConceptDescription lcs = it.next();
//    while (it.hasNext())
//      lcs = _of(lcs, it.next());
//    lcsCache.put(_concepts, lcs);
//    return lcs.clone();
//  }

  public static final ELConceptDescription lcs(final ELConceptDescription C, final ELConceptDescription D) {
    return lcs(Sets.newHashSet(C, D));
  }

  public static final ELConceptDescription lcs(final Set<ELConceptDescription> Cs) {
    Cs.parallelStream().forEach(ELConceptDescription::reduce);
    final Set<ELConceptDescription> Ds = Collections3.representatives(Cs, (X, Y) -> X.isEquivalentTo(Y));
    if (Ds.isEmpty())
      return ELConceptDescription.bot();
    else if (Ds.size() == 1)
      return Ds.iterator().next().clone();
    else
      return lcsOfMutuallyIncomparable(Ds);
  }

  public static final ELConceptDescription
      lcsOfMutuallyIncomparable(final ELConceptDescription C, final ELConceptDescription D) {
    return lcsOfMutuallyIncomparable(Sets.newHashSet(C, D));
  }

  public static final ELConceptDescription lcsOfMutuallyIncomparable(final Set<ELConceptDescription> Ds) {
    final ELConceptDescription lcs = new ELConceptDescription();
    final Iterator<ELConceptDescription> it = Ds.iterator();
    final ELConceptDescription D = it.next();
    it.remove();
    final Set<IRI> commonConceptNames = D
        .getConceptNames()
        .parallelStream()
        .filter(A -> Ds.parallelStream().map(ELConceptDescription::getConceptNames).allMatch(As -> As.contains(A)))
        .collect(Collectors.toSet());
    lcs.getConceptNames().addAll(commonConceptNames);
    final Set<IRI> commonRoleNames = D
        .getExistentialRestrictions()
        .keySet()
        .parallelStream()
        .filter(
            r -> Ds.parallelStream().map(ELConceptDescription::getExistentialRestrictions).allMatch(
                ERs -> ERs.keySet().parallelStream().anyMatch(r::equals)))
        .collect(Collectors.toSet());
    Ds.add(D);
    commonRoleNames
        .parallelStream()
        .map(
            r -> Pair.of(
                r,
                Sets
                    .cartesianProduct(
                        Ds
                            .parallelStream()
                            .map(ELConceptDescription::getExistentialRestrictions)
                            .map(m -> m.get(r))
                            .map(HashSet::new)
                            .collect(Collectors.toList()))
                    .parallelStream()
                    .map(HashSet::new)
                    .map(ELLeastCommonSubsumer::lcsOfMutuallyIncomparable)
                    .map(ELConceptDescription::reduce)
                    .collect(Collectors.toSet())))
        .sequential()
        .forEach(p -> lcs.getExistentialRestrictions().putAll(p.x(), p.y()));;
    return lcs.clone().reduce();
  }

  public static final OWLClassExpression of(final OWLClassExpression concept1, final OWLClassExpression concept2) {
    return ELLeastCommonSubsumer
        .lcs(ELConceptDescription.of(concept1), ELConceptDescription.of(concept2))
        .toOWLClassExpression();
  }

  public static final OWLClassExpression of(final OWLClassExpression... concepts) {
    if (concepts.length == 0)
      return df.getOWLThing();
    if (concepts.length == 1)
      return concepts[0];
    if (concepts.length == 2)
      return of(concepts[0], concepts[1]);
    return of(Arrays.asList(concepts));
  }

  public static final OWLClassExpression of(final Collection<OWLClassExpression> concepts) {
    if (concepts.isEmpty())
      return df.getOWLThing();
    final Iterator<OWLClassExpression> it = concepts.iterator();
    if (concepts.size() == 1)
      return it.next();
    if (concepts.size() == 2)
      return of(it.next(), it.next());
    OWLClassExpression lcs = it.next();
    while (it.hasNext())
      lcs = of(lcs, it.next());
    return lcs;
  }

//if (concept1.isOWLNothing())
//  return concept2;
//if (concept2.isOWLNothing())
//  return concept1;
//if (concept1.isOWLThing())
//  return concept1;
//if (concept2.isOWLThing())
//  return concept2;
//if (concept1 instanceof OWLClass)
//  if (concept2 instanceof OWLClass)
//    if (((OWLClass) concept1).getIRI().equals(((OWLClass) concept2).getIRI()))
//      return concept1;
//    else
//      return df.getOWLThing();
//  else if (concept2 instanceof OWLObjectSomeValuesFrom)
//    return df.getOWLThing();
//  else if (concept2 instanceof OWLObjectIntersectionOf)
//    if (((OWLObjectIntersectionOf) concept2).asConjunctSet().contains(concept1))
//      return concept1;
//    else
//      return df.getOWLThing();
//  else
//    throw new ELSyntaxException();
//else if (concept1 instanceof OWLObjectSomeValuesFrom)
//  if (concept2 instanceof OWLClass)
//    return df.getOWLThing();
//  else if (concept2 instanceof OWLObjectSomeValuesFrom)
//    if (((OWLObjectSomeValuesFrom) concept1).getProperty().equals(
//        ((OWLObjectSomeValuesFrom) concept2).getProperty()))
//      return df.getOWLObjectSomeValuesFrom(
//          ((OWLObjectSomeValuesFrom) concept1).getProperty(),
//          of(((OWLObjectSomeValuesFrom) concept1).getFiller(), ((OWLObjectSomeValuesFrom) concept2).getFiller()));
//    else
//      return df.getOWLThing();
//  else if (concept2 instanceof OWLObjectIntersectionOf)
//    return null;
//  else
//    return df.getOWLThing();
//else if (concept1 instanceof OWLObjectIntersectionOf)
//
//  return df.getOWLThing();

}
