package conexp.fx.core.util;

import java.io.File;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Iterator;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
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
import org.ujmp.core.booleanmatrix.BooleanMatrix;

import com.google.common.base.Function;

import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.collections.setlist.SetLists;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.exporter.CXTExporter;

public class OWLUtil {

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
      s.append(c.toString().substring(1, c.toString().length() - 1));
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
    return propertyExpression.toString().substring(1, propertyExpression.toString().length() - 1);
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

  public static final String toLaTeX(final OWLClassExpression clazz) {
    final StringBuilder str = new StringBuilder();
    if (clazz instanceof OWLObjectSomeValuesFrom) {
      str.append("(");
      OWLObjectSomeValuesFrom exrest = (OWLObjectSomeValuesFrom) clazz;
      String r = exrest.getProperty().toString();
      r = r.substring(r.indexOf("#") + 1);
      r = r.substring(0, r.length() - 1);
      final OWLClassExpression c = exrest.getFiller();
      str.append("\\exists ");
      str.append(r + ".");
      str.append(toLaTeX(c));
      str.append(")");
    } else if (clazz instanceof OWLObjectComplementOf) {
      str.append("\\neg(");
      str.append(toLaTeX(((OWLObjectComplementOf) clazz).getOperand()));
      str.append(")");
    } else if (clazz instanceof OWLObjectAllValuesFrom) {
      final OWLObjectAllValuesFrom vr = (OWLObjectAllValuesFrom) clazz;
      str.append("(\\forall ");
      str.append(vr.getProperty().toString() + ".");
      str.append(toLaTeX(vr.getFiller()) + ")");
    } else if (clazz instanceof OWLObjectMinCardinality) {
      final OWLObjectMinCardinality qgr = (OWLObjectMinCardinality) clazz;
      str.append("(\\geq ");
      str.append(qgr.getCardinality());
      str.append(" " + qgr.getProperty() + ".");
      str.append(toLaTeX(qgr.getFiller()) + ")");
    } else if (clazz instanceof OWLObjectMaxCardinality) {
      final OWLObjectMaxCardinality qlr = (OWLObjectMaxCardinality) clazz;
      str.append("(\\leq ");
      str.append(qlr.getCardinality());
      str.append(" " + qlr.getProperty() + ".");
      str.append(toLaTeX(qlr.getFiller()) + ")");
    } else if (clazz instanceof OWLObjectExactCardinality) {
      final OWLObjectExactCardinality qer = (OWLObjectExactCardinality) clazz;
      str.append("(= ");
      str.append(qer.getCardinality());
      str.append(" " + qer.getProperty() + ".");
      str.append(toLaTeX(qer.getFiller()) + ")");
    } else if (clazz instanceof OWLObjectHasSelf) {
      final OWLObjectHasSelf sr = (OWLObjectHasSelf) clazz;
      str.append("\\exists ");
      str.append(sr.getProperty() + ".");
      str.append("\\mathrm{Self}");
    } else if (clazz instanceof OWLObjectIntersectionOf) {
      str.append("(");
      OWLObjectIntersectionOf conj = (OWLObjectIntersectionOf) clazz;
      if (conj.asConjunctSet().size() == 1)
        str.append(toLaTeX(conj.asConjunctSet().iterator().next()));
      boolean first = true;
      for (OWLClassExpression c : conj.asConjunctSet()) {
        if (first)
          first = false;
        else
          str.append("\\cap");
        str.append(toLaTeX(c));
      }
      str.append(")");
    } else if (clazz instanceof OWLClass) {
      OWLClass c = (OWLClass) clazz;
      String string = c.toString();
      string = string.substring(string.indexOf("#") + 1);
      str.append(string.substring(0, string.length() - 1));
    }
    return str.toString();
  }

  public static final MatrixContext<String, String>
      toLaTeXContext(final MatrixContext<OWLNamedIndividual, OWLClassExpression> context) {
    final BooleanMatrix matrix = context.matrix();
    final SetList<String> domain = SetLists.transform(context.rowHeads(), new Function<OWLNamedIndividual, String>() {

      @Override
      public final String apply(final OWLNamedIndividual input) {
        String string = input.toString();
        string = string.substring(string.indexOf("#") + 1);
        return string.substring(0, string.length() - 1);
      }
    });
    final SetList<String> codomain = SetLists.transform(context.colHeads(), new Function<OWLClassExpression, String>() {

      @Override
      public final String apply(final OWLClassExpression input) {
        return OWLUtil.toLaTeX(input);
      }
    });
    final MatrixContext<String, String> latexContext =
        new MatrixContext<String, String>(domain, codomain, matrix, false);
    return latexContext;
  }

  public static final void
      exportLaTeXContext(final MatrixContext<OWLNamedIndividual, OWLClassExpression> context, final File outputFile) {
    CXTExporter.<String, String> export(toLaTeXContext(context), outputFile);
  }
}
