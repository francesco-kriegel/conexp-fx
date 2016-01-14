package conexp.fx.core.algorithm.nextclosure.exploration;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import conexp.fx.core.collections.setlist.SetList;

public class LexicalOrder {

  public static final <M> Comparator<M> getSetListComparator(final SetList<M> setList) {
    return (x, y) -> {
      final int i = setList.indexOf(
          x);
      final int j = setList.indexOf(
          y);
      if (i == j)
        return 0;
      if (i < j)
        return -1;
      return 1;
    };
  }

  public static final <M> boolean
      isSmaller(final SetList<M> base, final Set<M> set1, final Set<M> set2, final M element) {
    if (set1.equals(
        set2))
      return false;
    if (!set2.contains(
        element))
      return false;
//    return Stream
//        .concat(
//            set1.parallelStream().filter(
//                s -> !set2.contains(
//                    s)),
//            set2.parallelStream().filter(
//                s -> !set1.contains(
//                    s)))
//        .min(
//            getSetListComparator(
//                base))
//        .get()
//        .equals(
//            element);
    return Sets
        .symmetricDifference(
            set1,
            set2)
        .stream()
        .sorted(
            getSetListComparator(
                base))
        .findFirst()
        .get()
        .equals(
            element);
  }

  public static final <M> Set<M> oplus(final SetList<M> base, final Set<M> set, final M m) {
    final Set<M> result = new HashSet<M>(set);
    final SetList<M> filterM = base.subList(
        0,
        base.indexOf(
            m));
    result.retainAll(
        filterM);
    result.add(
        m);
    return result;
  }

}
