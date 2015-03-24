package conexp.fx.experiment.cellautomata;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import conexp.fx.core.algorithm.nextclosures.NextClosures6C;
import conexp.fx.core.algorithm.nextclosures.NextClosuresMN2;
import conexp.fx.core.closureoperators.ClosureOperator;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.context.negation.Literal;
import conexp.fx.core.context.negation.NegationScaling;
import conexp.fx.core.importer.CXTImporter2;

public class Test {

  public static void main(String[] args) {
    MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
//    CXTImporter2.read(cxt, new File("/Users/francesco/workspace/Java/conexp-fx/formal-context-CA-ACRI.cxt"));
//    CXTImporter2.read(cxt, new File("/Users/francesco/workspace/Java/conexp-fx/formal-context2-CA-ACRI.cxt"));
    CXTImporter2
        .read(cxt, new File("/Users/francesco/workspace/Java/conexp-fx/formal-context-update-rule-CA-ACRI.cxt"));
    System.out.println("adding negated attributes...");
//    final Set<String> atts = new HashSet<String>();
//    atts.addAll(cxt.colHeads());
//    atts.remove("SU");
//    for (String att : atts) {
//      String natt = "-" + att;
//      cxt.colHeads().add(natt);
////      for (String obj : cxt.rowHeads())
////        if (!cxt.contains(obj, att))
////          cxt.addFast(obj, natt);
//    }
//    cxt.setMatrix(cxt
//        .matrix()
//        .selectColumns(Ret.NEW, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
//        .appendHorizontally(cxt.matrix().selectColumns(Ret.NEW, 0, 1, 2, 3, 4, 5, 6, 7, 8).not(Ret.NEW))
//        .toBooleanMatrix());
    final MatrixContext<String, Literal<String>> cxt2 =
        NegationScaling.negationScaling(cxt, Arrays.asList("N0", "N1", "N2", "N3", "N4", "N5", "N6", "N7", "N8"));
    System.out.println(cxt2.matrix());
    System.out.println(cxt2);

    final Literal<String> suLiteral = new Literal<String>("SU");
    
    final ClosureOperator<Literal<String>> clop = new ClosureOperator<Literal<String>>() {

      @Override
      public boolean isClosed(Set<Literal<String>> set) {
        if (set.contains(suLiteral) && set.size() < 19)
          return false;
        return true;
      }

      @Override
      public boolean close(Set<Literal<String>> set) {
        if (set.contains(suLiteral) && set.size() < 19) {
          set.addAll(cxt2.colHeads());
          return false;
        }
        return true;
      }

      @Override
      public Set<Literal<String>> closure(Set<Literal<String>> set) {
        final HashSet<Literal<String>> closure = new HashSet<Literal<String>>();
        if (set.contains(suLiteral) && set.size() < 19) {
          closure.addAll(cxt2.colHeads());
          return closure;
        }
        closure.addAll(set);
        return closure;
      }

    };
    final NextClosures6C.Result<String, Literal<String>> result = NextClosures6C.compute(cxt2, clop, true);
    for (Entry<Set<Literal<String>>, Set<Literal<String>>> e : result.implications.entrySet()) {
//    System.out.println(e.getKey() + " ==> " + e.getValue());
      final Set<Literal<String>> intent = cxt2.intent(e.getKey());
      System.out.println(e.getKey() + " ==> " + intent);
    }
    System.out.println("---");
    for (Entry<Set<Literal<String>>, Set<Literal<String>>> e : result.implications.entrySet()) {
//    System.out.println(e.getKey() + " ==> " + e.getValue());
      final Set<Literal<String>> intent = cxt2.intent(e.getKey());
      if (intent.contains(suLiteral))
        System.out.println(e.getKey() + " ==> " + "SU");
    }
    System.out.println("--- without contradictory premises");
    for (Entry<Set<Literal<String>>, Set<Literal<String>>> e : result.implications.entrySet())
      if (!isContradictory(e.getKey())) {
        final Set<Literal<String>> intent = cxt2.intent(e.getKey());
        if (intent.contains(suLiteral))
          System.out.println(e.getKey() + " ==> " + "SU");
      }
    System.out.println("--- without empty support");
    for (Entry<Set<Literal<String>>, Set<Literal<String>>> e : result.implications.entrySet())
      if (!cxt2.colAnd(e.getKey()).isEmpty()) {
        final Set<Literal<String>> intent = cxt2.intent(e.getKey());
        if (intent.contains(suLiteral))
          System.out.println(e.getKey() + " ==> " + "SU");
      }
    System.out.println("---");
//    final NextClosures6.Result<Literal<String>, Literal<String>> result2 = NextClosures6.compute(cxt, true);
//    for (Entry<Set<Literal<String>>, Set<Literal<String>>> e : result2.implications.entrySet())
//      System.out.println(e.getKey() + " ==> " + cxt.intent(e.getKey()));
//    System.out.println("---");
    // naive approach
//    final Set<Set<Literal<String>>> prems = new HashSet<Set<Literal<String>>>();
//    for (Literal<String> m1 : cxt.colHeads())
//      for (Literal<String> m2 : cxt.colHeads())
//        if (!m1.equals(m2) && !m1.equals("SU") && !m2.equals("SU")) {
//          final Set<Literal<String>> mm = new HashSet<Literal<String>>();
//          mm.add(m1);
//          mm.add(m2);
//          if (!prems.contains(mm)) {
//            prems.add(mm);
//            if (cxt.intent(mm).contains("SU"))
//              System.out.println(mm + " ==> SU");
//          }
//        }

//    Set<Literal<String>> su = cxt.colAnd("SU");
//    System.out.println(su);
//    System.out.println();
//    Set<Literal<String>> p1 = cxt.colAnd("-N3", "N1", "-N7");
//    System.out.println(p1);
//    System.out.println(su.containsAll(p1));
//    System.out.println();
//    Set<Literal<String>> p2 = cxt.colAnd("-N3", "N5", "-N7");
//    System.out.println(p2);
//    System.out.println(su.containsAll(p2));
//    System.out.println();
    final Set<Literal<String>> premises = new HashSet<Literal<String>>(cxt2.colHeads());
    premises.remove(suLiteral);
    final Set<Literal<String>> conclusions = new HashSet<Literal<String>>();
    conclusions.add(suLiteral);
//    final NextClosuresMN.Result<Literal<String>, Literal<String>> resultMN =
//        NextClosuresMN.<Literal<String>, Literal<String>> compute(cxt, premises, conclusions);
//    for (Entry<Set<Literal<String>>, Set<Literal<String>>> e : resultMN.getImplications().entrySet())
//      System.out.println(e.getKey() + " ==> " + e.getValue());
//    System.out.println("--- without contradictory premises");
//    for (Entry<Set<Literal<String>>, Set<Literal<String>>> e : resultMN.getImplications().entrySet())
//      if (!isContradictory(e.getKey()))
//        System.out.println(e.getKey() + " ==> " + e.getValue());
//    System.out.println("--- without empty support");
//    for (Entry<Set<Literal<String>>, Set<Literal<String>>> e : resultMN.getImplications().entrySet())
//      if (!cxt.colAnd(e.getKey()).isEmpty())
//        System.out.println(e.getKey() + " ==> " + e.getValue());
    System.out.println("-----");
    final NextClosuresMN2.Result<String, Literal<String>> resultMN2 =
        NextClosuresMN2.<String, Literal<String>> compute(cxt2, premises, conclusions);
    for (Entry<Set<Literal<String>>, Set<Literal<String>>> e : resultMN2.getImplications().entrySet())
      System.out.println(e.getKey() + " ==> " + e.getValue());
    System.out.println("--- without contradictory premises");
    for (Entry<Set<Literal<String>>, Set<Literal<String>>> e : resultMN2.getImplications().entrySet())
      if (!isContradictory(e.getKey()))
        System.out.println(e.getKey() + " ==> " + e.getValue());
    System.out.println("--- without empty support");
    for (Entry<Set<Literal<String>>, Set<Literal<String>>> e : resultMN2.getImplications().entrySet())
      if (!cxt2.colAnd(e.getKey()).isEmpty())
        System.out.println(e.getKey() + " ==> " + e.getValue());
  }

  private static final boolean isContradictory(final Set<Literal<String>> s) {
    for (Literal<String> t : s)
      if (!t.getM().startsWith("-") && s.contains(new Literal<String>("-" + t)))
        return true;
    return false;
  }
}
