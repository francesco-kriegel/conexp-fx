package conexp.fx.test;

import java.io.File;
import java.util.Map.Entry;
import java.util.Set;

import conexp.fx.core.algorithm.nextclosure.NextClosures6;
import conexp.fx.core.algorithm.nextclosure.NextClosures6.Result;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter2;

public class Test {

  public static void main(String[] args) {
    MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    CXTImporter2.read(cxt, new File("/Users/francesco/workspace/Java/conexp-fx/sample2.cxt"));
    System.out.println(cxt);
    final Result<String, String> result = NextClosures6.compute(cxt, true);
    for (Entry<Set<String>, Set<String>> e : result.implications.entrySet())
      System.out.println(e.getKey() + " ==> " + e.getValue());
  }

}
