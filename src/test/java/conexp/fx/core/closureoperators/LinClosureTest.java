package conexp.fx.core.closureoperators;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import junit.framework.Assert;

import org.junit.Test;

import conexp.fx.core.implication.Implication;

public class LinClosureTest {

  public long timeForNaiveClosure      = 0;
  public long timeForNaiveClosure2     = 0;
  public long timeForNaiveClosureP     = 0;
  public long timeForOptimizedClosure  = 0;
  public long timeForOptimizedClosureP = 0;
  public long timeForLinClosure        = 0;

  @Test
  public void testLinClosure() {

    long start;

    final Set<Integer> baseSet = Stream.iterate(
        0,
        n -> n + 1).limit(
        1048576).collect(
        Collectors.toSet());
    for (int i = 0; i < 64; i++) {
      final Set<Integer> set = generateRandomSubset(
          baseSet,
          rng.nextFloat());
      final Set<Implication<Object, Integer>> implications = generateRandomImplicationSet(
          baseSet,
          8192);

      for (int j = 0; j < 64; j++) {

        start = System.currentTimeMillis();
        final Set<Integer> naiveClosure = ClosureOperator.naiveClosure(
            set,
            implications);
        timeForNaiveClosure += System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        final Set<Integer> naiveClosure2 = ClosureOperator.naiveClosure2(
            set,
            implications);
        timeForNaiveClosure2 += System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        final Set<Integer> optimizedClosure = ClosureOperator.optimizedClosure(
            set,
            implications);
        timeForOptimizedClosure += System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        final Set<Integer> naiveClosureP = ClosureOperator.naiveClosureP(
            set,
            implications);
        timeForNaiveClosureP += System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        final Set<Integer> optimizedClosureP = ClosureOperator.optimizedClosureP(
            set,
            implications);
        timeForOptimizedClosureP += System.currentTimeMillis() - start;

//      final long start2 = System.currentTimeMillis();
//      final Set<Integer> linClosure = ClosureOperator.linClosure(
//          set,
//          implications);
//      timeForLinClosure += System.currentTimeMillis() - start2;

        Assert.assertEquals(
            naiveClosure,
            naiveClosure2);
        Assert.assertEquals(
            naiveClosure,
            optimizedClosure);
        Assert.assertEquals(
            naiveClosure,
            naiveClosureP);
        Assert.assertEquals(
            naiveClosure,
            optimizedClosureP);
//      try {
//        Assert.assertEquals(
//            naiveClosure,
//            linClosure);
//      } catch (AssertionFailedError e) {
//        System.out.println(Sets.difference(
//            naiveClosure,
//            linClosure));
//        System.out.println(Sets.difference(
//            linClosure,
//            naiveClosure));
//        implications.forEach(System.out::println);
//        throw e;
//      }
//      Assert.assertEquals(
//          naiveClosure,
//          naiveClosureP);
      }
    }
    System.out.println("time for naive closure     (single)   : " + timeForNaiveClosure + " ms");
    System.out.println("time for naive closure 2   (single)   : " + timeForNaiveClosure2 + " ms");
    System.out.println("time for naive closure     (parallel) : " + timeForNaiveClosureP + " ms");
    System.out.println("time for optimized closure (single)   : " + timeForOptimizedClosure + " ms");
    System.out.println("time for optimized closure (parallel) : " + timeForOptimizedClosureP + " ms");
    System.out.println("time for lin closure       (single)   : " + timeForLinClosure + " ms");
  }

  public static final Random rng = new Random();

  public static final <G, M> Set<Implication<G, M>> generateRandomImplicationSet(final Set<M> baseSet, final int size) {
    final Set<Implication<G, M>> implications = new HashSet<Implication<G, M>>();
    for (int n = 0; n < rng.nextInt(size); n++) {
      final Set<M> premise = generateRandomSubset(
          baseSet,
          rng.nextFloat());
      final Set<M> conclusion = generateRandomSubset(
          baseSet,
          rng.nextFloat());
      conclusion.removeAll(premise);
      final Implication<G, M> implication = new Implication<G, M>(premise, conclusion, Collections.emptySet());
      implications.add(implication);
    }
    return implications;
  }

  public static final <M> Set<M> generateRandomSubset(final Set<M> baseSet, final float threshold) {
    final Set<M> subset = new HashSet<M>();
    for (M m : baseSet)
      if (rng.nextFloat() < threshold / 2f)
        subset.add(m);
    return subset;
  }

}
