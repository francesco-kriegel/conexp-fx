package conexp.fx.core.algorithm.nextclosures;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import conexp.fx.core.algorithm.nextclosure.NextClosure;
import conexp.fx.core.algorithm.nextclosure.exploration.AttributeExploration;
import conexp.fx.core.algorithm.nextclosures.NextClosures.Result;
import conexp.fx.core.collections.either.Either;
import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.implication.Implication;
import conexp.fx.core.importer.CXTImporter;

public class Test {

  public static void main(String[] args) throws IOException {
    final Path contexts = Paths.get("/", "Volumes", "francesco", "Data", "Contexts");
    Files
        .list(contexts.resolve("random"))
        .filter(p -> p.toFile().getName().endsWith(".cxt"))
        .filter(
            p -> Integer.valueOf(
                p.toFile().getName().substring(
                    p.toFile().getName().indexOf("a") + 1,
                    p.toFile().getName().indexOf("d"))) < 30)
        .sequential()
        .forEach(Test::runTest);
    Files
        .list(contexts.resolve("testing-data"))
        .filter(p -> Sets.newHashSet("24.cxt", "35.cxt", "51.cxt", "54.cxt", "79.cxt").contains(p.toFile().getName()))
        .sequential()
        .forEach(Test::runTest);
    Files.list(contexts.resolve("big")).filter(p -> p.toFile().getName().endsWith("car.cxt")).sequential().forEach(
        Test::runTest);
  }

  protected static void runTest(Path path) {
    System.out.println(path);
    final MatrixContext<String, String> cxt = CXTImporter.read(path.toFile());
    final long s2 = System.currentTimeMillis();
    final conexp.fx.core.algorithm.nextclosures.NextClosures2.Result<String, String> r2 = NextClosures2.compute(cxt);
    final long t2 = System.currentTimeMillis() - s2;
    final long s1 = System.currentTimeMillis();
    final Result<String, String> r1 = NextClosures.compute(cxt, false);
    final long t1 = System.currentTimeMillis() - s1;
    final long s0 = System.currentTimeMillis();
    final Set<Implication<String, String>> canonicalBase = AttributeExploration.getCanonicalBase(cxt);
    final long t0 = System.currentTimeMillis() - s0;

    final long s4 = System.currentTimeMillis();
    final Pair<Set<Concept<String, String>>, Set<Implication<String, String>>> conceptsAndImplications =
        NextClosure.conceptsAndImplications(cxt);
    final long t4 = System.currentTimeMillis() - s4;

    final long s5 = System.currentTimeMillis();
    int k = 0;
    for (final Iterator<Either<Concept<String, String>, Implication<String, String>>> it =
        NextClosure.implications(cxt).iterator(); it.hasNext(); k++)
      it.next();
    final long t5 = System.currentTimeMillis() - s5;

    System.out.println(
        t5 + " : " + t4 + " : " + t0 + " : " + t1 + " : " + t2 + " : "
            + (eqq(canonicalBase, r2.implications)
                && k == conceptsAndImplications.first().size() + conceptsAndImplications.second().size()
                && eqq(canonicalBase, conceptsAndImplications.second())
                && r1.concepts.equals(conceptsAndImplications.first()) && r1.concepts.equals(r2.concepts)
                && eq(r1.implications, r2.implications)));
  }

  private static boolean eqq(Set<Implication<String, String>> is, Set<Implication<String, String>> js) {
    return is.size() == js.size() && sub(is, js);
  }

  protected static boolean sub(Set<Implication<String, String>> is, Set<Implication<String, String>> js) {
    return is.parallelStream().allMatch(
        i -> js.parallelStream().anyMatch(
            j -> i.getPremise().equals(j.getPremise()) && i.getConclusion().equals(j.getConclusion())));
  }

  private static boolean eq(Map<Set<String>, Set<String>> is, Set<Implication<String, String>> js) {
    if (is.size() != js.size())
      return false;
    return js.parallelStream().allMatch(j -> is.get(j.getPremise()).equals(j.getConclusion()));
  }

}
