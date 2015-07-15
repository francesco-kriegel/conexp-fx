package conexp.fx.core.algorithm.nextclosures;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import conexp.fx.core.context.MatrixContext;
import de.tudresden.inf.tcs.fcalib.Implication;

public class GantersClosureContext {

  public static final void main(String[] args) {
    final long start = System.currentTimeMillis();
    final MatrixContext<Set<Integer>, Implication<Integer>> cxt = generateContext(6);
    final long time = System.currentTimeMillis() - start;
    System.out.println("Context generated in " + time + " ms.");
    System.out.println(cxt.rowHeads().size() + " objects");
    System.out.println(cxt.colHeads().size() + " attributes");

    final long start2 = System.currentTimeMillis();
    NextClosures.compute(
        cxt,
        true,
        12);
    final long time2 = System.currentTimeMillis() - start2;

    System.out.println("Computation took " + time2 + " ms.");
  }

  public static final MatrixContext<Set<Integer>, Implication<Integer>> generateContext(final int size) {
    final MatrixContext<Set<Integer>, Implication<Integer>> cxt =
        new MatrixContext<Set<Integer>, Implication<Integer>>(false);

    final HashSet<Integer> baseSet = Sets.newHashSet();
    for (int i = 1; i < 7; i++)
      baseSet.add(i);
    final Set<Set<Integer>> objects = Sets.newHashSet(Sets.powerSet(baseSet));
    objects.remove(baseSet);
    final Set<Implication<Integer>> attributes = Sets.newHashSet();
    for (Set<Integer> premise : objects)
      for (int i = 1; i < 7; i++)
        if (!premise.contains(i))
          attributes.add(new Implication<Integer>(premise, Sets.newHashSet(i)));

    cxt.rowHeads().addAll(
        objects);
    cxt.colHeads().addAll(
        attributes);

    for (Set<Integer> g : objects)
      for (Implication<Integer> m : attributes)
        if (!g.containsAll(m.getPremise()) || g.containsAll(m.getConclusion()))
          cxt.addFastSilent(
              g,
              m);

    return cxt;
  }

}
