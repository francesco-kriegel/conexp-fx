package conexp.fx.core.algorithm.nextclosures;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;

import conexp.fx.core.algorithm.nextclosure.exploration.AttributeExploration;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.implication.Implication;
import conexp.fx.core.importer.CXTImporter;

public class NextClosureComparison {

  public static final Map<File, Long[]> results    = new HashMap<File, Long[]>();

  public static final int               iterations = 1;

  public static void main(String[] args) {
//    processDirectory(new File("/Users/francesco/Data/Contexts"));
    for (String file : new String[] {
//        "big/car.cxt",
//        "big/tic-tac-toe.cxt",
        "random/o1000a36d17.cxt",
        "big/wine.cxt",
        "huge/algorithms.cxt"
//        "random/o1000a10d10.cxt",
//        "random/o1000a20d10.cxt",
//        "testing-data/24.cxt", 
//        "testing-data/35.cxt",
//        "testing-data/51.cxt",
//        "testing-data/54.cxt",
//        "testing-data/79.cxt"
    })
      processFile(new File("/Users/francesco/Data/Contexts/", file));
  }

  public static void processDirectory(final File directory) {
    for (File file : directory.listFiles())
      if (file.isDirectory())
        processDirectory(file);
      else
        processFile(file);
  }

  public static void processFile(final File file) {
    if (file.getName().endsWith(
        ".cxt")) {
      System.out.println("Starting Benchmark on " + file);
      MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
      CXTImporter.read(
          cxt,
          file);

      final long start1 = System.currentTimeMillis();
      for (int i = 0; i < iterations; i++) {
        AttributeExploration.getCanonicalBase(cxt);
      }
      final long time1 = (System.currentTimeMillis() - start1) / iterations;
      System.out.println(time1 + " ms");

      Map<Set<String>, Set<String>> canonicalBase3 = null;
      final long start3 = System.currentTimeMillis();
      for (int i = 0; i < iterations; i++) {
        NextClosures.compute(
            cxt,
            false,
            1);
      }
      final long time3 = (System.currentTimeMillis() - start3) / iterations;
      System.out.println(time3 + " ms");

      final long start4 = System.currentTimeMillis();
      for (int i = 0; i < iterations; i++) {
        NextClosures.compute(
            cxt,
            false,
            2);
      }
      final long time4 = (System.currentTimeMillis() - start4) / iterations;
      System.out.println(time4 + " ms");

      final long start5 = System.currentTimeMillis();
      for (int i = 0; i < iterations; i++) {
        NextClosures.compute(
            cxt,
            false,
            3);
      }
      final long time5 = (System.currentTimeMillis() - start5) / iterations;
      System.out.println(time5 + " ms");

      final long start6 = System.currentTimeMillis();
      for (int i = 0; i < iterations; i++) {
        NextClosures.compute(
            cxt,
            false,
            4);
      }
      final long time6 = (System.currentTimeMillis() - start6) / iterations;
      System.out.println(time6 + " ms");
      System.out.println();

//      if (!compare(
//          canonicalBase,
//          canonicalBase3)) {
//        System.out.println(canonicalBase);
//        System.out.println(canonicalBase3);
//        throw new RuntimeException("different results.");
//      }

      results.put(
          file,
          new Long[] { time1, time3, time4, time5, time6 });
    }
    writeResultsToCSV();
  }

  private static final boolean compare(
      Set<Implication<String, String>> canonicalBase,
      Map<Set<String>, Set<String>> canonicalBase3) {
    if (canonicalBase == null || canonicalBase3 == null)
      return false;
    if (canonicalBase.size() != canonicalBase3.entrySet().size())
      return false;
    for (Implication<String, String> impl : canonicalBase)
      if (!Sets.union(
          impl.getPremise(),
          canonicalBase3.get(impl.getPremise())).equals(
          Sets.union(
              impl.getPremise(),
              impl.getConclusion()))) {
        return false;
      }
    return true;
  }

  private static final void writeResultsToCSV() {
    final File out = new File("/Users/francesco/workspace/LaTeX/cla2015/csv/comparison.csv");
    try {
      final BufferedWriter bw = new BufferedWriter(new FileWriter(out));
      bw.append("File;NextClosure;NextClosures1;NextClosures2;NextClosures3;NextClosures4\r\n");
      for (Entry<File, Long[]> e : results.entrySet()){
        bw.append(e.getKey() + ";" + e.getValue()[0] + ";" + e.getValue()[1] + ";" + e.getValue()[2] + ";"
            + e.getValue()[3] + ";" + e.getValue()[4] + "\r\n");
        bw.append("(0.5,"+e.getValue()[0]+")\r\n");
        bw.append("(1,"+e.getValue()[1]+")\r\n");
        bw.append("(2,"+e.getValue()[2]+")\r\n");
        bw.append("(3,"+e.getValue()[3]+")\r\n");
        bw.append("(4,"+e.getValue()[4]+")\r\n");
      }
      bw.flush();
      bw.close();
    } catch (IOException x) {}
  }
}
