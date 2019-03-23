package conexp.fx.core.dl.deprecated;

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

import java.util.Collection;
import java.util.Set;

import conexp.fx.core.collections.Pair;
import conexp.fx.core.context.Context;
import conexp.fx.core.math.ClosureOperator;

@Deprecated
public interface Interpretation<I, C, G, T> {

  public Set<I> getDomain();

  public Signature getSignature();

  public boolean addConceptNameAssertion(I conceptName, I individual);

  public boolean addRoleNameAssertion(I roleName, I individual1, I individual2);

  public Collection<I> getConceptNameExtension(I conceptName);

  public Collection<Pair<I, I>> getRoleNameExtension(I roleName);

  public Set<I> getRoleSuccessors(I roleName, I individual);

  public Set<I> getRolePredecessors(I roleName, I individual);

  public boolean isInstanceOf(I individual, C conceptExpression);

  public Set<I> getConceptExpressionExtension(C conceptExpression);

  public boolean subsumes(C subsumer, C subsumee);

  public boolean isSubsumedBy(C subsumee, C subsumer);

  public boolean satisfies(G gci);

  public boolean models(T tbox);

  public C getMostSpecificConcept(I individual, int roleDepth, int maxCardinality, Constructor... constructors);

  public C getMostSpecificConcept(Set<I> individuals, int roleDepth, int maxCardinality, Constructor... constructors);

  public Set<C> getAllMostSpecificConcepts(int roleDepth, int maxCardinality, Constructor... constructors);

  public Context<I, C>
      getInducedContext(Collection<I> individuals, int roleDepth, int maxCardinality, Constructor... constructors);

//  public T computeTBoxBase(int roleDepth) throws Exception;

  public T computeTBoxBase(int roleDepth, int maxCardinality, T backgroundOntology, Constructor... constructors)
      throws Exception;

  public ClosureOperator<I> getClosureOperator(int roleDepth, int maxCardinality, Constructor... constructors);

  default void checkRoleDepth(final int roleDepth) {
    if (roleDepth < 0)
      throw new IllegalArgumentException("Negative role-depths are not allowed.");
  }

}
