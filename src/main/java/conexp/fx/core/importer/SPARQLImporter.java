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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.collections.setlist.HashSetArrayList;
import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.context.MatrixContext;

public class SPARQLImporter
{
  public static final class TemporaryRepository
  {
    public final Repository repository = new SailRepository(new MemoryStore());

    public TemporaryRepository(final File file) throws RepositoryException, RDFParseException, IOException
    {
      repository.initialize();
      final RepositoryConnection connection = repository.getConnection();
      connection.add(file, null, RDFFormat.forFileName(file.getName(), RDFFormat.RDFXML));
      connection.commit();
      connection.close();
    }

    public TemporaryRepository(final URL url) throws RepositoryException, RDFParseException, IOException
    {
      repository.initialize();
      final RepositoryConnection connection = repository.getConnection();
      connection.add(url, null, RDFFormat.forFileName(url.toString(), RDFFormat.RDFXML));
      connection.commit();
      connection.close();
    }
  }

  private static final class ContextTupleQueryResultHandler
    implements TupleQueryResultHandler
  {
    private final MatrixContext<String, String> context;
    private boolean                             objectTuples          = true;
    private boolean                             attributeTuples       = true;
    private final List<String>                  objectBindingNames    = new ArrayList<String>();
    private final List<String>                  attributeBindingNames = new ArrayList<String>();
    private final SetList<String>               objects               = new HashSetArrayList<String>();
    private final SetList<String>               attributes            = new HashSetArrayList<String>();
    private final Set<Pair<String, String>>     crosses               = new HashSet<Pair<String, String>>();

    private ContextTupleQueryResultHandler(final MatrixContext<String, String> context)
    {
      this.context = context;
//    context.lock();
      context.rowHeads().add("null");
      context.colHeads().add("null");
    }

    public void handleBoolean(final boolean value) throws QueryResultHandlerException
    {
      System.out.println("handle boolean " + value);
    }

    public void handleLinks(final List<String> linkUrls) throws QueryResultHandlerException
    {
      System.out.println("handle links " + linkUrls);
    }

    public final void startQueryResult(final List<String> bindingNames) throws TupleQueryResultHandlerException
    {
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

    public final void endQueryResult() throws TupleQueryResultHandlerException
    {
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

    public final void handleSolution(final BindingSet bindingSet) throws TupleQueryResultHandlerException
    {
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

  public static void importXML(final MatrixContext<String, String> context, String url, String query)
  {
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

  public static void importRepository(MatrixContext<String, String> context, Repository repo, String query)
  {
    try {
      final RepositoryConnection connection = repo.getConnection();
      connection.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate(new ContextTupleQueryResultHandler(context));
      connection.close();
    } catch (QueryEvaluationException | RepositoryException | MalformedQueryException
        | TupleQueryResultHandlerException e) {
      e.printStackTrace();
    }
  }

  public static void importFile(MatrixContext<String, String> context, File file, String query)
  {
    try {
      importRepository(context, new TemporaryRepository(file).repository, query);
    } catch (RepositoryException | RDFParseException | IOException e) {
      e.printStackTrace();
    }
  }

  public static void importURL(MatrixContext<String, String> context, String url, String query)
  {
    try {
      importRepository(context, new TemporaryRepository(new URL(url)).repository, query);
    } catch (RepositoryException | RDFParseException | IOException e) {
      e.printStackTrace();
    }
  }
}
