package conexp.fx.core.algorithm.nextclosures;

/*-
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
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
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import conexp.fx.core.collections.Collections3;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.Implication;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter;
import conexp.fx.core.math.ClosureOperator;

public final class ProbDLExample {

  private static Set<Implication<String, String>> bimp;

  public static final void main(final String[] args) throws Exception {
    final File file = new File("../../LaTeX/cla2015-prob-ijgs/example-pdl-induced.cxt");
    final MatrixContext<String, String> cxt = CXTImporter.read(file);
    bimp = Sets.newHashSet(
        new Implication<>("$\\bot$", "$A_1$"),
        new Implication<>("$\\bot$", "$A_2$"),
        new Implication<>("$\\bot$", "$A_3$"),
        new Implication<>("$\\bot$", "$r_1:C_1$"),
        new Implication<>("$\\bot$", "$r_1:C_7$"),
        new Implication<>("$\\bot$", "$r_2:C_1$"),
        new Implication<>("$\\bot$", "$r_2:C_7$"),
        new Implication<>("$r_1:C_1$", "$r_1:C_3$"),
        new Implication<>("$r_1:C_3$", "$r_1:C_5$"),
        new Implication<>("$r_1:C_5$", "$r_1:C_2$"),
        new Implication<>("$r_1:C_7$", "$r_1:C_6$"),
        new Implication<>("$r_1:C_6$", "$r_1:C_4$"),
        new Implication<>("$r_1:C_6$", "$r_1:C_5$"),
        new Implication<>("$r_1:C_4$", "$r_1:C_2$"),
        new Implication<>("$r_2:C_1$", "$r_2:C_3$"),
        new Implication<>("$r_2:C_3$", "$r_2:C_5$"),
        new Implication<>("$r_2:C_5$", "$r_2:C_2$"),
        new Implication<>("$r_2:C_7$", "$r_2:C_6$"),
        new Implication<>("$r_2:C_6$", "$r_2:C_4$"),
        new Implication<>("$r_2:C_6$", "$r_2:C_5$"),
        new Implication<>("$r_2:C_4$", "$r_2:C_2$"));
    final Set<Double> probs =
        Sets.powerSet(Sets.newHashSet(1d / 2d, 1d / 3d, 1d / 6d)).stream().map(Collections3::sum).collect(
            Collectors.toSet());
    probs.remove(0d);
    probs.forEach(System.out::println);
    final Set<Implication<String, String>> i = NextClosures2.compute(cxt, bimp).second();
    i.stream().map(ProbDLExample::minimize).forEach(System.out::println);
    NextClosures2.compute(cxt).first().stream().map(Concept::getIntent).forEach(System.out::println);

    final Set<Implication<String, String>> j =
        NextClosures2C.compute(cxt, ClosureOperator.fromImplications(bimp)).second();
    System.out.println(i.size() == j.size());
    System.out.println(i.equals(j));
    System.out.println(Implication.equivalent(i, j));
  }

  private static final Implication<String, String> minimize(final Implication<String, String> impl) {
    return new Implication<>(b(impl.getPremise()), b(impl.getConclusion()));
  }

  private static final Set<String> b(final Set<String> s) {
//  final HashSet<String> result = Sets.newHashSet(s);
//  final Set<String> r = Sets.newHashSet(s);
    final ArrayList<String> r = Lists.newArrayList(s);
    Collections.shuffle(r);
    String a;
    do {
      a = null;
      for (String x : r)
        if (ClosureOperator
            .fromImplications(bimp, false, true)
            .closure(Sets.difference(Sets.newHashSet(r), Collections.singleton(x)))
            .contains(x)) {
          a = x;
          break;
        }
      r.remove(a);
    } while (a != null);
//  result.removeAll(r);
//  return r;
    return Sets.newHashSet(r);
  }

}
