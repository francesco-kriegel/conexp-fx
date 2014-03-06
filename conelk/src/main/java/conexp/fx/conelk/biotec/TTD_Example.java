package conexp.fx.conelk.biotec;

import java.io.File;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.exporter.CXTExporter;
import conexp.fx.core.importer.SPARQLImporter;

public class TTD_Example {

  public static final void main(String[] args) {
    try {
      initRepository();
//      testRepository();
      initFormalContext();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static final Repository                    repository = new SailRepository(new MemoryStore());
  private static final MatrixContext<String, String> context    = new MatrixContext<String, String>(false);

  public static final void initRepository() throws RepositoryException {
    repository.initialize();
    final RepositoryConnection connection = repository.getConnection();
    connection.setNamespace("ttd", "<" + TTD.NAMESPACE + ">");
    TTD_Data.CLASS_A.addTo(connection);
    TTD_Data.CLASS_B.addTo(connection);
    // TTD_Data.CLASS_B_.addToRepository(connection);
    TTD_Data.CLASS_C.addTo(connection);
    TTD_Data.LINK_1.addTo(connection);
    TTD_Data.LINK_2.addTo(connection);
    TTD_Data.LINK_3.addTo(connection);
    TTD_Data.LINK_C.addTo(connection);
    TTD_Data.TYPE_A.addTo(connection);
    TTD_Data.TYPE_A_.addTo(connection);
    TTD_Data.TYPE_A__.addTo(connection);
    TTD_Data.TYPE_C.addTo(connection);
    TTD_Data.TYPE_3.addTo(connection);
  }

  public static final void testRepository() throws RepositoryException, QueryEvaluationException,
      MalformedQueryException {
    final RepositoryConnection connection = repository.getConnection();
    connection.setNamespace("ttd", "<" + TTD.NAMESPACE + ">");
    final String query =
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + "PREFIX ttd: <" + TTD.NAMESPACE + ">\n"
            + "SELECT ?subject ?object\n" + "WHERE {\n" + "?subject ttd:hasName ?object .\n" + "}";
    final TupleQueryResult result = connection.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();
    while (result.hasNext())
      System.out.println(result.next().toString().replace(TTD.NAMESPACE, "ttd:"));
    connection.commit();
    connection.close();
  }

  public static final void initFormalContext() {
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
    SPARQLImporter.importRepository(context, repository, query);
    CXTExporter.<String, String> export(context, new File("ttd.cxt"));
  }

  private static final void initOntology() throws OWLOntologyCreationException {
    final OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
    final ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
    final OWLOntology ontology = ontologyManager.createOntology();
    final OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
    final OWLDataFactory owlDataFactory = ontologyManager.getOWLDataFactory();

  }

  public static final void writeABox() {

  }

}
