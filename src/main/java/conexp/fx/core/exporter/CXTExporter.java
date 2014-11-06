/*
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.exporter;

/*
 * #%L
 * Concept Explorer FX - Core
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
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
import java.util.Map;

import conexp.fx.core.context.MatrixContext;


public class CXTExporter<G, M> {

  public static <G, M> void export(
      MatrixContext<G, M> context,
      Map<Integer, Integer> domainPermutation,
      Map<Integer, Integer> codomainPermutation,
      File file) {
    try {
      if (!file.exists()) {
        if (!file.getParentFile().exists())
          file.mkdirs();
        file.createNewFile();
      }
      final BufferedWriter outputWriter = new BufferedWriter(new FileWriter(file));
      outputWriter.append("B\r\n");
      outputWriter.append("\r\n");
      outputWriter.append(context.rowHeads().size() + "\r\n");
      outputWriter.append(context.colHeads().size() + "\r\n");
      outputWriter.append("\r\n");
      for (int domainIndex = 0; domainIndex < context.rowHeads().size(); domainIndex++) {
        G object =
            context.rowHeads().get(
                domainPermutation.containsKey(domainIndex) ? domainPermutation.get(domainIndex) : domainIndex);
        outputWriter.append(object + "\r\n");
      }
      for (int codomainIndex = 0; codomainIndex < context.colHeads().size(); codomainIndex++) {
        M attribute =
            context
                .colHeads()
                .get(
                    codomainPermutation.containsKey(codomainIndex) ? codomainPermutation.get(codomainIndex)
                        : codomainIndex);
        outputWriter.append(attribute + "\r\n");
      }
      for (int domainIndex = 0; domainIndex < context.rowHeads().size(); domainIndex++) {
        G object =
            context.rowHeads().get(
                domainPermutation.containsKey(domainIndex) ? domainPermutation.get(domainIndex) : domainIndex);
        for (int codomainIndex = 0; codomainIndex < context.colHeads().size(); codomainIndex++) {
          M attribute =
              context.colHeads().get(
                  codomainPermutation.containsKey(codomainIndex) ? codomainPermutation.get(codomainIndex)
                      : codomainIndex);
          outputWriter.append((context.contains(object, attribute) ? "X" : "."));
        }
        outputWriter.append("\r\n");
      }
      outputWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static <G, M> void export(MatrixContext<G, M> formalContext, File file) {
    try {
      if (!file.exists())
        file.createNewFile();
      final BufferedWriter outputWriter = new BufferedWriter(new FileWriter(file));
      outputWriter.append("B\r\n");
      outputWriter.append("\r\n");
      outputWriter.append(formalContext.rowHeads().size() + "\r\n");
      outputWriter.append(formalContext.colHeads().size() + "\r\n");
      outputWriter.append("\r\n");
      for (G object : formalContext.rowHeads()) {
        outputWriter.append(object + "\r\n");
      }
      for (M attribute : formalContext.colHeads()) {
        outputWriter.append(attribute + "\r\n");
      }
      for (G object : formalContext.rowHeads()) {
        for (M attribute : formalContext.colHeads()) {
          outputWriter.append((formalContext.contains(object, attribute) ? "X" : "."));
        }
        outputWriter.append("\r\n");
      }
      outputWriter.append("\r\n");
      outputWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
