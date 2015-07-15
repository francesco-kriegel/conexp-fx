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
import java.io.Writer;
import java.util.Arrays;

import conexp.fx.core.algorithm.nextclosures.NextClosures;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter;

public class NextClosures6Benchmark {

//  public static void main(String[] args) {
//    try {
//      test(Integer.valueOf(args[0]), Integer.valueOf(args[1]), Boolean.valueOf(args[2]));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }
//
//  @Test
//  public static void test(final int cores, final int iterations, final boolean ht) throws IOException {
//    final Writer writer = new BufferedWriter(new FileWriter("NextClosures6Benchmark.csv"));
//    final List<MatrixContext<String, String>> contexts = Lists.newArrayList(TestContexts.random());
//    contexts.sort((x, y) -> Integer.signum(x.colHeads().size() - y.colHeads().size()));
//    for (MatrixContext<String, String> cxt : contexts)
////      if (cxt.colHeads().size() <= 40)
//      for (int poolSize = 16; poolSize <= cores / (ht ? 2 : 1); poolSize = poolSize * 2) {
//        System.out.print(cxt.id.get() + ";" + poolSize + "cpu;");
//        final long start = System.currentTimeMillis();
//        for (int i = 0; i < iterations; i++)
//          NextClosures6.compute(cxt, false, poolSize);
//        final long duration = (long) (((double) (System.currentTimeMillis() - start)) / (double) iterations);
//        final String result = cxt.id.get() + ";" + poolSize + "cpu;" + duration + "ms\r\n";
//        System.out.print(duration + "ms\r\n");
//        writer.append(result);
//        writer.flush();
//      }
//    writer.close();
//  }

  public static void main(String[] args) {
    if (args.length < 3) {
      System.out.println("Please specify at least 4 arguments on the command line and start again.");
      System.out.println("argument 1: path to formal context");
      System.out.println("argument 2: suffix for benchmark result file");
      System.out.println("argument 3: number of iterations");
      System.out.println("argument 4: number of cpu cores");
      System.out.println("argument 5: ram in mb");
//      System.out.println("argument 4-n: number of cpu cores");
      throw new RuntimeException("invalid number of arguments.");
    }
//    String cpus = "";
//    int[] cores = new int[args.length - 3];
//    for (int i = 3; i < args.length; i++) {
//      cpus += args[i] + "_";
//      cores[i - 3] = Integer.valueOf(args[i]);
//    }
//    cpus = cpus.substring(0, cpus.length() - 1);
    String resultFilename =
        args[0].substring(args[0].indexOf("Contexts") + "Contexts".length() + 1).replace("/", "_").replace(".", "_")
            + "-" + args[1] + "-" + args[2] + "it-" + args[3] + "cpu-" + args[4] + "ram" + ".csv";
    try {
      benchmark(resultFilename, new File(args[0]), args[1], Integer.valueOf(args[2]), Integer.valueOf(args[3]));
    } catch (NumberFormatException | IOException e) {
      e.printStackTrace();
    }
  }

  public static void benchmark(
      final String resultsFile,
      final File cxtFile,
      final String suffix,
      final int iterations,
      final int... cores) throws IOException {
//    final String cxtName =
//        cxtFile.getName().contains(".") ? cxtFile.getName().substring(0, cxtFile.getName().lastIndexOf(".")) : cxtFile
//            .getName();
//    final String resultsFile = cxtName + "-" + suffix + "-" + iterations + "iterations" + ".csv";
    System.out.println("Benchmark started.");
    System.out.println("Formal Context: " + cxtFile.getName());
    System.out.println("Iterations: " + iterations);
    System.out.println("Cores: " + Arrays.toString(cores));
    System.out.println("Results file: " + resultsFile);
    final MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    CXTImporter.read(cxt, cxtFile);
    final Writer writer = new BufferedWriter(new FileWriter(resultsFile, true));
    for (int core : cores) {
      for (int it = 0; it < iterations; it++) {
        final long start = System.currentTimeMillis();
        NextClosures.compute(cxt, false, core);
        final long duration = System.currentTimeMillis() - start;
//        final long duration = (long) (((double) (System.currentTimeMillis() - start)) / (double) iterations);
        System.out.println("Runtime: " + duration + "ms @ " + core + "cpus");
        writer.append(core + ";" + duration);
        writer.append("\r\n");
        writer.flush();
      }
    }
    writer.close();
  }
}
