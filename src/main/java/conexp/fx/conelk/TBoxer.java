package conexp.fx.conelk;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import conexp.fx.core.algorithm.nextclosure.NextImplication;
import conexp.fx.core.context.MatrixContext;
import de.tudresden.inf.tcs.fcalib.Implication;

public class TBoxer {

  private final OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
  private final OWLDataFactory     owlDataFactory  = ontologyManager.getOWLDataFactory();
  private final Dataset            dataset;

  public TBoxer(final Dataset dataset) {
    super();
    this.dataset = dataset;
  }

  private final OWLOntology process(final OWLOntology input) throws OWLOntologyCreationException {
    final OWLOntology output = ontologyManager.createOntology();
    final MatrixContext<OWLNamedIndividual, OWLClassExpression> context = new LogicalContextGenerator(input).build(2);
    final Iterator<Implication<OWLClassExpression>> it =
        new NextImplication<OWLNamedIndividual, OWLClassExpression>(context).iterator();
    while (it.hasNext()) {
      final Implication<OWLClassExpression> imp = it.next();
      if (imp != null)
        ontologyManager.applyChange(new AddAxiom(output, owlDataFactory.getOWLSubClassOfAxiom(
            owlDataFactory.getOWLObjectIntersectionOf(imp.getPremise()),
            owlDataFactory.getOWLObjectIntersectionOf(imp.getConclusion()))));
    }
    return output;
  }

  private class Process implements Callable<OWLOntology> {

    private final OWLOntology input;
    private final String      key;

    public Process(final String key, final OWLOntology input) {
      super();
      this.key = key;
      this.input = input;
    }

    @Override
    public OWLOntology call() throws Exception {
      return process(input);
    }

  }

  public final OWLOntology getIntersectionTBox() {
    return null;
  }

  public final OWLOntology getUnionTBox() {
    return null;
  }

  public final Map<String, OWLOntology> getAllTBoxes() throws OWLOntologyCreationException {
    final Map<String, OWLOntology> tBoxes = new ConcurrentHashMap<String, OWLOntology>();
    final ThreadPoolExecutor tpe =
        new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(),
            1000,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
//    final Set<Future<?>> futures = new HashSet<Future<?>>();
//    for (final Entry<String, OWLOntology> data : dataset.getData().entrySet())
//      futures.add(tpe.submit(new Runnable() {
//
//        @Override
//        public void run() {
//          try {
//            tBoxes.put(data.getKey(), process(data.getValue()));
//          } catch (OWLOntologyCreationException e) {
//            e.printStackTrace();
//          }
//        }
//      }));
//    for (Future<?> f : futures)
//      try {
//        f.get();
//      } catch (InterruptedException | ExecutionException e) {
//        e.printStackTrace();
//      }
    final Set<Future<OWLOntology>> futures = new HashSet<Future<OWLOntology>>();
    for (Entry<String, OWLOntology> entry : dataset.getData().entrySet())
      tpe.submit(new Process(entry.getKey(), entry.getValue()));
    for (Future<OWLOntology> f : futures)
      try {
        tBoxes.put(((Process) f).key, f.get());
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    tpe.shutdown();
    return tBoxes;
  }

}
