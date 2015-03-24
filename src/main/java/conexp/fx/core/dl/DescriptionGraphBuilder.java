package conexp.fx.core.dl;


public class DescriptionGraphBuilder {

//  public final static DescriptionGraph fromRDFGraph(final File file) throws RepositoryException, RDFParseException,
//      IOException, MalformedQueryException {
//    final DescriptionGraph graph = new DescriptionGraph();
//    final Repository repository = new SailRepository(new MemoryStore());
//    repository.initialize();
//    final RepositoryConnection connection = repository.getConnection();
//    connection.add(file, null, RDFFormat.forFileName(file.getName(), RDFFormat.RDFXML));
//    connection.commit();
//    connection.prepareQuery(QueryLanguage.SPARQL, "");
//
//    connection.close();
//    return graph;
//  }
//
//  public final static void testDDT() throws RepositoryException {
//    final DescriptionGraph graph = new DescriptionGraph();
//    System.out.println("loading data");
//    TTD_Example.TripleStore.initRepository();
//    System.out.println("done");
//    final RepositoryConnection c = TTD_Example.TripleStore.repository.getConnection();
//    final RepositoryResult<Statement> s = c.getStatements(null, null, null, false);
//    int i = 0;
//    while (s.hasNext()) {
//      final Statement n = s.next();
//      System.out.println(n);
//      if (n.getPredicate().equals(RDF.TYPE))
//        graph.nodes.add(
//            graph.df.getOWLNamedIndividual(IRI.create(n.getSubject().stringValue())),
//            graph.df.getOWLClass(IRI.create(n.getObject().stringValue())));
//      else {
//        final OWLObjectProperty key = graph.df.getOWLObjectProperty(IRI.create(n.getPredicate().stringValue()));
//        if (!graph.edges.containsKey(key))
//          graph.edges.put(key, new MatrixContext<OWLNamedIndividual, OWLNamedIndividual>(true));
//        graph.edges.get(key).add(
//            graph.df.getOWLNamedIndividual(IRI.create(n.getSubject().stringValue())),
//            graph.df.getOWLNamedIndividual(IRI.create(n.getObject().stringValue())));
//      }
//      if (i++ > 1000)
//        break;
//    }
//    s.close();
//    c.close();
//    System.out.println(graph.nodes);
//    System.out.println(graph.edges);
//  }
//
//  public final static Interpretation2 testDDT_() throws RepositoryException {
//    final Interpretation2 in = new Interpretation2(10000, 10000, 10000);
//    System.out.println("interpretation created");
//    TTD_Example.TripleStore.initRepository();
//    final RepositoryConnection c = TTD_Example.TripleStore.repository.getConnection();
//    final RepositoryResult<Statement> s = c.getStatements(null, null, null, false);
//    int i = 0;
//    while (s.hasNext()) {
//      final Statement n = s.next();
////      System.out.println(n);
//      if (n.getPredicate().equals(RDF.TYPE)) {
//        in.addIndividual(n.getSubject().stringValue());
//        in.addConcept(n.getObject().stringValue());
//        in.addConceptAssertion(n.getSubject().stringValue(), n.getObject().stringValue());
//      } else {
//        in.addIndividual(n.getSubject().stringValue());
//        in.addIndividual(n.getObject().stringValue());
//        in.addRole(n.getPredicate().stringValue());
//        in.addRoleAssertion(n.getSubject().stringValue(), n.getPredicate().stringValue(), n.getObject().stringValue());
//      }
////      if (i++ > 1000)
////        break;
//    }
//    s.close();
//    c.close();
//    return in;
//  }
//
//  public final static DescriptionGraph test57() {
//    final DescriptionGraph graph = new DescriptionGraph();
//    final MatrixContext<OWLNamedIndividual, OWLNamedIndividual> cxt =
//        new MatrixContext<OWLNamedIndividual, OWLNamedIndividual>(true);
//    for (String s : new String[] { "Kirk", "Luann", "Milhouse", "Clancy", "Jackie", "Selma" }) {
//      final OWLNamedIndividual i = graph.df.getOWLNamedIndividual(IRI.create(s));
//      graph.nodes.rowHeads().add(i);
//      cxt.rowHeads().add(i);
//    }
//    for (String s : new String[] { "Male", "Female", "Father", "Mother" })
//      graph.nodes.colHeads().add(graph.df.getOWLClass(IRI.create(s)));
//    graph.nodes.add(graph.df.getOWLNamedIndividual(IRI.create("Kirk")), graph.df.getOWLClass(IRI.create("Male")));
//    graph.nodes.add(graph.df.getOWLNamedIndividual(IRI.create("Kirk")), graph.df.getOWLClass(IRI.create("Father")));
//    graph.nodes.add(graph.df.getOWLNamedIndividual(IRI.create("Clancy")), graph.df.getOWLClass(IRI.create("Male")));
//    graph.nodes.add(graph.df.getOWLNamedIndividual(IRI.create("Clancy")), graph.df.getOWLClass(IRI.create("Father")));
//    graph.nodes.add(graph.df.getOWLNamedIndividual(IRI.create("Luann")), graph.df.getOWLClass(IRI.create("Female")));
//    graph.nodes.add(graph.df.getOWLNamedIndividual(IRI.create("Luann")), graph.df.getOWLClass(IRI.create("Mother")));
//    graph.nodes.add(graph.df.getOWLNamedIndividual(IRI.create("Jackie")), graph.df.getOWLClass(IRI.create("Female")));
//    graph.nodes.add(graph.df.getOWLNamedIndividual(IRI.create("Jackie")), graph.df.getOWLClass(IRI.create("Mother")));
//    graph.nodes.add(graph.df.getOWLNamedIndividual(IRI.create("Selma")), graph.df.getOWLClass(IRI.create("Female")));
//    graph.nodes.add(graph.df.getOWLNamedIndividual(IRI.create("Milhouse")), graph.df.getOWLClass(IRI.create("Male")));
//    cxt.add(graph.df.getOWLNamedIndividual(IRI.create("Kirk")), graph.df.getOWLNamedIndividual(IRI.create("Milhouse")));
//    cxt
//        .add(
//            graph.df.getOWLNamedIndividual(IRI.create("Luann")),
//            graph.df.getOWLNamedIndividual(IRI.create("Milhouse")));
//    cxt.add(graph.df.getOWLNamedIndividual(IRI.create("Clancy")), graph.df.getOWLNamedIndividual(IRI.create("Selma")));
//    cxt.add(graph.df.getOWLNamedIndividual(IRI.create("Jackie")), graph.df.getOWLNamedIndividual(IRI.create("Selma")));
//    graph.edges.put(graph.df.getOWLObjectProperty(IRI.create("child")), cxt);
//    System.out.println(graph.nodes);
//    System.out.println(graph.edges);
//    return graph;
//  }
//
//  public final static Interpretation2 test57_() {
//    final Interpretation2 in = new Interpretation2(6, 4, 1);
//    for (String s : new String[] { "Kirk", "Luann", "Milhouse", "Clancy", "Jackie", "Selma" })
//      in.addIndividual(s);
//    for (String s : new String[] { "Male", "Female", "Father", "Mother" })
//      in.addConcept(s);
//    in.addRole("child");
//    in.addConceptAssertion("Kirk", "Male");
//    in.addConceptAssertion("Kirk", "Father");
//    in.addConceptAssertion("Clancy", "Male");
//    in.addConceptAssertion("Clancy", "Father");
//    in.addConceptAssertion("Luann", "Female");
//    in.addConceptAssertion("Luann", "Mother");
//    in.addConceptAssertion("Jackie", "Female");
//    in.addConceptAssertion("Jackie", "Mother");
//    in.addConceptAssertion("Milhouse", "Male");
//    in.addConceptAssertion("Selma", "Female");
//    in.addRoleAssertion("Kirk", "child", "Milhouse");
//    in.addRoleAssertion("Luann", "child", "Milhouse");
//    in.addRoleAssertion("Clancy", "child", "Selma");
//    in.addRoleAssertion("Jackie", "child", "Selma");
//    return in;
//  }
//
//  public static void main(String[] args) {
//    final DescriptionGraph graph = test57();
//    final MatrixContext<OWLNamedIndividual, OWLClassExpression> logicalContext = graph.toLogicalContext(40);
//    logicalContext.colHeads().forEach(new Consumer<OWLClassExpression>() {
//
//      @Override
//      public void accept(OWLClassExpression t) {
//        System.out.println(t);
//      }
//    });
//    System.out.println(logicalContext);
//
//    final Interpretation2 in = test57_();
//    final MatrixContext<OWLNamedIndividual, OWLClassExpression> lcxt = in.toLogicalContext(2, DescriptionLogic.EL);
//    lcxt.colHeads().forEach(new Consumer<OWLClassExpression>() {
//
//      @Override
//      public void accept(OWLClassExpression t) {
//        System.out.println(t);
//      }
//    });
//    System.out.println(lcxt);
//
//    final Result<OWLNamedIndividual, OWLClassExpression> base = NextClosures6.compute(lcxt, true);
//    for (Entry<?, ?> e : base.implications.entrySet())
//      System.out.println(e.getKey() + " ==> " + e.getValue());
//
//    try {
//      final Interpretation2 inDDT = testDDT_();
//      System.out.println("creating logical scaling");
//      final MatrixContext<OWLNamedIndividual, OWLClassExpression> lcxt2 =
//          inDDT.toLogicalContext(0, DescriptionLogic.EL);
//      System.out.println("done");
//      System.out.println(lcxt2);
//    } catch (RepositoryException e1) {
//      // TODO Auto-generated catch block
//      e1.printStackTrace();
//    }
//
////    try {
////      testDDT();
////    } catch (RepositoryException e) {
////      // TODO Auto-generated catch block
////      e.printStackTrace();
////    }
//  }
}
