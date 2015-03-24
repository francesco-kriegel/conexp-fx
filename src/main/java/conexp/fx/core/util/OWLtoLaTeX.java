package conexp.fx.core.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.ujmp.core.booleanmatrix.BooleanMatrix;

import com.google.common.base.Function;

import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.collections.setlist.SetLists;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.exporter.CXTExporter;

public class OWLtoLaTeX {

  public static final String toLaTeX(final OWLClassExpression clazz) {
    final StringBuilder str = new StringBuilder();
    if (clazz instanceof OWLObjectSomeValuesFrom) {
      str.append("(");
      OWLObjectSomeValuesFrom exrest = (OWLObjectSomeValuesFrom) clazz;
      String r = exrest.getProperty().toString();
      r = r.substring(r.indexOf("#") + 1);
      r = r.substring(
          0,
          r.length() - 1);
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
      str.append(string.substring(
          0,
          string.length() - 1));
    }
    return str.toString();
  }

  public static final MatrixContext<String, String> toLaTeXContext(
      final MatrixContext<OWLNamedIndividual, OWLClassExpression> context) {
    final BooleanMatrix matrix = context.matrix();
    final SetList<String> domain = SetLists.transform(
        context.rowHeads(),
        new Function<OWLNamedIndividual, String>() {

          @Override
          public final String apply(final OWLNamedIndividual input) {
            String string = input.toString();
            string = string.substring(string.indexOf("#") + 1);
            return string.substring(
                0,
                string.length() - 1);
          }
        });
    final SetList<String> codomain = SetLists.transform(
        context.colHeads(),
        new Function<OWLClassExpression, String>() {

          @Override
          public final String apply(final OWLClassExpression input) {
            return OWLtoLaTeX.toLaTeX(input);
          }
        });
    final MatrixContext<String, String> latexContext =
        new MatrixContext<String, String>(domain, codomain, matrix, false);
    return latexContext;
  }

  public static final void exportLaTeXContext(
      final MatrixContext<OWLNamedIndividual, OWLClassExpression> context,
      final File outputFile) {
    CXTExporter.<String, String> export(
        toLaTeXContext(context),
        outputFile);
  }

}
