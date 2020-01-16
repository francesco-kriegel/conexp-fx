package conexp.fx.core.dl.deprecated;

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

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import conexp.fx.core.dl.ELConceptDescription;
import conexp.fx.core.dl.ELConceptInclusion;
import conexp.fx.core.dl.ELInterpretation2;
import conexp.fx.core.dl.ELParser;
import conexp.fx.core.dl.ELTBox;

public class ELCanModTest {

  public static void main(String[] args) {
//    foo();
//    bar();
//    baz2();
    System.out.println(ELConceptDescription.parse("∃r.(B⊓∃r.⊤⊓∃r.C)⊓∃r.(C⊓∃r.B⊓∃r.⊤)").reduce());
  }

  private static void foo() {
    // This is the example in my DAM 2019 paper.
    final ELTBox tBox = new ELTBox();
//    tBox
//        .getConceptInclusions()
//        .add(
//            new ELConceptInclusion(
//                ELConceptDescription.conceptName(IRI.create("A")),
//                ELConceptDescription
//                    .existentialRestriction(
//                        IRI.create("r"),
//                        ELConceptDescription.existentialRestriction(IRI.create("r"), ELConceptDescription.top()))));
//    tBox
//        .getConceptInclusions()
//        .add(
//            new ELConceptInclusion(
//                ELConceptDescription.existentialRestriction(IRI.create("r"), ELConceptDescription.top()),
//                ELConceptDescription
//                    .existentialRestriction(IRI.create("s"), ELConceptDescription.conceptName(IRI.create("A")))));
    tBox.getConceptInclusions().add(new ELConceptInclusion(ELParser.read("A"), ELParser.read("exists r.exists r.Top")));
    tBox.getConceptInclusions().add(new ELConceptInclusion(ELParser.read("exists r.Top"), ELParser.read("exists s.A")));
//    final ELConceptDescription C = new ELConceptDescription();
//    C.getConceptNames().add(IRI.create("A"));
//    C.getConceptNames().add(IRI.create("B"));
    final ELConceptDescription C = ELParser.read("A and B");
    System.out.println(tBox);
    System.out.println(C);
    System.out.println(tBox.getCanonicalModel(C));
    for (int d = 0; d < 5; d++) {
      System.out.println(tBox.getMostSpecificConsequence(C, d));
    }
  }

  private static void bar() {
    final Set<Set<Integer>> hypergraph = new HashSet<>();
    hypergraph.add(Sets.newHashSet(1, 2, 3));
    hypergraph.add(Sets.newHashSet(2, 3, 4));
    hypergraph.add(Sets.newHashSet(2, 5, 8));
    hypergraph.add(Sets.newHashSet(7, 8, 9));
//    hypergraph.add(Sets.newHashSet());
//    hypergraph.add(Sets.newHashSet());
    final Set<Set<Integer>> mhs = ELInterpretation2.getMinimalHittingSets(hypergraph);
    System.out.println(hypergraph);
    System.out.println(mhs);
  }

  private static ELConceptInclusion parseCI(String subsumee, String subsumer) {
    return new ELConceptInclusion(ELParser.read(subsumee), ELParser.read(subsumer));
  }

  private static void baz0() {
    final ELTBox tBox = new ELTBox();
    tBox.getConceptInclusions().add(parseCI("Elefant", "Mammal"));
    tBox.getConceptInclusions().add(parseCI("Mammal", "exists hasParent. Mammal"));
    tBox.getConceptInclusions().add(parseCI("Elefant", "exists hasParent. Elefant"));
    final ELConceptDescription C = ELParser.read("Elefant");
    System.out.println(tBox);
    System.out.println(C);
    System.out.println(tBox.getCanonicalModel(C));
    for (int d = 0; d < 5; d++) {
      System.out.println(tBox.getMostSpecificConsequence(C, d));
    }
  }

  private static void baz1() {
    final ELTBox tBox = new ELTBox();
    tBox.getConceptInclusions().add(parseCI("exists hasChild. Top", "Parent"));
    tBox.getConceptInclusions().add(parseCI("exists hasChild. exists hasChild. Top", "Grandparent"));
    final ELConceptDescription C = ELParser.read("exists hasChild. exists hasChild. Top");
    System.out.println(tBox);
    System.out.println(C);
    System.out.println(tBox.getCanonicalModel(C));
    for (int d = 0; d < 5; d++) {
      System.out.println(tBox.getMostSpecificConsequence(C, d));
    }
  }

  private static void baz2() {
    final ELTBox tBox = new ELTBox();
    tBox.getConceptInclusions().add(parseCI("A", "exists r.B"));
    tBox.getConceptInclusions().add(parseCI("B", "C"));
    tBox.getConceptInclusions().add(parseCI("exists r. C", "exists s. exists r.B"));
    tBox.getConceptInclusions().add(parseCI("exists s. Top", "C"));
    tBox.getConceptInclusions().add(parseCI("A and B", "exists r. C"));
    final ELConceptDescription C = ELParser.read("A");
    System.out.println(tBox);
    System.out.println(C);
    System.out.println(tBox.getCanonicalModel(C));
    System.out.println(tBox.getCanonicalModelLutz(C));
    for (int d = 0; d < 5; d++) {
      final ELConceptDescription mss = tBox.getMostSpecificConsequence(C, d);
      System.out.println(mss);
      final ELConceptDescription mssL = tBox.getMostSpecificConsequenceLutz(C, d);
      System.out.println(mssL);
      System.out.println(mss.isEquivalentTo(mssL));
    }
  }

}
