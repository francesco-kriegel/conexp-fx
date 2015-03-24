package conexp.fx.core.importer;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
