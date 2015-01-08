package conexp.fx.core.mmsc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import conexp.fx.core.context.MatrixContext;

/**
 * @author francesco
 *
 */
public class DescriptionGraph {

  protected final MatrixContext<OWLNamedIndividual, OWLClass>                                   nodes;
  protected final Map<OWLObjectProperty, MatrixContext<OWLNamedIndividual, OWLNamedIndividual>> edges;
  protected final OWLDataFactory                                                                df;

  public DescriptionGraph() {
    super();
    this.nodes = new MatrixContext<OWLNamedIndividual, OWLClass>(false);
    this.edges = new HashMap<OWLObjectProperty, MatrixContext<OWLNamedIndividual, OWLNamedIndividual>>();
    this.df = OWLManager.getOWLDataFactory();
  }

  public final OWLClassExpression getMMSC(final OWLNamedIndividual object, final int roleDepth) {
    final Set<OWLClassExpression> conjuncts = new HashSet<OWLClassExpression>();
    conjuncts.addAll(nodes.row(object));
    if (roleDepth > 0)
      for (Entry<OWLObjectProperty, MatrixContext<OWLNamedIndividual, OWLNamedIndividual>> e : edges.entrySet())
        for (OWLNamedIndividual p : e.getValue().row(object))
          conjuncts.add(df.getOWLObjectSomeValuesFrom(e.getKey(), getMMSC(p, roleDepth - 1)));
    return new OWLObjectIntersectionOfImpl(conjuncts);
  }

  public final OWLClassExpression getMMSC(final Set<OWLNamedIndividual> objects, final int roleDepth) {
    final Set<OWLClassExpression> conjuncts = new HashSet<OWLClassExpression>();
    conjuncts.addAll(nodes.rowAnd(objects));
    if (roleDepth > 0)
      for (Entry<OWLObjectProperty, MatrixContext<OWLNamedIndividual, OWLNamedIndividual>> e : edges.entrySet())
        for (Set<OWLNamedIndividual> p : createAllMinSets(objects, e.getValue()))
          conjuncts.add(df.getOWLObjectSomeValuesFrom(e.getKey(), getMMSC(p, roleDepth - 1)));
    if (conjuncts.size() > 1)
      return new OWLObjectIntersectionOfImpl(conjuncts);
    if (conjuncts.size() == 1)
      return conjuncts.iterator().next();
    return df.getOWLThing();
  }

  /**
   * @param objects
   * @param cxt
   * @return
   * 
   *         This method creates all minSets. For a set of objects and a role matrix, it computes all sets of
   *         owlindividuals, such that they contain exactly one role successor for each object in objects.
   * 
   */
  private HashSet<Set<OWLNamedIndividual>> createAllMinSets(
      Set<OWLNamedIndividual> objects,
      MatrixContext<OWLNamedIndividual, OWLNamedIndividual> cxt) {
//    final HashSet<Set<OWLNamedIndividual>> minSets = new HashSet<Set<OWLNamedIndividual>>();
//    for (OWLNamedIndividual o : objects) {
//      for (OWLNamedIndividual i : cxt.row(o)) {
//        final HashSet<OWLNamedIndividual> minSet = new HashSet<OWLNamedIndividual>();
//        minSet.add(i);
//      }
//    }
//    cxt.rowAnd(objects);
//    return minSets;

    final HashSet<Set<OWLNamedIndividual>> minSets =
        new HashSet<Set<OWLNamedIndividual>>(Sets.filter(
            Sets.powerSet(nodes.rowHeads()),
            new Predicate<Set<OWLNamedIndividual>>() {

              @Override
              public final boolean apply(final Set<OWLNamedIndividual> input) {
                for (OWLNamedIndividual o : objects)
                  if (Sets.intersection(input, cxt.row(o)).isEmpty())
                    return false;
                return true;
              }
            }));
    final HashSet<Set<OWLNamedIndividual>> t = new HashSet<Set<OWLNamedIndividual>>();
    for (Set<OWLNamedIndividual> x : minSets)
      for (Set<OWLNamedIndividual> y : minSets)
        if (!x.equals(y) && x.containsAll(y))
          t.add(x);
    minSets.removeAll(t);
    return minSets;
  }

  private boolean instance(OWLNamedIndividual i, OWLClassExpression c) {
    if (c.equals(df.getOWLThing()))
      return true;
    if (c.equals(df.getOWLNothing()))
      return false;
    if (c instanceof OWLClass)
      return nodes.contains(i, (OWLClass) c);
    if (c instanceof OWLObjectSomeValuesFrom) {
      for (OWLNamedIndividual j : edges.get((OWLObjectProperty) ((OWLObjectSomeValuesFrom) c).getProperty()).row(i))
        if (instance(j, ((OWLObjectSomeValuesFrom) c).getFiller()))
          return true;
      return false;
    }
    for (OWLClassExpression x : ((OWLObjectIntersectionOf) c).asConjunctSet())
      if (x instanceof OWLClass)
        if (!nodes.contains(i, (OWLClass) x))
          return false;
    for (OWLClassExpression x : ((OWLObjectIntersectionOf) c).asConjunctSet())
      if (x instanceof OWLObjectSomeValuesFrom) {
        boolean f = false;
        for (OWLNamedIndividual j : edges.get((OWLObjectProperty) ((OWLObjectSomeValuesFrom) x).getProperty()).row(i)) {
          if (instance(j, ((OWLObjectSomeValuesFrom) x).getFiller())) {
            f = true;
            break;
          }
        }
        if (!f)
          return false;
      }
    return true;
  }

  public final MatrixContext<OWLNamedIndividual, OWLClassExpression> toLogicalContext(final int roleDepth) {
    MatrixContext<OWLNamedIndividual, OWLClassExpression> cxt =
        new MatrixContext<OWLNamedIndividual, OWLClassExpression>(false);
    cxt.rowHeads().addAll(nodes.rowHeads());
    cxt.colHeads().addAll(createMMSCConjuncts(roleDepth));
    for (OWLNamedIndividual i : cxt.rowHeads())
      for (OWLClassExpression c : cxt.colHeads())
        if (instance(i, c))
          cxt.addFast(i, c);
    return cxt;
  }

  private List<OWLClassExpression> createMMSCConjuncts(int roleDepth) {
    final List<OWLClassExpression> mmscConjuncts = new LinkedList<OWLClassExpression>();
    mmscConjuncts.add(df.getOWLNothing());
    mmscConjuncts.addAll(nodes.colHeads());
    if (roleDepth > 0) {
      for (Set<OWLNamedIndividual> s : Sets.powerSet(nodes.rowHeads()))
        if (!s.isEmpty()) {
          final OWLClassExpression mmsc = getMMSC(s, roleDepth - 1);
          for (OWLObjectProperty role : edges.keySet()) {
            mmscConjuncts.add(df.getOWLObjectSomeValuesFrom(role, mmsc));
          }
        }
    }
    return mmscConjuncts;
  }

//  public final PowersetDescriptionGraph toPowersetDescriptionGraph() {
//    PowersetDescriptionGraph graph = new PowersetDescriptionGraph();
//    for (Set<OWLNamedIndividual> objects : Sets.powerSet(nodes.rowHeads())) {
//      final HashSet<OWLNamedIndividual> key = new HashSet<OWLNamedIndividual>(objects);
//      graph.nodes.putAll(key, new HashSet<OWLClassExpression>(nodes.rowAnd(key)));
//      for (Entry<OWLObjectProperty, MatrixContext<OWLNamedIndividual, OWLNamedIndividual>> e : edges
//          .entrySet()) {
//        graph.edges.putAll(e.getKey(), e.getValue().rowAnd(key));
//      }
//    }
//    return graph;
//  }
}
