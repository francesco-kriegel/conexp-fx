package conexp.fx.conelk;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import conexp.fx.core.context.MatrixContext;
import de.tudresden.inf.lat.gel.GelReasoner;

public class LogicalContextGenerator {

  private final OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
  private final OWLOntology        ontology;

  public LogicalContextGenerator(final OWLOntology ontology) {
    super();
    this.ontology = ontology;
  }

  public LogicalContextGenerator(final File ontologyFile) throws OWLOntologyCreationException {
    super();
    this.ontology = ontologyManager.loadOntologyFromOntologyDocument(ontologyFile);
  }

  public final MatrixContext<OWLNamedIndividual, OWLClassExpression> build(final int roleDepth) {
    final OWLReasoner elkReasoner = new ElkReasonerFactory().createReasoner(ontology);
//  elkReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
    final MatrixContext<OWLNamedIndividual, OWLClassExpression> context =
        new MatrixContext<OWLNamedIndividual, OWLClassExpression>(false);
    final Set<OWLNamedIndividual> rowHeads = new HashSet<OWLNamedIndividual>();
    final Set<OWLClassExpression> colHeads = new HashSet<OWLClassExpression>();
    Map<OWLNamedIndividual, OWLClassExpression> mscs = computeMSCs(roleDepth);
    for (OWLNamedIndividual i : mscs.keySet())
      rowHeads.add(i);
    for (Entry<OWLNamedIndividual, OWLClassExpression> e : mscs.entrySet())
      colHeads.add(e.getValue());
    context.rowHeads().addAll(rowHeads);
    context.colHeads().addAll(colHeads);
    for (Entry<OWLNamedIndividual, OWLClassExpression> e : mscs.entrySet()) {
      context.addFastSilent(e.getKey(), e.getValue());
      for (OWLClassExpression c : colHeads)
        if (elkReasoner
            .getInstances(c, false)
            .getFlattened()
            .containsAll(elkReasoner.getInstances(e.getValue(), false).getFlattened()))
//        if (Ontology.reasoner.isEntailed(Ontology.owlDataFactory.getOWLClassAssertionAxiom(c, e.getKey())))
          context.addFastSilent(e.getKey(), c);
    }
    elkReasoner.dispose();
    return context;
  }

  private final Map<OWLNamedIndividual, OWLClassExpression> computeMSCs(final int roleDepth) {
    final GelReasoner gelReasoner = new GelReasoner(ontology);
    final ConcurrentHashMap<OWLNamedIndividual, OWLClassExpression> mscs =
        new ConcurrentHashMap<OWLNamedIndividual, OWLClassExpression>();
    final ThreadPoolExecutor tpe =
        new ThreadPoolExecutor(2, 2, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    final HashSet<Future<?>> futures = new HashSet<Future<?>>();
    for (final OWLNamedIndividual individual : ontology.getIndividualsInSignature()) {
      futures.add(tpe.submit(new Runnable() {

        @Override
        public void run() {
          final OWLClassExpression msc = gelReasoner.ComputeMsc(roleDepth, individual, true);
          mscs.put(individual, msc);
        }
      }));
    }
    for (Future<?> f : futures)
      try {
        f.get();
      } catch (Exception e) {
        e.printStackTrace();
      }
    tpe.shutdown();
    return mscs;
  }

}
