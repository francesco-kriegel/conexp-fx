package conexp.fx.core.context;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2020 Francesco Kriegel
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

import java.util.Random;
import java.util.Set;

import org.ujmp.core.booleanmatrix.BooleanMatrix;
import org.ujmp.core.calculation.Calculation.Ret;

import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.collections.setlist.SetLists;
import conexp.fx.core.exporter.CXTExporter;
import conexp.fx.core.importer.CXTImporter;

public class Contexts {

  public static final MatrixContext<Integer, Integer>
      randomWithDensity(final int objects, final int attributes, final int density) {
    final MatrixContext<Integer, Integer> cxt =
        new MatrixContext<>(SetLists.integers(objects), SetLists.integers(attributes), false);
    final Random rng = new Random();
    final int target = (int) (((double) (objects * attributes * density)) / 100d);
    int current = 0;
    while (current < target) {
      final int g = rng.nextInt(objects), m = rng.nextInt(attributes);
      if (!cxt.matrix().getBoolean(g, m)) {
        cxt.matrix().setBoolean(true, g, m);
        current++;
      }
    }
    return cxt;
  }

  public static final MatrixContext<Integer, Integer>
      randomWithProbability(final int objects, final int attributes, final int probability) {
    final MatrixContext<Integer, Integer> cxt =
        new MatrixContext<>(SetLists.integers(objects), SetLists.integers(attributes), false);
    final Random rng = new Random();
    for (int g = 0; g < objects; g++)
      for (int m = 0; m < attributes; m++)
        if (rng.nextDouble() <= probability)
          cxt.addFast(g, m);
    return cxt;
  }

  /**
   * 
   * computes all pseudo-intents of a {@link MatrixContext}. This method may be slow, it is only intended for testing
   * purposes.
   * 
   * Definition: For a given context {@code (G,M,I)} a {@literal pseudo-intent} is a subset {@code B} of the codomain
   * {@code M}, such that it is no intent (i.e. {@code B≠B''}) and furthermore each strictly smaller pseudo-intent
   * {@code D⊊B} has its closure in {@code B} (i.e. {@code D''⊆B}).
   * 
   * @param cxt
   *          {@link MatrixContext}
   * @return a {@link Map} whose keys are all pseudo-intents and their intents as values
   */
  public final static <G, M> Map<Set<M>, Set<M>> pseudoIntents(final MatrixContext<G, M> cxt) {
    final Map<Set<M>, Set<M>> pseudoIntents = new HashMap<Set<M>, Set<M>>();
    // computes all pseudo-intents from smallest to biggest
    for (Set<M> attributes : SetLists.powerSet(cxt.colHeads())) {
      Set<M> intent = cxt.intent(attributes);
      // checks whether attributes is already an intent
      if (attributes.containsAll(intent))
        continue;
      // checks if all strictly smaller pseudo-intents have their closure in attributes
      boolean isPseudoIntent = true;
      for (Entry<Set<M>, Set<M>> pseudoIntent : pseudoIntents.entrySet())
        if (attributes.size() > pseudoIntent.getKey().size() && attributes.containsAll(pseudoIntent.getKey())
            && !attributes.containsAll(pseudoIntent.getValue())) {
          // no pseudo-intent
          isPseudoIntent = false;
          break;
        }
      if (isPseudoIntent) {
        intent = new HashSet<M>(intent);
        intent.removeAll(attributes);
        pseudoIntents.put(attributes, intent);
      }
    }
    return pseudoIntents;
  }

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
