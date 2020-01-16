package conexp.fx.core.dl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.Sets;

import conexp.fx.core.algorithm.nextclosures.NextClosures2;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.context.Implication;
import conexp.fx.core.math.DualClosureOperator;
import conexp.fx.core.math.SetClosureOperator;

/*-
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2020 Francesco Kriegel
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

public final class ELAxiomatizer {

  public static final <I> ELAxiomatizer from(final Signature sigma, final ELInterpretation2<I> i, final int roleDepth) {
    return new ELAxiomatizer(sigma, roleDepth, true, __ -> false, DualClosureOperator.fromInterpretation(i, roleDepth));
  }

  public static final <I> ELAxiomatizer
      from(final Signature sigma, final ELInterpretation2<I> i, final ELTBox t, final int roleDepth) {
    return new ELAxiomatizer(
        sigma,
        roleDepth,
        true,
        __ -> false,
        DualClosureOperator
            .infimum(DualClosureOperator.fromInterpretation(i, roleDepth), DualClosureOperator.fromTBox(t, roleDepth)));
  }

  private final Signature                                 sigma;
  private final int                                       roleDepth;
  private final boolean                                   withBot;
  private final Predicate<ELConceptDescription>           hasEmptySupport;
  private final DualClosureOperator<ELConceptDescription> clop;
  private Set<ELConceptDescription>                       closures;
  private final Set<ELConceptDescription>                 visited;
  private final Set<ELConceptDescription>                 attributes;
  private SetClosureOperator<ELConceptDescription>        setClop;

  public ELAxiomatizer(
      final Signature sigma,
      final int roleDepth,
      final boolean withBot,
      final Predicate<ELConceptDescription> hasEmptySupport,
      final DualClosureOperator<ELConceptDescription> clop) {
    super();
    this.sigma = sigma;
    this.roleDepth = roleDepth;
    this.withBot = withBot;
    this.hasEmptySupport = hasEmptySupport;
    this.clop = clop;
    this.closures = Sets.newConcurrentHashSet();
    this.visited = Sets.newConcurrentHashSet();
    this.attributes = new HashSet<>();
  }

  public final void initialize() {
    computeAttributeSet();
    setClop = set -> {
      final ELConceptDescription dlClosure = clop.closure(ELConceptDescription.conjunction(set).reduce()).reduce();
      return attributes.parallelStream().filter(dlClosure::isSubsumedBy).collect(Collectors.toSet());
    };
  }

  private final void computeAttributeSet() {
    System.out.println("Computing all closures...");
    findClosuresBelow(ELConceptDescription.top());
    closures = Collections3.representatives(closures, ELConceptDescription::equivalent);
    System.out.println("  " + closures.size() + " closures");
    System.out.println("Constructing attributes...");
    if (withBot)
      attributes.add(ELConceptDescription.bot());
    for (IRI A : sigma.getConceptNames())
      attributes.add(ELConceptDescription.conceptName(A));
    for (IRI r : sigma.getRoleNames())
      for (ELConceptDescription D : closures)
        attributes.add(ELConceptDescription.existentialRestriction(r, D));
    System.out.println("  " + attributes.size() + " attributes");
    System.out.println(attributes);
  }

  private AtomicInteger num = new AtomicInteger();

  private final void findClosuresBelow(final ELConceptDescription c) {
    if (!visited.contains(c))
      try {
        visited.add(c);
        System.out.println("Searching closures below " + c);
        final ELConceptDescription closure = clop.closure(c);
        closure.restrictTo(roleDepth - 1);
        closures.add(closure);
        System.out.println("closure " + num.incrementAndGet() + " " + closure);
        for (final ELConceptDescription lower : closure.lowerNeighborsB(sigma))
          if (lower.roleDepth() < roleDepth)
            findClosuresBelow(lower);
      } catch (IllegalArgumentException __) {}
  }

  public final ELTBox compute() {
    System.out.println("Generating background knowledge...");
    final Set<Implication<Object, ELConceptDescription>> backgroundKnowledge = Sets.newConcurrentHashSet();
    attributes.parallelStream().forEach(X -> attributes.parallelStream().forEach(Y -> {
      if (X.isSubsumedBy(Y))
        backgroundKnowledge.add(new Implication<Object, ELConceptDescription>(X, Y));
    }));
    System.out.println("Starting axiomatization...");
    final Set<Implication<Object, ELConceptDescription>> implications = NextClosures2
        .compute(
            attributes,
            setClop,
            __ -> Collections.emptySet(),
            set -> hasEmptySupport.test(ELConceptDescription.conjunction(set)),
            Executors.newWorkStealingPool(),
            __ -> {},
            implication -> {
              System.out.println(implication);
              final ELConceptDescription subsumee = ELConceptDescription.conjunction(implication.getPremise()).reduce();
              final ELConceptDescription subsumer =
                  ELConceptDescription.conjunction(implication.getConclusion()).reduce();
              final ELConceptInclusion ci = new ELConceptInclusion(subsumee, subsumer.without(subsumee));
              if (!ci.isTautological())
                System.out.println(ci);
              else
                System.out.println("tautology");
            },
            System.out::println,
            __ -> {},
            () -> false,
            backgroundKnowledge)
        .second();
    final ELTBox result = new ELTBox();
    for (Implication<Object, ELConceptDescription> implication : implications) {
      implication.getConclusion().removeAll(implication.getPremise());
      final ELConceptDescription subsumee = ELConceptDescription.conjunction(implication.getPremise()).reduce();
      final ELConceptDescription subsumer = ELConceptDescription.conjunction(implication.getConclusion()).reduce();
      final ELConceptInclusion ci = new ELConceptInclusion(subsumee, subsumer.without(subsumee));
      if (!ci.isTautological())
        result.getConceptInclusions().add(ci);
    }
    return result;
  }

  public static void main(String[] args) {
//    foo();
    bar();
  }

  private static void foo() {
    final Signature sigma = new Signature(IRI.create("foo"));
    sigma.addConceptNames("A", "B", "C");
    sigma.addRoleNames("r");
    final ELTBox t1 = new ELTBox();
    t1.getConceptInclusions().add(ELConceptInclusion.parse("A", "exists r. B"));
    t1.getConceptInclusions().add(ELConceptInclusion.parse("B", "exists r. B"));
    final ELTBox t2 = new ELTBox();
    t2.getConceptInclusions().add(ELConceptInclusion.parse("A", "exists r. C"));
    t2.getConceptInclusions().add(ELConceptInclusion.parse("C", "exists r. C"));
    for (int d = 0; d < 5; d++) {
      final DualClosureOperator<ELConceptDescription> clop =
          DualClosureOperator.infimum(DualClosureOperator.fromTBox(t1, d), DualClosureOperator.fromTBox(t2, d));
      final ELAxiomatizer axiomatizer = new ELAxiomatizer(sigma, d, true, __ -> false, clop);
      axiomatizer.initialize();
      final ELTBox base = axiomatizer.compute();
      System.out.println("d=" + d);
      System.out.println(base);
      System.out.println();
    }
  }

  private static void bar() {
    // This is the toy example from my DAM 2019 paper.
    final Signature sigma = new Signature(IRI.create("bar"));
    sigma.addConceptNames("Person", "Car", "Wheel");
    sigma.addRoleNames("child");
    final ELInterpretation2<Integer> i = new ELInterpretation2<>();
    i.add(0, "Car");
    i.add(1, "Wheel");
    i.add(2, "Person");
    i.add(3, "Person");
    i.add(0, "child", 1);
    i.add(2, "child", 3);
    final ELTBox t = new ELTBox();
    t.getConceptInclusions().add(ELConceptInclusion.parse("exists child. Top", "Person"));
    t.getConceptInclusions().add(ELConceptInclusion.parse("Person and Car", "Bot"));
    System.out.println(t);
    for (int d = 0; d < 5; d++) {
//      final DualClosureOperator<ELConceptDescription> clop = DualClosureOperator.fromInterpretation(i, d);
      final DualClosureOperator<ELConceptDescription> clop = DualClosureOperator
          .supremum(DualClosureOperator.fromInterpretation(i, d), DualClosureOperator.fromTBox(t, d));
      System.out.println("Car has closure " + clop.closure(ELConceptDescription.parse("Car")));
      final ELAxiomatizer axiomatizer = new ELAxiomatizer(sigma, d, true, __ -> false, clop);
      axiomatizer.initialize();
      final ELTBox base = axiomatizer.compute();
      System.out.println("d=" + d);
      System.out.println(base);
      System.out.println();
    }
  }

  private static void baz() {

  }

}
