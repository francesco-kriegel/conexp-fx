package conexp.fx.experiment;

import java.io.File;

import conexp.fx.core.algorithm.nextclosures.NextClosures6;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter2;

public class Foo {

  public static void main(String[] args) {
    MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    CXTImporter2.read(cxt, new File("Contexts/scales/contranominal_5.cxt"));
    System.out.println(NextClosures6.compute(cxt, true).implications.size());;
  }

}
