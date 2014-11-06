package conexp.fx.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import conexp.fx.core.builder.Request;
import conexp.fx.core.builder.Requests;
import conexp.fx.core.collections.ListIterators;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ImplicationSet;
import conexp.fx.core.context.MatrixContext;
import de.tudresden.inf.tcs.fcalib.Implication;

@SuppressWarnings("deprecation")
public final class TestContexts {

  @Test
  public final void testAvailability() {
    Assert.assertTrue(BASEDIR.exists() && BASEDIR.isDirectory());
    Assert.assertTrue(!small().isEmpty());
    Assert.assertTrue(!big().isEmpty());
    Assert.assertTrue(!scales().isEmpty());
    Assert.assertTrue(!danielsImplicationTest().data().isEmpty());
  }

  public static final ConceptTest<String, String> smallConceptTest() {
    return new ConceptTest<String, String>(small());
  }

  public static final ConceptTest<String, String> scalesConceptTest() {
    return new ConceptTest<String, String>(scales());
  }

  /**
   * 
   * generates a {@link ConceptTest} containing all ordinal scales of size {@code [from,to]}
   * 
   * @param from
   *          (lower bound for context size)
   * @param to
   *          (upper bound for context size)
   * @return {@link ConceptTest}
   */
  public static final ConceptTest<Integer, Integer> ordinalScaleConceptTest(final int from, final int to) {
    final ConceptTest<Integer, Integer> test = new ConceptTest<Integer, Integer>();
    for (int n = from; n < to; n++) {
      final MatrixContext<Integer, Integer> cxt = fromRequest(new Requests.Scale.OrdinalScaleFromInt(n));
      final Set<Concept<Integer, Integer>> concepts = new HashSet<Concept<Integer, Integer>>();
      final Set<Integer> codomain = Sets.newHashSet(cxt.colHeads());
      for (int i = 0; i < n; i++) {
        final Set<Integer> extent = Sets.newHashSet(ListIterators.integers(i + 1));
        final Set<Integer> intent =
            Sets.newHashSet(Sets.union(Sets.difference(codomain, extent), Collections.singleton(i)));
        concepts.add(new Concept<Integer, Integer>(extent, intent));
      }
      test.data().put(cxt, concepts);
    }
    return test;
  }

  /**
   * 
   * generates a {@link ConceptTest} containing all nominal scales of size {@code [from,to]}
   * 
   * @param from
   *          (lower bound for context size)
   * @param to
   *          (upper bound for context size)
   * @return {@link ConceptTest}
   */
  public static final ConceptTest<Integer, Integer> nominalScaleConceptTest(final int from, final int to) {
    final ConceptTest<Integer, Integer> test = new ConceptTest<Integer, Integer>();
    for (int n = from; n < to; n++) {
      final MatrixContext<Integer, Integer> cxt = fromRequest(new Requests.Scale.NominalScaleFromInt(n));
      final Set<Concept<Integer, Integer>> concepts = new HashSet<Concept<Integer, Integer>>();
      final HashSet<Integer> codomain = Sets.newHashSet(cxt.colHeads());
      concepts.add(new Concept<Integer, Integer>(Sets.<Integer> newHashSet(), codomain));
      for (int i = 0; i < n; i++)
        concepts.add(new Concept<Integer, Integer>(Sets.newHashSet(i), Sets.newHashSet(i)));
      concepts.add(new Concept<Integer, Integer>(codomain, Sets.<Integer> newHashSet()));
      test.data().put(cxt, concepts);
    }
    return test;
  }

  /**
   * 
   * generates a {@link ConceptTest} containing all contra-nominal scales with size in {@code [from,to]}
   * 
   * @param from
   *          (smallest size of constructed nominal scale)
   * @param to
   *          (largest size of constructed nominal scale)
   * @return {@link ConceptTest}
   */
  public static final ConceptTest<Integer, Integer> contraNominalScaleConceptTest(final int from, final int to) {
    final ConceptTest<Integer, Integer> test = new ConceptTest<Integer, Integer>();
    for (int n = from; n < to; n++) {
      final MatrixContext<Integer, Integer> cxt = fromRequest(new Requests.Scale.ContraNominalScaleFromInt(n));
      final Set<Concept<Integer, Integer>> concepts = new HashSet<Concept<Integer, Integer>>();
      final HashSet<Integer> codomain = Sets.newHashSet(cxt.colHeads());
      for (Set<Integer> intent : Sets.newHashSet(Sets.powerSet(codomain)))
        concepts.add(new Concept<Integer, Integer>(Sets.newHashSet(Sets.difference(codomain, intent)), intent));
      test.data().put(cxt, concepts);
    }
    return test;
  }

  public static final ImplicationTest<String, String> smallImplicationTest() {
    return new ImplicationTest<String, String>(small());
  }

  public static final ImplicationTest<String, String> scalesImplicationTest() {
    return new ImplicationTest<String, String>(scales());
  }

  /**
   * 
   * generates an {@link ImplicationTest} containing some random contexts and their implications (generated using
   * {@code conexp-clj} from Daniel Borchmann)
   * 
   * @return {@ImplicationTest}
   */
  public static final ImplicationTest<String, String> danielsImplicationTest() {
    final ImplicationTest<String, String> test = new ImplicationTest<String, String>();
    for (File file : new File(BASEDIR, "testing-data").listFiles(CXT_FILTER))
      test.data().put(fromFile(file), parseDanielsImplicationSet(file));
    return test;
  }

  /**
   * 
   * generates an {@link ImplicationTest} containing all ordinal scales with size {@code [from,to]}
   * 
   * @param from
   *          (lower bound for context size)
   * @param to
   *          (upper bound for context size)
   * @return {@link ImplicationTest}
   */
  public static final ImplicationTest<Integer, Integer> ordinalScaleImplicationTest(final int from, final int to) {
    final ImplicationTest<Integer, Integer> test = new ImplicationTest<Integer, Integer>();
    for (int n = from; n < to; n++) {
      final MatrixContext<Integer, Integer> cxt = fromRequest(new Requests.Scale.OrdinalScaleFromInt(n));
      final ImplicationSet<Integer> implications = new ImplicationSet<Integer>();
      for (int i = 0; i < n; i++) {
        final Set<Integer> premise = new HashSet<Integer>();
        final Set<Integer> conclusion = new HashSet<Integer>();
        premise.add(i);
        for (int j = i + 1; j < n; j++)
          conclusion.add(j);
        implications.add(new Implication<Integer>(premise, conclusion));
      }
      test.data().put(cxt, implications);
    }
    return test;
  }

  /**
   * 
   * generates an {@link ImplicationTest} containing all nominal scales of size {@code [from,to]}
   * 
   * @param from
   *          (lower bound for context size)
   * @param to
   *          (upper bound for context size)
   * @return {@link ImplicationTest}
   */
  public static final ImplicationTest<Integer, Integer> nominalScaleImplicationTest(final int from, final int to) {
    final ImplicationTest<Integer, Integer> test = new ImplicationTest<Integer, Integer>();
    for (int n = from; n < to; n++) {
      final MatrixContext<Integer, Integer> cxt = fromRequest(new Requests.Scale.NominalScaleFromInt(n));
      final ImplicationSet<Integer> implications = new ImplicationSet<Integer>();
      for (int i = 0; i < n; i++)
        for (int j = 0; j < i; j++)
          implications.add(new Implication<Integer>(Sets.newHashSet(i, j), Sets.newHashSet(cxt.colHeads())));
      test.data().put(cxt, implications);
    }
    return test;
  }

  /**
   * 
   * generates an {@link ImplicationTest} containing all contra-nominal scales with size in {@code [from,to]}
   * 
   * @param from
   *          (smallest size of constructed nominal scale)
   * @param to
   *          (largest size of constructed nominal scale)
   * @return {@link ImplicationTest}
   */
  public static final ImplicationTest<Integer, Integer> contraNominalScaleImplicationTest(final int from, final int to) {
    final ImplicationTest<Integer, Integer> test = new ImplicationTest<Integer, Integer>();
    for (int n = from; n < to; n++)
      test.data().put(fromRequest(new Requests.Scale.ContraNominalScaleFromInt(n)), new ImplicationSet<Integer>());
    return test;
  }

  private static final File                                          BASEDIR         = new File("../contexts");
  private static final FilenameFilter                                CXT_FILTER      = new FilenameFilter() {

                                                                                       @Override
                                                                                       public final boolean accept(
                                                                                           final File dir,
                                                                                           final String name) {
                                                                                         return name.endsWith(".cxt");
                                                                                       }
                                                                                     };
  private static final Function<File, MatrixContext<String, String>> IMPORT_FUNCTION =
                                                                                         new Function<File, MatrixContext<String, String>>() {

                                                                                           @Override
                                                                                           public final
                                                                                               MatrixContext<String, String>
                                                                                               apply(final File file) {
                                                                                             return fromFile(file);
                                                                                           }
                                                                                         };

  public static final <G, M> MatrixContext<G, M> fromRequest(final Request<G, M> request) {
    final MatrixContext<G, M> context = request.createContext();
    request.setContent();
    return context;
  }

  public static final MatrixContext<String, String> fromFile(final File file) {
    return fromRequest(new Requests.Import.ImportCXT(file));
  }

  private static final ImplicationSet<String> parseDanielsImplicationSet(final File file) {
    final ImplicationSet<String> impls = new ImplicationSet<String>();
    try {
      final BufferedReader reader =
          new BufferedReader(new FileReader(new File(file.getParent(), file.getName() + "-implications")));
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        line = line.replace("((", "");
        line = line.replace("))", "");
        final String[] split = line.split("\\) \\(");
        for (String imp : split) {
          final Implication<String> im = new Implication<String>();
          imp = imp.replace("#", "");
          imp = imp.replace("{", "");
          imp = imp.replace("}", "");
          imp = imp.replace("\"", "");
          final String[] spl = imp.split("⟶ ");
          final String premise = spl[0];
          final String conclusion = spl[1];
          for (String x : premise.split(" "))
            im.getPremise().add(x);
          for (String y : conclusion.split(" "))
            im.getConclusion().add(y);
          impls.add(im);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return impls;
  }

  /**
   * @return a {@link List} of small {@link MatrixContext}s read from example directory
   */
  private static final List<MatrixContext<String, String>> small() {
    return Lists.transform(Arrays.asList(new File(BASEDIR, "small").listFiles(CXT_FILTER)), IMPORT_FUNCTION);
  }

  /**
   * @return a {@link List} of big {@link MatrixContext}s read from example directory
   */
  private static final List<MatrixContext<String, String>> big() {
    return Lists.transform(Arrays.asList(new File(BASEDIR, "big").listFiles(CXT_FILTER)), IMPORT_FUNCTION);
  }

  /**
   * @return a {@link List} of scales as {@link MatrixContext}s read from example directory
   */
  private static final List<MatrixContext<String, String>> scales() {
    return Lists.transform(Arrays.asList(new File(BASEDIR, "scales").listFiles(CXT_FILTER)), IMPORT_FUNCTION);
  }
}
