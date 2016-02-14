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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.elk.util.collections.Triple;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

import conexp.fx.core.algorithm.nextclosures.NextClosures1;
import conexp.fx.core.algorithm.nextclosures.NextClosures1.Result;
import conexp.fx.core.algorithm.nextclosures.NextClosuresC;
import conexp.fx.core.algorithm.nextclosures.NextClosuresC.ResultC;
import conexp.fx.core.collections.Pair;
import conexp.fx.core.collections.setlist.HashSetArrayList;
import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.context.Context;
import conexp.fx.core.context.Implication;
import conexp.fx.core.context.SparseContext;
import conexp.fx.core.math.ClosureOperator;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectComplementOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

//import org.semanticweb.owlapi.model.parameters.Imports;

public class OWLInterpretation extends AInterpretation<OWLClassExpression, OWLSubClassOfAxiom, OWLOntology> {

  public OWLInterpretation(final IRI baseIRI) {
    super(baseIRI);
  }

  public OWLInterpretation(final Signature signature) {
    super(signature);
  }

  public OWLInterpretation(final Signature signature, final Set<IRI> domain) {
    super(signature, domain);
  }

  public final OWLDataFactory df = OWLManager.getOWLDataFactory();

  @Override
  public final boolean isInstanceOf(final IRI individual, final OWLClassExpression conceptExpression) {
    if (conceptExpression.isOWLNothing())
      return false;
    if (conceptExpression.isOWLThing())
      return true;
    if (conceptExpression instanceof OWLClass)
      return getConceptNameExtension(((OWLClass) conceptExpression).getIRI()).contains(individual);
    if (conceptExpression instanceof OWLObjectIntersectionOf)
      return ((OWLObjectIntersectionOf) conceptExpression)
          .asConjunctSet()
          .parallelStream()
          .allMatch(c -> isInstanceOf(individual, c));
    if (conceptExpression instanceof OWLObjectSomeValuesFrom) {
      final OWLObjectSomeValuesFrom existentialRestriction = (OWLObjectSomeValuesFrom) conceptExpression;
      return getRoleSuccessorStream(((OWLObjectProperty) existentialRestriction.getProperty()).getIRI(), individual)
          .anyMatch(succ -> isInstanceOf(succ, existentialRestriction.getFiller()));
    }
    if (conceptExpression instanceof OWLObjectAllValuesFrom) {
      final OWLObjectAllValuesFrom valueRestriction = (OWLObjectAllValuesFrom) conceptExpression;
      return getRoleSuccessorStream(((OWLObjectProperty) valueRestriction.getProperty()).getIRI(), individual)
          .allMatch(succ -> isInstanceOf(succ, valueRestriction.getFiller()));
    }
    if (conceptExpression instanceof OWLObjectHasSelf) {
      final OWLObjectHasSelf existentialSelfRestriction = (OWLObjectHasSelf) conceptExpression;
      return getRoleNameExtension(((OWLObjectProperty) existentialSelfRestriction.getProperty()).getIRI())
          .contains(Pair.of(individual, individual));
    }
    if (conceptExpression instanceof OWLObjectMinCardinality) {
      final OWLObjectMinCardinality qualifiedNumberRestriction = (OWLObjectMinCardinality) conceptExpression;
      return getRoleSuccessorStream(((OWLObjectProperty) qualifiedNumberRestriction.getProperty()).getIRI(), individual)
          .filter(succ -> isInstanceOf(succ, qualifiedNumberRestriction.getFiller()))
          .skip(qualifiedNumberRestriction.getCardinality() - 1)
          .findAny()
          .isPresent();
    }
    if (conceptExpression instanceof OWLObjectMaxCardinality) {
      final OWLObjectMaxCardinality qualifiedNumberRestriction = (OWLObjectMaxCardinality) conceptExpression;
      return !getRoleSuccessorStream(
          ((OWLObjectProperty) qualifiedNumberRestriction.getProperty()).getIRI(),
          individual)
              .filter(succ -> isInstanceOf(succ, qualifiedNumberRestriction.getFiller()))
              .skip(qualifiedNumberRestriction.getCardinality())
              .findAny()
              .isPresent();
    }
    if (conceptExpression instanceof OWLObjectExactCardinality) {
      final OWLObjectExactCardinality qualifiedNumberRestriction = (OWLObjectExactCardinality) conceptExpression;
      return getRoleSuccessorStream(((OWLObjectProperty) qualifiedNumberRestriction.getProperty()).getIRI(), individual)
          .filter(succ -> isInstanceOf(succ, qualifiedNumberRestriction.getFiller()))
          .skip(qualifiedNumberRestriction.getCardinality() - 1)
          .findAny()
          .isPresent()
          && !getRoleSuccessorStream(
              ((OWLObjectProperty) qualifiedNumberRestriction.getProperty()).getIRI(),
              individual)
                  .filter(succ -> isInstanceOf(succ, qualifiedNumberRestriction.getFiller()))
                  .skip(qualifiedNumberRestriction.getCardinality())
                  .findAny()
                  .isPresent();
    }
    if (conceptExpression instanceof OWLObjectComplementOf) {
      final OWLObjectComplementOf primitiveNegation = (OWLObjectComplementOf) conceptExpression;
      if (primitiveNegation.isClassExpressionLiteral())
        return !isInstanceOf(individual, (OWLClass) primitiveNegation.getOperand());
    }
    throw new ELSyntaxException();
  }

//  @Override
//  public Set<IRI> getConceptExpressionExtension(final OWLClassExpression conceptExpression) {
//    if (conceptExpression.isOWLNothing())
//      return Collections.emptySet();
//    if (conceptExpression.isOWLThing())
//      return getDomain();
//    if (conceptExpression instanceof OWLClass) {
//      return Sets.newHashSet(getConceptNameExtension(((OWLClass) conceptExpression).getIRI()));
//    }
//    if (conceptExpression instanceof OWLObjectIntersectionOf) {
//      final OWLObjectIntersectionOf conjunction = (OWLObjectIntersectionOf) conceptExpression;
//      final Set<IRI> extension = new HashSet<IRI>();
//      extension.addAll(getDomain());
//      for (OWLClassExpression conjunct : conjunction.asConjunctSet()) {
//        extension.retainAll(getConceptExpressionExtension(conjunct));
//      }
//      return extension;
//    }
//    if (conceptExpression instanceof OWLObjectSomeValuesFrom) {
//      final OWLObjectSomeValuesFrom existentialRestriction = (OWLObjectSomeValuesFrom) conceptExpression;
//      final Set<IRI> extension = new HashSet<IRI>();
//      for (Pair<IRI, IRI> pair : getRoleNameExtension(((OWLObjectProperty) existentialRestriction.getProperty())
//          .getIRI()))
//        if (getConceptExpressionExtension(existentialRestriction.getFiller()).contains(pair.y()))
//          extension.add(pair.x());
//      return extension;
//    }
//    throw new ELSyntaxException();
//  }

  @Override
  public final boolean satisfies(final OWLSubClassOfAxiom gci) {
    return isSubsumedBy(gci.getSubClass(), gci.getSuperClass());
  }

  @Override
  public final boolean models(final OWLOntology tBox) {
    return tBox
        // .getTBoxAxioms(Imports.INCLUDED)
        .getTBoxAxioms(true).parallelStream().allMatch(
            gci -> (gci instanceof OWLSubClassOfAxiom) ? satisfies((OWLSubClassOfAxiom) gci) : true);
  }

  public OWLClassExpression getMostSpecificConceptALQ(
      final IRI individual,
      final int roleDepth,
      final int maxCardinality,
      final Constructor... constructors) {
    return getMostSpecificConceptALQ(Collections.singleton(individual), roleDepth, maxCardinality, constructors);
  }

  public OWLClassExpression getMostSpecificConceptALQ(
      final Set<IRI> individuals,
      final int roleDepth,
      final int maxCardinality,
      final Constructor... constructors) {
    checkRoleDepth(roleDepth);
    if (individuals.isEmpty())
      return df.getOWLNothing();
    final Set<OWLClassExpression> conjuncts = new HashSet<OWLClassExpression>();
    for (IRI conceptName : conceptNameExtensions.keySet())
      if (getConceptNameExtension(conceptName).containsAll(individuals))
        conjuncts.add(new OWLClassImpl(conceptName));
      else if (Arrays.asList(constructors).contains(Constructor.PRIMITIVE_NEGATION) && Collections2
          .filter(getDomain(), d -> !getConceptNameExtension(conceptName).contains(d))
          .containsAll(individuals))
        conjuncts.add(new OWLObjectComplementOfImpl(new OWLClassImpl(conceptName)));
    if (roleDepth > 0) {
      for (IRI roleName : signature.getRoleNames()) {
        final OWLObjectProperty property = df.getOWLObjectProperty(roleName);
        if (Arrays.asList(constructors).contains(Constructor.EXISTENTIAL_SELF_RESTRICTION)) {
          if (individuals.parallelStream().allMatch(
              individual -> getRoleNameExtension(roleName).contains(Pair.of(individual, individual))))
            conjuncts.add(df.getOWLObjectHasSelf(property));
        }
        if (Arrays.asList(constructors).contains(Constructor.EXISTENTIAL_RESTRICTION)) {
          for (Set<IRI> successors : getSuccessorSetsER(individuals, roleName))
            conjuncts.add(df.getOWLObjectSomeValuesFrom(
                property,
                getMostSpecificConceptALQ(successors, roleDepth - 1, maxCardinality, constructors)));
        }
        if (Arrays.asList(constructors).contains(Constructor.VALUE_RESTRICTION)) {
//        for (Set<IRI> successors : getSuccessorSetsVR(individuals, roleName))
          conjuncts.add(
              df.getOWLObjectAllValuesFrom(
                  property,
                  getMostSpecificConceptALQ(
                      getAllSuccessors(individuals, roleName),
                      roleDepth - 1,
                      maxCardinality,
                      constructors)));
        }
        if (Arrays.asList(constructors).contains(Constructor.UNQUALIFIED_AT_MOST_RESTRICTION)) {
          for (int cardinality = 1; cardinality < maxCardinality; cardinality++)
            for (Set<IRI> successors : getSuccessorSetsQLR(individuals, roleName, cardinality))
              conjuncts.add(df.getOWLObjectMaxCardinality(
                  cardinality,
                  property,
                  getMostSpecificConceptALQ(successors, roleDepth - 1, maxCardinality, constructors)));
        }
        if (Arrays.asList(constructors).contains(Constructor.QUALIFIED_AT_LEAST_RESTRICTION)) {
          for (int cardinality = 2; cardinality < maxCardinality; cardinality++)
            for (Set<IRI> successors : getSuccessorSetsQGR(individuals, roleName, cardinality))
              conjuncts.add(df.getOWLObjectMinCardinality(
                  cardinality,
                  property,
                  getMostSpecificConceptALQ(successors, roleDepth - 1, maxCardinality, constructors)));
        }
        // TODO
      }
    }
    if (conjuncts.isEmpty())
      return df.getOWLThing();
    if (conjuncts.size() == 1)
      return conjuncts.iterator().next();
    return new OWLObjectIntersectionOfImpl(conjuncts);
  }

  protected final Set<IRI> getAllSuccessors(final Set<IRI> individuals, final IRI roleName) {
    if (individuals.isEmpty())
      return Collections.emptySet();
    final Optional<Stream<IRI>> optional = individuals
        .parallelStream()
        .map(individual -> getRoleSuccessorStream(roleName, individual))
        .reduce(Stream::concat);
    if (optional.isPresent())
      return optional.get().collect(Collectors.toSet());
    System.out.println("optional is not present");
    System.out.println(getRoleSuccessors(roleName, individuals.iterator().next()));
    return Collections.emptySet();
  }

  protected final Set<Set<IRI>> getSuccessorSetsER(final Set<IRI> individuals, final IRI roleName) {
    return filterMinimal(
        Sets
            .powerSet(getAllSuccessors(individuals, roleName))
            .parallelStream()
            .filter(
                successors -> individuals.parallelStream().allMatch(individual -> successors.parallelStream().anyMatch(
                    successor -> getRoleNameExtension(roleName).contains(Pair.of(individual, successor)))))
            .collect(Collectors.toSet()));
  }

  protected final Set<Set<IRI>> getSuccessorSetsER2(final Set<IRI> individuals, final IRI roleName) {
    // this method should not filter the powerset, which is a costly operation, but instead create the minimal
    // successors directly
    Set<Set<IRI>> successorSets = Collections.emptySet();
    final Iterator<IRI> it = individuals.iterator();
    if (it.hasNext()) {
      successorSets = firstSuccessorSetsER(it.next(), roleName);
    }
    while (it.hasNext()) {
      successorSets = extendSuccessorSetsER(successorSets, it.next(), roleName);
    }
    return filterMinimal(successorSets);
  }

  private final Set<Set<IRI>> firstSuccessorSetsER(final IRI individual, final IRI roleName) {
    return getRoleSuccessorStream(roleName, individual)
        .map(successor -> Sets.newHashSet(successor))
        .collect(Collectors.toSet());
  }

  private final Set<Set<IRI>>
      extendSuccessorSetsER(final Set<Set<IRI>> successorSets, final IRI individual, final IRI roleName) {
    final Set<Set<IRI>> extendedSets = new HashSet<Set<IRI>>();
    final Set<Set<IRI>> extendedSetsP = Collections.synchronizedSet(extendedSets);
    successorSets.parallelStream().forEach(successorSet -> {
      getRoleSuccessorStream(roleName, individual).forEach(successor -> {
        final Set<IRI> extendedSet = new HashSet<IRI>();
        extendedSet.addAll(successorSet);
        extendedSet.add(successor);
        extendedSetsP.add(extendedSet);
      });
    });
    return extendedSets;
  }

  public static final <T> Set<Set<T>> filterMinimal(final Set<Set<T>> sets) {
    return OWLMinimizer.filterMinimalParallel(sets, (set1, set2) -> set1.containsAll(set2));
//    return sets.parallelStream().filter(
//        set1 -> !sets.parallelStream().anyMatch(
//            set2 -> !set1.equals(set2) && set1.containsAll(set2))).collect(
//        Collectors.toSet());
//    final Set<Set<T>> minSets = new HashSet<Set<T>>();
//    for (Set<T> set1 : sets) {
//      boolean isMinimal = true;
//      for (Set<T> set2 : sets)
//        if (!set1.equals(set2))
//          if (set1.containsAll(set2)) {
//            isMinimal = false;
//            break;
//          }
//      if (isMinimal)
//        minSets.add(set1);
//    }
//    return minSets;
  }

  private final Set<Set<IRI>>
      getSuccessorSetsQGR(final Set<IRI> individuals, final IRI roleName, final int cardinality) {
    return filterMinimal(
        Sets
            .powerSet(getAllSuccessors(individuals, roleName))
            .parallelStream()
            .filter(
                successors -> individuals
                    .parallelStream()
                    .allMatch(
                        individual -> successors
                            .parallelStream()
                            .filter(
                                successor -> getRoleNameExtension(roleName).contains(Pair.of(individual, successor)))
                            .skip(cardinality - 1)
                            .findAny()
                            .isPresent()))
            .collect(Collectors.toSet()));
  }

  private final Set<Set<IRI>>
      getSuccessorSetsQLR(final Set<IRI> individuals, final IRI roleName, final int cardinality) {
    return filterMinimal(
        Sets
            .powerSet(getAllSuccessors(individuals, roleName))
            .parallelStream()
            .filter(
                successors -> individuals
                    .parallelStream()
                    .allMatch(
                        individual -> !successors
                            .parallelStream()
                            .filter(
                                successor -> getRoleNameExtension(roleName).contains(Pair.of(individual, successor)))
                            .skip(cardinality)
                            .findAny()
                            .isPresent()))
            .collect(Collectors.toSet()));
  }

  @Override
  public OWLClassExpression getMostSpecificConcept(
      final IRI individual,
      final int roleDepth,
      final int maxCardinality,
      final Constructor... constructors) {
    return getMostSpecificConcept(Collections.singleton(individual), roleDepth, maxCardinality, constructors);
  }

  private final Map<Set<IRI>, OWLClassExpression> cache = new ConcurrentHashMap<>();

  @Override
  public OWLClassExpression getMostSpecificConcept(
      final Set<IRI> individuals,
      final int roleDepth,
      final int maxCardinality,
      final Constructor... constructors) {
    if (cache.containsKey(individuals))
      return cache.get(individuals);
    final OWLClassExpression mmsc = getMostSpecificConceptALQ(individuals, roleDepth, maxCardinality, constructors);
    cache.put(individuals, mmsc);
    return mmsc;
  }

  public OWLClassExpression _getMostSpecificConcept(final IRI individual, final int roleDepth) {
    checkRoleDepth(roleDepth);
    final Set<OWLClassExpression> conjuncts = new HashSet<OWLClassExpression>();
    for (IRI conceptName : conceptNameExtensions.keySet())
      if (getConceptNameExtension(conceptName).contains(individual))
        conjuncts.add(new OWLClassImpl(conceptName));
    if (roleDepth > 0)
      for (IRI roleName : roleNameExtensions.keySet())
        for (Pair<IRI, IRI> pair : getRoleNameExtension(roleName))
          if (pair.x().equals(individual))
            conjuncts.add(
                df.getOWLObjectSomeValuesFrom(
                    df.getOWLObjectProperty(roleName),
                    getMostSpecificConcept(pair.y(), 0, roleDepth - 1)));
    if (conjuncts.isEmpty())
      return df.getOWLThing();
    if (conjuncts.size() == 1)
      return conjuncts.iterator().next();
    return new OWLObjectIntersectionOfImpl(conjuncts);
  }

  public final OWLClassExpression _getMostSpecificConcept(final Set<IRI> individuals, final int roleDepth) {
    checkRoleDepth(roleDepth);
    return ELLeastCommonSubsumer.of(Collections2.transform(individuals, individual -> {
      return getMostSpecificConcept(individual, 0, roleDepth);
    }));
  }

//  private Set<OWLClassExpression> getAllMostSpecificConcepts(final int roleDepth) {
//    System.out.println("Computing all mmscs...");
//    final Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
//    if (roleDepth > -1) {
//      final Map<IRI, OWLClassExpression> mmscs = new HashMap<IRI, OWLClassExpression>();
//      final Map<Integer, Map<Set<IRI>, OWLClassExpression>> _mmscs =
//          new HashMap<Integer, Map<Set<IRI>, OWLClassExpression>>();
//      for (IRI individual : getDomain())
//        mmscs.put(individual, getMostSpecificConcept(individual, roleDepth));
//      for (int i = 2; i < getDomain().size(); i++) {
//        System.out.println("cardinality " + i);
//        final HashMap<Set<IRI>, OWLClassExpression> map = new HashMap<Set<IRI>, OWLClassExpression>();
//        _mmscs.put(i, map);
//        if (i == 2) {
//          for (Entry<IRI, OWLClassExpression> e1 : mmscs.entrySet())
//            for (Entry<IRI, OWLClassExpression> e2 : mmscs.entrySet())
//              if (!e1.getKey().equals(e2.getKey())) {
//                final HashSet<IRI> key = Sets.newHashSet(e1.getKey(), e2.getKey());
//                if (!map.containsKey(key)) {
//                  map.put(key, ELLeastCommonSubsumer.of(e1.getValue(), e2.getValue()));
//                  System.out.print(".");
//                }
//              }
//        } else {
//          for (Entry<IRI, OWLClassExpression> e1 : mmscs.entrySet())
//            for (Entry<Set<IRI>, OWLClassExpression> e2 : _mmscs.get(i - 1).entrySet())
//              if (!e2.getKey().contains(e1.getKey())) {
//                final HashSet<IRI> key = Sets.newHashSet(Sets.union(e2.getKey(), Collections.singleton(e1.getKey())));
//                if (!map.containsKey(key)) {
//                  map.put(key, ELLeastCommonSubsumer.of(e1.getValue(), e2.getValue()));
//                  System.out.print(".");
//                }
//              }
//        }
//      }
//      result.addAll(mmscs.values());
//      for (Entry<Integer, Map<Set<IRI>, OWLClassExpression>> entry : _mmscs.entrySet())
//        result.addAll(entry.getValue().values());
//    }
//    return result;
//  }

  @Override
  protected final SetList<OWLClassExpression> getAttributeSetForInducedContext(
      final int roleDepth,
      final int maxCardinality,
      final Constructor... constructors) {
    final Set<OWLClassExpression> mmscs = roleDepth > 0
        ? getAllMostSpecificConcepts(roleDepth - 1, maxCardinality, constructors) : Collections.emptySet();
    final SetList<OWLClassExpression> _codomain = new HashSetArrayList<OWLClassExpression>();
    _codomain.add(df.getOWLNothing());
    _codomain.addAll(Collections2.transform(signature.getConceptNames(), df::getOWLClass));
    if (Arrays.asList(constructors).contains(Constructor.PRIMITIVE_NEGATION))
      _codomain.addAll(
          Collections2.transform(
              signature.getConceptNames(),
              conceptName -> df.getOWLObjectComplementOf(df.getOWLClass(conceptName))));
    for (IRI roleName : signature.getRoleNames())
      for (Constructor c : constructors)
        switch (c) {
        case EXISTENTIAL_RESTRICTION:
          _codomain.addAll(Collections2.transform(mmscs, mmsc -> {
            return df.getOWLObjectSomeValuesFrom(df.getOWLObjectProperty(roleName), mmsc);
          }));
          break;
        case VALUE_RESTRICTION:
          _codomain.addAll(Collections2.transform(mmscs, mmsc -> {
            return df.getOWLObjectAllValuesFrom(df.getOWLObjectProperty(roleName), mmsc);
          }));
          break;
        case QUALIFIED_AT_LEAST_RESTRICTION:
          Stream
              .iterate(0, n -> n + 1)
              .limit(maxCardinality)
              .skip(2)
              .map(
                  cardinality -> Collections2
                      .transform(
                          mmscs,
                          mmsc -> df.getOWLObjectMinCardinality(cardinality, df.getOWLObjectProperty(roleName), mmsc))
                      .stream())
              .reduce(Stream::concat)
              .ifPresent(stream -> _codomain.addAll(stream.collect(Collectors.toSet())));
          break;
        case UNQUALIFIED_AT_MOST_RESTRICTION:
          Stream
              .iterate(0, n -> n + 1)
              .limit(maxCardinality)
              .map(cardinality -> df.getOWLObjectMaxCardinality(cardinality, df.getOWLObjectProperty(roleName)))
              .forEach(_codomain::add);
//          Stream.iterate(
//              0,
//              n -> n + 1).limit(
//              maxCardinality).skip(
//              1).map(
//              cardinality -> Collections2.transform(
//                  mmscs,
//                  mmsc -> df.getOWLObjectMaxCardinality(
//                      cardinality,
//                      df.getOWLObjectProperty(roleName),
//                      mmsc)).stream()).reduce(
//              Stream::concat).ifPresent(
//              stream -> _codomain.addAll(stream.collect(Collectors.toSet())));
          break;
        case EXISTENTIAL_SELF_RESTRICTION:
          _codomain.add(df.getOWLObjectHasSelf(df.getOWLObjectProperty(roleName)));
          break;
        }
    return _codomain;
  };

  @Override
  protected final Set<Implication<IRI, OWLClassExpression>> getBackgroundImplications(
      final Context<IRI, OWLClassExpression> inducedContext,
      final OWLOntology backgroundOntology) {
    final BiPredicate<OWLClassExpression, OWLClassExpression> subsumptionTest;
    if (backgroundOntology == null)
      subsumptionTest = (concept1, concept2) -> ELReasoner.isSubsumedBy(concept1, concept2);
    else
      subsumptionTest = (concept1, concept2) -> ELReasoner.isSubsumedBy(concept1, concept2, backgroundOntology);
    final Set<Implication<IRI, OWLClassExpression>> backgroundImplications =
        new HashSet<Implication<IRI, OWLClassExpression>>();
    for (OWLClassExpression concept1 : inducedContext.colHeads())
      for (OWLClassExpression concept2 : inducedContext.colHeads())
        if (!concept1.equals(concept2)) {
          if (subsumptionTest.test(concept1, concept2))
            backgroundImplications.add(
                new Implication<IRI, OWLClassExpression>(
                    Collections.singleton(concept1),
                    Collections.singleton(concept2),
                    Collections.emptySet()));
        }
    return backgroundImplications;
  }

  @Override
  public final OWLOntology computeTBoxBase(
      final int roleDepth,
      final int maxCardinality,
      final OWLOntology backgroundOntology,
      final Constructor... constructors) throws OWLOntologyCreationException {
    checkRoleDepth(roleDepth);
    final OWLOntologyManager om = OWLManager.createOWLOntologyManager();
    final OWLOntology ontology = om.createOntology();
    final Context<IRI, OWLClassExpression> inducedContext = getInducedContext(roleDepth, maxCardinality, constructors);
    final Set<Implication<IRI, OWLClassExpression>> backgroundImplications =
        getBackgroundImplications(inducedContext, backgroundOntology);
    final ResultC<IRI, OWLClassExpression> result =
        NextClosuresC.computeWithBackgroundImplications(inducedContext, backgroundImplications, true);
    for (Entry<Set<OWLClassExpression>, Set<OWLClassExpression>> entry : result.implications.entrySet())
      om.applyChange(
          new AddAxiom(
              ontology,
              df.getOWLSubClassOfAxiom(
                  ELConceptDescription
                      .of(df.getOWLObjectIntersectionOf(entry.getKey()))
                      .minimize()
                      .toOWLClassExpression(),
                  ELConceptDescription
                      .of(df.getOWLObjectIntersectionOf(entry.getValue()))
                      .minimize()
                      .toOWLClassExpression())));
    return ontology;
  }

  public final class Variable {

    final int index;

    public Variable(final int index) {
      super();
      this.index = index;
    }
  }

  private final SetList<ArrayList<IRI>> getFirstFunctions() {
    final SetList<ArrayList<IRI>> fs = new HashSetArrayList<ArrayList<IRI>>();
    for (IRI individual : getDomain()) {
      final ArrayList<IRI> f = new ArrayList<IRI>();
      f.add(individual);
      fs.add(f);
    }
    return fs;
  }

  private final SetList<ArrayList<IRI>> getNextFunctions(final SetList<ArrayList<IRI>> _fs) {
    final SetList<ArrayList<IRI>> fs = new HashSetArrayList<ArrayList<IRI>>();
    for (ArrayList<IRI> _f : _fs) {
      for (IRI individual : getDomain()) {
        final ArrayList<IRI> f = new ArrayList<IRI>(_f);
        f.add(individual);
        fs.add(f);
      }
    }
    return fs;
  }

  private final SetList<ArrayList<IRI>> getFunctions(final int roleDepth) {
    SetList<ArrayList<IRI>> fs = new HashSetArrayList<ArrayList<IRI>>();
    fs.add(new ArrayList<IRI>());
    for (int i = 0; i <= roleDepth; i++) {
      fs = getNextFunctions(fs);
    }
    return fs;
  }

  private final boolean applies(final ArrayList<IRI> f, final Triple<Integer, IRI, Integer> t) {
    final IRI d = f.get(t.getFirst());
    final IRI r = t.getSecond();
    final IRI e = f.get(t.getThird());
    return roleNameExtensions.get(r).contains(Pair.of(d, e));
  }

  public final SparseContext<ArrayList<IRI>, Triple<Integer, IRI, Integer>> getInducedRoleContext(final int roleDepth) {
    final SetList<ArrayList<IRI>> _domain = getFunctions(roleDepth + 1);

    final SetList<Triple<Integer, IRI, Integer>> _codomain = new HashSetArrayList<Triple<Integer, IRI, Integer>>();
    for (IRI roleName : signature.getRoleNames()) {
      for (int i = 2; i <= roleDepth; i++)
        for (int j = 1; j <= i; j++) {
          _codomain.add(new Triple<Integer, IRI, Integer>(j, roleName, j + 1));
          if (i != 1)
            _codomain.add(new Triple<Integer, IRI, Integer>(1, roleName, j + 1));
        }

    }
    final SparseContext<ArrayList<IRI>, Triple<Integer, IRI, Integer>> inducedRoleContext =
        new SparseContext<ArrayList<IRI>, Triple<Integer, IRI, Integer>>(_domain, _codomain, false);

    for (ArrayList<IRI> o : _domain)
      for (Triple<Integer, IRI, Integer> a : _codomain) {
        if (applies(o, a))
          inducedRoleContext.add(o, a);
      }

    return inducedRoleContext;
  }

  public final Set<OWLSubPropertyChainOfAxiom> getRoleInclusionBase(final int roleDepth) {
    final Set<OWLSubPropertyChainOfAxiom> base = new HashSet<OWLSubPropertyChainOfAxiom>();

    final SparseContext<ArrayList<IRI>, Triple<Integer, IRI, Integer>> cxt = getInducedRoleContext(roleDepth);
    final ClosureOperator<Triple<Integer, IRI, Integer>> clop = new ClosureOperator<Triple<Integer, IRI, Integer>>() {

      @Override
      public Set<Triple<Integer, IRI, Integer>> closure(Set<Triple<Integer, IRI, Integer>> set) {
        return set;
//        if (set.parallelStream().anyMatch(
//            t -> t.getFirst().equals(
//                1) && t.getThird().equals(
//                2)))
//          return new HashSet<Triple<Integer, IRI, Integer>>(set);
//        return new HashSet<Triple<Integer, IRI, Integer>>(cxt.colHeads());
      }

    };
    final Result<ArrayList<IRI>, Triple<Integer, IRI, Integer>> result = NextClosures1.compute(
        cxt,
        // clop,
        true);
    for (Entry<Set<Triple<Integer, IRI, Integer>>, Set<Triple<Integer, IRI, Integer>>> e : result.implications
        .entrySet()) {
      final List<Triple<Integer, IRI, Integer>> chain = new ArrayList<Triple<Integer, IRI, Integer>>(roleDepth);
      final Triple<Integer, IRI, Integer> conclusion;
      boolean chainEnded = false;
      int i;
      for (i = 1; !chainEnded || i <= roleDepth; i++) {
        final int j = i;
        final Optional<Triple<Integer, IRI, Integer>> opt =
            e.getKey().parallelStream().filter(t -> t.getFirst().equals(j) && t.getThird().equals(j + 1)).findFirst();
        if (opt.isPresent())
          chain.add(opt.get());
        else
          chainEnded = true;
      }
      if (!chain.isEmpty())
        System.out.println("found chain of size " + chain.size() + " " + chain);
      final int j = chain.size() + 1;// i;
      final Optional<Triple<Integer, IRI, Integer>> opt =
          e.getValue().parallelStream().filter(t -> t.getFirst().equals(1) && t.getThird().equals(j)).findFirst();
      if (opt.isPresent())
        conclusion = opt.get();
      else
        conclusion = null;
      if (conclusion != null) {
        final List<OWLObjectPropertyExpression> subProperty =
            chain.stream().map(t -> df.getOWLObjectProperty(t.getSecond())).collect(Collectors.toList());
        base.add(df.getOWLSubPropertyChainOfAxiom(subProperty, df.getOWLObjectProperty(conclusion.getSecond())));
      }
    }
    return base;
  }
}
