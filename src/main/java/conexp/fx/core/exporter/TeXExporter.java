/*
 * @author Francesco.Kriegel@gmx.de
 */
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
import java.util.Map;

import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.layout.ConceptLayout;

public class TeXExporter<G, M> {

  public enum ScaleEnum {
    Fit,
    FitWidth,
    FitHeight,
    FitRatio;

    public ScaleOption toOption(final int w, final int h) {
      switch (this) {
      case Fit:
        return new FitScale(w, h);
      case FitWidth:
        return new FitWidthScale(w);
      case FitHeight:
        return new FitHeightScale(h);
      case FitRatio:
        return new FitRatioScale(w, h);
      }
      return null;
    }
  }

  public static abstract class ScaleOption {

    protected ScaleEnum scale;
    protected double    widthInMillimeter, heightInMillimeter;

    public abstract double widthFactor(double dWidth, double dHeight);

    public abstract double heightFactor(double dWidth, double dHeight);
  }

  public static final class FitScale extends ScaleOption {

    public FitScale(final int widthInMillimeter, final int heightInMillimeter) {
      super();
      this.scale = ScaleEnum.Fit;
      this.widthInMillimeter = widthInMillimeter;
      this.heightInMillimeter = heightInMillimeter;
    }

    @Override
    public final double widthFactor(final double dWidth, final double dHeight) {
      return widthInMillimeter / dWidth;
    }

    @Override
    public final double heightFactor(final double dWidth, final double dHeight) {
      return heightInMillimeter / dHeight;
    }
  }

  public static final class FitRatioScale extends ScaleOption {

    public FitRatioScale(final int widthInMillimeter, final int heightInMillimeter) {
      super();
      this.scale = ScaleEnum.FitRatio;
      this.widthInMillimeter = widthInMillimeter;
      this.heightInMillimeter = heightInMillimeter;
    }

    @Override
    public final double widthFactor(final double dWidth, final double dHeight) {
      final double w = widthInMillimeter / dWidth;
      final double h = heightInMillimeter / dHeight;
      return Math.min(w, h);
    }

    @Override
    public final double heightFactor(final double dWidth, final double dHeight) {
      return widthFactor(dWidth, dHeight);
    }
  }

  public static final class FitHeightScale extends ScaleOption {

    public FitHeightScale(final int heightInMillimeter) {
      super();
      this.scale = ScaleEnum.FitHeight;
      this.heightInMillimeter = heightInMillimeter;
    }

    @Override
    public final double widthFactor(final double dWidth, final double dHeight) {
      return heightFactor(dWidth, dHeight);
    }

    @Override
    public final double heightFactor(final double dWidth, final double dHeight) {
      return heightInMillimeter / dHeight;
    }
  }

  public static final class FitWidthScale extends ScaleOption {

    public FitWidthScale(final int widthInMillimeter) {
      super();
      this.scale = ScaleEnum.FitWidth;
      this.widthInMillimeter = widthInMillimeter;
    }

    @Override
    public final double widthFactor(final double dWidth, final double dHeight) {
      return widthInMillimeter / dWidth;
    }

    @Override
    public final double heightFactor(final double dWidth, final double dHeight) {
      return widthFactor(dWidth, dHeight);
    }
  }

  public static final class TeXOptions {

    public File              file              = null;
    public boolean           arrows            = false;
    public boolean           labels            = true;
    public boolean           standAlone        = false;
    public ContextTeXPackage contextTeXPackage = ContextTeXPackage.Tabular;
    public DiagramTeXPackage diagramTeXPackage = DiagramTeXPackage.ConExpFX;
    public ScaleOption       scale             = null;

    public TeXOptions(
        File file,
        boolean arrows,
        boolean labels,
        boolean standAlone,
        ContextTeXPackage contextTeXPackage,
        DiagramTeXPackage diagramTeXPackage,
        ScaleOption scale) {
      super();
      this.file = file;
      this.arrows = arrows;
      this.labels = labels;
      this.standAlone = standAlone;
      this.contextTeXPackage = contextTeXPackage;
      this.diagramTeXPackage = diagramTeXPackage;
      this.scale = scale;
    }
  }

  public enum ContextTeXPackage {
    None,
    Ganter,
    Tabular
  }

  public enum DiagramTeXPackage {
    None,
    Ganter,
    ConExpFX
  }

  private final MatrixContext<G, M>   formalContext;
  private final Map<Integer, Integer> objectPermutation;
  private final Map<Integer, Integer> attributePermutation;
  private final ConceptLayout<G, M>   conceptLayout;
  private final TeXOptions            teXOptions;
  private final StringBuffer          buffer = new StringBuffer();

  public TeXExporter(
      final MatrixContext<G, M> formalContext,
      final Map<Integer, Integer> objectPermutation,
      final Map<Integer, Integer> attributePermutation,
      final ConceptLayout<G, M> conceptLayout,
      final TeXOptions teXOptions) {
    this.formalContext = formalContext;
    this.objectPermutation = objectPermutation;
    this.attributePermutation = attributePermutation;
    this.conceptLayout = conceptLayout;
    this.teXOptions = teXOptions;;
  }

  public final void export() throws IOException {
    final BufferedWriter writer = createFile();
    newLine();
    if (teXOptions.standAlone) {
      appendHeader();
      newLine();
      newLine();
    }
    switch (teXOptions.contextTeXPackage) {
    case None:
      break;
    case Ganter:
      appendGanterContext();
      break;
    case Tabular:
      appendTabularContext();
      break;
    }
    newLine();
    newLine();
    switch (teXOptions.diagramTeXPackage) {
    case None:
      break;
    case Ganter:
      appendGanterDiagram();
      break;
    case ConExpFX:
      appendConExpFXDiagram();
      break;
    }
    newLine();
    newLine();
    if (teXOptions.standAlone) {
      appendFooter();
      newLine();
      newLine();
    }
    writer.append(buffer);
    writer.close();
  }

  private final BufferedWriter createFile() throws IOException {
    if (!teXOptions.file.exists()) {
      if (!teXOptions.file.getParentFile().exists())
        teXOptions.file.mkdirs();
      teXOptions.file.createNewFile();
    }
    return new BufferedWriter(new FileWriter(teXOptions.file));
  }

  private final void append(final String string) {
    buffer.append(string);
  }

  private final void newLine() {
    append("\r\n");
  }

  private final void appendHeader() {
    // TODO Auto-generated method stub

  }

  private final void appendFooter() {
    // TODO Auto-generated method stub

  }

  private final void appendGanterContext() {
    append("\\begin{cxt}\r\n\\cxtName{" + teXOptions.file.getName() + "}\r\n");
    final int js = formalContext.colHeads().size();
    final int is = formalContext.rowHeads().size();
    for (int j = 0; j < js; j++) {
      final int jj =
          attributePermutation == null || !attributePermutation.containsKey(j) ? j : attributePermutation.get(j);
      final M m = formalContext.colHeads().get(jj);
      append("\\atr{" + (teXOptions.labels ? m : "") + "}\r\n");
    }
    for (int i = 0; i < is; i++) {
      final int ii = objectPermutation == null || !objectPermutation.containsKey(i) ? i : objectPermutation.get(i);
      final G g = formalContext.rowHeads().get(ii);
      String incidences = "";
      for (int j = 0; j < js; j++) {
        final int jj =
            attributePermutation == null || !attributePermutation.containsKey(j) ? j : attributePermutation.get(j);
        final M m = formalContext.colHeads().get(jj);
        if (formalContext.contains(g, m)) {
          incidences += "X";
        } else if (teXOptions.arrows) {
          final boolean isDownArrow = formalContext.DownArrows.contains(g, m);
          final boolean isUpArrow = formalContext.UpArrows.contains(g, m);
          if (isDownArrow) {
            if (isUpArrow) {
              incidences += "b";
            } else {
              incidences += "d";
            }
          } else {
            if (isUpArrow) {
              incidences += "u";
            } else {
              incidences += ".";
            }
          }
        } else {
          incidences += ".";
        }
      }
      append("\\obj{" + incidences + "}{" + (teXOptions.labels ? g : "") + "}\r\n");
    }
    append("\\end{cxt}\r\n");
  }

  private final void appendTabularContext() {
    final int is = formalContext.rowHeads().size();
    final int js = formalContext.colHeads().size();
    append("{\\setlength{\\tabcolsep}{1pt}");
    newLine();
    if (js == 0)
      append("\\begin{tabular}{r@{\\hspace{3pt}}|}");
    else if (js == 1)
      append("\\begin{tabular}{r@{\\hspace{3pt}}| c|}");
    else
      append("\\begin{tabular}{r@{\\hspace{3pt}}| *{" + (js - 1) + "}{c} c|}");
    newLine();
    append("\\multicolumn{1}{c}{}");
    newLine();
    for (int j = 0; j < js; j++) {
      final int jj =
          attributePermutation == null || !attributePermutation.containsKey(j) ? j : attributePermutation.get(j);
      final M m = formalContext.colHeads().get(jj);
      append("&\\multicolumn{1}{c}{\\begin{turn}{90}" + (teXOptions.labels ? m : "") + "\\end{turn}}");
      newLine();
    }
    append("\\\\ \\cline{2-" + (js + 1) + "}");
    newLine();
    for (int i = 0; i < is; i++) {
      final int ii = objectPermutation == null || !objectPermutation.containsKey(i) ? i : objectPermutation.get(i);
      final G g = formalContext.rowHeads().get(ii);
      append((teXOptions.labels ? g : "") + "");
      for (int j = 0; j < js; j++) {
        final int jj =
            attributePermutation == null || !attributePermutation.containsKey(j) ? j : attributePermutation.get(j);
        final M m = formalContext.colHeads().get(jj);
        if (formalContext.contains(g, m)) {
          append("& $\\times$ ");
        } else if (teXOptions.arrows) {
          final boolean isDownArrow = formalContext.DownArrows.contains(g, m);
          final boolean isUpArrow = formalContext.UpArrows.contains(g, m);
          if (isDownArrow) {
            if (isUpArrow) {
              append("& $\\Doppelpfeil$ ");
            } else {
              append("& $\\Runterpfeil$ ");
            }
          } else {
            if (isUpArrow) {
              append("& $\\Hochpfeil$ ");
            } else {
              append("& $\\cdot$ ");
            }
          }
        } else {
          append("& $\\cdot$ ");
        }
      }
      if (i == is - 1)
        append("\\\\ \\cline{2-" + (js + 1) + "}");
      else
        append("\\\\");
      newLine();
    }
    append("\\end{tabular}}");
    newLine();
  }

  private final void appendGanterDiagram() {
    final double width = conceptLayout.getCurrentBoundingBox().getWidth();
    final double minX = conceptLayout.getCurrentBoundingBox().getMinX();
    final double height = conceptLayout.getCurrentBoundingBox().getHeight();
    final double w = teXOptions.scale.widthFactor(width, height);
    final double h = teXOptions.scale.heightFactor(width, height);
    final double unit =
        (teXOptions.scale.scale == ScaleEnum.FitHeight ? h : (teXOptions.scale.scale == ScaleEnum.FitWidth ? w : Math
            .min(w, h)));
    append("\\begin{diagram}{" + width + "}{" + height + "}\r\n");
    append("\\unitlength " + unit + "mm\r\n");
    append("\\CircleSize{1}\r\n");
    append("\\NodeThickness{1pt}\r\n");
    append("\\EdgeThickness{1pt}\r\n");
    for (int i = 0; i < conceptLayout.lattice.rowHeads().size(); i++) {
      final Concept<G, M> concept = conceptLayout.lattice.rowHeads().get(i);
      final double x = conceptLayout.positions.get(concept).getX();
      final double y = conceptLayout.positions.get(concept).getY();
      append("\\Node{" + i + "}{" + (x - minX) + "}{" + (height - y) + "}\r\n");
    }
    for (int i = 0; i < conceptLayout.lattice.rowHeads().size(); i++) {
      for (int j = 0; j < conceptLayout.lattice.rowHeads().size(); j++) {
        if (conceptLayout.lattice._contains(i, j))
          append("\\Edge{" + i + "}{" + j + "}\r\n");
      }
    }
    if (teXOptions.labels)
      for (int i = 0; i < conceptLayout.lattice.rowHeads().size(); i++) {
        final Concept<G, M> concept = conceptLayout.lattice.rowHeads().get(i);
        final String objLabels =
            conceptLayout.lattice
                .objectLabels(concept)
                .toString()
                .substring(1, conceptLayout.lattice.objectLabels(concept).toString().length() - 1)
                .trim();
        final String attLabels =
            conceptLayout.lattice
                .attributeLabels(concept)
                .toString()
                .substring(1, conceptLayout.lattice.attributeLabels(concept).toString().length() - 1)
                .trim();
        if (!objLabels.isEmpty())
          append("\\centerObjbox{" + i + "}{0}{1}{" + objLabels + "}\r\n");
        if (!attLabels.isEmpty())
          append("\\centerAttbox{" + i + "}{0}{1}{" + attLabels + "}\r\n");
      }
    append("\\end{diagram}\r\n");
  }

  private final void appendConExpFXDiagram() {
    final double width = conceptLayout.getCurrentBoundingBox().getWidth();
    final double height = conceptLayout.getCurrentBoundingBox().getHeight();
    final double minX = conceptLayout.getCurrentBoundingBox().getMinX();
    final double w = teXOptions.scale.widthFactor(width, height);
    final double h = teXOptions.scale.heightFactor(width, height);
    append("\\begin{conceptdiagram}[x=" + w + "mm, y=" + h + "mm]\r\n");
    append("\\setlength{\\nodesize}{3mm}\r\n");
    append("\\setlength{\\edgewidth}{0.4pt}\r\n");
    append("\\setlength{\\labeldistance}{1pt}\r\n");
    for (int i = 0; i < conceptLayout.lattice.rowHeads().size(); i++) {
      final Concept<G, M> concept = conceptLayout.lattice.rowHeads().get(i);
      final double x = conceptLayout.positions.get(concept).getX();
      final double y = conceptLayout.positions.get(concept).getY();
      append("\\conceptnode{" + i + "}{" + (x - minX) + "}{" + (height - y) + "}\r\n");
    }
    for (int i = 0; i < conceptLayout.lattice.rowHeads().size(); i++) {
      for (int j = 0; j < conceptLayout.lattice.rowHeads().size(); j++) {
        if (conceptLayout.lattice._contains(i, j))
          append("\\conceptedge{" + i + "}{" + j + "}\r\n");
      }
    }
    if (teXOptions.labels)
      for (int i = 0; i < conceptLayout.lattice.rowHeads().size(); i++) {
        final Concept<G, M> concept = conceptLayout.lattice.rowHeads().get(i);
        final String objLabels =
            conceptLayout.lattice
                .objectLabels(concept)
                .toString()
                .substring(1, conceptLayout.lattice.objectLabels(concept).toString().length() - 1)
                .trim();
        final String attLabels =
            conceptLayout.lattice
                .attributeLabels(concept)
                .toString()
                .substring(1, conceptLayout.lattice.attributeLabels(concept).toString().length() - 1)
                .trim();
        if (!attLabels.isEmpty())
          append("\\attributelabel{" + i + "}{" + attLabels + "}\r\n");
        if (!objLabels.isEmpty())
          append("\\objectlabel{" + i + "}{" + objLabels + "}\r\n");
      }
    append("\\end{conceptdiagram}\r\n");
  }
}
