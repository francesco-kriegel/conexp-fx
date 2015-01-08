package conexp.fx.core.importer;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.context.temporal.TemporalContext;
import conexp.fx.core.util.IterableFile;

public class CASimImporter {

  public static final TemporalContext<String, String> read(final File file) {
    final TemporalContext<String, String> tcxt = new TemporalContext<String, String>();
    final Iterator<String> it = new IterableFile(file).iterator();
    while (it.hasNext()) {
      final String l = it.next();
      if (l.contains("formal context"))
        if (it.hasNext()) {
          final MatrixContext<String, String> cxt = tcxt.addTimepoint();
          int i = 0;
          cxt.colHeads().addAll(Arrays.asList(it.next().trim().split(" ")));
          while (it.hasNext()) {
            final String r = it.next();
            if (r.trim().isEmpty())
              break;
            final String[] split = r.split(":");
            cxt.rowHeads().add(split[0]);
            final String c = split[1].toLowerCase().replaceAll("\\s", "");
            for (int j = 0; j < c.length(); j++)
              if (c.charAt(j) == 'x')
                cxt._add(i, j);
            i++;
          }
          System.out.println(cxt);
        }
    }
    return tcxt;
  }
}
