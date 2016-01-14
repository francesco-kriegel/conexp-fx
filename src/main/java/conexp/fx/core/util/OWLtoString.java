package conexp.fx.core.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Iterator;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

public class OWLtoString {

  public static final String toString(final OWLClassExpression classExpression) {
    if (classExpression.isOWLNothing()) {
      return UnicodeSymbols.BOT;
    }
    if (classExpression.isOWLThing()) {
      return UnicodeSymbols.TOP;
    }
    final StringBuilder s = new StringBuilder();
    if (classExpression instanceof OWLClass) {
      final OWLClass c = (OWLClass) classExpression;
      s.append(c.toString().substring(
          1,
          c.toString().length() - 1));
    } else if (classExpression instanceof OWLObjectComplementOf) {
      final OWLObjectComplementOf c = (OWLObjectComplementOf) classExpression;
      s.append(UnicodeSymbols.NEG);
      s.append(toString(c.getOperand()));
    } else if (classExpression instanceof OWLObjectIntersectionOf) {
      final OWLObjectIntersectionOf c = (OWLObjectIntersectionOf) classExpression;
      if (c.getOperands().isEmpty())
        return UnicodeSymbols.TOP;
      if (c.getOperands().size() == 1)
        return toString(c.getOperands().iterator().next());
      s.append("(");
      final Iterator<OWLClassExpression> i = c.getOperands().iterator();
      s.append(toString(i.next()));
      while (i.hasNext())
        s.append(UnicodeSymbols.SQCAP + toString(i.next()));
      s.append(")");
    } else if (classExpression instanceof OWLObjectUnionOf) {
      final OWLObjectUnionOf c = (OWLObjectUnionOf) classExpression;
      if (c.getOperands().isEmpty())
        return UnicodeSymbols.BOT;
      if (c.getOperands().size() == 1)
        return toString(c.getOperands().iterator().next());
      s.append("(");
      final Iterator<OWLClassExpression> i = c.getOperands().iterator();
      s.append(toString(i.next()));
      while (i.hasNext())
        s.append(UnicodeSymbols.SQCUP + toString(i.next()));
      s.append(")");
    } else if (classExpression instanceof OWLObjectSomeValuesFrom) {
      final OWLObjectSomeValuesFrom c = (OWLObjectSomeValuesFrom) classExpression;
      s.append(UnicodeSymbols.EXISTS);
      s.append(toString(c.getProperty()));
      s.append(".");
      s.append(toString(c.getFiller()));
    } else if (classExpression instanceof OWLObjectAllValuesFrom) {
      final OWLObjectAllValuesFrom c = (OWLObjectAllValuesFrom) classExpression;
      s.append(UnicodeSymbols.FORALL);
      s.append(toString(c.getProperty()));
      s.append(".");
      s.append(toString(c.getFiller()));
    } else if (classExpression instanceof OWLObjectHasSelf) {
      final OWLObjectHasSelf c = (OWLObjectHasSelf) classExpression;
      s.append(UnicodeSymbols.EXISTS);
      s.append(toString(c.getProperty()));
      s.append(".");
      s.append("Self");
    } else if (classExpression instanceof OWLObjectMinCardinality) {
      final OWLObjectMinCardinality c = (OWLObjectMinCardinality) classExpression;
      s.append(UnicodeSymbols.GEQ);
      s.append(c.getCardinality());
      s.append(".");
      s.append(toString(c.getProperty()));
      s.append(".");
      s.append(toString(c.getFiller()));
    } else if (classExpression instanceof OWLObjectMaxCardinality) {
      final OWLObjectMaxCardinality c = (OWLObjectMaxCardinality) classExpression;
      s.append(UnicodeSymbols.LEQ);
      s.append(c.getCardinality());
      s.append(".");
      s.append(toString(c.getProperty()));
      s.append(".");
      s.append(toString(c.getFiller()));
    } else if (classExpression instanceof OWLObjectExactCardinality) {
      final OWLObjectExactCardinality c = (OWLObjectExactCardinality) classExpression;
      s.append("=");
      s.append(c.getCardinality());
      s.append(".");
      s.append(toString(c.getProperty()));
      s.append(".");
      s.append(toString(c.getFiller()));
    } else {
      s.append(classExpression.toString());
    }
    return s.toString();
  }

  public static final String toString(final OWLObjectPropertyExpression propertyExpression) {
    return propertyExpression.toString().substring(
        1,
        propertyExpression.toString().length() - 1);
  }

  public static final String toString(final OWLAxiom axiom) {
    final StringBuilder s = new StringBuilder();
    if (axiom instanceof OWLSubClassOfAxiom) {
      final OWLSubClassOfAxiom a = (OWLSubClassOfAxiom) axiom;
      s.append(toString(a.getSubClass()));
      s.append(UnicodeSymbols.SQSUBSETEQ);
      s.append(toString(a.getSuperClass()));
    } else if (axiom instanceof OWLEquivalentClassesAxiom) {
      final OWLEquivalentClassesAxiom a = (OWLEquivalentClassesAxiom) axiom;
      final Iterator<OWLClassExpression> i = a.getClassExpressions().iterator();
      if (i.hasNext()) {
        s.append(toString(i.next()));
        while (i.hasNext())
          s.append(UnicodeSymbols.EQUIV + toString(i.next()));
      }
    } else if (axiom instanceof OWLSubObjectPropertyOfAxiom) {
      final OWLSubObjectPropertyOfAxiom a = (OWLSubObjectPropertyOfAxiom) axiom;
      s.append(toString(a.getSubProperty()));
      s.append(UnicodeSymbols.SQSUBSETEQ);
      s.append(toString(a.getSuperProperty()));
    } else if (axiom instanceof OWLSubPropertyChainOfAxiom) {
      final OWLSubPropertyChainOfAxiom a = (OWLSubPropertyChainOfAxiom) axiom;
      final Iterator<OWLObjectPropertyExpression> i = a.getPropertyChain().iterator();
      if (i.hasNext())
        s.append(toString(i.next()));
      while (i.hasNext()) {
        s.append(UnicodeSymbols.CIRC);
        s.append(toString(i.next()));
      }
      s.append(UnicodeSymbols.SQSUBSETEQ);
      s.append(toString(a.getSuperProperty()));
    } else {
      s.append(axiom.toString());
    }
    return s.toString();
  }
}
