package conexp.fx.core.dl;

/*-
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.IRI;

import conexp.fx.core.collections.BitSetFX;
import conexp.fx.core.util.Meter;

public final class ELNeighborhoodTest {

  public static void main(String[] args) {
    test12();
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
    sigma.addConceptNames("A1", "A2", "A3", "A4");
    sigma.addRoleNames("r", "s");
    final ELConceptDescription C = ELParser.read(
        // "A1⊓A2⊓A3⊓∃s.(A1⊓∃r.A3⊓∃s.A3⊓∃r.(A2⊓∃s.⊤))⊓∃s.(∃r.⊤⊓∃s.(A2⊓∃r.A3)⊓∃s.∃r.A4)⊓∃r.(A1⊓A2⊓A3⊓∃r.(A1⊓A2⊓A3))⊓∃s.(A3⊓A4)⊓∃s.A2");
        // "A1 and A2 and A3 and exists r.(A1 and A2) and exists r.(A1 and A3) and exists r.(A2 and A3) and exists r.(A1
        // and A2 and A3 and exists r.(A1 and A2 and A3)) and exists s.(A1 and exists r.(A2 and exists s.Top) and exists
        // s.A3 and exists r.A3) and exists s.A2 and exists s.(A3 and A4) and exists s.(exists r.Top and exists s.(A2
        // and exists r.A3) and exists s.exists r.A4) and exists r.exists r.exists r.exists r.exists r.Top and exists
        // s.exists r.(A2 and exists s.A4 and exists r.exists s.A3 and exists r.exists r.Top)"
        "exists r.(A1⊓A2⊓A3⊓∃s.(A1⊓∃r.A3⊓∃s.A3⊓∃r.(A2⊓∃s.⊤))⊓∃s.(∃r.⊤⊓∃s.(A2⊓∃r.A3)⊓∃s.∃r.A4)⊓∃r.(A1⊓A2⊓A3⊓∃r.(A1⊓A2⊓A3)))");
    C.reduce();
    System.out.println(C);
    final Meter<Long> timer = Meter.newNanoStopWatch();
    System.out.println("has role depth: " + C.roleDepth());
    System.out.println("computation time: " + timer.measureAndFormat());
    timer.reset();
    System.out.println("has size: " + C.size());
    System.out.println("computation time: " + timer.measureAndFormat());
    timer.reset();
    System.out.println("has rank: " + C.rank());
    System.out.println("computation time: " + timer.measureAndFormat());
    System.out.println("and has the following upper neighbors:");
    timer.reset();
    Set<ELConceptDescription> upperNeighbors = C.upperNeighborsReduced();
    final String upperNeighborsTime = timer.measureAndFormat();
    final AtomicInteger i = new AtomicInteger(0);
    upperNeighbors.forEach(D -> {
      System.out.println(i.incrementAndGet() + " --- " + D.rank() + " --- " + D);
//      System.out.println(i.incrementAndGet() + " --- " + D);
    });
    System.out.println("computation time: " + upperNeighborsTime);
    System.out.println("and has the following lower neighbors:");
    timer.reset();
    Set<ELConceptDescription> lowerNeighbors = C.lowerNeighbors(sigma);
    final String lowerNeighborsTime = timer.measureAndFormat();
    i.set(0);
    lowerNeighbors.forEach(D -> {
      D.reduce();
      System.out.println(
          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
              + " --- " + D.rank() + " --- " + D);
//      System.out.println(i.incrementAndGet() + " --- " + D);
    });
    System.out.println("computation time: " + lowerNeighborsTime);
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
    System.out.println("and has the following lower neighbors (v3):");
    timer.reset();
    Set<ELConceptDescription> lowerNeighbors3 = C.lowerNeighbors3(sigma);
    final String lowerNeighbors3Time = timer.measureAndFormat();
    i.set(0);
    lowerNeighbors3.forEach(D -> {
      D.reduce();
      System.out.println(
          i.incrementAndGet() + " --- " + D.upperNeighborsReduced().parallelStream().anyMatch(C::isEquivalentTo)
              + " --- " + D.rank() + " --- " + D);
//      System.out.println(i.incrementAndGet() + " --- " + D);
    });
    System.out.println("computation time: " + lowerNeighbors3Time);
    System.out.println();
//    System.out.println("1=2   " + equalsEquivalent(lowerNeighbors, lowerNeighbors2));
    System.out.println("1=3   " + equalsEquivalent(lowerNeighbors, lowerNeighbors3));
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

}
