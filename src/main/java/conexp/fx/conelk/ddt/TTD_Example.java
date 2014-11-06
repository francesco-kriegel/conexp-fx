package conexp.fx.conelk.ddt;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.ujmp.core.booleanmatrix.BooleanMatrix;

import com.google.common.base.Function;

import conexp.fx.core.algorithm.nextclosure.NextImplication2;
import conexp.fx.core.collections.relation.MatrixRelation;
import conexp.fx.core.collections.setlist.HashSetArrayList;
import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.collections.setlist.SetLists;
import conexp.fx.core.context.AbstractContext;
import conexp.fx.core.context.Context;
import conexp.fx.core.context.ContextExtractor;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.exporter.CXTExporter;
import conexp.fx.core.exporter.ImplicationWriter;
import de.tudresden.inf.lat.gel.GelReasoner;
import de.tudresden.inf.tcs.fcalib.Implication;

public class TTD_Example {

  public static String base = "/Users/francesco/Documents/workspace/conexp-fx/data/biotec";

  public static final void main(String[] args) {
    if (args.length > 0)
      base = args[0];
    try {
      TripleStore.initRepository();
//      TripleStore.testRepository();
      Ontology.initOntology();
      Ontology.computeMSCs();
      FormalConceptAnalysis.initFormalContext();
//      FormalConceptAnalysis.writeImplications();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static final MatrixContext<OWLNamedIndividual, OWLClassExpression> context =
                                                                                         new MatrixContext<OWLNamedIndividual, OWLClassExpression>(
                                                                                             false);

  public final static class TripleStore {

    private static final String     RDF_PREFIX  = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
    private static final String     RDFS_PREFIX = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
    private static final String     TTD_PREFIX  = "PREFIX ttd: <" + TTD.NAMESPACE + ">\n";
    private static final Repository repository  = new SailRepository(new MemoryStore());

    public static final void initRepository() throws RepositoryException {
      repository.initialize();
      final RepositoryConnection connection = repository.getConnection();
      connection.setNamespace("ttd", "<" + TTD.NAMESPACE + ">");
      TTDData.CLASS_A.addTo(connection);
      TTDData.CLASS_B.addTo(connection);
      // TTD_Data.CLASS_B_.addToRepository(connection);
      TTDData.CLASS_C.addTo(connection);
      TTDData.LINK_1.addTo(connection);
      TTDData.LINK_2.addTo(connection);
      TTDData.LINK_3.addTo(connection);
      TTDData.LINK_C.addTo(connection);
      TTDData.TYPE_A.addTo(connection);
      TTDData.TYPE_A_.addTo(connection);
      TTDData.TYPE_A__.addTo(connection);
      TTDData.TYPE_C.addTo(connection);
      TTDData.TYPE_3.addTo(connection);
    }

    public static final void testRepository() throws RepositoryException, QueryEvaluationException,
        MalformedQueryException {
      final RepositoryConnection connection = repository.getConnection();
      connection.setNamespace("ttd", "<" + TTD.NAMESPACE + ">");
      final String query = "SELECT ?subject ?object\n" + "WHERE {\n" + "?subject ttd:hasName ?object .\n" + "}";
      final TupleQueryResult result = connection.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();
      while (result.hasNext())
        System.out.println(result.next().toString().replace(TTD.NAMESPACE, "ttd:"));
      connection.commit();
      connection.close();
    }

    public static final SetList<URI> getListOfAll(final String target) throws Exception {
      final SetList<URI> diseases = new HashSetArrayList<URI>();
      final String query = "SELECT ?x \n WHERE { ?x rdf:type " + target + " . }";
      for (BindingSet bindingset : query(query))
        diseases.add(new URI(bindingset.getBinding("x").getValue().stringValue()));
      return diseases;
    }

    private static final boolean ask(final String query) throws Exception {
      final RepositoryConnection conn = repository.getConnection();
      final boolean result =
          conn.prepareBooleanQuery(QueryLanguage.SPARQL, RDF_PREFIX + RDFS_PREFIX + TTD_PREFIX + query).evaluate();
      conn.close();
      return result;
    }

    private static final List<BindingSet> query(final String query) throws Exception {
      final List<BindingSet> tuples = new ArrayList<BindingSet>();
      final RepositoryConnection conn = repository.getConnection();
      final TupleQueryResult result =
          conn.prepareTupleQuery(QueryLanguage.SPARQL, RDF_PREFIX + RDFS_PREFIX + TTD_PREFIX + query).evaluate();
      int i = 0;
      while (result.hasNext()) {
        System.out.println(i++);
        tuples.add(result.next());
      }
      conn.close();
      return tuples;
    }
  }

  public static final class Ontology {

    public static final OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
    public static final ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
    public static OWLOntology              ontology;
    public static OWLReasoner              reasoner;
    public static GelReasoner              gelReasoner;
    public static final OWLDataFactory     owlDataFactory  = ontologyManager.getOWLDataFactory();

    static {
      try {
        ontology = ontologyManager.createOntology();
        reasoner = reasonerFactory.createReasoner(ontology);
        gelReasoner = new GelReasoner(ontology);
      } catch (OWLOntologyCreationException e) {
        e.printStackTrace();
      }
    }

    private static final void initOntology() {
      try {
        loadFromTripleStore();
      } catch (RepositoryException e) {
        e.printStackTrace();
      }
    }

    private static final void loadFromTripleStore() throws RepositoryException {
      final RepositoryConnection conn = TripleStore.repository.getConnection();
      final RepositoryResult<Statement> statements = conn.getStatements(null, null, null, false);
      while (statements.hasNext()) {
        final Statement stmt = statements.next();
        if (stmt.getPredicate().equals(RDF.TYPE)) {
          addClassAssertion(stmt.getObject().stringValue(), stmt.getSubject().stringValue());
        } else {
          addRoleAssertion(stmt.getPredicate().stringValue(), stmt.getSubject().stringValue(), stmt
              .getObject()
              .stringValue());
        }
      }
      conn.close();
    }

    private static final void addClassAssertion(final String clazz, final String individual) {
      final OWLClassAssertionAxiom ax =
          owlDataFactory.getOWLClassAssertionAxiom(
              owlDataFactory.getOWLClass(IRI.create("", clazz)),
              owlDataFactory.getOWLNamedIndividual(IRI.create("", individual)));
//      System.out.println(ax);
      ontologyManager.applyChange(new AddAxiom(ontology, ax));
    }

    private static final void addRoleAssertion(final String role, final String individual1, final String individual2) {
      final OWLObjectPropertyAssertionAxiom ax =
          owlDataFactory.getOWLObjectPropertyAssertionAxiom(
              owlDataFactory.getOWLObjectProperty(IRI.create("", role)),
              owlDataFactory.getOWLNamedIndividual(IRI.create("", individual1)),
              owlDataFactory.getOWLNamedIndividual(IRI.create("", individual2)));
//      System.out.println(ax);
      ontologyManager.applyChange(new AddAxiom(ontology, ax));
    }

    public static final ConcurrentHashMap<OWLNamedIndividual, OWLClassExpression> mscs =
                                                                                           new ConcurrentHashMap<OWLNamedIndividual, OWLClassExpression>();

    private static ThreadPoolExecutor                                             tpe  =
                                                                                           new ThreadPoolExecutor(
                                                                                               16,
                                                                                               16,
                                                                                               10000,
                                                                                               TimeUnit.MILLISECONDS,
                                                                                               new LinkedBlockingQueue<Runnable>());

    private static final void computeMSCs() {
      final HashSet<Future> futures = new HashSet<Future>();
      for (final OWLNamedIndividual i : ontology.getIndividualsInSignature()) {
        futures.add(tpe.submit(new Runnable() {

          @Override
          public void run() {
            System.out.println("computing msc for " + i);
            final OWLClassExpression msc = mostSpecificConcept(i);
            System.out.println(msc);
            mscs.put(i, msc);
          }
        }));
      }
      for (Future f : futures)
        try {
          f.get();
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
      System.out.println("msc computation done");
    }

    private static final OWLClassExpression mostSpecificConcept(final OWLNamedIndividual individual) {
      return gelReasoner.ComputeMsc(1, individual, true);
    }

    private static final String toLaTeX(final OWLClassExpression clazz) {
      final StringBuilder str = new StringBuilder();
      str.append("(");
      if (clazz instanceof OWLObjectSomeValuesFrom) {
        OWLObjectSomeValuesFrom exrest = (OWLObjectSomeValuesFrom) clazz;
        final String r = exrest.getProperty().toString();
        final OWLClassExpression c = exrest.getFiller();
        str.append("\\exists ");
        str.append(r + ".");
        str.append(toLaTeX(c));
      } else if (clazz instanceof OWLObjectIntersectionOf) {
        OWLObjectIntersectionOf conj = (OWLObjectIntersectionOf) clazz;
        boolean first = true;
        for (OWLClassExpression c : conj.asConjunctSet()) {
          if (first)
            first = false;
          else
            str.append("\\sqcon");
          str.append(toLaTeX(c));
        }
      } else if (clazz instanceof OWLClass) {
        OWLClass c = (OWLClass) clazz;
        str.append(c.toString());
      }
      str.append(")");
      return str.toString();
    }

    public static final void writeOntology() {

    }
  }

  public static final class FormalConceptAnalysis {

    @Deprecated
    public static final void initDiseaseContext() throws Exception {
      for (URI disease : TripleStore.getListOfAll("ttd:disease"))
        System.out.println(disease);
      final SetList<URI> diseases = TripleStore.getListOfAll("ttd:disease");
      final SetList<URI> drugs = TripleStore.getListOfAll("ttd:drug");
      final Context<URI, URI> diseaseContext =
          new AbstractContext<URI, URI>(new HashSetArrayList<URI>(diseases.subList(0, 10)), new HashSetArrayList<URI>(
              drugs.subList(0, 10)), false) {

            @Override
            public boolean contains(Object o1, Object o2) {
              try {
                return TripleStore.ask("ASK { " + o1.toString() + " ttd:heals " + o2.toString() + " }");
              } catch (Exception e) {
                return false;
              }
            }

          };
      final MatrixRelation<URI, URI> diseaseOrder = diseaseContext.objectQuasiOrder().clone();
      CXTExporter.<URI, URI> export(toMatrixContext(diseaseOrder), new File("diseaseorder.cxt"));
    }

    @Deprecated
    public static final void initFormalContextFromTripleStore() {
//    final String query =
//        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
//            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + "PREFIX ttd: <" + TTD.NAMESPACE + ">\n"
//            + "SELECT ?object ?attribute1 ?attribute2 \n" + "WHERE {\n" + "?object ?attribute1 ?attribute2 ;\n"
//            + "rdf:type ttd:drug .\n" + "}";
      final String query =
          "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
              + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + "PREFIX ttd: <" + TTD.NAMESPACE + ">\n"
              + "SELECT ?object ?attribute \n" + "WHERE {\n" + "?drug ttd:heals ?attribute ;\n"
              + "ttd:hasType ?object .\n" + "}";
//      SPARQLImporter.importRepository(context, repository, query);
//      CXTExporter.<String, String> export(context, new File("ttd.cxt"));
      ContextExtractor.extractFast(new File(base + "/ttd.cxt"), new File(base + "/ttd.cxt"), 10);
    }

    public static final void initFormalContext() {
      final Set<OWLNamedIndividual> rowHeads = new HashSet<OWLNamedIndividual>();
      final Set<OWLClassExpression> colHeads = new HashSet<OWLClassExpression>();
      for (OWLNamedIndividual i : Ontology.mscs.keySet())
        rowHeads.add(i);
      for (Entry<OWLNamedIndividual, OWLClassExpression> e : Ontology.mscs.entrySet())
        colHeads.add(e.getValue());
      context.rowHeads().addAll(rowHeads);
      context.colHeads().addAll(colHeads);
      for (Entry<OWLNamedIndividual, OWLClassExpression> e : Ontology.mscs.entrySet()) {
        context.addFastSilent(e.getKey(), e.getValue());
        for (OWLClassExpression c : colHeads)
          if (Ontology.reasoner.isEntailed(Ontology.owlDataFactory.getOWLClassAssertionAxiom(c, e.getKey())))
            context.addFastSilent(e.getKey(), c);
      }
      final BooleanMatrix matrix = context.matrix();
      final SetList<String> domain = SetLists.transform(context.rowHeads(), new Function<OWLNamedIndividual, String>() {

        @Override
        public final String apply(final OWLNamedIndividual input) {
          return input.toString();
        }
      });
      final SetList<String> codomain =
          SetLists.transform(context.colHeads(), new Function<OWLClassExpression, String>() {

            @Override
            public final String apply(final OWLClassExpression input) {
              return Ontology.toLaTeX(input);
            }
          });
      final MatrixContext<String, String> latexContext =
          new MatrixContext<String, String>(domain, codomain, matrix, false);
      CXTExporter.<String, String> export(latexContext, new File("ttd.cxt"));
      ContextExtractor.extractFast(new File(base + "/ttd.cxt"), new File(base + "/ttd.cxt"), 10);
    }

    private static void writeImplications() {
      List<Implication<String>> implications = new ArrayList<Implication<String>>();
      final Iterator<Implication<String>> it = new NextImplication2(context).iterator();
      int i = 0;
      while (it.hasNext()) {
        System.out.println("implication " + i++);
        implications.add(it.next());
      }

      try {
        ImplicationWriter.export(implications, new File("ttd.implications.txt"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    private static final <G, M> MatrixContext<G, M> toMatrixContext(final MatrixRelation<G, M> r) {
      return new MatrixContext<G, M>(r.rowHeads(), r.colHeads(), r.matrix(), r.isHomogen());
    }
  }
}
