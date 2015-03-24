package conexp.fx.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.Assert;

import com.google.common.collect.Sets;

import conexp.fx.core.algorithm.nextclosure.NextConcept;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;

public final class ConceptTest<G, M> extends TestingData<G, M, Set<Concept<G, M>>> {

  @SafeVarargs
  public ConceptTest(final MatrixContext<G, M>... contexts) {
    this(Arrays.asList(contexts));
  }

  public ConceptTest(final Collection<MatrixContext<G, M>> contexts) {
    super();
    for (MatrixContext<G, M> cxt : contexts)
      if (cxt.rowHeads().size() < 18)
        data().put(cxt, concepts(cxt));
  }

  /**
   * 
   * computes all formal concepts of a {@link MatrixContext}. This method is just for testing more complex
   * implementations, don't expect it to be fast.
   * 
   * @param a
   *          {@link MatrixContext} cxt
   * @return a {@link Set} of all formal concepts
   */
  private final Set<Concept<G, M>> concepts(final MatrixContext<G, M> cxt) {
    final Set<Concept<G, M>> concepts = new HashSet<Concept<G, M>>();
    for (Set<G> objects : Sets.powerSet(cxt.rowHeads())) {
      final Set<M> intent = cxt.rowAnd(objects);
      final Set<G> extent = cxt.colAnd(intent);
      concepts.add(new Concept<G, M>(extent, intent));
    }
    return concepts;
  }

  public void run() {
    for (Entry<MatrixContext<G, M>, Set<Concept<G, M>>> entry : data().entrySet()) {
      final Set<Concept<G, M>> expected = entry.getValue();
      final Set<Concept<G, M>> actual = fromNC(new NextConcept<G, M>(entry.getKey()));
      Assert.assertEquals(expected.size(), actual.size());
      Assert.assertEquals(expected, actual);
    }
  }

  private Set<Concept<G, M>> fromNC(final NextConcept<G, M> nc) {
    final Set<Concept<G, M>> cs = new HashSet<Concept<G, M>>();
    final Iterator<Concept<G, M>> it = nc.iterator();
    while (it.hasNext())
      cs.add(it.next());
    return cs;
  }

}
