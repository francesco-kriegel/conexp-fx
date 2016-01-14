package conexp.fx.core.dl;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import com.google.common.collect.Sets;

import conexp.fx.core.collections.pair.Pair;

public class ELLeastCommonSubsumer {

  private static final OWLDataFactory                       df       = OWLManager.getOWLDataFactory();

  private static final Map<Set<ELConceptDescription>, ELConceptDescription> lcsCache = new HashMap<Set<ELConceptDescription>, ELConceptDescription>();

  protected static final ELConceptDescription _of(final ELConceptDescription concept1, final ELConceptDescription concept2) {
    final Set<ELConceptDescription> _concepts = Sets.newHashSet(concept1, concept2);
    final ELConceptDescription _lcs = lcsCache.get(_concepts);
    if (_lcs != null)
      return _lcs;
    if (concept1.isBot())
      return concept2;
    if (concept2.isBot())
      return concept1;
    if (concept1.isTop())
      return concept1;
    if (concept2.isTop())
      return concept2;
//    System.out.println("computing lcs of " + concept1 + " and " + concept2);
    final Set<IRI> commonNames = new HashSet<IRI>();
    commonNames.addAll(Sets.intersection(concept1.getConceptNames(), concept2.getConceptNames()));
    final Set<Pair<IRI, ELConceptDescription>> commonRestrictions = new HashSet<Pair<IRI, ELConceptDescription>>();
    for (Pair<IRI, ELConceptDescription> existentialRestriction1 : concept1.getExistentialRestrictions())
      for (Pair<IRI, ELConceptDescription> existentialRestriction2 : concept2.getExistentialRestrictions())
        if (existentialRestriction1.x().equals(existentialRestriction2.x())) {
          commonRestrictions.add(new Pair<IRI, ELConceptDescription>(existentialRestriction1.x(), _of(
              existentialRestriction1.y(),
              existentialRestriction2.y())));
        }
    final ELConceptDescription lcs = new ELConceptDescription(commonNames, commonRestrictions);
//    System.out.println("lcs( " + concept1 + " , " + concept2 + " ) = " + lcs);
    lcsCache.put(_concepts, lcs);
    return lcs;
  }

  protected static final ELConceptDescription _of(ELConceptDescription... concepts) {
//    final Set<ELNormalForm> _concepts = Sets.newHashSet(concepts);
//    final ELNormalForm _lcs = lcsCache.get(_concepts);
//    if (_lcs != null)
//      return _lcs;
//    if (concepts.length == 0)
//      return new ELNormalForm();
//    if (concepts.length == 1)
//      return concepts[0];
//    if (concepts.length == 2)
//      return _of(concepts[0], concepts[1]);
    return _of(Arrays.asList(concepts));
  }

  protected static final ELConceptDescription _of(Collection<ELConceptDescription> concepts) {
    final Set<ELConceptDescription> _concepts = Sets.newHashSet(concepts);
    final ELConceptDescription _lcs = lcsCache.get(_concepts);
    if (_lcs != null)
      return _lcs;
    if (concepts.isEmpty())
      return new ELConceptDescription();
    final Iterator<ELConceptDescription> it = concepts.iterator();
    if (concepts.size() == 1)
      return it.next();
    if (concepts.size() == 2) {
      final ELConceptDescription lcs = _of(it.next(), it.next());
      lcsCache.put(_concepts, lcs);
      return lcs;
    }
    ELConceptDescription lcs = it.next();
    while (it.hasNext())
      lcs = _of(lcs, it.next());
    lcsCache.put(_concepts, lcs);
    return lcs;
  }

  public static final OWLClassExpression of(final OWLClassExpression concept1, final OWLClassExpression concept2) {
    return ELLeastCommonSubsumer._of(ELConceptDescription.of(concept1), ELConceptDescription.of(concept2)).toOWLClassExpression();
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
