package conexp.fx.test.algorithm.nextclosure;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import conexp.fx.core.algorithm.nextclosure.NextClosures6C;
import conexp.fx.core.algorithm.nextclosure.NextClosures6C.ClosureOperator;
import conexp.fx.core.algorithm.nextclosure.NextClosures6C.Result;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter2;

public class TestC {

  public static void main(String[] args) {
    final FilenameFilter cxtFilter = new FilenameFilter() {

      @Override
      public final boolean accept(final File dir, final String name) {
        return name.endsWith(".cxt");
      }
    };
//    foo(new File("/Volumes/francesco@tcs/Data/Datasets/ERT/algorithms.cxt"));
    for (File file : new File("/Volumes/francesco@tcs/Data/Contexts/small").listFiles(cxtFilter))
      foo(file);
//    for (File file : new File("/Volumes/francesco@tcs/Data/Contexts/scales").listFiles(cxtFilter))
//      foo(file);
//    for (File file : new File("/Volumes/francesco@tcs/Data/Contexts/random").listFiles(cxtFilter))
//      foo(file);
//    for (File file : new File("/Volumes/francesco@tcs/Data/Contexts/testing-data").listFiles(cxtFilter))
//      foo(file);
  }

  public static void foo(final File file) {
    String space = "";
    for (int i = 0; i < 40 - file.getName().length(); i++)
      space += " ";
    System.out.print(file.getName() + space + " -- ");
    final MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    CXTImporter2.read(cxt, file);
    if (!file.getName().equals("algorithms.cxt")
        && (cxt.colHeads().size() > 30 || cxt.colHeads().isEmpty() || cxt.rowHeads().isEmpty())) {
      System.out.println("skipped");
      return;
    }
    final int rows = cxt.rowHeads().size();
    if (rows > 100) {
      final HashSet<String> truncatedRows = new HashSet<String>(cxt.rowHeads().subList(100, rows));
      cxt.rowHeads().removeAll(truncatedRows);
    }

    final double startTime3 = System.currentTimeMillis();
    NextClosures6C.Result<String, String> result3 = null;
    double runTime3;
    try {
      result3 = NextClosures6C.compute(cxt, new NextClosures6C.ClosureOperator<String>() {

        @Override
        public boolean isClosed(Set<String> set) {
          return set.size() < 3;
        }

        @Override
        public boolean close(Set<String> set) {
          if (set.size() < 3 || set.size() == cxt.colHeads().size())
            return true;
          set.addAll(cxt.colHeads());
          return false;
        }

        @Override
        public Set<String> closure(Set<String> set) {
          if (set.size() < 3 || set.size() == cxt.colHeads().size())
            return new HashSet<String>(set);
          return new HashSet<String>(cxt.colHeads());
        }
      });
      runTime3 = System.currentTimeMillis() - startTime3;
    } catch (Exception e) {
      runTime3 = -1;
    }
    System.out.print("     -- runtime: " + runTime3 + "ms");
    System.out.println(" -- " + cxt.rowHeads().size() + "x" + cxt.colHeads().size());

    for (Entry<Set<String>, Set<String>> i : result3.implications.entrySet())
      System.out.println(i.getKey() + " ==> " + i.getValue());
  }

}