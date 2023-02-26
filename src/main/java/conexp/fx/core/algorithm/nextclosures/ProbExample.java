package conexp.fx.core.algorithm.nextclosures;

/*-
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2023 Francesco Kriegel
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import conexp.fx.core.collections.Pair;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.Implication;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter;

public final class ProbExample {

  private static Set<Implication<String, String>> backgroundImplications;
  private static Map<String, Set<Integer>>        elems;
  private static Map<String, Double>              probs;

  private static final void e(final String s, final Integer... es) {
    elems.put(s, Sets.newHashSet(es));
  }

  private static final void p(final String s, final Double p) {
    probs.put(s, p);
  }

  private static final void f(final String p, final String... cs) {
    for (String c : Arrays.asList(cs))
      backgroundImplications.add(new Implication<String, String>(Collections.singleton(p), Collections.singleton(c)));
  }

  private static final void g(final String p, final String q, final String c) {
    backgroundImplications.add(new Implication<String, String>(Sets.newHashSet(p, q), Collections.singleton(c)));
  }

  public static final void main(final String[] args) throws Exception {
    elems = new HashMap<>();
//    e("A", 2);
//    e("B", 3);
//    e("C", 1, 3);
//    e("D", 2, 3);
//    e("E", 1, 2, 3);
    e("F", 2);
    e("G", 3);
    e("H", 1, 3);
    e("I", 2, 3);
    e("J", 1, 2, 3);
    e("K", 2);
    e("L", 3);
    e("M", 1, 3);
    e("N", 2, 3);
    e("O", 1, 2, 3);
    e("P", 2);
    e("Q", 3);
    e("R", 1, 3);
    e("S", 2, 3);
    e("T", 1, 2, 3);
    e("U", 2);
    e("V", 3);
    e("W", 1, 3);
    e("X", 2, 3);
    e("Y", 1, 2, 3);
    e("Z", 2);
    e("0", 3);
    e("1", 1, 3);
    e("2", 2, 3);
    e("3", 1, 2, 3);

    probs = new HashMap<>();
//    p("A", 0d);
//    p("B", 0d);
//    p("C", 0d);
//    p("D", 0d);
//    p("E", 0d);
    p("F", 1d / 6d);
    p("G", 1d / 6d);
    p("H", 1d / 6d);
    p("I", 1d / 6d);
    p("J", 1d / 6d);
    p("K", 1d / 3d);
    p("L", 1d / 3d);
    p("M", 1d / 3d);
    p("N", 1d / 3d);
    p("O", 1d / 3d);
    p("P", 2d / 3d);
    p("Q", 2d / 3d);
    p("R", 2d / 3d);
    p("S", 2d / 3d);
    p("T", 2d / 3d);
    p("U", 5d / 6d);
    p("V", 5d / 6d);
    p("W", 5d / 6d);
    p("X", 5d / 6d);
    p("Y", 5d / 6d);
    p("Z", 1d);
    p("0", 1d);
    p("1", 1d);
    p("2", 1d);
    p("3", 1d);

    backgroundImplications = new HashSet<>();

//    f("3", "Y", "T", "O", "J", "E");
//    f("2", "X", "S", "N", "I", "D");
//    f("1", "W", "R", "M", "H", "C");
//    f("0", "V", "Q", "L", "G", "B");
//    f("Z", "U", "P", "K", "F", "A");
//
//    f("3", "2", "1", "0", "Z");
//    f("Y", "X", "W", "V", "U");
//    f("T", "S", "R", "Q", "P");
//    f("O", "N", "M", "L", "K");
//    f("J", "I", "H", "G", "F");
//    f("E", "D", "C", "B", "A");
//
//    f("2", "0", "Z");
//    f("X", "V", "U");
//    f("S", "Q", "P");
//    f("N", "L", "K");
//    f("I", "G", "F");
//    f("D", "B", "A");
//
//    f("1", "0");
//    f("W", "V");
//    f("R", "Q");
//    f("M", "L");
//    f("H", "G");
//    f("C", "B");

    for (String p : elems.keySet())
      for (String q : elems.keySet())
        if (!p.equals(q))
          if (probs.get(p) >= probs.get(q))
            if (elems.get(p).containsAll(elems.get(q)))
              f(p, q);

//    g("Z", "0", "2");
//    g("Z", "V", "X");
//    g("Z", "Q", "S");
//    g("Z", "L", "N");
//    g("Z", "G", "I");
//    g("Z", "B", "D");

    for (String p : elems.keySet())
      for (String q : elems.keySet())
        if (!p.equals(q))
          for (String c : elems.keySet())
            if (!c.equals(p))
              if (!c.equals(q))
                // if (Collections.disjoint(elems.get(p), elems.get(q)))
                if (probs.get(p) + probs.get(q) - 1d > 0d)
                if (probs.get(c) == probs.get(p) + probs.get(q) - 1d)
                if (elems.get(c).containsAll(Sets.union(elems.get(p), elems.get(q))))
                if (Sets.union(elems.get(p), elems.get(q)).containsAll(elems.get(c)))
                g(p, q, c);

    backgroundImplications.forEach(System.out::println);

    final MatrixContext<String, String> cxt =
        CXTImporter.read(new File("../../LaTeX/cla2015-prob-ijgs/example-scaling-2.cxt"));
    final Pair<Set<Concept<String, String>>, Set<Implication<String, String>>> result2 =
        NextClosures2.compute(cxt, backgroundImplications);
    final Set<Implication<String, String>> impls2 = result2.second();
    impls2
        .stream()
        .map(i -> new Implication<String, String>(b(i.getPremise()), b(i.getConclusion())))
        .sequential()
        .forEach(System.out::println);
//    final List<Set<Implication<String, String>>> foos = Lists.newArrayList();
//    for (int k = 0; k < 100; k++) {
//      foos.add(Sets.newHashSet());
//      impls2
//          .stream()
//          .map(i -> new Implication<String, String>(b(i.getPremise()), b(i.getConclusion())))
//          .sequential()
//          .forEach(foos.get(k)::add);
//    }
//    for (int k = 0; k < 100; k++)
//      for (int l = 0; l < 100; l++)
//        if (k != l) {
//          System.out.println(foos.get(k).containsAll(foos.get(l)));
//          System.out.println(foos.get(l).containsAll(foos.get(k)));
//          Sets.difference(foos.get(k), foos.get(l)).forEach(System.out::println);
//          Sets.difference(foos.get(l), foos.get(k)).forEach(System.out::println);
//          System.out.println();
//        }
//
//    System.out
//        .println(ClosureOperator.fromImplications(backgroundImplications, false, true).closure(Sets.newHashSet("3")));
//    System.out.println(
//        ClosureOperator.fromImplications(backgroundImplications, false, true).closure(Sets.newHashSet("1", "2", "Y")));
//    final Pair<Set<Concept<String, String>>, Set<Implication<String, String>>> resultC =
//        NextClosures2C.compute(cxt, ClosureOperator.fromImplications(backgroundImplications, false, false));
//    System.out.println();
//    System.out.println(result2.first().containsAll(resultC.first()));
//    System.out.println(resultC.first().containsAll(result2.first()));
//    System.out.println(result2.second().containsAll(resultC.second()));
//    System.out.println(resultC.second().containsAll(result2.second()));
//    System.out.println();
//    Sets.difference(result2.second(), resultC.second()).forEach(System.out::println);
//    System.out.println();
//    Sets.difference(resultC.second(), result2.second()).forEach(System.out::println);
//    System.out.println();
  }

  private static final Set<String> b(final Set<String> s) {
//    final HashSet<String> result = Sets.newHashSet(s);
//    final Set<String> r = Sets.newHashSet(s);
    final ArrayList<String> r = Lists.newArrayList(s);
    Collections.shuffle(r);
    String a;
    do {
      a = null;
      for (String x : r) {
        if (a != null)
          break;
        for (String y : r) {
          if (a != null)
            break;
          if (backgroundImplications.contains(new Implication<>(x, y))) {
            a = y;
            break;
          } else
            for (String z : r)
              if (backgroundImplications.contains(new Implication<>(Sets.newHashSet(x, y), z))) {
                a = z;
                break;
              }
        }
      }
      r.remove(a);
    } while (a != null);
//    result.removeAll(r);
//    return r;
    return Sets.newHashSet(r);
  }

}
