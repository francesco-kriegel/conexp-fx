package conexp.fx.core;

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
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import conexp.fx.core.algorithm.nextclosure.NextClosure2;
import conexp.fx.core.algorithm.nextclosures.NextClosures;
import conexp.fx.core.algorithm.nextclosures.NextClosures4;
import conexp.fx.core.algorithm.nextclosures.NextClosures6;
import conexp.fx.core.algorithm.nextclosures.NextClosures6C;
import conexp.fx.core.algorithm.nextclosures.NextClosures4.Result;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter2;

public class NextClosuresTest {

  public static final void main(String[] args) {
    run("/Volumes/francesco/Data/");
  }

  public static final void run(final String path) {
    final FilenameFilter cxtFilter = new FilenameFilter() {

      @Override
      public final boolean accept(final File dir, final String name) {
        return name.endsWith(".cxt");
      }
    };
//    test(new File(path + "Datasets/ERT/algorithms.cxt"));
    for (File file : new File(path + "Contexts/small").listFiles(cxtFilter))
      test(file);
    for (File file : new File(path + "Contexts/scales").listFiles(cxtFilter))
      test(file);
//    for (File file : new File(path + "Contexts/big").listFiles(cxtFilter))
//      test(file);
    for (File file : new File(path + "Contexts/random").listFiles(cxtFilter))
      test(file);
//    for (File file : new File(path + "Contexts/testing-data").listFiles(cxtFilter))
//      test(file);
  }

  private static void test(final File file) {
    String space = "";
    for (int i = 0; i < 40 - file.getName().length(); i++)
      space += " ";
    System.out.print(file.getName() + space + " -- ");
    final MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    CXTImporter2.read(cxt, file);
    if (!file.getName().equals("algorithms.cxt")
        && (cxt.colHeads().size() > 125 || cxt.colHeads().isEmpty() || cxt.rowHeads().isEmpty())) {
      System.out.println("skipped");
      return;
    }
//    final int rows = cxt.rowHeads().size();
//    if (rows > 100) {
//      final HashSet<String> truncatedRows = new HashSet<String>(cxt.rowHeads().subList(100, rows));
//      cxt.rowHeads().removeAll(truncatedRows);
//    }

    final double startTime1 = System.currentTimeMillis();
    Map<Set<String>, Set<String>> result1 = null;
    double runTime1;
    try {
//      result1 = BruteForce.pseudoIntents(cxt);
      runTime1 = System.currentTimeMillis() - startTime1;
    } catch (Exception e) {
      runTime1 = -1;
    }

    final double startTime2 = System.currentTimeMillis();
    NextClosures.Result<String, String> result2 = null;
    double runTime2;
    try {
//      result2 = NextClosures.compute(cxt);
      runTime2 = System.currentTimeMillis() - startTime2;
    } catch (Exception e) {
      runTime2 = -1;
    }

    final double startTime3 = System.currentTimeMillis();
    NextClosures6C.Result<String, String> result3 = null;
    double runTime3;
    try {
//      result3 = NextClosures6C.compute(cxt);
      runTime3 = System.currentTimeMillis() - startTime3;
    } catch (Exception e) {
      runTime3 = -1;
    }

    final double startTime4 = System.currentTimeMillis();
    NextClosures6.Result<String, String> result4 = null;
    double runTime4;
    try {
      result4 = NextClosures6.compute(cxt, false);
      runTime4 = System.currentTimeMillis() - startTime4;
    } catch (Exception e) {
      runTime4 = -1;
    }

    final double startTime5 = System.currentTimeMillis();
    NextClosure2.Result<String, String> result5 = null;
    double runTime5;
    try {
      result5 = NextClosure2.compute(cxt);
      runTime5 = System.currentTimeMillis() - startTime5;
    } catch (Exception e) {
      runTime5 = -1;
    }

    final double startTime6 = System.currentTimeMillis();
    NextClosures4.Result<String, String> result6 = null;
    double runTime6;
    try {
//      result6 = NextClosures4.compute(cxt);
      runTime6 = System.currentTimeMillis() - startTime6;
    } catch (Exception e) {
      e.printStackTrace();
      runTime6 = -1;
    }

    result1 = result4.implications;

    boolean equal2 = result1 != null && result2 != null && result2.implications.size() == result1.size();
    if (equal2)
      for (Entry<Set<String>, Set<String>> implication : result1.entrySet()) {
        equal2 &= result2.implications.get(new HashSet<String>(implication.getKey())).equals(implication.getValue());
        if (!equal2)
          break;
      }

    boolean equal3 = result1 != null && result3 != null && result3.implications.size() == result1.size();
    if (equal3)
      for (Entry<Set<String>, Set<String>> implication : result1.entrySet()) {
        equal3 &= result3.implications.get(new HashSet<String>(implication.getKey())).equals(implication.getValue());
        if (!equal3)
          break;
      }

    boolean equal4 = result1 != null && result4 != null && result4.implications.size() == result1.size();
    if (equal4)
      for (Entry<Set<String>, Set<String>> implication : result1.entrySet()) {
        equal4 &= result4.implications.get(new HashSet<String>(implication.getKey())).equals(implication.getValue());
        if (!equal4)
          break;
      }

    boolean equal5 = result1 != null && result5 != null && result5.implications.size() == result1.size();
    if (equal5)
      for (Entry<Set<String>, Set<String>> implication : result1.entrySet()) {
        equal5 &= result5.implications.get(new HashSet<String>(implication.getKey())).equals(implication.getValue());
        if (!equal5)
          break;
      }

    boolean equal6 = result1 != null && result6 != null && result6.implications.size() == result1.size();
    if (equal6)
      for (Entry<Set<String>, Set<String>> implication : result1.entrySet()) {
        equal6 &= result6.implications.get(new HashSet<String>(implication.getKey())).equals(implication.getValue());
        if (!equal6)
          break;
      }

    System.out.print("equal to brute-force result: " + equal2 + " / " + equal3 + " / " + equal4 + " / " + equal5
        + " / " + equal6);
    System.out.print("     -- runtimes: " + runTime1 + "ms / " + runTime2 + "ms / " + runTime3 + "ms / " + runTime4
        + "ms / " + runTime5 + "ms / " + runTime6 + "ms");
    System.out.println(" -- " + cxt.rowHeads().size() + "x" + cxt.colHeads().size());
  }

}
