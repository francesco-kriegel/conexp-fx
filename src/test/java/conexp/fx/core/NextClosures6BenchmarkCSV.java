package conexp.fx.core;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import conexp.fx.core.util.IterableFile;

public final class NextClosures6BenchmarkCSV {

  public final static void main(String[] args) {
    final File file = new File("/Users/francesco/hrsk"); // new File(args[0]);
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

  private final static Multimap<String, Result> results = ArrayListMultimap.create();

  private final static void readResults(final File csvDir) {
    Arrays.asList(csvDir.listFiles((f, s) -> s.endsWith(".csv"))).forEach(NextClosures6BenchmarkCSV::readResultFile);
  }

  private final static void readResultFile(final File csvFile) {
    final String[] x = csvFile.getName().split("cxt-");
    final String[] y = x[1].replace(".csv", "").split("-");
    final String cxt = (x[0] + "cxt").replace("_cxt", ".cxt").replace("_", "/"); // .replace("//", "");
    final String cluster = y[0];
    final int it = Integer.valueOf(y[1].replace("it", ""));
    final int cpu = Integer.valueOf(y[2].replace("cpu", ""));
    final int ram = Integer.valueOf(y[3].replace("ram", ""));
    final List<Integer> runtimes = new ArrayList<Integer>();
    final Iterator<String> csvIt = new IterableFile(csvFile).iterator();
    while (csvIt.hasNext()) {
      final String next = csvIt.next();
      if (next.contains(";"))
        runtimes.add(Integer.valueOf(next.split(";")[1]));
    }
    results.put(cxt, new Result(cxt, cluster, cpu, ram, runtimes));
  }

  private final static void processResults(final File dir) {
    writeBest(dir);
//    writeAverageBest(dir, "small");
//    writeAverageBest(dir, "scales");
    writeAverageBest(dir, "smallsize", "testing-data");
    writeAverageBest(dir, "midsize", "big", "huge");
    writeAverageBest(dir, "bigsize", "random");
//    writeAverageBest(dir, "__");
    writeAverageBest(dir, "allsize", "testing-data", "huge", "random", "big");
  }

  private final static void printResults() {
    results.entries().forEach(System.out::println);
  }

  private final static void writeBest(final File dir) {
    final Map<String, Map<Integer, Integer>> taurus = new HashMap<String, Map<Integer, Integer>>();
    final Map<String, Map<Integer, Integer>> atlas = new HashMap<String, Map<Integer, Integer>>();
    for (String cxt : results.keySet()) {
      final Map<Integer, Integer> mapTaurus = new HashMap<Integer, Integer>();
      final Map<Integer, Integer> mapAtlas = new HashMap<Integer, Integer>();
      for (Result result : results.get(cxt))
        if (result.cluster.equals("taurus")) {
          if (!result.runtimes.isEmpty())
            mapTaurus.put(result.cpu, Collections.min(result.runtimes));
        } else if (result.cluster.equals("atlas"))
          if (!result.runtimes.isEmpty())
            mapAtlas.put(result.cpu, Collections.min(result.runtimes));
      taurus.put(cxt, mapTaurus);
      atlas.put(cxt, mapAtlas);
    }
    try {
      for (Entry<String, Map<Integer, Integer>> e : taurus.entrySet()) {
        if (e.getValue().size() != 6)
          System.out.println(e.getKey() + " has empty result on taurus.");
        else
          writeCSV(e.getValue(), new File(dir, "/results/" + e.getKey().replace(".cxt", "_cxt").replace("/", "_")
              + "-taurus-best.csv"));
      }
      for (Entry<String, Map<Integer, Integer>> e : atlas.entrySet()) {
        if (e.getValue().size() != 12)
          System.out.println(e.getKey() + " has empty result on atlas.");
        else
          writeCSV(e.getValue(), new File(dir, "/results/" + e.getKey().replace(".cxt", "_cxt").replace("/", "_")
              + "-atlas-best.csv"));
      }
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
              mapTaurus.put(result.cpu, Collections.min(result.runtimes));
          } else if (result.cluster.equals("atlas"))
            if (!result.runtimes.isEmpty())
              mapAtlas.put(result.cpu, Collections.min(result.runtimes));
        if (mapTaurus.size() == 6)
          taurus.put(cxt, mapTaurus);
        if (mapAtlas.size() == 12)
          atlas.put(cxt, mapAtlas);
      }
    final Map<Integer, Integer> averageBestTaurus = new HashMap<Integer, Integer>();
    averageBestTaurus.put(1, 0);
    averageBestTaurus.put(2, 0);
    averageBestTaurus.put(4, 0);
    averageBestTaurus.put(8, 0);
    averageBestTaurus.put(12, 0);
    averageBestTaurus.put(16, 0);
    final Map<Integer, Integer> averageBestAtlas = new HashMap<Integer, Integer>();
    averageBestAtlas.put(1, 0);
    averageBestAtlas.put(2, 0);
    averageBestAtlas.put(4, 0);
    averageBestAtlas.put(8, 0);
    averageBestAtlas.put(12, 0);
    averageBestAtlas.put(16, 0);
    averageBestAtlas.put(24, 0);
    averageBestAtlas.put(32, 0);
    averageBestAtlas.put(40, 0);
    averageBestAtlas.put(48, 0);
    averageBestAtlas.put(56, 0);
    averageBestAtlas.put(64, 0);
    for (Entry<String, Map<Integer, Integer>> e : taurus.entrySet())
      for (Entry<Integer, Integer> f : e.getValue().entrySet())
        averageBestTaurus.put(f.getKey(), averageBestTaurus.get(f.getKey()) + f.getValue());
    for (Integer cpu : averageBestTaurus.keySet())
      if (!taurus.isEmpty())
        averageBestTaurus.put(cpu, averageBestTaurus.get(cpu) / taurus.keySet().size());
    for (Entry<String, Map<Integer, Integer>> e : atlas.entrySet())
      for (Entry<Integer, Integer> f : e.getValue().entrySet())
        averageBestAtlas.put(f.getKey(), averageBestAtlas.get(f.getKey()) + f.getValue());
    for (Integer cpu : averageBestAtlas.keySet())
      if (!atlas.isEmpty())
        averageBestAtlas.put(cpu, averageBestAtlas.get(cpu) / atlas.keySet().size());
    try {
      writeCSV(averageBestTaurus, new File(dir, "/results/" + key + "-average_best-taurus.csv"));
      writeCSV(averageBestAtlas, new File(dir, "/results/" + key + "-average_best-atlas.csv"));
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
          bestTaurus.put(result.cpu, Collections.min(result.runtimes));
        else if (result.cluster.equals("atlas"))
          bestAtlas.put(result.cpu, Collections.min(result.runtimes));
    final Map<Integer, Integer> averageBestTaurus = new HashMap<Integer, Integer>();
    final Map<Integer, Integer> averageBestAtlas = new HashMap<Integer, Integer>();
    for (int cpu : bestTaurus.keySet())
      averageBestTaurus.put(cpu, average(bestTaurus.get(cpu)));
    for (int cpu : bestAtlas.keySet())
      averageBestAtlas.put(cpu, average(bestAtlas.get(cpu)));
    try {
      writeCSV(averageBestTaurus, new File(dir, "/results/average_best-taurus.csv"));
      writeCSV(averageBestAtlas, new File(dir, "/results/average_best-atlas.csv"));
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
