package conexp.fx.core.algorithm.nextclosures;

/*-
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2019 Francesco Kriegel
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
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

import conexp.fx.core.context.Context;
import conexp.fx.core.context.Implication;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter;
import conexp.fx.core.math.SetClosureOperator;

public class ICFCA2019 {

  public static void main(String[] args) throws IOException {
    final MatrixContext<String, String> cxt =
        CXTImporter.read(new File("/Users/francesco/workspace/LaTeX/icfca2019-join-implications/example1.cxt"));
    cxt.colHeads().forEach(System.out::println);
    cxt.rowHeads().forEach(System.out::println);
    final Set<String> pset = new HashSet<>();
    pset.add("$\\textsf{Abrupt Onset}$");
    pset.add("$\\textsf{Fever}$");
    pset.add("$\\textsf{Aches}$");
    pset.add("$\\textsf{Chills}$");
    pset.add("$\\textsf{Fatigue}$");
    pset.add("$\\textsf{Sneezing}$");
    pset.add("$\\textsf{Cough}$");
    pset.add("$\\textsf{Stuffy Nose}$");
    pset.add("$\\textsf{Sore Throat}$");
    pset.add("$\\textsf{Headache}$");
    final Set<String> cset = new HashSet<>();
    cset.add("$\\textsf{Cold}$");
    cset.add("$\\textsf{Flu}$");
    final Set<String> baseSet = new HashSet<>();
    baseSet.addAll(pset);
    baseSet.addAll(cset);
    final SetClosureOperator<String> clop = SetClosureOperator.joiningImplications(cxt, pset, cset);
    final Set<Implication<String, String>> implications = NextClosures2
        .compute(
            baseSet,
            clop,
            cxt::colAnd,
            __ -> false,
            Executors.newWorkStealingPool(),
            __ -> {},
            __ -> {},
            System.out::println,
            __ -> {},
            () -> false,
            Collections.emptySet())
        .second();
    System.out.println("implications of clop:");
    implications.forEach(System.out::println);
    System.out.println();
    final Set<Implication<String, String>> pcimplications =
        NextClosures2.transformToJoiningImplications(cxt, pset, cset, implications);
    System.out.println("pc-implications of cxt:");
    pcimplications.forEach(System.out::println);
  }

}
