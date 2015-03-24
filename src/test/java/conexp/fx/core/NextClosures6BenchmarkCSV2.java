package conexp.fx.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import conexp.fx.core.util.IterableFile;

public class NextClosures6BenchmarkCSV2 {

  public static void main(String[] args) {
    process("/Users/francesco/workspace/LaTeX/NextClosures_report/csv");
  }

  public static final void process(final String dir) {
    Arrays.asList(new File(dir).listFiles((f, n) -> n.endsWith(".csv") && !n.contains("-relative"))).forEach(
        NextClosures6BenchmarkCSV2::processCSV);
  }

  public static final void processCSV(final File file) {
    final Iterator<String> it = new IterableFile(file).iterator();
    final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
    while (it.hasNext()) {
      final String next = it.next();
      if (next.contains(";")) {
        final String[] s = next.split(";");
        map.put(Integer.valueOf(s[0]), Integer.valueOf(s[1]));
      }
    }
    final Map<Integer, Integer> relmap = new HashMap<Integer, Integer>();
    final int basetime = map.get(1);
    for (Entry<Integer, Integer> e : map.entrySet())
      relmap.put(e.getKey(), basetime / e.getValue());
    final File output = new File(file.getParentFile(), file.getName().replace(".csv", "-relative.txt"));
    try {
      final Writer fw = new BufferedWriter(new FileWriter(output));
      final List<Integer> keys = new ArrayList<Integer>(relmap.keySet());
      keys.sort(Integer::compare);
      for (Integer key : keys)
        fw.append(key + "&$\\frac{1}{" + relmap.get(key) + "}$\\\\\r\n");
      fw.flush();
      fw.close();
    } catch (IOException x) {
      x.printStackTrace();
    }
  }

}
