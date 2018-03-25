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
import java.util.Set;
import java.util.stream.Collectors;

import conexp.fx.core.algorithm.nextclosures.NextClosures1.Result;
import conexp.fx.core.collections.Pair;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.Implication;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter;
import conexp.fx.core.util.Meter;

public final class Benchmark {

  private static final boolean compare(
      final Pair<Set<Concept<String, String>>, Set<Implication<String, String>>> r,
      final Pair<Set<Concept<String, String>>, Set<Implication<String, String>>> s) {
    return r.x().size() == s.x().size() && r.x().containsAll(s.x()) && s.x().containsAll(r.x())
        && r.y().size() == s.y().size() && r.y().containsAll(s.y()) && s.y().containsAll(r.y());
  }

  private static final boolean compareSem(
      final Pair<Set<Concept<String, String>>, Set<Implication<String, String>>> r,
      final Pair<Set<Concept<String, String>>, Set<Implication<String, String>>> s) {
    return r.x().size() == s.x().size() && r.x().containsAll(s.x()) && s.x().containsAll(r.x())
        && Implication.equivalent(r.y(), s.y());
  }

  public static void main(String[] args) throws Exception {
    for (int i = 0; i < 10; i++) {
      final Meter<Long> meter = Meter.newNanoStopWatch();
      final MatrixContext<String, String> cxt =
          CXTImporter.read(new File("../../Data/Contexts/random/o1000a20d10.cxt"));
      System.out.println("Import:                  " + meter.measureAndFormat());
      meter.reset();
      final Result<String, String> x = NextClosures1.compute(cxt, false);
      System.out.println("NextClosures1:           " + meter.measureAndFormat());
      final Pair<Set<Concept<String, String>>, Set<Implication<String, String>>> x0 = Pair.of(
          x.concepts,
          x.implications
              .entrySet()
              .parallelStream()
              .map(e -> new Implication<String, String>(e.getKey(), e.getValue()))
              .collect(Collectors.toSet()));
      meter.reset();
      final Pair<Set<Concept<String, String>>, Set<Implication<String, String>>> r0 = NextClosures2.compute(cxt);
      System.out.println("NextClosures2:           " + meter.measureAndFormat());
//      meter.reset();
//      final Pair<Set<Concept<String, String>>, Set<Implication<String, String>>> r1 =
//          NextClosures2Bit.bitBitCompute(cxt);
//      System.out.println("NextClosures2BitBit:     " + meter.measureAndFormat());
//      meter.reset();
//      final Pair<Set<Concept<String, String>>, Set<Implication<String, String>>> s1 =
//          NextClosures2BitNew.bitBitCompute(cxt);
//      System.out.println("NextClosures2BitBitNew:     " + meter.measureAndFormat());
      meter.reset();
      final Pair<Set<Concept<String, String>>, Set<Implication<String, String>>> r2 = NextClosures2Bit.bitCompute(cxt);
      System.out.println("NextClosures2Bit:        " + meter.measureAndFormat());
//      meter.reset();
//      final Pair<Set<Concept<String, String>>, Set<Implication<String, String>>> s2 =
//          NextClosures2BitNew.bitCompute(cxt);
//      System.out.println("NextClosures2BitNew:        " + meter.measureAndFormat());
//      meter.reset();
//      final Pair<Set<Concept<String, String>>, Set<Implication<String, String>>> r3 = NextClosures2Bit
//          .bitCleanedCompute(cxt, Executors.newWorkStealingPool(), __ -> {}, __ -> {}, __ -> {}, __ -> {}, () -> false);
//      System.out.println("NextClosures2BitCleaned: " + meter.measureAndFormat());
//      meter.reset();
//      final Pair<Set<Concept<String, String>>, Set<Implication<String, String>>> r4 = NextClosures2Bit
//          .bitReducedCompute(cxt, Executors.newWorkStealingPool(), __ -> {}, __ -> {}, __ -> {}, __ -> {}, () -> false);
//      System.out.println("NextClosures2BitReduced: " + meter.measureAndFormat());
      System.out.println("############################################");
      System.out.println(compare(r0, x0) + " : " + compareSem(r0, x0));
      System.out.println(compare(x0, r2) + " : " + compareSem(x0, r2));
//      System.out.println(compare(r1, s1) + " : " + compareSem(r1, s1));
//      System.out.println(r1.y().size() + " : " + s1.y().size());
//      System.out.println(Sets.difference(r1.y(), s1.y()));
//      System.out.println(Sets.difference(s1.y(), r1.y()));
//      System.out.println(compare(r1, r2) + " : " + compareSem(r1, r2));
//      System.out.println(compare(r1, s2) + " : " + compareSem(r1, s2));
//      System.out.println(r1.y().size() + " : " + s2.y().size());
//      System.out.println(Sets.difference(r1.y(), s2.y()));
//      System.out.println(Sets.difference(s2.y(), r1.y()));
//      System.out.println(compareSem(r0, r3));
//      System.out.println(compareSem(r0, r4));
      System.out.println("############################################");
      System.out.println();
    }
  }

}
