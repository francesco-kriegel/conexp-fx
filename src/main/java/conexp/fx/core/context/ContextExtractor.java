package conexp.fx.core.context;

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

import org.ujmp.core.booleanmatrix.BooleanMatrix;
import org.ujmp.core.calculation.Calculation.Ret;

import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.exporter.CXTExporter;
import conexp.fx.core.importer.CXTImporter;

public class ContextExtractor {

  public static final void extractFast(final File input, final File output, final int steps) throws Exception {
    final MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    CXTImporter.read(cxt, input);
    final SetList<String> domain = cxt.rowHeads();
    final SetList<String> codomain = cxt.colHeads();
    final BooleanMatrix matrix = cxt.matrix();
    final int objs = domain.size();
    final int atts = codomain.size();
    for (int att = steps; att < atts; att = att + steps) {
      final BooleanMatrix submatrix = matrix.subMatrix(Ret.LINK, 0, 0, objs - 1, att - 1).toBooleanMatrix();
      CXTExporter.export(
          new MatrixContext<String, String>(domain, codomain.subList(0, att), submatrix, false),
          new File(output.getAbsolutePath().replace(".cxt", "_" + att + ".cxt")));
    }
  }

  public static final void extractAttributeSubcontextFamily(final File input, final File output, final int steps)
      throws Exception {
    final MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    CXTImporter.read(cxt, input);
    final int atts = cxt.colHeads().size();
    for (int att = steps; att < atts; att = att + steps) {
      final MatrixContext<String, String> subcxt =
          cxt.subRelation(cxt.rowHeads(), cxt.colHeads().subList(0, att)).clone();
      System.out.println(subcxt.rowHeads().size());
      System.out.println(subcxt.colHeads().size());
//      CXTExporter.export(subcxt, new File(output.getAbsolutePath().replace(".cxt", "_" + att + ".cxt")));
    }
  }

  public static final void extractSubcontext(final File input, final File output, final int objs, final int atts)
      throws Exception {
    final MatrixContext<String, String> context = new MatrixContext<String, String>(false);
    CXTImporter.read(context, input);
    final MatrixContext<String, String> subcxt =
        context.subRelation(context.rowHeads().subList(0, objs), context.colHeads().subList(0, atts)).clone();
    CXTExporter.export(subcxt, output);
  }

}
