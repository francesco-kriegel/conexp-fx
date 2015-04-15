package conexp.fx.core.algorithm.exploration;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import conexp.fx.core.collections.setlist.SetList;

public class LexicalOrder {

  public static final <M> Comparator<M> getSetListComparator(final SetList<M> setList) {
    return (x, y) -> {
      final int i = setList.indexOf(x);
      final int j = setList.indexOf(y);
      if (i == j)
        return 0;
      if (i < j)
        return -1;
      return 1;
    };
  }

  public static final <M> boolean
      isSmaller(final SetList<M> base, final Set<M> set1, final Set<M> set2, final M element) {
    if (set1.equals(set2))
      return false;
    if (!set2.contains(element))
      return false;
    final SetView<M> symDiff = Sets.symmetricDifference(
        set1,
        set2);
    final M min = symDiff.stream().sorted(
        getSetListComparator(base)).findFirst().get();
    if (min.equals(element))
      return true;
    return false;
  }

  public static final <M> Set<M> oplus(final SetList<M> base, final Set<M> set, final M m) {
    final Set<M> result = new HashSet<M>(set);
    final SetList<M> filterM = base.subList(
        0,
        base.indexOf(m));
    result.retainAll(filterM);
    result.add(m);
    return result;
  }

//  private final Set<M> _APlusG(final Set<M> set, final M m) {
//    final Set<M> result = new HashSet<M>(set);
//    result.removeIf(el -> context.colHeads().indexOf(
//        el) > context.colHeads().indexOf(
//        m));
//    result.add(m);
//    return result;
//  }

}
