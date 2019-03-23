/*
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.exporter;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2019 Francesco Kriegel
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import conexp.fx.core.collections.relation.MatrixRelation;

public class CXTExporter<G, M> {

  public static <G, M> void export(
      MatrixRelation<G, M> context,
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
        G object = context.rowHeads().get(
            domainPermutation.containsKey(domainIndex) ? domainPermutation.get(domainIndex) : domainIndex);
        outputWriter.append(object + "\r\n");
      }
      for (int codomainIndex = 0; codomainIndex < context.colHeads().size(); codomainIndex++) {
        M attribute = context.colHeads().get(
            codomainPermutation.containsKey(codomainIndex) ? codomainPermutation.get(codomainIndex) : codomainIndex);
        outputWriter.append(attribute + "\r\n");
      }
      for (int domainIndex = 0; domainIndex < context.rowHeads().size(); domainIndex++) {
        G object = context.rowHeads().get(
            domainPermutation.containsKey(domainIndex) ? domainPermutation.get(domainIndex) : domainIndex);
        for (int codomainIndex = 0; codomainIndex < context.colHeads().size(); codomainIndex++) {
          M attribute = context.colHeads().get(
              codomainPermutation.containsKey(codomainIndex) ? codomainPermutation.get(codomainIndex) : codomainIndex);
          outputWriter.append((context.contains(object, attribute) ? "X" : "."));
        }
        outputWriter.append("\r\n");
      }
      outputWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static <G, M> void export(MatrixRelation<G, M> formalContext, File file) {
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
