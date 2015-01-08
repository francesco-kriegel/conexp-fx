package conexp.fx.experiment.cellautomata;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import conexp.fx.core.algorithm.nextclosure.NextClosures6C;
import conexp.fx.core.algorithm.nextclosure.NextClosures6C.ClosureOperator;
import conexp.fx.core.algorithm.nextclosure.NextClosuresMN2;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter2;

public class TestMN {

  public static void main(String[] args) {
    final Random r = new Random();
    for (File file : new File("Contexts/small").listFiles(new FilenameFilter() {

      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".cxt");
      }
    })) {
      final MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
      CXTImporter2.read(cxt, file);
      System.out.println(cxt);
      final Set<String> premises = new HashSet<String>();
      final Set<String> conclusions = new HashSet<String>();
      for (String m : cxt.colHeads())
        if (r.nextBoolean())
          premises.add(m);
        else
          conclusions.add(m);
      System.out.println(premises);
      System.out.println(conclusions);
      final ClosureOperator<String> clop = new ClosureOperator<String>() {

        @Override
        public boolean isClosed(Set<String> set) {
          return Sets.intersection(set, conclusions).isEmpty() || set.size() == cxt.colHeads().size();
        }

        @Override
        public boolean close(Set<String> set) {
          if (!isClosed(set)) {
            set.addAll(cxt.colHeads());
            return false;
          }
          return true;
        }

        @Override
        public Set<String> closure(Set<String> set) {
          final HashSet<String> closure = new HashSet<String>();
          if (!isClosed(set)) {
            closure.addAll(cxt.colHeads());
            return closure;
          }
          closure.addAll(set);
          return closure;
        }

      };
//      final NextClosures6C.Result<String, String> result = NextClosures6C.compute(cxt, clop);
//      for (Entry<Set<String>, Set<String>> e : result.implications.entrySet())
//        if (!cxt.intent(e.getKey()).isEmpty())
//          System.out.println(e.getKey() + " ==> " + Sets.intersection(cxt.intent(e.getKey()), conclusions));
//      System.out.println("---");
      final NextClosuresMN2.Result<String, String> resultMN =
          NextClosuresMN2.<String, String> compute(cxt, premises, conclusions);
      for (Entry<Set<String>, Set<String>> e : resultMN.getImplications().entrySet())
        if (!cxt.intent(e.getKey()).isEmpty())
          System.out.println(e.getKey() + " ==> " + e.getValue());
    }
  }
}
