package conexp.fx.experiment;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.collect.Sets;

import conexp.fx.core.dl.ELConceptInclusion;
import conexp.fx.core.dl.ELInterpretation;
import conexp.fx.core.dl.ELTBox;
import conexp.fx.core.util.IterableFile;

/**
 * The following properties in the wikidataset are of interest here: P19 - place of birth P20 - place of death P22 -
 * father P25 - mother P66 - place of origin (family) P69 - alma mater P106 - occupation Note: In some files these
 * properties have a further suffix "c".
 * 
 * Currently all triples are exported from the file "wikidata-simple-statements.nt", which contain the P106 predicate.
 * Then for all of the abbreviated entities in the exported triples their labels have been read from
 * "wikidata-terms.nt". Finally the encrypted triples have been replaced by their labels, and the new triple set was
 * written to the file "extractL.nt".
 * 
 * The excerpt shall be used to extract knowledge on the involved occupations.
 */
public class WikidataExample {

  private final static String      datapath           = "/Volumes/francesco/Data/WikidataRDFdumps20150223/";
  private final static String      resultpath         = "/Volumes/francesco/Data/WikidataRDFdumps20150223/results/";
  private final static String      wikidata           = "http://www.wikidata.org/entity/";

  private static final String      birthplaceProperty = "<" + wikidata + "P19c>";
  private static final String      deathplaceProperty = "<" + wikidata + "P20c>";
  private static final String      originProperty     = "<" + wikidata + "P66c>";
  private static final String      almamaterProperty  = "<" + wikidata + "P69c>";
  private static final String      fatherProperty     = "<" + wikidata + "P22c>";
  private static final String      motherProperty     = "<" + wikidata + "P25c>";
  private static final String      occupationProperty = "<" + wikidata + "P106c>";
  private static final Set<String> properties         = Sets.newHashSet(
                                                          birthplaceProperty,
                                                          deathplaceProperty,
                                                          originProperty,
                                                          almamaterProperty,
                                                          fatherProperty,
                                                          motherProperty,
                                                          occupationProperty);

  public static void main(String[] args) throws RepositoryException, RDFParseException, IOException {
//    final long s1 = System.currentTimeMillis();
//    writeExtract();
//    final long s2 = System.currentTimeMillis();
//    System.out.println("writing extract took " + (s2 - s1) + " ms");
//    extractLabelsEN();
//    final long s3 = System.currentTimeMillis();
//    System.out.println("extracting labels took " + (s3 - s2) + " ms");
//
//    final long t1 = System.currentTimeMillis();
//    final Set<String[]> triples = readExtract();
//    final long t2 = System.currentTimeMillis();
//    System.out.println("reading extract done in " + (t2 - t1) + " ms");
//    final Set<String> entities = getEntities(triples);
//    final long t3 = System.currentTimeMillis();
//    System.out.println("getting entities done in " + (t3 - t2) + " ms");
//    final Map<String, String> labels = getLabels(entities);
//    labels.put(occupationProperty, "hasOccupation");
//    final long t4 = System.currentTimeMillis();
//    System.out.println("got labels in " + (t4 - t3) + " ms");
//    final Set<String[]> triples2 = transformTriples(triples, labels);
//    final long t5 = System.currentTimeMillis();
//    System.out.println("triples transformed in " + (t5 - t4) + " ms");
//    writeTriples(triples2, new File(resultpath + "extractL.nt"));
//    final long t6 = System.currentTimeMillis();
//    System.out.println("wrote triples in " + (t6 - t5) + " ms");
////    System.out.println("overall time was " + (t6 - t1) + " ms");

    final long t7 = System.currentTimeMillis();
    try {
      _computeBase();
    } catch (OWLOntologyCreationException e) {
      e.printStackTrace();
    }
    final long t8 = System.currentTimeMillis();
    System.out.println("base computation took " + (t8 - t7) + " ms");

//    System.out.println("overall computation time was " + (t8 - s1) + " ms");
  }

//  private static final void computeBase() throws OWLOntologyCreationException {
//    final Interpretation i = ELInterpretation.fromTriples(new File(resultpath + "extractL.nt"), "hasOccupation");
//    System.out.println(i);
////    final Context<IRI, OWLClassExpression> cxt = i.getInducedContext(0);
////    cxt.colHeads().forEach(System.out::println);
////    System.out.println(cxt);
//    final OWLOntology base = i.computeTBoxBase(0, null);
//    for (OWLAxiom gci : base.getTBoxAxioms(null))
//      System.out.println(gci);
//  }

  private static final void _computeBase() throws OWLOntologyCreationException {
    final ELInterpretation i =
        ELInterpretation.fromTriples(new File(resultpath + "extractL.nt"), null, "hasOccupation");
    System.out.println("interpretation has " + i.getDomain().size() + " elements.");
    final Set<IRI> _subdomain = new HashSet<IRI>();
    i.getRoleNameExtension(IRI.create("hasFather")).forEach(p -> {
      _subdomain.add(p.x());
      _subdomain.add(p.y());
    });
    final Set<IRI> subdomain = new HashSet<IRI>();
    final Iterator<IRI> it = _subdomain.iterator();
    for (int j = 0; j < 1000; j++)
      if (it.hasNext())
        subdomain.add(it.next());
    i.getDomain().retainAll(subdomain);
    i.getConceptNameExtensions().values().retainAll(subdomain);
    i.getRoleNameExtensions().values().removeIf(p -> {
      return !subdomain.contains(p.x()) || !subdomain.contains(p.y());
    });
    System.out.println("interpretation has " + subdomain.size() + " elements.");
//    System.out.println(i);
//    final Context<IRI, ELNormalForm> cxt = i._getInducedContext(0);
//    cxt.colHeads().forEach(System.out::println);
//    System.out.println(cxt);
    System.out.println(i.getSignature().getRoleNames());
    final ELTBox base = i.computeTBoxBase(1, null);
    for (ELConceptInclusion gci : base.getGCIs())
      System.out.println(gci);
  }

  private static final void extractLabelsEN() throws IOException {
    final File file = new File(resultpath + "labelsEN.nt");
    file.createNewFile();
    final Writer w = new BufferedWriter(new FileWriter(file));
//    final Map<String, String> labels = new HashMap<String, String>();
    final Iterator<String> it = IterableFile.iterator(new File(datapath + "wikidata-terms.nt"));
    while (it.hasNext()) {
      final String next = it.next();
      final String[] triple = next.split(" ");
      if (triple.length > 2 && triple[1].contains(RDFS.LABEL.stringValue()) && triple[2].contains("@en")
          && !triple[2].contains("@en-"))
        w.append(next + "\r\n");
    }
    w.flush();
    w.close();
  }

  private static void writeExtract() throws IOException {
    final long start = System.currentTimeMillis();
    Iterator<String> it = new IterableFile(new File(datapath + "wikidata-simple-statements.nt")).iterator();
    final File file = new File(resultpath + "extract.nt");
    file.createNewFile();
    Writer writer = new BufferedWriter(new FileWriter(file));
    int counter = 1;
//    final Set<String> entities = new HashSet<String>();
//    entities.add(occupationProperty);
    while (it.hasNext()) {
      final String next = it.next();
      final String[] triple = next.split(" ");
      if (triple.length > 2
          && (triple[1].contains(occupationProperty) || triple[1].contains(fatherProperty) || triple[2]
              .contains(motherProperty))) {
        counter++;
        writer.append(next + "\r\n");
//        entities.add(triple[0]);
//        entities.add(triple[2]);
      }
    }
    writer.flush();
    writer.close();
    System.out.println(counter + " triples exported");
    System.out.println(System.currentTimeMillis() - start + " ms");
  }

  private static final Set<String[]> readExtract() {
    final Set<String[]> triples = new HashSet<String[]>();
    final Iterator<String> it = IterableFile.iterator(new File(resultpath + "extract.nt"));
    while (it.hasNext()) {
      final String next = it.next();
      final String[] triple = next.split(" ");
      if (triple.length > 2)
        triples.add(triple);
    }
    return triples;
  }

  private static final Set<String> getEntities(final Set<String[]> triples) {
    final Set<String> entities = new HashSet<String>();
    triples.forEach(triple -> {
//      entities.add(triple[0]);
        entities.add(triple[2]);
      });
    return entities;
  }

  private static final Set<String[]> transformTriples(final Set<String[]> triples, final Map<String, String> labels) {
    final Set<String[]> triples2 = new HashSet<String[]>();
    triples.forEach(triple -> {
      if (triple[1].contains(occupationProperty)) {
        final String s = triple[0];
        final String p = triple[1];
        final String o = labels.get(triple[2]);
        if (o != null)
          triples2.add(new String[] { s, p, o });
      } else {
        triples2.add(triple);
//        final String s = labels.get(triple[0]);
//        final String p = labels.get(triple[1]);
//        final String o = labels.get(triple[2]);
//        if (s != null && p != null && o != null)
//          triples2.add(new String[] { s, p, o });
      }
    });
    return triples2;
  }

  private static final void writeTriples(final Set<String[]> triples, final File file) throws IOException {
    file.createNewFile();
    final Writer w = new BufferedWriter(new FileWriter(file));
    triples.forEach(triple -> {
      try {
        w.append("<" + triple[0] + "> <" + triple[1] + "> <" + triple[2] + "> .\r\n");
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    w.flush();
    w.close();
  }

  private static final Map<String, String> getLabels(final Set<String> entities) {
    final Map<String, String> labels = new HashMap<String, String>();
    final Iterator<String> it = IterableFile.iterator(new File(resultpath + "labelsEN.nt"));
    while (it.hasNext()) {
      final String next = it.next();
      final String[] triple = next.split(" ");
      if (triple.length > 2 && entities.contains(triple[0]) && triple[1].contains(RDFS.LABEL.stringValue())
          && triple[2].contains("@en")) {
        labels.put(triple[0], triple[2]);
      }
    }
    return labels;
  }
}
