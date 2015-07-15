package conexp.fx.core.dl;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.semanticweb.elk.util.collections.Triple;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

import conexp.fx.core.algorithm.nextclosures.NextClosures;
import conexp.fx.core.algorithm.nextclosures.Result;
import conexp.fx.core.context.SparseContext;
import conexp.fx.core.util.OWLtoString;

public class OWLInterpretationTest3 {

  private final OWLInterpretation i;

  public OWLInterpretationTest3() {
    i = new OWLInterpretation((IRI) null);
    i.getSignature().addRoleNames(
        "r1",
        "r2",
        "r3",
        "s");
    i.getDomain().addAll(
        Stream.iterate(
            0,
            n -> n + 1).limit(
            8).map(
            n -> IRI.create("d" + n)).collect(
            Collectors.toSet()));
    i.addRoleNameAssertion(
        "r1",
        "d0",
        "d1");
    i.addRoleNameAssertion(
        "r2",
        "d1",
        "d2");
    i.addRoleNameAssertion(
        "r3",
        "d2",
        "d3");
    i.addRoleNameAssertion(
        "s",
        "d0",
        "d3");
    i.addRoleNameAssertion(
        "r1",
        "d4",
        "d4");
    i.addRoleNameAssertion(
        "r2",
        "d5",
        "d5");
    i.addRoleNameAssertion(
        "r3",
        "d6",
        "d6");
    i.addRoleNameAssertion(
        "s",
        "d7",
        "d7");
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGteInducedRoleContext() {
    final SparseContext<ArrayList<IRI>, Triple<Integer, IRI, Integer>> cxt = i.getInducedRoleContext(3);
//    cxt.rowHeads().forEach(
//        System.out::println);
//    cxt.colHeads().forEach(
//        triple -> {
//          System.out.println(triple.getFirst() + "-" + triple.getSecond() + "-" + triple.getThird());
//        });
    final Result<ArrayList<IRI>, Triple<Integer, IRI, Integer>> base = NextClosures.compute(
        cxt,
        true);
    base.implications.entrySet().stream().filter(
        e -> !base.supports.get(
            e.getKey()).isEmpty()).forEach(
        e -> {
          System.out.println(toString(e.getKey()) + "==>  " + toString(e.getValue()));
        });
//    Assert.assertEquals(
//        (int) Math.pow(
//            i.getDomain().size(),
//            4),
//        cxt.rowHeads().size());
//    Assert.assertEquals(
//        i.getSignature().getRoleNames().size() * i.getDomain().size() * 2,
//        cxt.colHeads().size());

  }

  @Test
  public void testGetRoleInclusionBase() {
    final Set<OWLSubPropertyChainOfAxiom> rBox = i.getRoleInclusionBase(3);
    rBox.forEach(ax -> OWLtoString.toString(ax));
  }

  public static final <X, Y, Z> String toString(Triple<X, Y, Z> t) {
    return "(" + t.getFirst() + "|" + t.getSecond() + "|" + t.getThird() + ")";
  }

  public static final <X, Y, Z> String toString(Collection<Triple<X, Y, Z>> c) {
    final StringBuilder s = new StringBuilder();
    final Iterator<Triple<X, Y, Z>> i = c.iterator();
    while (i.hasNext()) {
      s.append(toString(i.next()) + "  ");
    }
    return s.toString();
  }
}
