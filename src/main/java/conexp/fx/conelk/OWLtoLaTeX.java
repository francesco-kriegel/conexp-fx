package conexp.fx.conelk;

import java.io.File;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
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
      r = r.substring(0, r.length() - 1);
      final OWLClassExpression c = exrest.getFiller();
      str.append("\\exists ");
      str.append(r + ".");
      str.append(toLaTeX(c));
      str.append(")");
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

  public static final MatrixContext<String, String> toLaTeXContext(
      final MatrixContext<OWLNamedIndividual, OWLClassExpression> context) {
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
    CXTExporter.<String, String> export(toLaTeXContext(context), outputFile);
  }

}
