package conexp.fx.core.dl;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import com.google.common.collect.Sets;

import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.util.UnicodeSymbols;

public class ELConceptDescription {

  private static final OWLDataFactory df = OWLManager.getOWLDataFactory();

  public static final ELConceptDescription of(final OWLClassExpression concept) {
    return new ELConceptDescription(concept);
  }

  public static final ELConceptDescription bot() {
    return new ELConceptDescription(Sets.newHashSet(df.getOWLNothing().getIRI()), Sets.newHashSet());
  }

  public static final ELConceptDescription top() {
    return new ELConceptDescription();
  }

  public static final ELConceptDescription conceptName(final IRI conceptName) {
    return new ELConceptDescription(Sets.newHashSet(conceptName), Sets.newHashSet());
  }

  public static final ELConceptDescription existentialRestriction(
      final Pair<IRI, ELConceptDescription> existentialRestriction) {
    return new ELConceptDescription(Sets.newHashSet(), Sets.newHashSet(existentialRestriction));
  }

  public static final ELConceptDescription conjunction(final ELConceptDescription... conjuncts) {
    return conjunction(Arrays.asList(conjuncts));
  }

  public static final ELConceptDescription conjunction(final Collection<ELConceptDescription> conjuncts) {
    final ELConceptDescription conjunction = new ELConceptDescription();
    conjuncts.forEach(conjunct -> {
      conjunction.getConceptNames().addAll(conjunct.getConceptNames());
      conjunction.getExistentialRestrictions().addAll(conjunct.getExistentialRestrictions());
    });
    return conjunction;
  }

  private final Set<IRI>                                            conceptNames;
  private final Set<Pair<IRI, ELConceptDescription>>                existentialRestrictions;
//  private final Set<Pair<IRI, ELConceptDescription>>                valueRestrictions;
//  private final Set<Pair<Pair<Integer, IRI>, ELConceptDescription>> qualifiedGreaterThanRestrictions;
//  private final Set<Pair<Pair<Integer, IRI>, ELConceptDescription>> qualifiedSmallerThanRestrictions;
//  private final Set<IRI>                                            negatedConceptNames;
//  private final Set<IRI>                                            extistentialSelfRestrictions;

  /**
   * @param concept
   * 
   *          Creates a new EL normal form from an OWLClassExpression.
   */
  public ELConceptDescription(final OWLClassExpression concept) {
    super();
    this.conceptNames = new HashSet<IRI>();
    this.existentialRestrictions = new HashSet<Pair<IRI, ELConceptDescription>>();
    if (concept.isOWLThing())
      return;
    if (concept.isOWLNothing()) {
      conceptNames.add(df.getOWLNothing().getIRI());
      return;
    }
    if (concept instanceof OWLClass) {
      this.conceptNames.add(((OWLClass) concept).getIRI());
      return;
    }
    if (concept instanceof OWLObjectSomeValuesFrom) {
      final OWLObjectSomeValuesFrom existentialRestriction = (OWLObjectSomeValuesFrom) concept;
      this.existentialRestrictions.add(new Pair<IRI, ELConceptDescription>(((OWLObjectProperty) existentialRestriction
          .getProperty()).getIRI(), new ELConceptDescription(existentialRestriction.getFiller())));
      return;
    }
    if (concept instanceof OWLObjectIntersectionOf) {
      final OWLObjectIntersectionOf conjunction = (OWLObjectIntersectionOf) concept;
      for (OWLClassExpression conjunct : conjunction.asConjunctSet())
        if (conjunct instanceof OWLClass)
          this.conceptNames.add(((OWLClass) conjunct).getIRI());
        else if (conjunct instanceof OWLObjectSomeValuesFrom)
          this.existentialRestrictions.add(new Pair<IRI, ELConceptDescription>(
              ((OWLObjectProperty) ((OWLObjectSomeValuesFrom) conjunct).getProperty()).getIRI(),
              new ELConceptDescription(((OWLObjectSomeValuesFrom) conjunct).getFiller())));
        else
          throw new ELSyntaxException();
      return;
    }
    throw new ELSyntaxException();
  }

  /**
   * @param conceptNames
   * @param existentialRestrictions
   * 
   *          Creates a new EL normal form. If the sets conceptNames and existentitalRestrictions are both empty, then
   *          the constructed normal form represents the top concept.
   */
  public ELConceptDescription(
      final Set<IRI> conceptNames,
      final Set<Pair<IRI, ELConceptDescription>> existentialRestrictions) {
    super();
    this.conceptNames = conceptNames;
    this.existentialRestrictions = existentialRestrictions;
  }

  /**
   * Creates a new EL normal form, that represents the bottom concept.
   */
  public ELConceptDescription() {
    this.conceptNames = Sets.newHashSet();
    this.existentialRestrictions = Sets.newHashSet();
  }

  public final boolean isBot() {
    return conceptNames.contains(df.getOWLNothing().getIRI());
  }

  public final boolean isTop() {
    return conceptNames.isEmpty() && existentialRestrictions.isEmpty();
  }

  public final Set<IRI> getConceptNames() {
    return conceptNames;
  }

  public final Set<Pair<IRI, ELConceptDescription>> getExistentialRestrictions() {
    return existentialRestrictions;
  }

  public final OWLClassExpression toOWLClassExpression() {
    if (isTop())
      return df.getOWLThing();
    if (isBot())
      return df.getOWLNothing();
    if (conceptNames.size() == 1 && existentialRestrictions.isEmpty())
      return df.getOWLClass(conceptNames.iterator().next());
    if (conceptNames.isEmpty() && existentialRestrictions.size() == 1) {
      final Pair<IRI, ELConceptDescription> existentialRestriction = existentialRestrictions.iterator().next();
      return df.getOWLObjectSomeValuesFrom(df.getOWLObjectProperty(existentialRestriction.x()), existentialRestriction
          .y()
          .toOWLClassExpression());
    }
    final Set<OWLClassExpression> conjuncts = new HashSet<OWLClassExpression>();
    for (IRI conceptName : conceptNames)
      conjuncts.add(df.getOWLClass(conceptName));
    for (Pair<IRI, ELConceptDescription> existentialRestriction : existentialRestrictions)
      conjuncts.add(df.getOWLObjectSomeValuesFrom(
          df.getOWLObjectProperty(existentialRestriction.x()),
          existentialRestriction.y().toOWLClassExpression()));
    return df.getOWLObjectIntersectionOf(conjuncts);
  }

  public final ELConceptDescription minimize() {
    if (conceptNames.contains(df.getOWLNothing().getIRI())) {
      conceptNames.retainAll(Collections.singleton(df.getOWLNothing().getIRI()));
      existentialRestrictions.clear();
    }
    for (Pair<IRI, ELConceptDescription> existentialRestriction : existentialRestrictions)
      existentialRestriction.y().minimize();
    final Set<Pair<IRI, ELConceptDescription>> toRemove = new HashSet<Pair<IRI, ELConceptDescription>>();
    for (Pair<IRI, ELConceptDescription> existentialRestriction1 : existentialRestrictions)
      for (Pair<IRI, ELConceptDescription> existentialRestriction2 : existentialRestrictions)
        if (!existentialRestriction1.equals(existentialRestriction2)
            && existentialRestriction1.x().equals(existentialRestriction2.x()))
          if (existentialRestriction1.y().isSubsumedBy(existentialRestriction2.y()))
            toRemove.add(existentialRestriction1);
    existentialRestrictions.removeAll(toRemove);
    return this;
  }

  public final boolean isSubsumedBy(final ELConceptDescription other) {
    return ELReasoner.isSubsumedBy(this, other);
  }

  public final boolean subsumes(final ELConceptDescription other) {
    return ELReasoner.subsumes(this, other);
  }

  @Override
  public String toString() {
    if (isBot())
      return UnicodeSymbols.BOT;
    if (isTop())
      return UnicodeSymbols.TOP;
    final StringBuilder sb = new StringBuilder();
    final Iterator<IRI> it1 = conceptNames.iterator();
    if (it1.hasNext()) {
      sb.append(it1.next().toString());
    }
    while (it1.hasNext()) {
      sb.append(" ");
      sb.append(UnicodeSymbols.SQCAP);
      sb.append(" ");
      sb.append(it1.next().toString());
    }
    if (!conceptNames.isEmpty() && !existentialRestrictions.isEmpty()) {
      sb.append(" ");
      sb.append(UnicodeSymbols.SQCAP);
      sb.append(" ");
    }
    final Iterator<Pair<IRI, ELConceptDescription>> it2 = existentialRestrictions.iterator();
    if (it2.hasNext()) {
      final Pair<IRI, ELConceptDescription> existentialRestriction = it2.next();
      sb.append(UnicodeSymbols.EXISTS);
      sb.append(" ");
      sb.append(existentialRestriction.x().toString());
      sb.append(" ");
      sb.append(".");
      sb.append("(");
      sb.append(existentialRestriction.y().toString());
      sb.append(")");
    }
    while (it2.hasNext()) {
      sb.append(" ");
      sb.append(UnicodeSymbols.SQCAP);
      final Pair<IRI, ELConceptDescription> existentialRestriction = it2.next();
      sb.append(" ");
      sb.append(UnicodeSymbols.EXISTS);
      sb.append(" ");
      sb.append(existentialRestriction.x().toString());
      sb.append(" ");
      sb.append(".");
      sb.append("(");
      sb.append(existentialRestriction.y().toString());
      sb.append(")");
    }
    return sb.toString();
  }

  public String toLaTeXString() {
    if (this.isBot())
      return "\\bot";
    if (this.isTop())
      return "\\top";
    final StringBuilder sb = new StringBuilder();
    final Iterator<IRI> it1 = conceptNames.iterator();
    if (it1.hasNext()) {
      sb.append(it1.next().toString());
    }
    while (it1.hasNext()) {
      sb.append(" \\sqcap ");
      sb.append(it1.next().toString());
    }
    final Iterator<Pair<IRI, ELConceptDescription>> it2 = existentialRestrictions.iterator();
    if (conceptNames.isEmpty())
      sb.append(" \\sqcap ");
    if (it2.hasNext()) {
      final Pair<IRI, ELConceptDescription> existentialRestriction = it2.next();
      sb.append(" \\exists ");
      sb.append(existentialRestriction.x().toString());
      sb.append(" . ");
      sb.append(" \\left( ");
      sb.append(existentialRestriction.y().toLaTeXString());
      sb.append(" \\right) ");
    }
    while (it2.hasNext()) {
      final Pair<IRI, ELConceptDescription> existentialRestriction = it2.next();
      sb.append(" \\sqcap ");
      sb.append(" \\exists ");
      sb.append(existentialRestriction.x().toString());
      sb.append(" . ");
      sb.append(" \\left( ");
      sb.append(existentialRestriction.y().toLaTeXString());
      sb.append(" \\right) ");

    }
    return sb.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof ELConceptDescription))
      return false;
    final ELConceptDescription other = (ELConceptDescription) obj;
    return this.conceptNames.equals(other.conceptNames)
        && this.existentialRestrictions.equals(other.existentialRestrictions);
  }

  @Override
  public int hashCode() {
    return 2 * conceptNames.hashCode() + 3 * existentialRestrictions.hashCode();
  }

  @Override
  public ELConceptDescription clone() {
    final ELConceptDescription clone = new ELConceptDescription();
    clone.getConceptNames().addAll(this.getConceptNames());
    clone.getExistentialRestrictions().addAll(this.getExistentialRestrictions());
    return clone;
  }

}
