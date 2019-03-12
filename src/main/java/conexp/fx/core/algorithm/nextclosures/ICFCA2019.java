package conexp.fx.core.algorithm.nextclosures;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

import conexp.fx.core.context.Context;
import conexp.fx.core.context.Implication;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter;
import conexp.fx.core.math.ClosureOperator;

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
    final ClosureOperator<String> clop = ClosureOperator.joiningImplications(cxt, pset, cset);
    final Set<Implication<String, String>> implications = NextClosures2
        .compute(
            baseSet,
            clop,
            cxt::colAnd,
            Executors.newWorkStealingPool(),
            __ -> {},
            __ -> {},
            System.out::println,
            __ -> {},
            () -> false)
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
