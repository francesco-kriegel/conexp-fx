package conexp.fx.core.exporter;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import de.tudresden.inf.tcs.fcalib.Implication;

public class ImplicationWriter {

  public final static <M> void export(final List<Implication<M>> implications, final File file) throws IOException {
    System.out.println("writing " + implications.size() + " to " + file.getAbsolutePath());
    FileWriter fw = null;
    BufferedWriter bw = null;
    try {
      fw = new FileWriter(file);
      bw = new BufferedWriter(fw);
      bw.append("implications:\n");
      for (Implication<M> imp : implications)
        bw.append(imp.toString());
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      bw.close();
      fw.close();
    }
  }

}
