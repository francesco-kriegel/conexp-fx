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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter;
import conexp.fx.core.util.IterableFile;

public final class NextClosures6BenchmarkCSV {

  public final static void main(String[] args) {
    final File file = new File("/Volumes/francesco/hrsk"); // new File(args[0]);
    readResults(file);
    processResults(file);
  }

  private final static class Result {

    private final String        cxt;
    private final String        cluster;
    private final int           cpu;
    private final int           ram;
    private final List<Integer> runtimes;

    private Result(final String cxt, final String cluster, final int cpu, final int ram, final List<Integer> runtimes) {
      super();
      this.cxt = cxt;
      this.cluster = cluster;
      this.cpu = cpu;
      this.ram = ram;
      this.runtimes = runtimes;
    }

    @Override
    public String toString() {
      return cxt + " -- " + cluster + " -- " + cpu + " -- " + ram + " -- " + runtimes;
    }
  }

  private static final class BestResult {

    private final String                cxt;
    private final int                   rows;
    private final int                   cols;
    private final int                   dens;
    private final Map<Integer, Integer> atlasRAM;
    private final Map<Integer, Integer> atlasTimes;
    private final Map<Integer, Integer> taurusRAM;
    private final Map<Integer, Integer> taurusTimes;

    public BestResult(String cxt, int rows, int cols, int dens) {
      super();
      this.cxt = cxt;
      this.rows = rows;
      this.cols = cols;
      this.dens = dens;
      this.atlasRAM = Maps.newHashMap();
      this.atlasTimes = Maps.newHashMap();
      this.taurusRAM = Maps.newHashMap();
      this.taurusTimes = Maps.newHashMap();
    }

  }

  private final static Multimap<String, Result> results = ArrayListMultimap.create();

  private final static void readResults(final File csvDir) {
    Arrays.asList(
        csvDir.listFiles((f, s) -> s.endsWith(".csv"))).forEach(
        NextClosures6BenchmarkCSV::readResultFile);
  }

  private final static void readResultFile(final File csvFile) {
    final String[] x = csvFile.getName().split(
        "cxt-");
    final String[] y = x[1].replace(
        ".csv",
        "").split(
        "-");
    final String cxt = (x[0] + "cxt").replace(
        "_cxt",
        ".cxt").replace(
        "small_",
        "small/").replace(
        "big_",
        "big/").replace(
        "huge_",
        "huge/").replace(
        "testing-data_",
        "testing-data/").replace(
        "random_",
        "random/").replace(
        "scales_",
        "scales/").replace(
        "__",
        "");
    // .replace("//", "");
    if (!(cxt.contains("big") || cxt.contains("huge") || cxt.contains("random") || cxt.contains("testing-data")))
      return;
    final String cluster = y[0];
    final int it = Integer.valueOf(y[1].replace(
        "it",
        ""));
    final int cpu = Integer.valueOf(y[2].replace(
        "cpu",
        ""));
    final int ram = Integer.valueOf(y[3].replace(
        "ram",
        ""));
    final List<Integer> runtimes = new ArrayList<Integer>();
    final Iterator<String> csvIt = new IterableFile(csvFile).iterator();
    while (csvIt.hasNext()) {
      final String next = csvIt.next();
      if (next.contains(";"))
        runtimes.add(Integer.valueOf(next.split(";")[1]));
    }
    results.put(
        cxt,
        new Result(cxt, cluster, cpu, ram, runtimes));
  }

  private final static void processResults(final File dir) {
    mergeResults();
//    writeBest(dir);
////    writeAverageBest(dir, "small");
////    writeAverageBest(dir, "scales");
//    writeAverageBest(dir, "smallsize", "testing-data");
//    writeAverageBest(dir, "midsize", "big", "huge");
//    writeAverageBest(dir, "bigsize", "random");
////    writeAverageBest(dir, "__");
//    writeAverageBest(dir, "allsize", "testing-data", "huge", "random", "big");
  }

  private final static void mergeResults() {
    final Set<BestResult> bestResults = new HashSet<BestResult>();
    for (String cxt : results.keySet()) {
      final int[] stat = statistics(new File("/Volumes/francesco/Data/Contexts/", cxt));
      final BestResult br = new BestResult(cxt, stat[0], stat[1], stat[2]);
      for (Result r : results.get(cxt)) {
        if (r.cluster.equals("atlas")) {
          br.atlasRAM.put(
              r.cpu,
              r.ram);
          br.atlasTimes.put(
              r.cpu,
              Collections.min(r.runtimes));
        } else if (r.cluster.equals("taurus")) {
          br.taurusRAM.put(
              r.cpu,
              r.ram);
          br.taurusTimes.put(
              r.cpu,
              Collections.min(r.runtimes));
        }
      }
      bestResults.add(br);
    }
    bestResults.stream().forEach(
        r -> {
          try {
            final BufferedWriter w =
                new BufferedWriter(new FileWriter(new File("/Volumes/francesco/Data/results/", r.cxt.replace(
                    "/",
                    "-") + "-atlas.csv")));
            r.atlasTimes.entrySet().stream().sorted(
                (x, y) -> Integer.compare(
                    x.getKey(),
                    y.getKey())).forEach(
                e -> {
                  try {
                    w.append(e.getKey() + ";" + e.getValue() + "\r\n");
                  } catch (Exception e1) {
                    e1.printStackTrace();
                  }
                });
            w.flush();
            w.close();
            final BufferedWriter v =
                new BufferedWriter(new FileWriter(new File("/Volumes/francesco/Data/results/", r.cxt.replace(
                    "/",
                    "-") + "-taurus.csv")));
            r.taurusTimes.entrySet().stream().sorted(
                (x, y) -> Integer.compare(
                    x.getKey(),
                    y.getKey())).forEach(
                e -> {
                  try {
                    v.append(e.getKey() + ";" + e.getValue() + "\r\n");
                  } catch (Exception e1) {
                    e1.printStackTrace();
                  }
                });
            v.flush();
            v.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
    try {
      final BufferedWriter w =
          new BufferedWriter(new FileWriter(new File("/Volumes/francesco/Data/NextClosuresBenchmark.csv")));
      w
          .append("Context;Objects;Attributes;Density;Atlas 1CPU;Atlas 2CPU;Atlas 4CPU;Atlas 8CPU;Atlas 16CPU;Atlas 32CPU;Atlas 48CPU;Atlas 64CPU;Taurus 1CPU;Taurus 2CPU;Taurus 4CPU;Taurus 8CPU;Taurus 16CPU\r\n");
      bestResults.stream().filter(
          r -> !r.cxt.contains("/")).sorted(
          (r, s) -> String.CASE_INSENSITIVE_ORDER.compare(
              r.cxt,
              s.cxt)).forEach(
          r -> {
            try {
//              String[] s = r.cxt.replace(
//                  "_",
//                  "-").split(
//                  "/");
//              String t = "";
//              if (s.length > 1)
//                t = s[0] + ";" + s[1];
//              else
//                t = ";" + s[0];
              w.append(r.cxt.replace(
                  "_",
                  "-") + ";" + r.rows + ";" + r.cols + ";" + r.dens);
              for (int i : Lists.newArrayList(
                  1,
                  2,
                  4,
                  8,
//                  12,
                  16,
//                  24,
                  32,
//                  40,
                  48,
//                  56,
                  64)) {
                final Integer j = r.atlasTimes.get(i);
                w.append(j == null ? ";" : ";" + j);
              }
              for (int i : Lists.newArrayList(
                  1,
                  2,
                  4,
                  8,
//                  12,
                  16)) {
                final Integer j = r.taurusTimes.get(i);
                w.append(j == null ? ";" : ";" + j);
              }
              w.append("\r\n");
            } catch (Exception e) {}
          });
      bestResults.stream().filter(
          r -> r.cxt.contains("/")).sorted(
          (r, s) -> String.CASE_INSENSITIVE_ORDER.compare(
              r.cxt,
              s.cxt)).forEach(
          r -> {
            try {
//              String[] s = r.cxt.replace(
//                  "_",
//                  "-").split(
//                  "/");
//              String t = "";
//              if (s.length > 1)
//                t = s[0] + ";" + s[1];
//              else
//                t = ";" + s[0];
              w.append(r.cxt.replace(
                  "_",
                  "-") + ";" + r.rows + ";" + r.cols + ";" + r.dens);
              for (int i : Lists.newArrayList(
                  1,
                  2,
                  4,
                  8,
//                  12,
                  16,
//                  24,
                  32,
//                  40,
                  48,
//                  56,
                  64)) {
                final Integer j = r.atlasTimes.get(i);
                w.append(j == null ? ";" : ";" + j);
              }
              for (int i : Lists.newArrayList(
                  1,
                  2,
                  4,
                  8,
//                  12,
                  16)) {
                final Integer j = r.taurusTimes.get(i);
                w.append(j == null ? ";" : ";" + j);
              }
              w.append("\r\n");
            } catch (Exception e) {}
          });
      w.flush();
      w.close();
    } catch (IOException e) {}
  }

  public static final String formatTime(final int time) {
    int s = time / 1000;
    int msec = time - s * 1000;

    int m = s / 60;
    int sec = s - m * 60;

    int h = m / 60;
    int min = m - h * 60;

    if (h == 0)
      if (min == 0)
        return String.format(
            "%02ds %03d",
            sec,
            msec);
      else
        return String.format(
            "%02dmin %02ds %03d",
            min,
            sec,
            msec);
    return String.format(
        "%02dh %02dmin %02ds %03d",
        h,
        min,
        sec,
        msec);
  }

  public static final int[] statistics(final File file) {
    final MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    CXTImporter.read(
        cxt,
        file);
    final int rows = cxt.rowHeads().size();
    final int cols = cxt.colHeads().size();
    final int dens;
    if (rows * cols == 0)
      dens = 0;
    else
      dens = (100 * cxt.size()) / (rows * cols);
    return new int[] { rows, cols, dens };
  }

  private final static void printResults() {
    results.entries().forEach(
        System.out::println);
  }

  private final static void writeBest(final File dir) {
    try {
      final BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, "/results/table.csv")));
      final Map<String, Map<Integer, Integer>> taurus = new HashMap<String, Map<Integer, Integer>>();
      final Map<String, Map<Integer, Integer>> atlas = new HashMap<String, Map<Integer, Integer>>();
      for (String cxt : results.keySet()) {
        final Map<Integer, Integer> mapTaurus = new HashMap<Integer, Integer>();
        final Map<Integer, Integer> mapAtlas = new HashMap<Integer, Integer>();
        for (Result result : results.get(cxt))
          if (result.cluster.equals("taurus")) {
            if (!result.runtimes.isEmpty())
              mapTaurus.put(
                  result.cpu,
                  Collections.min(result.runtimes));
          } else if (result.cluster.equals("atlas"))
            if (!result.runtimes.isEmpty())
              mapAtlas.put(
                  result.cpu,
                  Collections.min(result.runtimes));
        taurus.put(
            cxt,
            mapTaurus);
        atlas.put(
            cxt,
            mapAtlas);
      }
      for (Entry<String, Map<Integer, Integer>> e : taurus.entrySet()) {
        if (e.getValue().size() != 6)
          System.out.println(e.getKey() + " has empty result on taurus.");
        else {
          writeCSV(
              e.getValue(),
              new File(dir, "/results/" + e.getKey().replace(
                  ".cxt",
                  "_cxt").replace(
                  "/",
                  "_") + "-taurus-best.csv"));
          bw.append(e.getKey());
          e.getValue().entrySet().stream().sorted(
              (e1, e2) -> Integer.compare(
                  e1.getKey(),
                  e2.getKey())).map(
              Entry::getValue).forEach(
              v -> {
                try {
                  bw.append(";" + v);
                } catch (Exception x) {}
              });
          bw.append("\r\n");
        }
      }
      for (Entry<String, Map<Integer, Integer>> e : atlas.entrySet()) {
        if (e.getValue().size() != 12)
          System.out.println(e.getKey() + " has empty result on atlas.");
        else {
          writeCSV(
              e.getValue(),
              new File(dir, "/results/" + e.getKey().replace(
                  ".cxt",
                  "_cxt").replace(
                  "/",
                  "_") + "-atlas-best.csv"));
          bw.append(e.getKey());
          e.getValue().entrySet().stream().sorted(
              (e1, e2) -> Integer.compare(
                  e1.getKey(),
                  e2.getKey())).map(
              Entry::getValue).forEach(
              v -> {
                try {
                  bw.append(";" + v);
                } catch (Exception x) {}
              });
          bw.append("\r\n");
        }
      }
      bw.flush();
      bw.close();
    } catch (IOException x) {
      x.printStackTrace();
    }

  }

  private static final void writeAverageBest(File dir, String key, String... keys) {
    final Map<String, Map<Integer, Integer>> taurus = new HashMap<String, Map<Integer, Integer>>();
    final Map<String, Map<Integer, Integer>> atlas = new HashMap<String, Map<Integer, Integer>>();
    final Predicate<String> p = s -> {
      for (String k : keys)
        if (s.contains(k))
          return true;
      return false;
    };
    for (String cxt : results.keySet())
      if (p.test(cxt)) {
        final Map<Integer, Integer> mapTaurus = new HashMap<Integer, Integer>();
        final Map<Integer, Integer> mapAtlas = new HashMap<Integer, Integer>();
        for (Result result : results.get(cxt))
          if (result.cluster.equals("taurus")) {
            if (!result.runtimes.isEmpty())
              mapTaurus.put(
                  result.cpu,
                  Collections.min(result.runtimes));
          } else if (result.cluster.equals("atlas"))
            if (!result.runtimes.isEmpty())
              mapAtlas.put(
                  result.cpu,
                  Collections.min(result.runtimes));
        if (mapTaurus.size() == 6)
          taurus.put(
              cxt,
              mapTaurus);
        if (mapAtlas.size() == 12)
          atlas.put(
              cxt,
              mapAtlas);
      }
    final Map<Integer, Integer> averageBestTaurus = new HashMap<Integer, Integer>();
    averageBestTaurus.put(
        1,
        0);
    averageBestTaurus.put(
        2,
        0);
    averageBestTaurus.put(
        4,
        0);
    averageBestTaurus.put(
        8,
        0);
    averageBestTaurus.put(
        12,
        0);
    averageBestTaurus.put(
        16,
        0);
    final Map<Integer, Integer> averageBestAtlas = new HashMap<Integer, Integer>();
    averageBestAtlas.put(
        1,
        0);
    averageBestAtlas.put(
        2,
        0);
    averageBestAtlas.put(
        4,
        0);
    averageBestAtlas.put(
        8,
        0);
    averageBestAtlas.put(
        12,
        0);
    averageBestAtlas.put(
        16,
        0);
    averageBestAtlas.put(
        24,
        0);
    averageBestAtlas.put(
        32,
        0);
    averageBestAtlas.put(
        40,
        0);
    averageBestAtlas.put(
        48,
        0);
    averageBestAtlas.put(
        56,
        0);
    averageBestAtlas.put(
        64,
        0);
    for (Entry<String, Map<Integer, Integer>> e : taurus.entrySet())
      for (Entry<Integer, Integer> f : e.getValue().entrySet())
        averageBestTaurus.put(
            f.getKey(),
            averageBestTaurus.get(f.getKey()) + f.getValue());
    for (Integer cpu : averageBestTaurus.keySet())
      if (!taurus.isEmpty())
        averageBestTaurus.put(
            cpu,
            averageBestTaurus.get(cpu) / taurus.keySet().size());
    for (Entry<String, Map<Integer, Integer>> e : atlas.entrySet())
      for (Entry<Integer, Integer> f : e.getValue().entrySet())
        averageBestAtlas.put(
            f.getKey(),
            averageBestAtlas.get(f.getKey()) + f.getValue());
    for (Integer cpu : averageBestAtlas.keySet())
      if (!atlas.isEmpty())
        averageBestAtlas.put(
            cpu,
            averageBestAtlas.get(cpu) / atlas.keySet().size());
    try {
      writeCSV(
          averageBestTaurus,
          new File(dir, "/results/" + key + "-average_best-taurus.csv"));
      writeCSV(
          averageBestAtlas,
          new File(dir, "/results/" + key + "-average_best-atlas.csv"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static final void writeAverageBest2(File dir) {
    final Multimap<Integer, Integer> bestTaurus = ArrayListMultimap.create();
    final Multimap<Integer, Integer> bestAtlas = ArrayListMultimap.create();
    for (Result result : results.values())
      if (!result.runtimes.isEmpty())
        if (result.cluster.equals("taurus"))
          bestTaurus.put(
              result.cpu,
              Collections.min(result.runtimes));
        else if (result.cluster.equals("atlas"))
          bestAtlas.put(
              result.cpu,
              Collections.min(result.runtimes));
    final Map<Integer, Integer> averageBestTaurus = new HashMap<Integer, Integer>();
    final Map<Integer, Integer> averageBestAtlas = new HashMap<Integer, Integer>();
    for (int cpu : bestTaurus.keySet())
      averageBestTaurus.put(
          cpu,
          average(bestTaurus.get(cpu)));
    for (int cpu : bestAtlas.keySet())
      averageBestAtlas.put(
          cpu,
          average(bestAtlas.get(cpu)));
    try {
      writeCSV(
          averageBestTaurus,
          new File(dir, "/results/average_best-taurus.csv"));
      writeCSV(
          averageBestAtlas,
          new File(dir, "/results/average_best-atlas.csv"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private final static int average(final Collection<Integer> c) {
    int sum = 0;
    for (int i : c)
      sum += i;
    return c.isEmpty() ? Integer.MAX_VALUE : sum / c.size();
  }

  private final static void writeCSV(final Map<Integer, ?> map, final File csvFile) throws IOException {
    Writer writer = new BufferedWriter(new FileWriter(csvFile));
    final ArrayList<Integer> keys = Lists.newArrayList(map.keySet());
    keys.sort(Integer::compare);
    for (Integer key : keys)
//      if (key > 7)
      writer.append(key + ";" + map.get(key) + "\r\n");
    writer.flush();
    writer.close();
  }
}
