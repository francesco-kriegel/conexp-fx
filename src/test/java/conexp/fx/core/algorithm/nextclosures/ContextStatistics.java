package conexp.fx.core.algorithm.nextclosures;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.io.File;
import java.util.Map;

import com.google.common.collect.Maps;

import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter;

public class ContextStatistics {

  public static final Map<File, Integer> objects    = Maps.newHashMap();
  public static final Map<File, Integer> attributes = Maps.newHashMap();
  public static final Map<File, Integer> density    = Maps.newHashMap();

  static int                             d          = 0;
  static int                             n          = 0;

  public static final void main(String[] args) {
//    processDir(new File("/Users/francesco/Data/Contexts"));
//    processDir(new File("/Users/francesco/Data/Contexts/small"));
//    processDir(new File("/Users/francesco/Data/Contexts/scales"));
//    processDir(new File("/Users/francesco/Data/Contexts/big"));
    processDir(new File("/Users/francesco/Data/Contexts/random"));
//    processDir(new File("/Users/francesco/Data/Contexts/testing-data"));
    System.out.println("average density: " + (d / n));
  }

  public static final void processDir(final File dir) {
    int num = 0;
    for (File file : dir.listFiles())
      if (file.isFile() && file.getName().endsWith(
          ".cxt")) {
        processFile(file);
        num++;
      }
    System.out.println(dir + " contains " + num + " contexts.");
    System.out.println();
    System.out.println();
  }

  public static final void processFile(final File file) {
    final MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    CXTImporter.read(
        cxt,
        file);
    final int rows = cxt.rowHeads().size();
    final int cols = cxt.colHeads().size();
    final int dens;
    if (rows * cols == 0)
      dens = 0;
    else
      dens = (100 * cxt.size()) / (rows * cols);
    objects.put(
        file,
        rows);
    attributes.put(
        file,
        cols);
    density.put(
        file,
        dens);
    d += dens;
    n++;
    System.out.println(file + " " + rows + " rows, " + cols + " cols, " + dens + "% density");
  }

}
