package conexp.fx.core.mmsc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.openrdf.model.vocabulary.RDF;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import com.google.common.collect.Sets;

import conexp.fx.core.context.MatrixContext;

public class Interpretation {

  protected final Matrix3D<OWLNamedIndividual, OWLObjectProperty, OWLClass>           concepts;
  protected final Matrix3D<OWLNamedIndividual, OWLObjectProperty, OWLNamedIndividual> roles;
  protected final OWLDataFactory                                                      df;
  protected final OWLObjectProperty                                                   type;

  public Interpretation(final int individuals, final int concepts, final int roles) {
    super();
    this.concepts = new Matrix3D<OWLNamedIndividual, OWLObjectProperty, OWLClass>(individuals, 1, concepts);
    this.roles =
        new Matrix3D<OWLNamedIndividual, OWLObjectProperty, OWLNamedIndividual>(individuals, roles, individuals);
    this.df = OWLManager.getOWLDataFactory();
    this.type = df.getOWLObjectProperty(IRI.create(RDF.TYPE.stringValue()));
    this.concepts.addM(type);
  }

  public final void addIndividual(final OWLNamedIndividual d) {
    concepts.addG(d);
    roles.addG(d);
    roles.addW(d);
  }

  public final void addIndividual(final String d) {
    this.addIndividual(df.getOWLNamedIndividual(IRI.create(d)));
  }

  public final void addConcept(final OWLClass c) {
    concepts.addW(c);
  }

  public final void addConcept(final String c) {
    this.addConcept(df.getOWLClass(IRI.create(c)));
  }

  public final void addRole(final OWLObjectProperty r) {
    roles.addM(r);
  }

  public final void addRole(final String r) {
    this.addRole(df.getOWLObjectProperty(IRI.create(r)));
  }

  public final void addConceptAssertion(final OWLNamedIndividual d, final OWLClass c) {
    concepts.add(d, type, c);
  }

  public final void addConceptAssertion(final String d, final String c) {
    this.addConceptAssertion(df.getOWLNamedIndividual(IRI.create(d)), df.getOWLClass(IRI.create(c)));
  }

  public final void addRoleAssertion(final OWLNamedIndividual d, final OWLObjectProperty r, final OWLNamedIndividual e) {
    roles.add(d, r, e);
  }

  public final void addRoleAssertion(final String d, final String r, final String e) {
    this.addRoleAssertion(
        df.getOWLNamedIndividual(IRI.create(d)),
        df.getOWLObjectProperty(IRI.create(r)),
        df.getOWLNamedIndividual(IRI.create(e)));
  }

  public final boolean isInstanceOf(final OWLNamedIndividual i, final OWLClassExpression c) {
    if (c.equals(df.getOWLThing()))
      return true;
    if (c.equals(df.getOWLNothing()))
      return false;
    if (c instanceof OWLClass)
      return concepts.get(i, type, (OWLClass) c);
    if (c instanceof OWLObjectSomeValuesFrom)
      return roles.cut(i, (OWLObjectProperty) ((OWLObjectSomeValuesFrom) c).getProperty()).anyMatch(
          j -> isInstanceOf(j, ((OWLObjectSomeValuesFrom) c).getFiller()));
    if (c instanceof OWLObjectIntersectionOf)
      return ((OWLObjectIntersectionOf) c).asConjunctSet().stream().allMatch(d -> isInstanceOf(i, d));
    return false;
  }

  public final boolean isInstanceOf(final String i, final String c) {
    return this.isInstanceOf(df.getOWLNamedIndividual(IRI.create(i)), df.getOWLClass(IRI.create(c)));
  }

  public static enum Constructor {
    CONJUNCTION,
    EXISTENTIAL_RESTRICTION,
    VALUE_RESTRICTION,
    QUALIFIED_AT_LEAST_RESTRICTION,
    QUALIFIED_AT_MOST_RESTRICTION;
  }

  public static enum DescriptionLogic {
    L0(Constructor.CONJUNCTION),
    EL(Constructor.CONJUNCTION, Constructor.EXISTENTIAL_RESTRICTION),
    FL0(Constructor.CONJUNCTION, Constructor.VALUE_RESTRICTION),
    FLE(Constructor.CONJUNCTION, Constructor.EXISTENTIAL_RESTRICTION, Constructor.VALUE_RESTRICTION),
    FLG(
        Constructor.CONJUNCTION,
        Constructor.EXISTENTIAL_RESTRICTION,
        Constructor.VALUE_RESTRICTION,
        Constructor.QUALIFIED_AT_LEAST_RESTRICTION),
    FLQ(
        Constructor.CONJUNCTION,
        Constructor.EXISTENTIAL_RESTRICTION,
        Constructor.VALUE_RESTRICTION,
        Constructor.QUALIFIED_AT_LEAST_RESTRICTION,
        Constructor.QUALIFIED_AT_MOST_RESTRICTION);

    public final Constructor[] constructors;

    DescriptionLogic(final Constructor... constructors) {
      this.constructors = constructors;
    }
  }

  public final OWLClassExpression getMMSC(
      final Set<OWLNamedIndividual> individuals,
      final int roleDepth,
      final DescriptionLogic dl) {
    final Set<OWLClassExpression> conjuncts = new HashSet<OWLClassExpression>();
    concepts.cut(individuals.stream(), type).forEach(new Consumer<OWLClass>() {

      @Override
      public void accept(OWLClass conceptName) {
        conjuncts.add(conceptName);
      }
    });
    if (roleDepth > 0)
      for (OWLObjectProperty role : roles.getMs())
        for (Constructor constructor : dl.constructors)
          for (Set<OWLNamedIndividual> successor : minimalSuccessorSets(individuals, role, constructor))
            switch (constructor) {
            case EXISTENTIAL_RESTRICTION:
              conjuncts.add(df.getOWLObjectSomeValuesFrom(role, getMMSC(successor, roleDepth - 1, dl)));
              break;
            case VALUE_RESTRICTION:
              conjuncts.add(df.getOWLObjectAllValuesFrom(role, getMMSC(successor, roleDepth - 1, dl)));
              break;
            case QUALIFIED_AT_LEAST_RESTRICTION:
//              conjuncts.add(df.getOWLObjectMinCardinality(cardinality, role, getMMSC(successor, roleDepth - 1, dl)));
              break;
            case QUALIFIED_AT_MOST_RESTRICTION:
//              conjuncts.add(df.getOWLObjectMaxCardinality(cardinality, role, getMMSC(successor, roleDepth - 1, dl)));
              break;
            case CONJUNCTION:
            default:
            }
    if (conjuncts.size() > 1)
      return new OWLObjectIntersectionOfImpl(conjuncts);
    if (conjuncts.size() == 1)
      return conjuncts.iterator().next();
    return df.getOWLThing();
  }

  public final OWLClassExpression
      getMMSC_(final Set<String> individuals, final int roleDepth, final DescriptionLogic dl) {
    return getMMSC(
        individuals.stream().map(i -> df.getOWLNamedIndividual(IRI.create(i))).collect(Collectors.toSet()),
        roleDepth,
        dl);
  }

  private final Set<Set<OWLNamedIndividual>> minimalSuccessorSets(
      Set<OWLNamedIndividual> individuals,
      OWLObjectProperty role,
      Constructor constructor) {
    switch (constructor) {
    case EXISTENTIAL_RESTRICTION:
      final Set<OWLNamedIndividual> successors0 = new HashSet<OWLNamedIndividual>();
      for (OWLNamedIndividual individual : individuals)
        successors0.addAll(roles.cut(individual, role).collect(Collectors.toSet()));
      // succerssors0 contains all r-successors of individuals in individuals
//      final Set<Set<OWLNamedIndividual>> minSets =
//          Sets
//              .powerSet(successors0)
//              .stream()
//              .filter(
//                  set -> set.stream().allMatch(
//                      individual -> roles.row(role, individual).filter(individuals::contains).findFirst().isPresent()))
//              .collect(Collectors.toSet());
      final Set<Set<OWLNamedIndividual>> minSets = Sets.newHashSet(Sets.powerSet(successors0));
      final Set<Set<OWLNamedIndividual>> t = new HashSet<Set<OWLNamedIndividual>>();
      for (Set<OWLNamedIndividual> set : minSets) {
        for (OWLNamedIndividual individual : individuals)
          if (roles.cut(individual, role).noneMatch(set::contains))
            t.add(set);
      }
      minSets.removeAll(t);
      // minSets should here contain all subsets of successors0, that contain at least one r-successor
      // for every individual in individuals
//      final Set<Set<OWLNamedIndividual>> minSets = Sets.newHashSet(Sets.powerSet(successors0));
      final Set<Set<OWLNamedIndividual>> nonMinSets = new HashSet<Set<OWLNamedIndividual>>();
      for (Set<OWLNamedIndividual> x : minSets)
        for (Set<OWLNamedIndividual> y : minSets)
          if (!x.equals(y) && x.containsAll(y)) {
            nonMinSets.add(x);
            break;
          }
      minSets.removeAll(nonMinSets);
      return minSets;
    case VALUE_RESTRICTION:
      final Set<OWLNamedIndividual> successors = new HashSet<OWLNamedIndividual>();
      for (OWLNamedIndividual individual : individuals)
        successors.addAll(roles.cut(individual, role).collect(Collectors.toSet()));
      return Collections.singleton(successors);
    case QUALIFIED_AT_LEAST_RESTRICTION:
      return Collections.emptySet();
    case QUALIFIED_AT_MOST_RESTRICTION:
      return Collections.emptySet();
    case CONJUNCTION:
    default:
      return Collections.emptySet();
    }
  }

  public final MatrixContext<OWLNamedIndividual, OWLClassExpression> toLogicalContext(
      final int roleDepth,
      final DescriptionLogic dl) {
    MatrixContext<OWLNamedIndividual, OWLClassExpression> cxt =
        new MatrixContext<OWLNamedIndividual, OWLClassExpression>(false);
    cxt.rowHeads().addAll(concepts.getGs());
    cxt.colHeads().addAll(createMMSCConjuncts(roleDepth, dl));
    for (OWLNamedIndividual i : cxt.rowHeads())
      for (OWLClassExpression c : cxt.colHeads())
        if (isInstanceOf(i, c))
          cxt.addFast(i, c);
    return cxt;
  }

  private final List<OWLClassExpression> createMMSCConjuncts(final int roleDepth, final DescriptionLogic dl) {
    final List<OWLClassExpression> mmscConjuncts =
        new ArrayList<OWLClassExpression>();
//            (int) (1 + concepts.getWs().size() + roles.getMs().size()
//            * Math.pow(2, concepts.getGs().size())));
    mmscConjuncts.add(df.getOWLNothing());
    mmscConjuncts.addAll(concepts.getWs());
    if (roleDepth > 0) {
      for (Set<OWLNamedIndividual> s : Sets.powerSet(concepts.getGs()))
        if (!s.isEmpty()) {
          final OWLClassExpression mmsc = getMMSC(s, roleDepth - 1, dl);
          for (OWLObjectProperty role : roles.getMs()) {
            for (Constructor constructor : dl.constructors)
              switch (constructor) {
              case EXISTENTIAL_RESTRICTION:
                mmscConjuncts.add(df.getOWLObjectSomeValuesFrom(role, mmsc));
                break;
              case VALUE_RESTRICTION:
                mmscConjuncts.add(df.getOWLObjectAllValuesFrom(role, mmsc));
                break;
              case QUALIFIED_AT_LEAST_RESTRICTION:
              case QUALIFIED_AT_MOST_RESTRICTION:
              case CONJUNCTION:
              default:
              }
          }
        }
    }
    return mmscConjuncts;
  }
}
