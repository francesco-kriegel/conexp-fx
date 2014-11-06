package conexp.fx.core.algorithm.pseudointent;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;

import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter2;

public class PseudoIntents {

//  private final static <G, M> ImplicationSet<M> stemBase(final MatrixContext<G, M> cxt) {
//    final ImplicationSet<M> stemBase = new ImplicationSet<M>();
//    for (Entry<Set<M>, Set<M>> pseudoIntent : pseudoIntents(cxt).entrySet())
//      stemBase.add(new Implication<M>(pseudoIntent.getKey(), pseudoIntent.getValue()));
//    return stemBase;
//  }

  public static final void main(String[] args) {
    bar();
  }

  private static void bar() {
    MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    CXTImporter2.read(cxt, new File("/Users/francesco/workspace/3obj3att_N5.cxt"));
    for (Entry<Set<String>, Set<String>> e : BruteForce.pseudoIntents(cxt).entrySet())
      System.out.println(e.getKey() + " ==> " + e.getValue());
  }

  private static void foo() {
    MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    CXTImporter2.read(cxt, new File("/Users/francesco/workspace/o1000a10d10.cxt"));
    cxt.clean();
    final SetList<Set<String>> equivalenceClasses = cxt.cleaned.clone().rowHeads();
    cxt.deselectAllObjects();
//    System.out.println(cxt);
//    printImplications(cxt);
    final List<Set<String>> pi1 = new LinkedList<Set<String>>();
    final List<Set<String>> pi2 = new LinkedList<Set<String>>();
    Map<Set<String>, Set<String>> imp1 = new HashMap<Set<String>, Set<String>>();
    Map<Set<String>, Set<String>> imp2 = new HashMap<Set<String>, Set<String>>();
    final List<Set<String>> piboth = new LinkedList<Set<String>>();
    final List<Set<String>> pinew = new LinkedList<Set<String>>();
    final List<Set<String>> piold = new LinkedList<Set<String>>();
    final List<String> representingElements = new LinkedList<String>();
    for (Set<String> equivalenceClass : equivalenceClasses)
      representingElements.add(equivalenceClass.iterator().next());
//    for (String g : cxt.rowHeads().subList(0, 10)) {
    for (String g : representingElements) {
      pi1.clear();
      pi1.addAll(pi2);
      pi2.clear();
      piboth.clear();
      pinew.clear();
      piold.clear();
      cxt.selectObject(g);
//      final NextImplication2<String, String> ni = new NextImplication2<String, String>(cxt);
//      final Iterator<Implication<String>> it = ni.iterator();
//      while (it.hasNext())
//        try {
//          pi2.add(it.next().getPremise());
//        } catch (NullPointerException e) {}
      // piboth contains intersection of pi1 and pi2
      imp1 = imp2;
      imp2 = BruteForce.pseudoIntents(cxt.selection);
      pi2.addAll(imp2.keySet());
      Collections.sort(pi2, new Comparator<Set<String>>() {

        @Override
        public int compare(Set<String> o1, Set<String> o2) {
          if (o1.equals(o2))
            return 0;
          if (o2.contains(Collections.max(
              Sets.difference(Sets.union(o1, o2), Sets.intersection(o1, o2)),
              new Comparator<String>() {

                @Override
                public int compare(String o1, String o2) {
                  if (Integer.valueOf(o1) < Integer.valueOf(o2))
                    return -1;
                  else if (Integer.valueOf(o1) == Integer.valueOf(o2))
                    return 0;
                  return 1;
                }
              })))
            return -1;
          return 1;
        }
      });
//      System.out.println(pi2.containsAll(pseudoIntents) && pseudoIntents.containsAll(pi2));
      piboth.addAll(pi1);
      piboth.retainAll(pi2);
      // pinew contains pi2 without pi1
      pinew.addAll(pi2);
      pinew.removeAll(pi1);
      // piold contains pi1 without pi2
      piold.addAll(pi1);
      piold.removeAll(pi2);
      if (pi1.contains(cxt.row(g))) {
        // System.out.println("--------------------------------------------------------------------------------");
        // System.out.println("objects: " + cxt.selectedObjects());
        // System.out.println("--------------------------------------------------------------------------------");
        // System.out.println(cxt.selection);
        // System.out.println(cxt.selection.UpArrows);
        // System.out.println(cxt.selection.DownArrows);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("new intent: " + cxt.row(g));
        System.out.println(pi1.contains(cxt.row(g)));
        // System.out.println("--pseudo-intents:---------------------------------------------------------------");
        // for (Set<String> p : pi2)
        // System.out.println(p + " => " + imp2.get(p));
        System.out.println("--difference, only new----------------------------------------------------------");
        for (Set<String> p : pinew)
          System.out.println(p);
        System.out.println("--difference, only old----------------------------------------------------------");
        for (Set<String> p : piold)
          System.out.println(p);
        System.out.println("--intersection, both new and old------------------------------------------------");
        for (Set<String> p : piboth)
          System.out.println(p);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("");
        System.out.println("");
      }
    }
  }

}
