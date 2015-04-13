package conexp.fx.core.dl;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import conexp.fx.core.algorithm.nextclosures.NextClosures6C;
import conexp.fx.core.algorithm.nextclosures.ResultC;
import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.collections.setlist.HashSetArrayList;
import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.context.Context;
import conexp.fx.core.implication.Implication;
import conexp.fx.core.util.IterableFile;

public final class ELInterpretation extends AInterpretation<ELConceptDescription, ELConceptInclusion, ELTBox> {

  public ELInterpretation(final IRI baseIRI) {
    super(baseIRI);
  }

  public ELInterpretation(final Signature signature) {
    super(signature);
  }

  public ELInterpretation(final Signature signature, final Set<IRI> domain) {
    super(signature, domain);
  }

  @Override
  public final boolean isInstanceOf(IRI individual, ELConceptDescription conceptExpression) {
    if (conceptExpression.isBot())
      return false;
    if (conceptExpression.isTop())
      return true;
    return conceptExpression.getConceptNames().parallelStream().allMatch(
        conceptName -> conceptNameExtensions.get(
            conceptName).contains(
            individual)) && conceptExpression.getExistentialRestrictions().parallelStream().allMatch(
        existentialRestriction -> getRoleSuccessorStream(
            existentialRestriction.first(),
            individual).anyMatch(
            successor -> isInstanceOf(
                successor,
                existentialRestriction.second())));
  }

//  @Override
//  public Set<IRI> getConceptExpressionExtension(final ELNormalForm conceptExpression) {
//    if (conceptExpression.isBot())
//      return Collections.emptySet();
//    if (conceptExpression.isTop())
//      return getDomain();
//    return getDomain()
//        .parallelStream()
//        .filter(individual -> isInstanceOf(individual, conceptExpression))
//        .collect(Collectors.toSet());
////    return getDomain()
////        .parallelStream()
////        .filter(
////            individual -> conceptExpression
////                .getConceptNames()
////                .parallelStream()
////                .allMatch(conceptName -> getConceptNameExtension(conceptName).contains(individual))
////                && conceptExpression
////                    .getExistentialRestrictions()
////                    .parallelStream()
////                    .allMatch(
////                        existentialRestriction -> getRoleSuccessorStream(existentialRestriction.first(), individual)
////                            .anyMatch(successor -> isInstanceOf(successor, existentialRestriction.second()))))
////        .collect(Collectors.toSet());
////    final Set<IRI> extension = new HashSet<IRI>();
////    extension.addAll(getDomain());
////    for (IRI conceptName : concept.getConceptNames())
////      extension.retainAll(getConceptNameExtension(conceptName));
////    for (Pair<IRI, ELNormalForm> existentialRestriction : concept.getExistentialRestrictions())
////      extension.retainAll(getExistentialRestrictionExtension(existentialRestriction));
////    return extension;
//  }
//
//  private final Set<IRI> getExistentialRestrictionExtension(final Pair<IRI, ELNormalForm> existentialRestriction) {
//    final Set<IRI> extension = new HashSet<IRI>();
//    for (Pair<IRI, IRI> pair : getRoleNameExtension(existentialRestriction.x()))
//      if (getConceptExpressionExtension(existentialRestriction.y()).contains(pair.y()))
//        extension.add(pair.x());
//    return extension;
//  }

  @Override
  public final boolean satisfies(final ELConceptInclusion gci) {
    return isSubsumedBy(
        gci.getSubsumee(),
        gci.getSubsumer());
  }

  @Override
  public final boolean models(ELTBox tBox) {
    return tBox.getGCIs().parallelStream().allMatch(
        this::satisfies);
  }

  @Override
  public ELConceptDescription getMostSpecificConcept(
      final IRI individual,
      final int roleDepth,
      final int maxCardinality,
      final Constructor... constructors) {
    checkRoleDepth(roleDepth);
    final Set<IRI> conceptNames = new HashSet<IRI>(conceptNamesPerIndividual.get(individual));
    final Set<Pair<IRI, ELConceptDescription>> existentialRestrictions;
//    for (IRI conceptName : conceptNameExtensions.keySet())
//      if (getConceptNameExtension(conceptName).contains(individual))
//        conceptNames.add(conceptName);
    if (roleDepth > 0)
      existentialRestrictions = roleSuccessors.get(
          individual).parallelStream().map(
          p -> {
            return new Pair<IRI, ELConceptDescription>(p.x(), getMostSpecificConcept(
                p.y(),
                0,
                roleDepth - 1));
          }).collect(
          Collectors.toSet());
//      for (IRI roleName : roleNameExtensions.keySet())
//        for (Pair<IRI, IRI> pair : getRoleNameExtension(roleName))
//          if (pair.x().equals(individual))
//            existentialRestrictions.add(new Pair<IRI, ELNormalForm>(roleName, _getMostSpecificConcept(
//                pair.y(),
//                roleDepth - 1)));
    else
      existentialRestrictions = new HashSet<Pair<IRI, ELConceptDescription>>();
    return new ELConceptDescription(conceptNames, existentialRestrictions).minimize();
  }

  @Override
  public final ELConceptDescription getMostSpecificConcept(
      final Set<IRI> individuals,
      final int roleDepth,
      final int maxCardinality,
      final Constructor... constructors) {
    checkRoleDepth(roleDepth);
    return ELLeastCommonSubsumer._of(Collections2.transform(
        individuals,
        individual -> {
          return getMostSpecificConcept(
              individual,
              0,
              roleDepth);
        }));
  }

//  private Set<ELNormalForm> getAllMostSpecificConcepts(final int roleDepth) {
//    System.out.println("Computing all mmscs...");
//    final Set<ELNormalForm> result = new HashSet<ELNormalForm>();
//    if (roleDepth > -1) {
//      final Map<IRI, ELNormalForm> mmscs = new HashMap<IRI, ELNormalForm>();
//      final Map<Integer, Map<Set<IRI>, ELNormalForm>> _mmscs = new HashMap<Integer, Map<Set<IRI>, ELNormalForm>>();
//      for (IRI individual : getDomain())
//        mmscs.put(individual, getMostSpecificConcept(individual, roleDepth));
//      int c = 0;
//      for (int i = 2; i < getDomain().size(); i++) {
//        System.out.println("cardinality " + i);
//        final HashMap<Set<IRI>, ELNormalForm> map = new HashMap<Set<IRI>, ELNormalForm>();
//        _mmscs.put(i, map);
//        if (i == 2) {
//          for (Entry<IRI, ELNormalForm> e1 : mmscs.entrySet())
//            for (Entry<IRI, ELNormalForm> e2 : mmscs.entrySet())
//              if (!e1.getKey().equals(e2.getKey())) {
//                final HashSet<IRI> key = Sets.newHashSet(e1.getKey(), e2.getKey());
//                if (!map.containsKey(key)) {
//                  map.put(key, ELLeastCommonSubsumer._of(e1.getValue(), e2.getValue()).minimize());
//                  System.out.println(c++);
//                }
//              }
//        } else {
//          for (Entry<IRI, ELNormalForm> e1 : mmscs.entrySet())
//            for (Entry<Set<IRI>, ELNormalForm> e2 : _mmscs.get(i - 1).entrySet())
//              if (!e2.getKey().contains(e1.getKey())) {
//                final HashSet<IRI> key = Sets.newHashSet(Sets.union(e2.getKey(), Collections.singleton(e1.getKey())));
//                if (!map.containsKey(key)) {
//                  map.put(key, ELLeastCommonSubsumer._of(e1.getValue(), e2.getValue()).minimize());
//                  System.out.println(c++);
//                }
//              }
//        }
//      }
//      result.addAll(mmscs.values());
//      for (Entry<Integer, Map<Set<IRI>, ELNormalForm>> entry : _mmscs.entrySet())
//        result.addAll(entry.getValue().values());
//    }
//    return result;
//  }

  @Override
  protected final SetList<ELConceptDescription> getAttributeSetForInducedContext(
      final int roleDepth,
      final int maxCardinality,
      final Constructor... constructors) {
    final Set<ELConceptDescription> mmscs = getAllMostSpecificConcepts(
        roleDepth - 1,
        0);
    final SetList<ELConceptDescription> _codomain = new HashSetArrayList<ELConceptDescription>();
    _codomain.add(ELConceptDescription.bot());
    _codomain.addAll(Collections2.transform(
        signature.getConceptNames(),
        ELConceptDescription::conceptName));
    for (IRI roleName : signature.getRoleNames())
      _codomain.addAll(Collections2.transform(
          mmscs,
          mmsc -> {
            return ELConceptDescription.existentialRestriction(Pair.of(
                roleName,
                mmsc));
          }));
    return _codomain;
  };

  @Override
  protected final Set<Implication<IRI, ELConceptDescription>> getBackgroundImplications(
      final Context<IRI, ELConceptDescription> inducedContext,
      final ELTBox backgroundTBox) {
    final BiPredicate<ELConceptDescription, ELConceptDescription> subsumptionTest;
    if (backgroundTBox == null)
      subsumptionTest = (concept1, concept2) -> ELReasoner.isSubsumedBy(
          concept1,
          concept2);
    else
      subsumptionTest = (concept1, concept2) -> ELReasoner.isSubsumedBy(
          concept1,
          concept2,
          backgroundTBox);
    final Set<Implication<IRI, ELConceptDescription>> backgroundImplications =
        new HashSet<Implication<IRI, ELConceptDescription>>();
    for (ELConceptDescription concept1 : inducedContext.colHeads())
      for (ELConceptDescription concept2 : inducedContext.colHeads())
        if (!concept1.equals(concept2))
          if (subsumptionTest.test(
              concept1,
              concept2))
            backgroundImplications.add(new Implication<IRI, ELConceptDescription>(
                Collections.singleton(concept1),
                Collections.singleton(concept2),
                Collections.emptySet()));
    return backgroundImplications;
  }

  @Override
  public final ELTBox computeTBoxBase(
      final int roleDepth,
      final int maxCardinality,
      final ELTBox backgroundTBox,
      final Constructor... constructors) {
    checkRoleDepth(roleDepth);
    final ELTBox tbox = new ELTBox();
    final Context<IRI, ELConceptDescription> inducedContext = getInducedContext(
        roleDepth,
        maxCardinality);
    final Set<Implication<IRI, ELConceptDescription>> backgroundImplications = getBackgroundImplications(
        inducedContext,
        backgroundTBox);
    final ResultC<IRI, ELConceptDescription> result = NextClosures6C.computeWithBackgroundImplications(
        inducedContext,
        backgroundImplications,
        false);
    for (Entry<Set<ELConceptDescription>, Set<ELConceptDescription>> entry : result.implications.entrySet())
      tbox.getGCIs().add(
          new ELConceptInclusion(ELConceptDescription.conjunction(
              entry.getKey()).minimize(), ELConceptDescription.conjunction(
              entry.getValue()).minimize()));
    return tbox;
  }

  public static final ELInterpretation fromTriples(final File rdfFile, final IRI baseIRI, final String isaRole) {
    final ELInterpretation i = new ELInterpretation(baseIRI);
    final Multimap<String, String> concepts = HashMultimap.create();
    final Multimap<String, Pair<String, String>> roles = HashMultimap.create();
    final Iterator<String> it = IterableFile.iterator(rdfFile);
    while (it.hasNext()) {
      String next = it.next();
      while (next.contains("  "))
        next = next.replace(
            "  ",
            " ");
      next = next.replace(
          "<",
          "").replace(
          ">",
          "");
      final String[] triple = next.split(" ");
      if (triple.length > 2)
        if (triple[1].contains(isaRole)) {
          concepts.put(
              triple[0],
              triple[2]);
        } else {
          roles.put(
              triple[1],
              new Pair<String, String>(triple[0], triple[2]));
        }
    }
    for (Entry<String, String> entry : concepts.entries()) {
      final IRI c = IRI.create(entry.getKey());
      final IRI d = IRI.create(entry.getValue());
      i.getSignature().getConceptNames().add(
          d);
      i.getDomain().add(
          c);
      i.addConceptNameAssertion(
          d,
          c);
    }
    for (Entry<String, Pair<String, String>> entry : roles.entries()) {
      final IRI r = IRI.create(entry.getKey());
      final IRI d = IRI.create(entry.getValue().x());
      final IRI e = IRI.create(entry.getValue().y());
      i.getSignature().getRoleNames().add(
          r);
      i.getDomain().add(
          d);
      i.getDomain().add(
          e);
      i.addRoleNameAssertion(
          r,
          d,
          e);
    }
    return i;
  }

  public static final ELInterpretation fromTriples(final List<IRI[]> triples, final IRI baseIRI, final IRI isaRole) {
    final ELInterpretation i = new ELInterpretation(baseIRI);
    for (IRI[] triple : triples) {
      if (triple[1].equals(isaRole)) {
        i.getSignature().getConceptNames().add(
            triple[2]);
        i.getDomain().add(
            triple[0]);
        i.addConceptNameAssertion(
            triple[2],
            triple[0]);
      } else {
        i.getSignature().getRoleNames().add(
            triple[1]);
        i.getDomain().add(
            triple[0]);
        i.getDomain().add(
            triple[2]);
        i.addRoleNameAssertion(
            triple[1],
            triple[0],
            triple[2]);
      }
    }
    return i;
  }

}
