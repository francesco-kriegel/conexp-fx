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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import conexp.fx.core.algorithm.nextclosure.NextClosure;
import conexp.fx.core.closureoperators.ClosureOperator;
import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.collections.setlist.HashSetArrayList;
import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.context.Context;
import conexp.fx.core.context.SparseContext;
import conexp.fx.core.implication.Implication;

public abstract class AInterpretation<C, G, T> implements Interpretation<IRI, C, G, T> {

  protected final Set<IRI>                      domain;
  protected final Signature                     signature;

  protected final Multimap<IRI, IRI>            conceptNameExtensions;
  protected final Multimap<IRI, Pair<IRI, IRI>> roleNameExtensions;

  public AInterpretation(final IRI baseIRI) {
    this(new Signature(baseIRI), new HashSet<IRI>());
  }

  public AInterpretation(final Signature signature) {
    this(signature, signature.getIndividualNames());
  }

  public AInterpretation(final Signature signature, final Set<IRI> domain) {
    super();
    this.domain = domain;
    this.signature = signature;
    this.conceptNameExtensions = HashMultimap.create();
    this.roleNameExtensions = HashMultimap.create();
  }

  @Override
  public Set<IRI> getDomain() {
    return domain;
  }

  @Override
  public Signature getSignature() {
    return signature;
  }

  @Override
  public boolean addConceptNameAssertion(final IRI conceptName, final IRI individual) {
    return conceptNameExtensions.put(
        conceptName,
        individual);
  }

  public boolean addConceptNameAssertion(final String conceptName, final String individual) {
    return addConceptNameAssertion(
        IRI.create(conceptName),
        IRI.create(individual));
  }

  @Override
  public boolean addRoleNameAssertion(final IRI roleName, final IRI individual1, final IRI individual2) {
    return roleNameExtensions.put(
        roleName,
        new Pair<IRI, IRI>(individual1, individual2));
  }

  public boolean addRoleNameAssertion(final String roleName, final String individual1, final String individual2) {
    return addRoleNameAssertion(
        IRI.create(roleName),
        IRI.create(individual1),
        IRI.create(individual2));
  }

  @Override
  public Collection<IRI> getConceptNameExtension(final IRI conceptName) {
    return conceptNameExtensions.get(conceptName);
  }

  @Override
  public Collection<Pair<IRI, IRI>> getRoleNameExtension(final IRI roleName) {
    return roleNameExtensions.get(roleName);
  }

  @Override
  public Set<IRI> getRoleSuccessors(final IRI roleName, final IRI individual) {
    return getRoleSuccessorStream(
        roleName,
        individual).collect(
        Collectors.toSet());
  }

  @Override
  public Set<IRI> getRolePredecessors(final IRI roleName, final IRI individual) {
    return getRolePredecessorStream(
        roleName,
        individual).collect(
        Collectors.toSet());
  }

  @Override
  public Set<IRI> getConceptExpressionExtension(C conceptExpression) {
    return getDomain().parallelStream().filter(
        individual -> isInstanceOf(
            individual,
            conceptExpression)).collect(
        Collectors.toSet());
  }

  @Override
  public boolean subsumes(final C subsumer, final C subsumee) {
    return getConceptExpressionExtension(
        subsumer).containsAll(
        getConceptExpressionExtension(subsumee));
  }

  @Override
  public boolean isSubsumedBy(final C subsumee, final C subsumer) {
    return getConceptExpressionExtension(
        subsumer).containsAll(
        getConceptExpressionExtension(subsumee));
  }

  @Override
  public Set<C> getAllMostSpecificConcepts(
      final int roleDepth,
      final int maxCardinality,
      final Constructor... constructors) {
    final Set<C> mmscs = new HashSet<C>();
    final Iterator<Set<IRI>> it = new NextClosure<IRI>(new HashSetArrayList<IRI>(domain), getClosureOperator(
        roleDepth,
        maxCardinality,
        constructors)).iterator();
    it.forEachRemaining(extension -> {
      mmscs.add(getMostSpecificConcept(
          extension,
          roleDepth,
          maxCardinality,
          constructors));
    });
    return mmscs;
  }

  protected abstract SetList<C> getAttributeSetForInducedContext(
      final int roleDepth,
      final int maxCardinality,
      final Constructor... constructors);

  @Override
  public Context<IRI, C> getInducedContext(
      final int roleDepth,
      final int maxCardinality,
      final Constructor... constructors) {
    checkRoleDepth(roleDepth);
    final SetList<IRI> _domain = new HashSetArrayList<IRI>(domain);
    final SetList<C> _codomain = getAttributeSetForInducedContext(
        roleDepth,
        maxCardinality,
        constructors);
    final Context<IRI, C> inducedContext = new SparseContext<IRI, C>(_domain, _codomain, false);
    for (C mmsc : _codomain)
      for (IRI individual : getConceptExpressionExtension(mmsc))
        inducedContext.add(
            individual,
            mmsc);
    return inducedContext;
  }

  protected abstract Set<Implication<IRI, C>> getBackgroundImplications(
      final Context<IRI, C> inducedContext,
      final T backgroundTBox);

  @Override
  public final ClosureOperator<IRI> getClosureOperator(
      final int roleDepth,
      final int maxCardinality,
      final Constructor... constructors) {
    return new ClosureOperator<IRI>() {

      @Override
      public final boolean isClosed(final Set<IRI> set) {
        return set.containsAll(closure(set));
      }

      @Override
      public final boolean close(final Set<IRI> set) {
        return !set.addAll(closure(set));
      }

      @Override
      public final Set<IRI> closure(final Set<IRI> set) {
        return getConceptExpressionExtension(getMostSpecificConcept(
            set,
            roleDepth,
            maxCardinality,
            constructors));
      }

    };
  }

  protected final Multimap<IRI, IRI>            conceptNamesPerIndividual = HashMultimap.create();
  protected final Multimap<IRI, Pair<IRI, IRI>> roleSuccessors            = HashMultimap.create();

  public Multimap<IRI, IRI> getConceptNameExtensions() {
    return conceptNameExtensions;
  }

  public Multimap<IRI, Pair<IRI, IRI>> getRoleNameExtensions() {
    return roleNameExtensions;
  }

  protected Stream<IRI> getRoleSuccessorStream(final IRI roleName, final IRI individual) {
    return getRoleNameExtension(
        roleName).parallelStream().filter(
        p -> p.x().equals(
            individual)).map(
        p -> p.y());
  }

  protected Stream<IRI> getRolePredecessorStream(final IRI roleName, final IRI individual) {
    return getRoleNameExtension(
        roleName).parallelStream().filter(
        p -> p.y().equals(
            individual)).map(
        p -> p.x());
  }

  /**
   * This method must be called prior to the computation of most specific concepts.
   */
  public final void updateSuccessorSets() {
    conceptNamesPerIndividual.clear();
    roleSuccessors.clear();
    for (Entry<IRI, IRI> entry : conceptNameExtensions.entries())
      conceptNamesPerIndividual.put(
          entry.getValue(),
          entry.getKey());
    for (Entry<IRI, Pair<IRI, IRI>> entry : roleNameExtensions.entries())
      roleSuccessors.put(
          entry.getValue().x(),
          Pair.of(
              entry.getKey(),
              entry.getValue().y()));
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(final Object obj) {
    if (!(obj instanceof Interpretation))
      return false;
    @SuppressWarnings("rawtypes")
    final Interpretation other = (Interpretation) obj;
    return this.getDomain().equals(
        other.getDomain()) && this.getSignature().equals(
        other.getSignature()) && getSignature().getConceptNames().parallelStream().allMatch(
        conceptName -> this.getConceptNameExtension(
            conceptName).equals(
            other.getConceptNameExtension(conceptName))) && getSignature().getRoleNames().parallelStream().allMatch(
        roleName -> this.getRoleNameExtension(
            roleName).equals(
            other.getRoleNameExtension(roleName)));
  }

  @Override
  public int hashCode() {
    return 7 + 11 * getDomain().hashCode() + 13 * getSignature().hashCode();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Interpretation\r\n");
    sb.append("with domain:\r\n" + getDomain().toString() + "\r\n");
    sb.append("on signature:\r\n" + getSignature().toString() + "\r\n");
    sb.append("with primitive extensions:\r\n");
    for (IRI conceptName : signature.getConceptNames())
      sb.append(conceptName + " :: " + getConceptNameExtension(
          conceptName).toString() + "\r\n");
    for (IRI roleName : signature.getRoleNames())
      sb.append(roleName + " :: " + getRoleNameExtension(
          roleName).toString() + "\r\n");
    return sb.toString();
  }

}
