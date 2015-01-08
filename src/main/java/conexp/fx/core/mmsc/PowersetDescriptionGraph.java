package conexp.fx.core.mmsc;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.context.MatrixContext;

public class PowersetDescriptionGraph {

  protected final Multimap<Set<OWLNamedIndividual>, OWLClassExpression>                                         nodes;
  protected final Multimap<Set<OWLNamedIndividual>, Pair<Set<OWLNamedIndividual>, OWLObjectPropertyExpression>> edges;
  private final OWLDataFactory                                                                                  df;

  public PowersetDescriptionGraph() {
    super();
    this.nodes = HashMultimap.<Set<OWLNamedIndividual>, OWLClassExpression> create();
    this.edges =
        HashMultimap.<Set<OWLNamedIndividual>, Pair<Set<OWLNamedIndividual>, OWLObjectPropertyExpression>> create();
    this.df = OWLManager.getOWLDataFactory();
  }

  public final OWLClassExpression getMMSC(final Set<OWLNamedIndividual> objects, final int roleDepth) {
    final Set<OWLClassExpression> conjuncts = new HashSet<OWLClassExpression>();
    conjuncts.addAll(nodes.get(objects));
    for (Pair<Set<OWLNamedIndividual>, OWLObjectPropertyExpression> p : edges.get(objects))
      conjuncts.add(df.getOWLObjectSomeValuesFrom(p.y(), getMMSC(p.x(), roleDepth - 1)));
    return new OWLObjectIntersectionOfImpl(conjuncts);
  }

  public final MatrixContext<URI, OWLClass> toLogicalContext(final int roleDepth) {
    MatrixContext<URI, OWLClass> cxt = new MatrixContext<URI, OWLClass>(false);

    return cxt;
  }

}
