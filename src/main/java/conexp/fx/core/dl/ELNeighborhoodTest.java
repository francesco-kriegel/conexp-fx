package conexp.fx.core.dl;

import java.util.Collections;

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

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.IRI;

import conexp.fx.core.collections.BitSetFX;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.util.Meter;

public final class ELNeighborhoodTest {

  public static void main(String[] args) {
    test8();
  }

  public static final void test1() {
    // final ELConceptDescription C = new ELConceptDescription();
    // C.getConceptNames().add(IRI.create("A"));
    // // C.getConceptNames().add(IRI.create("B"));
    // // C.getConceptNames().add(IRI.create("C"));
    // // C.getConceptNames().add(IRI.create("D"));
    // final ELConceptDescription C1 = new ELConceptDescription();
    // C1.getConceptNames().add(IRI.create("A"));
    // C.getExistentialRestrictions().add(Pair.of(IRI.create("r"), C1));
    // final ELConceptDescription C2 = new ELConceptDescription();
    // C2.getConceptNames().add(IRI.create("A"));
    // C1.getExistentialRestrictions().add(Pair.of(IRI.create("r"), C2));
    // final ELConceptDescription C3 = new ELConceptDescription();
    // C3.getConceptNames().add(IRI.create("A"));
    // C2.getExistentialRestrictions().add(Pair.of(IRI.create("r"), C3));
  }

  private static final void test2() {
    // final ELConceptDescription C = ELParser.read("A AND EXISTS r . ( A AND EXISTS
    // r . ( A AND EXISTS r . A ) )");
    final Deque<String> sources = new LinkedList<>();
    // sources.add("A AND B");
    sources.add("A1 and A2 and A3 and A4 and A5");
    for (int i = 1; i < 100; i++) {
      // sources.add("A AND B AND EXISTS r.(" + sources.getLast() + ") AND EXISTS s.("
      // + sources.getLast() + ")");
      sources.add(sources.getFirst() + " and exists r.(" + sources.getLast() + ")");
      final ELConceptDescription C = ELParser.read(sources.getLast());
      final Set<ELConceptDescription> Us = C.upperNeighbors();
      // System.out.println(i + " -- " + C);
      System.out.println("C" + i);
      System.out.println("has size " + C.size());
      System.out.println("and squared size " + C.size() * C.size());
      System.out.println(
          "and its upper neighbors have size "
              + Us.parallelStream().collect(Collectors.summingLong(ELConceptDescription::size)));
      // System.out.println(C + " has the following upper neighbors:");
      // Us.forEach(System.out::println);
      System.out.println();
    }
  }

  private static final void test3() {
    final Meter<Long> timer = Meter.newNanoStopWatch();
    final ELConceptDescription C = ELParser.read("exists r.(A and exists r.(A and B) and exists r.(B and C))");
    System.out.println(C);
    final AtomicInteger p = new AtomicInteger(0);
    final BitSetFX ds = new BitSetFX();
    recurse(C, 0, p::incrementAndGet, d -> {
      synchronized (ds) {
        ds.add(d);
      }
    });
    System.out.println("branches: " + p);
    System.out.println("quasi rank: " + ds);
    System.out.println("computation time: " + timer.measureAndFormat());
    timer.reset();
    System.out.println("rank: " + C.rank());
    System.out.println("computation time: " + timer.measureAndFormat());
  }

  private static final void
      recurse(final ELConceptDescription C, final int d, final Supplier<Integer> pinc, final Consumer<Integer> dadd) {
    if (C.isTop()) {
      pinc.get();
      dadd.accept(d);
      return;
    }
    C.upperNeighborsReduced().parallelStream().forEach(D -> {
//      D.reduce();
      recurse(D, d + 1, pinc, dadd);
    });
  }

  private static final void test4() {
    final ELConceptDescription C = ELParser.read("exists r.(A and B) and exists r.(A and C) and exists r.(B and C)");
    C.upperNeighborsReduced().forEach(System.out::println);
  }

  private static final void test5() {
    final ELConceptDescription C = ELParser.read("exists r.(A and exists r.(A and B))");
    C.upperNeighborsReduced().forEach(System.out::println);
  }

  private static final void test6() {
    final ELConceptDescription C = ELParser.read("A and exists r.(A and B)");
    C.upperNeighborsReduced().forEach(System.out::println);
  }

  private static final void test7() {
    final Meter<Long> timer = Meter.newNanoStopWatch();
    final ELConceptDescription C = ELParser.read("exists r.(A and exists r.(A and B) and exists r.(B and C))");
    System.out.println(C);
    System.out.println("rank: " + C.rank());
    System.out.println("computation time: " + timer.measureAndFormat());
  }

  private static final void test8() {
    final Signature sigma = new Signature(IRI.create("foo"));
//    sigma.addConceptNames("A1", "A2", "A3", "A4");
    sigma.addConceptNames("A", "B", "C", "D");
    sigma.addRoleNames("r", "s");
//    final ELConceptDescription C = ELParser.read(
//        // "exists r.(exists r.(A and B) and exists r.(A and C))");
//        // "exists r.(exists r.(A1 and A2) and exists r.(A1 and A3))");
//        // "A1⊓A2⊓A3⊓∃s.(A1⊓∃r.A3⊓∃s.A3⊓∃r.(A2⊓∃s.⊤))⊓∃s.(∃r.⊤⊓∃s.(A2⊓∃r.A3)⊓∃s.∃r.A4)⊓∃r.(A1⊓A2⊓A3⊓∃r.(A1⊓A2⊓A3))⊓∃s.(A3⊓A4)⊓∃s.A2");
//        // "∃r.(A1⊓A2⊓A3⊓∃s.(A1⊓∃r.A3⊓∃s.A3⊓∃r.(A2⊓∃s.⊤))⊓∃s.(∃r.⊤⊓∃s.(A2⊓∃r.A3)⊓∃s.∃r.A4)⊓∃r.(A1⊓A2⊓A3⊓∃r.(A1⊓A2⊓A3))⊓∃s.(A3⊓A4)⊓∃s.A2)");
//        // "∃r.(A1⊓A2⊓A3⊓∃s.(A1⊓∃r.A3⊓∃s.A3⊓∃r.(A2⊓∃s.⊤))⊓∃s.(∃r.⊤⊓∃s.(A2⊓∃r.A3)⊓∃s.∃r.A4)⊓∃r.(A1⊓A2⊓A3⊓∃r.(A1⊓A2⊓A3)))");
//        // "exists r.(exists s.(A and B) and exists r.(A and B) and exists s.(A and C and exists s.Top)) and exists r.(A
//        // and B and exists s.(A and B) and exists r.(A and B) and exists s.(A and C and exists s.Top)) and exists r.(A
//        // and D and exists s.(C and D) and exists r.Top) and exists r.(B and C and exists s.Top) and exists r.(A and B
//        // and C) and exists r.exists s.Top and exists r.(B and D and exists r.(A and B)) and exists r.(A and D and
//        // exists r.(A and C))");
//                "exists r.(A and B and exists r.Top) and exists r.(A and C and exists s.Top) and exists r.(A and D and exists r.A)");
//        //        "exists r.(exists r.(A and B and exists r.Top) and exists r.(A and C and exists s.Top) and exists r.(A and D and exists r.A)) and exists s.(exists r.(A and B and exists r.Top) and exists r.(A and C and exists s.Top) and exists r.(A and D and exists r.A))");
//        "exists r.(A and B and exists r.Top) and exists r.(A and C and exists s.Top) and exists r.(A and D and exists r.A) and "
//        + "exists r.(A and exists r.B) and exists r.(A and C and D) and exists r.(A and D and exists r.(A and B) and exists s.(A and B and C))");
////    "exists r.(A and B and exists r.Top) and exists r.(A and C and exists s.Top) and exists r.(A and D and exists r.A) and "
////    + "exists r.(A and exists r.B) and exists r.(A and C and D) and exists r.(A and D and exists r.(A and B) and exists s.(A and B and C)) and exists r.(exists s.(A and B) and C and exists s.D)");
////    "exists r.(exists r.(A and B and exists r.Top) and exists r.(A and C and exists s.Top) and exists r.(A and D and exists r.A)) and "
////    + "exists r.(exists r.(A and exists r.B) and exists r.(A and C and D) and exists r.(A and D and exists r.(A and B) and exists s.(A and B and C)))");
    final ELConceptDescription C = ELConceptDescription.random(sigma, 3, 16, 48);
    C.reduce();
    System.out.println(C);
    final Meter<Long> timer = Meter.newNanoStopWatch();
    System.out.println("has role depth: " + C.roleDepth());
    System.out.println("computation time: " + timer.measureAndFormat());
    timer.reset();
    System.out.println("has size: " + C.size());
    System.out.println("computation time: " + timer.measureAndFormat());
    timer.reset();
//    System.out.println("has rank: " + C.rank());
//    System.out.println("computation time: " + timer.measureAndFormat());
    System.out.println("and has the following upper neighbors:");
    timer.reset();
    Set<ELConceptDescription> upperNeighbors = C.upperNeighbors();
    final String upperNeighborsTime = timer.measureAndFormat();
    final AtomicInteger i = new AtomicInteger(0);
    upperNeighbors.forEach(D -> {
//      System.out.println(i.incrementAndGet() + " --- " + D.rank() + " --- " + D);
      System.out.println(i.incrementAndGet() + " --- " + D);
    });
    System.out.println("computation time: " + upperNeighborsTime);
//    System.out.println("and has the following lower neighbors:");
//    timer.reset();
//    Set<ELConceptDescription> lowerNeighbors = C.lowerNeighborsReduced(sigma);
//    final String lowerNeighborsTime = timer.measureAndFormat();
//    i.set(0);
//    lowerNeighbors.forEach(D -> {
////      final ELConceptDescription rE = D.clone();
////      rE.getConceptNames().removeAll(C.getConceptNames());
////      rE.getExistentialRestrictions().entries().removeAll(C.getExistentialRestrictions().entries());
//      D.reduce();
////      System.out.println(
////          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
////              + " --- " + D.rank() + " --- " + D);
//      System.out.println(
//          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
//              + " --- " + D);
////      System.out.println("   new conjunct: " + rE);
////      System.out.println(i.incrementAndGet() + " --- " + D);
//    });
//    System.out.println("computation time: " + lowerNeighborsTime);
//    System.out.println("and has the following lower neighbors (v2):");
//    timer.reset();
//    Set<ELConceptDescription> lowerNeighbors2 = C.lowerNeighbors2(sigma);
//    final String lowerNeighbors2Time = timer.measureAndFormat();
//    i.set(0);
//    lowerNeighbors2.forEach(D -> {
////      D.reduce();
////      System.out.println(
////          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
////              + " --- " + D.rank() + " --- " + D);
//      System.out.println(i.incrementAndGet() + " --- " + D);
//    });
//    System.out.println("computation time: " + lowerNeighbors2Time);
//    System.out.println("and has the following lower neighbors (v3):");
//    timer.reset();
//    Set<ELConceptDescription> lowerNeighbors3 = C.lowerNeighbors3(sigma);
//    final String lowerNeighbors3Time = timer.measureAndFormat();
//    i.set(0);
//    lowerNeighbors3.forEach(D -> {
//      D.reduce();
//      System.out.println(
//          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
//              + " --- " + D.rank() + " --- " + D);
////      System.out.println(i.incrementAndGet() + " --- " + D);
//    });
//    System.out.println("computation time: " + lowerNeighbors3Time);
//    System.out.println();
    System.out.println("and has the following lower neighbors (v4):");
    timer.reset();
    Set<ELConceptDescription> lowerNeighbors4 = Collections.emptySet();//C.lowerNeighbors4(sigma);
    final String lowerNeighbors4Time = timer.measureAndFormat();
    i.set(0);
    lowerNeighbors4.forEach(D -> {
      D.reduce();
//      System.out.println(
//          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
//              + " --- " + D.rank() + " --- " + D);
      System.out.println(
          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
              + " --- " + D);
//      System.out.println(i.incrementAndGet() + " --- " + D);
    });
    System.out.println("computation time: " + lowerNeighbors4Time);
    System.out.println();
    System.out.println("and has the following lower neighbors (v5):");
    timer.reset();
    Set<ELConceptDescription> lowerNeighbors5 = C.lowerNeighbors5(sigma);
    final String lowerNeighbors5Time = timer.measureAndFormat();
    i.set(0);
    lowerNeighbors5.forEach(D -> {
      D.reduce();
//      System.out.println(
//          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
//              + " --- " + D.rank() + " --- " + D);
      System.out.println(
          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
              + " --- " + D);
//      System.out.println(i.incrementAndGet() + " --- " + D);
    });
    System.out.println("computation time: " + lowerNeighbors5Time);
    System.out.println();
//    System.out.println("and has the following lower neighbors (v6):");
//    timer.reset();
//    Set<ELConceptDescription> lowerNeighbors6 = C.lowerNeighbors6(sigma);
//    final String lowerNeighbors6Time = timer.measureAndFormat();
//    i.set(0);
//    lowerNeighbors6.forEach(D -> {
//      D.reduce();
////      System.out.println(
////          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
////              + " --- " + D.rank() + " --- " + D);
//      System.out.println(
//          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
//              + " --- " + D);
////      System.out.println(i.incrementAndGet() + " --- " + D);
//    });
//    System.out.println("computation time: " + lowerNeighbors6Time);
//    System.out.println();
//    System.out.println("and has the following lower neighbors (v7):");
//    timer.reset();
//    Set<ELConceptDescription> lowerNeighbors7 = C.lowerNeighbors7(sigma);
//    final String lowerNeighbors7Time = timer.measureAndFormat();
//    i.set(0);
//    lowerNeighbors7.forEach(D -> {
//      D.reduce();
////      System.out.println(
////          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
////              + " --- " + D.rank() + " --- " + D);
//      System.out.println(
//          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
//              + " --- " + D);
////      System.out.println(i.incrementAndGet() + " --- " + D);
//    });
//    System.out.println("computation time: " + lowerNeighbors7Time);
//    System.out.println();
    System.out.println("and has the following lower neighbors (v8):");
    timer.reset();
    Set<ELConceptDescription> lowerNeighbors8 = C.lowerNeighbors8(sigma);
    final String lowerNeighbors8Time = timer.measureAndFormat();
    i.set(0);
    lowerNeighbors8.forEach(D -> {
      D.reduce();
//      System.out.println(
//          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
//              + " --- " + D.rank() + " --- " + D);
      System.out.println(
          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
              + " --- " + D);
//      System.out.println(i.incrementAndGet() + " --- " + D);
    });
    System.out.println("computation time: " + lowerNeighbors8Time);
    System.out.println();
    System.out.println("and has the following lower neighbors (v9):");
    timer.reset();
    Set<ELConceptDescription> lowerNeighbors9 = C.lowerNeighbors9(sigma);
    final String lowerNeighbors9Time = timer.measureAndFormat();
    i.set(0);
    lowerNeighbors9.forEach(D -> {
      D.reduce();
//      System.out.println(
//          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
//              + " --- " + D.rank() + " --- " + D);
      System.out.println(
          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
              + " --- " + D);
//      System.out.println(i.incrementAndGet() + " --- " + D);
    });
    System.out.println("computation time: " + lowerNeighbors9Time);
    System.out.println();
    Set<ELConceptDescription> lowerNeighbors10 = C.lowerNeighbors10(sigma);
    final String lowerNeighbors10Time = timer.measureAndFormat();
    i.set(0);
    lowerNeighbors10.forEach(D -> {
      D.reduce();
//      System.out.println(
//          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
//              + " --- " + D.rank() + " --- " + D);
      System.out.println(
          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
              + " --- " + D);
//      System.out.println(i.incrementAndGet() + " --- " + D);
    });
    System.out.println("computation time: " + lowerNeighbors10Time);
    System.out.println();
//    final int size1 = Collections3.quotient(lowerNeighbors, ELConceptDescription.equivalence()).size();
    final int size4 = Collections3.quotient(lowerNeighbors4, ELConceptDescription.equivalence()).size();
    final int size5 = Collections3.quotient(lowerNeighbors5, ELConceptDescription.equivalence()).size();
//    final int size6 = Collections3.quotient(lowerNeighbors6, ELConceptDescription.equivalence()).size();
//    final int size7 = Collections3.quotient(lowerNeighbors7, ELConceptDescription.equivalence()).size();
    final int size8 = Collections3.quotient(lowerNeighbors8, ELConceptDescription.equivalence()).size();
    final int size9 = Collections3.quotient(lowerNeighbors9, ELConceptDescription.equivalence()).size();
    final int size10 = Collections3.quotient(lowerNeighbors10, ELConceptDescription.equivalence()).size();
//    System.out.println("1=2   " + equalsEquivalent(lowerNeighbors, lowerNeighbors2));
//    System.out.println("1=3   " + equalsEquivalent(lowerNeighbors, lowerNeighbors3));
//    System.out.println("time1  = " + lowerNeighborsTime);
    System.out.println("time4  = " + lowerNeighbors4Time);
    System.out.println("time5  = " + lowerNeighbors5Time);
//    System.out.println("time6  = " + lowerNeighbors6Time);
//    System.out.println("time7  = " + lowerNeighbors7Time);
    System.out.println("time8  = " + lowerNeighbors8Time);
    System.out.println("time9  = " + lowerNeighbors9Time);
    System.out.println("time10 = " + lowerNeighbors10Time);
//    System.out.println("size1  = " + size1);
    System.out.println("size4  = " + size4);
    System.out.println("size5  = " + size5);
//    System.out.println("size6  = " + size6);
//    System.out.println("size7  = " + size7);
    System.out.println("size8  = " + size8);
    System.out.println("size9  = " + size9);
    System.out.println("size10 = " + size10);
//    System.out.println("1<4    " + containsEquivalent(lowerNeighbors, lowerNeighbors4));
//    System.out.println("4<1    " + containsEquivalent(lowerNeighbors4, lowerNeighbors));
//    System.out.println("1=4    " + equalsEquivalent(lowerNeighbors, lowerNeighbors4));
//    System.out.println("1<5    " + containsEquivalent(lowerNeighbors, lowerNeighbors5));
//    System.out.println("5<1    " + containsEquivalent(lowerNeighbors5, lowerNeighbors));
//    System.out.println("1=5    " + equalsEquivalent(lowerNeighbors, lowerNeighbors5));
    System.out.println("4<5    " + containsEquivalent(lowerNeighbors4, lowerNeighbors5));
    System.out.println("4>5    " + containsEquivalent(lowerNeighbors5, lowerNeighbors4));
    System.out.println("4=5    " + equalsEquivalent(lowerNeighbors4, lowerNeighbors5));
//    System.out.println("5<6    " + containsEquivalent(lowerNeighbors5, lowerNeighbors6));
//    System.out.println("5>6    " + containsEquivalent(lowerNeighbors6, lowerNeighbors5));
//    System.out.println("5=6    " + equalsEquivalent(lowerNeighbors5, lowerNeighbors6));
//    System.out.println("5<7    " + containsEquivalent(lowerNeighbors5, lowerNeighbors7));
//    System.out.println("5>7    " + containsEquivalent(lowerNeighbors7, lowerNeighbors5));
//    System.out.println("5=7    " + equalsEquivalent(lowerNeighbors5, lowerNeighbors7));
    System.out.println("5<8    " + containsEquivalent(lowerNeighbors5, lowerNeighbors8));
    System.out.println("5>8    " + containsEquivalent(lowerNeighbors8, lowerNeighbors5));
    System.out.println("5=8    " + equalsEquivalent(lowerNeighbors5, lowerNeighbors8));
    System.out.println("5<9    " + containsEquivalent(lowerNeighbors5, lowerNeighbors9));
    System.out.println("5>9    " + containsEquivalent(lowerNeighbors9, lowerNeighbors5));
    System.out.println("5=9    " + equalsEquivalent(lowerNeighbors5, lowerNeighbors9));
    System.out.println("5<10   " + containsEquivalent(lowerNeighbors5, lowerNeighbors10));
    System.out.println("5>10   " + containsEquivalent(lowerNeighbors10, lowerNeighbors5));
    System.out.println("5=10   " + equalsEquivalent(lowerNeighbors5, lowerNeighbors10));
    System.out.println();
//    System.out.println(C + " has the following lower neighbors, that method v6 does not compute:");
//    Collections3
//        .representatives(lowerNeighbors4, ELConceptDescription.equivalence())
//        .parallelStream()
//        .filter(L -> lowerNeighbors6.parallelStream().noneMatch(L::isEquivalentTo))
//        .forEach(System.out::println);
//    System.out.println();
//    System.out.println(C + " has the following lower neighbors, that method v7 does not compute:");
//    Collections3
//        .representatives(lowerNeighbors4, ELConceptDescription.equivalence())
//        .parallelStream()
//        .filter(L -> lowerNeighbors7.parallelStream().noneMatch(L::isEquivalentTo))
//        .forEach(System.out::println);
//    System.out.println();
//    System.out.println(C + " has the following lower neighbors, that method v8 does not compute:");
//    Collections3
//        .representatives(lowerNeighbors4, ELConceptDescription.equivalence())
//        .parallelStream()
//        .filter(L -> lowerNeighbors8.parallelStream().noneMatch(L::isEquivalentTo))
//        .forEach(System.out::println);
//    System.out.println("2=3   " + equalsEquivalent(lowerNeighbors2, lowerNeighbors3));
  }

  private static final boolean
      containsEquivalent(final Set<ELConceptDescription> Cs, final Set<ELConceptDescription> Ds) {
    return Cs.parallelStream().allMatch(C -> Ds.parallelStream().anyMatch(C::isEquivalentTo));
  }

  private static final boolean
      equalsEquivalent(final Set<ELConceptDescription> Cs, final Set<ELConceptDescription> Ds) {
    return Stream
        .<Supplier<Boolean>> of(() -> containsEquivalent(Cs, Ds), () -> containsEquivalent(Ds, Cs))
        .parallel()
        .allMatch(Supplier::get);
  }

  private static final void test9() {
    final ELConceptDescription C = ELParser.read("A1⊓A2⊓A3⊓∃r.(A1⊓A2⊓A3⊓∃r.(A1⊓A2⊓A3))"
//            "A1⊓A2⊓A3⊓∃s.(A1⊓∃r.A3⊓∃s.A3⊓∃r.(A2⊓∃s.⊤))⊓∃s.(∃r.⊤⊓∃s.(A2⊓∃r.A3)⊓∃s.∃r.A4)⊓∃r.(A1⊓A2⊓A3⊓∃r.(A1⊓A2⊓A3))⊓∃s.(A3⊓A4)⊓∃s.A2"
//            "A1⊓A2⊓A3⊓∃s.(A1⊓∃r.A3⊓∃s.A3⊓∃r.(A2⊓∃s.⊤))⊓∃s.(∃r.⊤⊓∃s.(A2⊓∃r.A3)⊓∃s.∃r.A4)⊓∃s.∃r.(A1⊓A2⊓A3⊓∃r.(A1⊓A2⊓A3))⊓∃s.(A3⊓A4)⊓∃s.A2"
//        "A1⊓A2⊓A3⊓∃s.(A1⊓∃r.A3⊓∃s.A3⊓∃r.(A2⊓∃s.⊤))⊓∃s.(∃r.⊤⊓∃s.(A2⊓∃r.A3)⊓∃s.∃r.A4)⊓∃r.(A1⊓A2⊓A3⊓∃r.(A1⊓A2⊓A3))⊓∃s.(A3⊓A4)⊓∃s.A2 and exists r.(A1⊓A2⊓A3⊓∃s.(A1⊓∃r.A3⊓∃s.A3⊓∃r.(A2⊓∃s.⊤))⊓∃s.(∃r.⊤⊓∃s.(A2⊓∃r.A3)⊓∃s.∃r.A4)⊓∃r.(A1⊓A2⊓A3⊓∃r.(A1⊓A2⊓A3))⊓∃s.(A3⊓A4)⊓∃s.A2)"
    );
    C.reduce();
    System.out.println(C);
    final Meter<Long> timer = Meter.newNanoStopWatch();
    for (int i = 0; i < 3; i++) {
      timer.reset();
      System.out.println("rank: " + C.unreducedRank());
      System.out.println("computation time: " + timer.measureAndFormat());
      timer.reset();
      System.out.println("rank2: " + C.unreducedRank2());
      System.out.println("computation time: " + timer.measureAndFormat());
      timer.reset();
      System.out.println("rank3: " + C.rank3());
      System.out.println("computation time: " + timer.measureAndFormat());
      timer.reset();
      System.out.println("rank4: " + C.unreducedRank4());
      System.out.println("computation time: " + timer.measureAndFormat());
    }
  }

  private static final void test10() {
    final Meter<Long> timer = Meter.newNanoStopWatch();
    String source = "(A⊓B⊓C)";
    for (int i = 0; i < 10; i++) {
      source = "∃r." + source;
      final ELConceptDescription C = ELParser.read(source);
      System.out.println(C);
      timer.reset();
      System.out.println("rank1: " + C.rank());
      System.out.println("computation time: " + timer.measureAndFormat());
//      timer.reset();
//      System.out.println("rank3: " + C.rank3());
//      System.out.println("computation time: " + timer.measureAndFormat());
      timer.reset();
      System.out.println("rank2: " + C.rank2());
      System.out.println("computation time: " + timer.measureAndFormat());
      System.out.println();
    }
  }

  private static final void test11() {
    final ELConceptDescription C = ELParser.read("A1⊓A2⊓A3⊓∃r.(A1⊓A2⊓A3⊓∃r.(A1⊓A2))");
    final ELConceptDescription D = ELParser.read("A1⊓A2⊓A3⊓∃r.(A1⊓A2⊓∃r.(A1⊓A2⊓A3)⊓∃r.∃s.(A1 and A2 AND A3))");
    C.reduce();
    D.reduce();
    System.out.println(C);
    System.out.println(D);
    final Meter<Long> timer = Meter.newNanoStopWatch();
    for (int i = 0; i < 3; i++) {
      timer.reset();
      System.out.println("distance: " + C.distanceTo(D));
      System.out.println("computation time: " + timer.measureAndFormat());
      timer.reset();
      System.out.println("distance2: " + C.distanceTo2(D));
      System.out.println("computation time: " + timer.measureAndFormat());
    }
  }

  private static final void test12() {
    final Signature sigma = new Signature(IRI.create("foo"));
    sigma.addConceptNames("A1", "A2", "A3", "A4");
    sigma.addRoleNames("r", "s");
    final ELConceptDescription C = ELParser.read("A1⊓A2⊓A3⊓∃r.(A1⊓A2⊓∃r.(A1⊓A2⊓A3)⊓∃s.(A1 and ∃r.A3))");
    C.reduce();
    final int radius = 3;
    System.out.println(C);
    System.out.println("radius: " + radius);
    final Meter<Long> timer = Meter.newNanoStopWatch();
    for (int i = 0; i < 3; i++) {
      timer.reset();
      final Set<ELConceptDescription> neighborhood = C.neighborhood(radius, sigma);
      final String ctime = timer.measureAndFormat();
      System.out.println(neighborhood.size() + " neighbors:");
      neighborhood.forEach(D -> System.out.println(D));
//      neighborhood.forEach(D -> System.out.println(D.rank() + "   " + D));
      System.out.println("computation time: " + ctime);
    }
  }

  private static final void test13() {
    final int n = 3;
    final IRI r = IRI.create("r");
    final List<ELConceptDescription> atoms = new LinkedList<>();
    for (int i = 1; i < n + 1; i++) {
      atoms.add(ELConceptDescription.conceptName(IRI.create("A" + i)));
    }
    ELConceptDescription C = ELConceptDescription.conjunction(atoms);
    int rd = 0;
    long rank = C.rank();
    while (true) {
//      final long lower = 1 + rank;
//      long upper = 1;
//      for (long i = 1; i < rank; i++) {
//        long fac = 1;
//        for (long j = 0; j < i - 2; j++)
//          fac *= rank - j;
//        upper += fac;
//      }
      C = ELConceptDescription.conjunction(C, ELConceptDescription.existentialRestriction(r, C)).reduce();
      rd++;
      rank = C.rank();
      System.out.println("role depth: " + rd + "    " + "rank: " + rank);
      System.out.println(C);
//      System.out.println("lower bound: " + lower);
//      System.out.println("upper bound: " + upper);
      System.out.println();
    }
  }

}
