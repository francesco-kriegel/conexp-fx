package conexp.fx.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import conexp.fx.core.algorithm.nextclosure.NextClosures6;
import conexp.fx.core.context.MatrixContext;

public class NextClosures6Benchmark {

  public static void main(String[] args) {
    try {
      test(3, true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public static void test(final int iterations, final boolean ht) throws IOException {
    final Writer writer = new BufferedWriter(new FileWriter("NextClosures6Benchmark.csv"));
    final List<MatrixContext<String, String>> contexts = Lists.newArrayList(TestContexts.random());
    contexts.sort((x, y) -> Integer.signum(x.colHeads().size() - y.colHeads().size()));
    for (MatrixContext<String, String> cxt : contexts)
//      if (cxt.colHeads().size() <= 40)
      for (int poolSize = 1; poolSize <= Runtime.getRuntime().availableProcessors() / (ht ? 2 : 1); poolSize++) {
        System.out.print(cxt.id.get() + ";" + poolSize + "cpu;");
        final long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++)
          NextClosures6.compute(cxt, false, poolSize);
        final long duration = (long) (((double) (System.currentTimeMillis() - start)) / (double) iterations);
        final String result = cxt.id.get() + ";" + poolSize + "cpu;" + duration + "ms\r\n";
        System.out.print(duration + "ms\r\n");
        writer.append(result);
        writer.flush();
      }
    writer.close();
  }
}
