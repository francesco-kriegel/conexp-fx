package conexp.fx.experiment.cellautomata;

import java.io.File;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;

import conexp.fx.core.algorithm.nextclosure.NextClosuresMN;
import conexp.fx.core.algorithm.nextclosure.NextClosuresMN2;
import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.context.temporal.LTL;
import conexp.fx.core.context.temporal.LTL.Type;
import conexp.fx.core.context.temporal.TemporalContext;
import conexp.fx.core.exporter.CXTExporter;
import conexp.fx.core.importer.CASimImporter;

public class CellAuto {

  public static void main(String[] args) {
    final TemporalContext<String, String> tcxt = CASimImporter.read(new File("GameOfLife.C1.CASim.txt"));
    final Set<LTL<String>> ltlAttributes = new HashSet<LTL<String>>();
    ltlAttributes.addAll(tcxt.getLTLAttributes(LTL.Type.NOW));
    ltlAttributes.add(new LTL<String>(LTL.Type.NEXTW, "N0"));
    final MatrixContext<Pair<String, Integer>, LTL<String>> cxt = tcxt.temporalScaling(ltlAttributes);
//    final List<LTL<String>> atts = new ArrayList<LTL<String>>(cxt.colHeads());
//    for (LTL<String> att : atts) {
//      LTL<String> natt = new LTL<String>(att.getType(), "-" + att.getM());
//      cxt.colHeads().add(natt);
//    }
//    cxt.setMatrix(cxt
//        .matrix()
//        .selectColumns(Ret.NEW, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
//        .appendHorizontally(cxt.matrix().selectColumns(Ret.NEW, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9).not(Ret.NEW))
//        .toBooleanMatrix());
//    System.out.println(cxt);
//    CXTExporter.export(cxt, new File("GameOfLife.C1.CASim.cxt"));

    final MatrixContext<Pair<String, Integer>, LTL<String>> cxt2 =
        new MatrixContext<Pair<String, Integer>, LTL<String>>(false);
    cxt2.rowHeads().addAll(cxt.rowHeads());
    cxt2.colHeads().add(new LTL<String>(Type.NOW, "Alive"));
    cxt2.colHeads().add(new LTL<String>(Type.NEXTW, "Alive"));
    cxt2.colHeads().add(new LTL<String>(Type.NOW, "Dead"));
    cxt2.colHeads().add(new LTL<String>(Type.NEXTW, "Dead"));
    final Set<LTL<String>> s = new HashSet<LTL<String>>();
    s.addAll(cxt.colHeads());
    s.remove(new LTL<String>(Type.NOW, "N0"));
    s.remove(new LTL<String>(Type.NEXTW, "N0"));
    for (int i = 0; i <= 8; i++)
      cxt2.colHeads().add(new LTL<String>(Type.NOW, "AN" + i));
    for (Pair<String, Integer> p : cxt2.rowHeads()) {
      final Set<LTL<String>> row = cxt.row(p);
      if (row.contains(new LTL<String>(Type.NOW, "N0")))
        cxt2.add(p, new LTL<String>(Type.NOW, "Alive"));
      else
        cxt2.add(p, new LTL<String>(Type.NOW, "Dead"));
      if (row.contains(new LTL<String>(Type.NEXTW, "N0")))
        cxt2.add(p, new LTL<String>(Type.NEXTW, "Alive"));
      else
        cxt2.add(p, new LTL<String>(Type.NEXTW, "Dead"));
      final int an = Sets.intersection(row, s).size();
//      for (int a = 0; a <= an; a++)
      cxt2.add(p, new LTL<String>(Type.NOW, "AN" + an));
    }
    System.out.println(cxt2);
    CXTExporter.export(cxt2, new File("GameOfLife.C1.CASim.modified.cxt"));

//    final Set<LTL<String>> premises = new HashSet<LTL<String>>(cxt.colHeads());
//    premises.remove(new LTL<String>(Type.NEXT, "N0"));
//    premises.remove(new LTL<String>(Type.NEXT, "-N0"));
//    final Set<LTL<String>> conclusions = new HashSet<LTL<String>>();
//    conclusions.add(new LTL<String>(Type.NEXT, "N0"));
//    conclusions.add(new LTL<String>(Type.NEXT, "-N0"));
//    final NextClosuresMN2.Result<Pair<String, Integer>, LTL<String>> resultMN2 =
//        NextClosuresMN2.<Pair<String, Integer>, LTL<String>> compute(cxt, premises, conclusions);
//    for (Entry<Set<LTL<String>>, Set<LTL<String>>> e : resultMN2.getImplications().entrySet())
//      if (!cxt.colAnd(e.getKey()).isEmpty())
//        System.out.println(e.getKey() + " ==> " + e.getValue());

    final Set<LTL<String>> premises = new HashSet<LTL<String>>(cxt2.colHeads());
    premises.remove(new LTL<String>(Type.NEXTW, "Dead"));
    premises.remove(new LTL<String>(Type.NEXTW, "Alive"));
    final Set<LTL<String>> conclusions = new HashSet<LTL<String>>();
    conclusions.add(new LTL<String>(Type.NEXTW, "Dead"));
    conclusions.add(new LTL<String>(Type.NEXTW, "Alive"));
    final NextClosuresMN.Result<Pair<String, Integer>, LTL<String>> resultMN =
        NextClosuresMN.<Pair<String, Integer>, LTL<String>> compute(cxt2, premises, conclusions);
    for (Entry<Set<LTL<String>>, Set<LTL<String>>> e : resultMN.getImplications().entrySet())
      if (!cxt2.colAnd(e.getKey()).isEmpty())
        System.out.println(e.getKey() + " ==> " + e.getValue());

//    final Set<LTL<String>> premises = new HashSet<LTL<String>>(cxt2.colHeads());
//    premises.remove(new LTL<String>(Type.NEXT, "Dead"));
//    premises.remove(new LTL<String>(Type.NEXT, "Alive"));
//    final Set<LTL<String>> conclusions = new HashSet<LTL<String>>();
//    conclusions.add(new LTL<String>(Type.NEXT, "Dead"));
//    conclusions.add(new LTL<String>(Type.NEXT, "Alive"));
    final NextClosuresMN2.Result<Pair<String, Integer>, LTL<String>> resultMN2 =
        NextClosuresMN2.<Pair<String, Integer>, LTL<String>> compute(cxt2, premises, conclusions);
    for (Entry<Set<LTL<String>>, Set<LTL<String>>> e : resultMN2.getImplications().entrySet())
      if (!cxt2.colAnd(e.getKey()).isEmpty())
        System.out.println(e.getKey() + " ==> " + e.getValue());
  }
}
