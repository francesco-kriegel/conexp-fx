package conexp.fx.core.dl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;

import conexp.fx.core.algorithm.nextclosures.NextClosures2;
import conexp.fx.core.algorithm.nextclosures.NextClosures2.Result;
import conexp.fx.core.context.Context;
import conexp.fx.core.importer.NTriplesImporter;

public class PMIDTest {

  public static void main(String[] args) throws IOException {
    final Path data4GCI = Paths.get("/", "Volumes", "francesco", "Data", "Datasets", "PMID", "data4GCI.v3");
    final NTriplesImporter triplesImporter = new NTriplesImporter(data4GCI.toFile().listFiles());
    System.out.println("reading triples...");
    triplesImporter.readTriples();
    System.out.println("creating interpretation...");
    final OWLInterpretation interpretation = triplesImporter.extractInterpretation(IRI.create("isa"));
    System.out.println("building context...");
    final Context<IRI, OWLClassExpression> inducedContext = interpretation.getInducedContext(
        1,
        0,
        Constructor.CONJUNCTION,
        Constructor.EXISTENTIAL_RESTRICTION,
        Constructor.VALUE_RESTRICTION);
    System.out.println(inducedContext.colHeads().size() + " attributes");
    System.out.println(inducedContext.rowHeads().size() + " objects");
    System.out.println("computing implicational base...");
    final Result<IRI, OWLClassExpression> result = NextClosures2.compute(inducedContext);
    result.implications.forEach(System.out::println);
  }

}
