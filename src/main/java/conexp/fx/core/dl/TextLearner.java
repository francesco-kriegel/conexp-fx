package conexp.fx.core.dl;

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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.util.IterableFile;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

public class TextLearner {

  public static final void main(String[] args) throws OWLOntologyCreationException {
    final TextLearner tl = new TextLearner(new File("texts"));
    tl.getSignature().getConceptNames().add(IRI.create("mammal"));
    tl.getSignature().getConceptNames().add(IRI.create("reptile"));
    tl.getSignature().getConceptNames().add(IRI.create("fish"));
    tl.getSignature().getConceptNames().add(IRI.create("viviparous"));
    tl.getSignature().getConceptNames().add(IRI.create("oviparous"));
    tl.getSignature().getConceptNames().add(IRI.create("ovoviviparous"));
    tl.getSignature().getRoleNames().add(IRI.create("is-a"));
    for (IRI conceptName : tl.getSignature().getConceptNames())
      tl.setSynonym(conceptName, conceptName.toString());
    for (IRI roleName : tl.getSignature().getRoleNames())
      tl.setSynonym(roleName, roleName.toString());
    tl.setSynonym(IRI.create("is-a"), "is");
    tl.setSynonym(IRI.create("lion"), "lion");
    tl.setSynonym(IRI.create("penguin"), "penguin");
    tl.setSynonym(IRI.create("snake"), "snake");
    tl.setSynonym(IRI.create("panda"), "panda");
    tl.setSynonym(IRI.create("chameleon"), "chameleon");
    tl.setSynonym(IRI.create("black molly"), "black molly");
    tl.readInterpretations();
    final Pair<ELTBox, ELTBox> knowledge = tl.learn();
    System.out.println(knowledge.first());
    System.out.println("------------------------");
    System.out.println(knowledge.second());
  }

  private final File                  dir;
  private final List<Interpretation>  interpretations;
  private final Signature             signature;
  private final Multimap<IRI, String> synonyms;

  public TextLearner(final File dir) {
    super();
    this.dir = dir;
    this.interpretations = new LinkedList<Interpretation>();
    this.signature = new Signature(null);
    this.synonyms = HashMultimap.create();
  }

  public final Pair<ELTBox, ELTBox> learn() throws OWLOntologyCreationException {
    Pair<ELTBox, ELTBox> result = new Pair<ELTBox, ELTBox>(new ELTBox(), new ELTBox());
    for (Interpretation i : interpretations) {
      final ELInterpretation _i = (ELInterpretation) i;
      final ELTBox refuted = new ELTBox();
      final ELTBox notRefuted = new ELTBox();
      for (ELConceptInclusion gci : result.first().getGCIs())
        if (!_i.satisfies(gci))
          refuted.getGCIs().add(gci);
        else
          notRefuted.getGCIs().add(gci);
      final ELTBox base = _i.computeTBoxBase(1, notRefuted);

      System.out.println("refuted " + refuted);
      System.out.println("background " + notRefuted);
      System.out.println("base " + base);

      final ELTBox truth = new ELTBox();
      final ELTBox falsehood = new ELTBox();
      truth.getGCIs().addAll(notRefuted.getGCIs());
      truth.getGCIs().addAll(base.getGCIs());
      falsehood.getGCIs().addAll(refuted.getGCIs());
      falsehood.getGCIs().addAll(result.second().getGCIs());

      result = new Pair<ELTBox, ELTBox>(truth, falsehood);
//      System.out.println(i);
      System.out.println(truth);
      System.out.println(falsehood);
      System.out.println();
    }
    return result;
  }

  public final void readInterpretations() {
    Arrays
        .asList(dir.listFiles(f -> f.isFile() && f.getName().endsWith(".txt")))
        .stream()
        .map(this::readTextFile)
        .forEach(interpretations::add);;
  }

  private final Signature getSignature() {
    return signature;
  }

  private final boolean setSynonym(final IRI iri, final String string) {
    return
//        (signature.getConceptNames().contains(iri) || signature.getRoleNames().contains(iri)) && 
    synonyms.put(iri, string);
  }

  public static final <T> Stream<T> fromIterator(final Iterator<T> it, final boolean parallel) {
    final Iterable<T> ite = () -> it;
    return StreamSupport.stream(ite.spliterator(), parallel);
  }

  public static final <T> Stream<T> fromIterator2(final Iterator<T> it, final boolean parallel) {
    return fromIterator2(it, Spliterator.ORDERED, parallel);
  }

  public static final <T> Stream<T> fromIterator2(
      final Iterator<T> it,
      final int characteristics,
      final boolean parallel) {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, characteristics), parallel);
  }

  private final Interpretation readTextFile(final File textFile) {
    System.out.println("Reading " + textFile.getAbsolutePath());
    final LexicalizedParser parser = LexicalizedParser.loadModel();
    final Iterable<String> ite = () -> new IterableFile(textFile).iterator();
    return ELInterpretation.fromTriples(
        StreamSupport
            .stream(ite.spliterator(), true)
            .map(parser::parse)
            .map(this::getSPOTriple)
            .map(this::transformTriple)
            .collect(Collectors.toList()),
        null,
        IRI.create("is-a"));
  }

  private final String[] getSPOTriple(final Tree tree) {
    final Tree sentence = getFirstChildrenByLabel(tree, "S");
    final Tree np1 = getFirstChildrenByLabel(sentence, "NP");
    final Tree vp = getFirstChildrenByLabel(sentence, "VP");
    final Tree vbz = getFirstChildrenByLabel(vp, "VBZ");
    final Tree np2 = getFirstChildrenByLabel(vp, "NP");
    return new String[] { getNouns(np1), vbz.children()[0].value(), getNouns(np2) };
  }

  private final IRI[] transformTriple(final String[] spo) {
    IRI ss = null;
    IRI pp = null;
    IRI oo = null;
    for (Entry<IRI, String> e : synonyms.entries()) {
      if (ss == null && e.getValue().toLowerCase().trim().equals(spo[0].toLowerCase().trim()))
        ss = e.getKey();
      if (pp == null && e.getValue().toLowerCase().trim().equals(spo[1].toLowerCase().trim()))
        pp = e.getKey();
      if (oo == null && e.getValue().toLowerCase().trim().equals(spo[2].toLowerCase().trim()))
        oo = e.getKey();
    }
    return new IRI[] { ss, pp, oo };
  }

  private final Tree getFirstChildrenByLabel(final Tree tree, final String label) {
    final Optional<Tree> first =
        tree.getChildrenAsList().parallelStream().filter(t -> t.label().value().equals(label)).findFirst();
    if (first.isPresent())
      return first.get();
    return null;
  }

  private final String getNouns(final Tree tree) {
//    System.out.println(Collections2.transform(
//        Collections2.filter(tree.getChildrenAsList(), t -> t.label().value().startsWith("NN")),
//        t -> t.getChildrenAsList().get(0)));
    final StringBuilder sb = new StringBuilder();
    tree
        .getChildrenAsList()
        .stream()
        .filter(t -> t.label().value().startsWith("NN"))
        .forEach(t -> sb.append(t.children()[0].value() + " "));
    return sb.toString().trim();
  }
}
