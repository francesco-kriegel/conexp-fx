package conexp.fx.core.importer;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLParser;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.model.IRI;

import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.collections.setlist.HashSetArrayList;
import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.dl.OWLInterpretation;
import conexp.fx.core.dl.Signature;

public class RDFImporter {

  public static final void readCSV(final Repository repository, final File file)
      throws RepositoryException, RDFParseException, IOException {
    final RepositoryConnection connection = repository.getConnection();
    final ValueFactory f = new ValueFactoryImpl();
    Files
        .lines(file.toPath())
        .map(line -> line.split(";"))
        .map(
            tuple -> new StatementImpl(
                f.createURI(file.getName() + ":", tuple[0]),
                f.createURI(file.getName() + ":", tuple[1]),
                f.createURI(file.getName() + ":", tuple[2])))
        .forEach(statement -> {
          try {
            connection.add(statement);
          } catch (RepositoryException e) {
            throw new RuntimeException(e);
          }
        });
    connection.commit();
    connection.close();
  }

  public static final Repository read(final File file) throws RepositoryException, RDFParseException, IOException {
    final Repository repository = new SailRepository(new MemoryStore());
    read(repository, file);
    return repository;
  }

  public static final void read(final Repository repository, final File file)
      throws RepositoryException, RDFParseException, IOException {
    final RepositoryConnection connection = repository.getConnection();
    connection.add(file, null, RDFFormat.forFileName(file.getName(), RDFFormat.RDFXML));
    connection.commit();
    connection.close();
  }

  public static final Repository read(final URL url) throws RepositoryException, RDFParseException, IOException {
    final Repository repository = new SailRepository(new MemoryStore());
    read(repository, url);
    return repository;
  }

  public static final void read(final Repository repository, final URL url)
      throws RepositoryException, RDFParseException, IOException {
    final RepositoryConnection connection = repository.getConnection();
    connection.add(url, null, RDFFormat.forFileName(url.toString(), RDFFormat.RDFXML));
    connection.commit();
    connection.close();
  }

  private static final class ContextTupleQueryResultHandler implements TupleQueryResultHandler {

    private final MatrixContext<String, String> context;
    private boolean                             objectTuples          = true;
    private boolean                             attributeTuples       = true;
    private final List<String>                  objectBindingNames    = new ArrayList<String>();
    private final List<String>                  attributeBindingNames = new ArrayList<String>();
    private final SetList<String>               objects               = new HashSetArrayList<String>();
    private final SetList<String>               attributes            = new HashSetArrayList<String>();
    private final Set<Pair<String, String>>     crosses               = new HashSet<Pair<String, String>>();

    private ContextTupleQueryResultHandler(final MatrixContext<String, String> context) {
      this.context = context;
//    context.lock();
      context.rowHeads().add("null");
      context.colHeads().add("null");
    }

    public void handleBoolean(final boolean value) throws QueryResultHandlerException {
      System.out.println("handle boolean " + value);
    }

    public void handleLinks(final List<String> linkUrls) throws QueryResultHandlerException {
      System.out.println("handle links " + linkUrls);
    }

    public final void startQueryResult(final List<String> bindingNames) throws TupleQueryResultHandlerException {
      for (String bindingName : bindingNames)
        if (bindingName.toLowerCase().startsWith("object"))
          objectBindingNames.add(bindingName);
        else if (bindingName.toLowerCase().startsWith("attribute"))
          attributeBindingNames.add(bindingName);
      if (objectBindingNames.size() == 1)
        objectTuples = false;
      if (attributeBindingNames.size() == 1)
        attributeTuples = false;
    }

    public final void endQueryResult() throws TupleQueryResultHandlerException {
      System.out.println("adding " + objects.size() + " objects");
      context.rowHeads().addAll(0, objects);
      System.out.println("adding " + attributes.size() + " attributes");
      context.colHeads().addAll(0, attributes);
      context.rowHeads().remove("null");
      context.colHeads().remove("null");
      System.out.println("adding " + crosses.size() + " crosses");
      for (Pair<String, String> p : crosses)
        context.addFastSilent(p.x(), p.y());
      // context.unlock();
      context.pushAllChangedEvent();
    }

    public final void handleSolution(final BindingSet bindingSet) throws TupleQueryResultHandlerException {
      String object = "", attribute = "";
      if (objectTuples) {
        for (String objectBindingName : objectBindingNames)
          object += bindingSet.getBinding(objectBindingName).getValue().stringValue() + "; ";
        object = object.substring(0, object.length() - 2);
      } else
        object = bindingSet.getBinding(objectBindingNames.get(0)).getValue().stringValue();
      if (attributeTuples) {
        for (String attributeBindingName : attributeBindingNames)
          attribute += bindingSet.getBinding(attributeBindingName).getValue().stringValue() + "; ";
        attribute = attribute.substring(0, attribute.length() - 2);
      } else
        attribute = bindingSet.getBinding(attributeBindingNames.get(0)).getValue().stringValue();
      objects.add(object);
      attributes.add(attribute);
      crosses.add(Pair.of(object, attribute));
    }
  }

  public static void importXML(final MatrixContext<String, String> context, String url, String query) {
    try {
      final SPARQLResultsXMLParser parser = new SPARQLResultsXMLParser();
      parser.setTupleQueryResultHandler(new ContextTupleQueryResultHandler(context));
      final String queryURL = new String(url).replace("<QUERY>", URLEncoder.encode(query, "UTF-8"));
      System.out.println("reading " + queryURL);
      final InputStream stream = new URL(queryURL).openStream();
      System.out.println("parsing results");
      parser.parseQueryResult(stream);
      System.out.println("parse done");
    } catch (QueryResultParseException | QueryResultHandlerException | IOException e) {
      e.printStackTrace();
    }
  }

  public static void importRepository(MatrixContext<String, String> context, Repository repo, String query) {
    try {
      final RepositoryConnection connection = repo.getConnection();
      connection.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate(new ContextTupleQueryResultHandler(context));
      connection.close();
    } catch (QueryEvaluationException | RepositoryException | MalformedQueryException
        | TupleQueryResultHandlerException e) {
      e.printStackTrace();
    }
  }

  public static void importFile(MatrixContext<String, String> context, File file, String query) {
    try {
      importRepository(context, read(file), query);
    } catch (RepositoryException | RDFParseException | IOException e) {
      e.printStackTrace();
    }
  }

  public static void importURL(MatrixContext<String, String> context, String url, String query) {
    try {
      importRepository(context, read(new URL(url)), query);
    } catch (RepositoryException | RDFParseException | IOException e) {
      e.printStackTrace();
    }
  }

  public static final OWLInterpretation extractInterpretation(final List<Statement> triples) {
    return extractInterpretation(triples, IRI.create(RDF.TYPE.stringValue()));
  }

  public static final OWLInterpretation
      extractInterpretation(final List<Statement> triples, final IRI selectedIsARoleName) {
    final List<IRI> roleNames =
        triples.parallelStream().map(triple -> IRI.create(triple.getPredicate().stringValue())).distinct().collect(
            Collectors.toList());
    if (!roleNames.contains(selectedIsARoleName))
      throw new IllegalArgumentException();
    roleNames.remove(selectedIsARoleName);
    final List<IRI> conceptNames = triples
        .parallelStream()
        .filter(triple -> IRI.create(triple.getPredicate().stringValue()).equals(selectedIsARoleName))
        .map(triple -> IRI.create(triple.getObject().stringValue()))
        .collect(Collectors.toList());
    return extractInterpretation(triples, conceptNames, roleNames, selectedIsARoleName);
  }

  public static final OWLInterpretation extractInterpretation(
      final List<Statement> triples,
      final List<IRI> selectedConceptNames,
      final List<IRI> selectedRoleNames,
      final IRI selectedIsARoleName) {
    final Signature signature = new Signature(null);
    signature.getConceptNames().addAll(selectedConceptNames);
    signature.getRoleNames().addAll(selectedRoleNames);
    signature.getIndividualNames().addAll(
        triples
            .parallelStream()
            .filter(
                triple -> IRI.create(triple.getPredicate().stringValue()).equals(selectedIsARoleName)
                    && signature.getConceptNames().contains(IRI.create(triple.getObject().stringValue())))
            .map(triple -> IRI.create(triple.getSubject().stringValue()))
            .collect(Collectors.toSet()));
    final OWLInterpretation i = new OWLInterpretation(signature);
    triples.stream().forEach(triple -> {
      if (IRI.create(triple.getPredicate().stringValue()).equals(selectedIsARoleName)) {
        if (signature.getConceptNames().contains(IRI.create(triple.getObject().stringValue()))
            && signature.getIndividualNames().contains(IRI.create(triple.getSubject().stringValue()))) {
          i.addConceptNameAssertion(
              IRI.create(triple.getObject().stringValue()),
              IRI.create(triple.getSubject().stringValue()));
        }
      } else
        if (signature.getRoleNames().contains(IRI.create(triple.getPredicate().stringValue()))
            && signature.getIndividualNames().contains(IRI.create(triple.getSubject().stringValue()))
            && signature.getIndividualNames().contains(IRI.create(triple.getObject().stringValue()))) {
        i.addRoleNameAssertion(
            IRI.create(triple.getPredicate().stringValue()),
            IRI.create(triple.getSubject().stringValue()),
            IRI.create(triple.getObject().stringValue()));
      }
    });
    return i;
  }

}
