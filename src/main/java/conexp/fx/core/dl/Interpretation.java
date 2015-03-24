package conexp.fx.core.dl;

import java.util.Collection;
import java.util.Set;

import conexp.fx.core.closureoperators.ClosureOperator;
import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.context.Context;

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

  public C getMostSpecificConcept(I individual, int roleDepth);

  public C getMostSpecificConcept(Set<I> individuals, int roleDepth);

  public Set<C> getAllMostSpecificConcepts(int roleDepth);

  public Context<I, C> getInducedContext(int roleDepth);

//  public T computeTBoxBase(int roleDepth) throws Exception;

  public T computeTBoxBase(int roleDepth, T backgroundOntology) throws Exception;

  public ClosureOperator<I> getClosureOperator(int roleDepth);

  default void checkRoleDepth(final int roleDepth) {
    if (roleDepth < 0)
      throw new IllegalArgumentException("Negative role-depths are not allowed.");
  }

}
