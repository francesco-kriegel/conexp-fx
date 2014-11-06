package conexp.fx.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.Assert;
import conexp.fx.core.algorithm.nextclosure.NextImplication;
import conexp.fx.core.collections.setlist.SetLists;
import conexp.fx.core.context.ImplicationSet;
import conexp.fx.core.context.MatrixContext;
import de.tudresden.inf.tcs.fcalib.Implication;

public final class ImplicationTest<G, M> extends TestingData<G, M, ImplicationSet<M>> {

  @SafeVarargs
  public ImplicationTest(final MatrixContext<G, M>... contexts) {
    this(Arrays.asList(contexts));
  }

  public ImplicationTest(final Collection<MatrixContext<G, M>> contexts) {
    super();
    for (MatrixContext<G, M> cxt : contexts)
      if (cxt.colHeads().size() < 30)
        data().put(cxt, stemBase(cxt));
  }

  private final ImplicationSet<M> stemBase(final MatrixContext<G, M> cxt) {
    final ImplicationSet<M> stemBase = new ImplicationSet<M>();
    for (Entry<Set<M>, Set<M>> pseudoIntent : pseudoIntents(cxt).entrySet())
      stemBase.add(new Implication<M>(pseudoIntent.getKey(), pseudoIntent.getValue()));
    return stemBase;
  }

  /**
   * 
   * computes all pseudo-intents of a {@link MatrixContext}. This method may be slow, it is only intended for testing
   * purposes.
   * 
   * Definition: For a given context {@code (G,M,I)} a {@literal pseudo-intent} is a subset {@code B} of the codomain
   * {@code M}, such that it is no intent (i.e. {@code B≠B''}) and furthermore each strictly smaller pseudo-intent
   * {@code D⊊B} has its closure in {@code B} (i.e. {@code D''⊆B}).
   * 
   * @param a
   *          {@link MatrixContext} cxt
   * @return a {@link Map} whose keys are all pseudo-intents and their intents as values
   */
  private final Map<Set<M>, Set<M>> pseudoIntents(final MatrixContext<G, M> cxt) {
    final Map<Set<M>, Set<M>> pseudoIntents = new HashMap<Set<M>, Set<M>>();
    // computes all pseudo-intents from smallest to biggest
    for (Set<M> attributes : SetLists.powerSet(cxt.colHeads())) {
      final Set<M> intent = cxt.intent(attributes);
      // checks whether attributes is already an intent
      if (attributes.containsAll(intent))
        continue;
      // checks if all strictly smaller pseudo-intents have their closure in attributes
      boolean isPseudoIntent = true;
      for (Entry<Set<M>, Set<M>> pseudoIntent : pseudoIntents.entrySet())
        if (attributes.size() > pseudoIntent.getKey().size() && attributes.containsAll(pseudoIntent.getKey())
            && !attributes.containsAll(pseudoIntent.getValue())) {
          // no pseudo-intent
          isPseudoIntent = false;
          break;
        }
      if (isPseudoIntent)
        pseudoIntents.put(attributes, intent);
    }
    return pseudoIntents;
  }

  public void run() {
    for (Entry<MatrixContext<G, M>, ImplicationSet<M>> entry : data().entrySet()) {
      final ImplicationSet<M> expected = entry.getValue();
      final ImplicationSet<M> actual = fromNI(new NextImplication<G, M>(entry.getKey()));
      Assert.assertEquals(expected.size(), actual.size());
      Assert.assertEquals(expected, actual);
      System.out.println(expected);
      System.out.println(actual);
    }
  }

  private ImplicationSet<M> fromNI(final NextImplication<G, M> ni) {
    final ImplicationSet<M> is = new ImplicationSet<M>();
    final Iterator<Implication<M>> it = ni.iterator();
    while (it.hasNext()) {
      final Implication<M> next = it.next();
      if (next != null)
        is.add(next);
    }
    return is;
  }

}
