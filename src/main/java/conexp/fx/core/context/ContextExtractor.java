package conexp.fx.core.context;

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

import org.ujmp.core.booleanmatrix.BooleanMatrix;
import org.ujmp.core.calculation.Calculation.Ret;

import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.exporter.CXTExporter;
import conexp.fx.core.importer.CXTImporter2;

public class ContextExtractor {
  
  public static final void extractFast(final File input, final File output, final int steps){
    final MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    CXTImporter2.read(cxt, input);
    final SetList<String> domain = cxt.rowHeads();
    final SetList<String> codomain=cxt.colHeads();
    final BooleanMatrix matrix = cxt.matrix();
    final int objs =domain.size();
    final int atts = codomain.size();
    for (int att = steps; att < atts; att = att + steps) {
      final BooleanMatrix submatrix=matrix.subMatrix(Ret.LINK, 0, 0, objs-1, att-1).toBooleanMatrix();
      CXTExporter.export(new MatrixContext<String,String>(domain,codomain.subList(0, att),submatrix,false), new File(output.getAbsolutePath().replace(".cxt", "_" + att + ".cxt")));
    }
  }

  public static final void extractAttributeSubcontextFamily(final File input, final File output, final int steps) {
    final MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    CXTImporter2.read(cxt, input);
    final int atts = cxt.colHeads().size();
    for (int att = steps; att < atts; att = att + steps) {
      final MatrixContext<String, String> subcxt =
          cxt.subRelation(cxt.rowHeads(), cxt.colHeads().subList(0, att)).clone();
      System.out.println(subcxt.rowHeads().size());
      System.out.println(subcxt.colHeads().size());
//      CXTExporter.export(subcxt, new File(output.getAbsolutePath().replace(".cxt", "_" + att + ".cxt")));
    }
  }

  public static final void extractSubcontext(final File input, final File output, final int objs, final int atts) {
    final MatrixContext<String, String> context = new MatrixContext<String, String>(false);
    CXTImporter2.read(context, input);
    final MatrixContext<String, String> subcxt =
        context.subRelation(context.rowHeads().subList(0, objs), context.colHeads().subList(0, atts)).clone();
    CXTExporter.export(subcxt, output);
  }

}
