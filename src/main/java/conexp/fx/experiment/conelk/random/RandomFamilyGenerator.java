package conexp.fx.experiment.conelk.random;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class RandomFamilyGenerator {

  private static final OWLOntologyManager                    om       = OWLManager.createOWLOntologyManager();
  private static OWLOntology                                 onto;
  private static final OWLDataFactory                        fac      = om.getOWLDataFactory();
  private static final OWLClass                              male     = fac.getOWLClass(IRI.create("http://f#", "Male"));
  private static final OWLClass                              female   = fac.getOWLClass(IRI
                                                                          .create("http://f#", "Female"));
  private static final OWLObjectProperty                     hasChild = fac.getOWLObjectProperty(IRI.create(
                                                                          "http://f#",
                                                                          "hasChild"));
  private static final Map<Integer, Set<OWLNamedIndividual>> females  = new HashMap<Integer, Set<OWLNamedIndividual>>();
  private static final Map<Integer, Set<OWLNamedIndividual>> males    = new HashMap<Integer, Set<OWLNamedIndividual>>();
  private static int                                         p        = 0;

  public static final void main(String[] args) throws OWLOntologyCreationException {
    onto = om.createOntology();
    initPopulation(50);
    evolveGenerations(2, 0.07, 4);
    export("/Users/francesco/Documents/workspace/ontologies/family1.owl");
  }

  private static final void initPopulation(int number) {
    females.put(0, new HashSet<OWLNamedIndividual>());
    males.put(0, new HashSet<OWLNamedIndividual>());
    System.out.println("initializing");
    for (int i = 0; i < number; i++)
      if (Math.random() < 0.5)
        addMale(0);
      else
        addFemale(0);
  }

  private static final OWLNamedIndividual addMale(int g) {
    final OWLNamedIndividual person = fac.getOWLNamedIndividual(IRI.create("Person" + p));
    om.addAxiom(onto, fac.getOWLClassAssertionAxiom(male, person));
    males.get(g).add(person);
    p++;
    return person;
  }

  private static final OWLNamedIndividual addFemale(int g) {
    final OWLNamedIndividual person = fac.getOWLNamedIndividual(IRI.create("Person" + p));
    om.addAxiom(onto, fac.getOWLClassAssertionAxiom(female, person));
    females.get(g).add(person);
    p++;
    return person;
  }

  private static final void evolveGenerations(int number, double probpair, int children) {
    for (int n = 1; n <= number; n++) {
      System.out.println("generation " + n);
      evolveNextGeneration(n, probpair, children);
      System.out.println("done");
    }
  }

  private static final void evolveNextGeneration(int g, double probpair, int childrennumber) {
    females.put(g, new HashSet<OWLNamedIndividual>());
    males.put(g, new HashSet<OWLNamedIndividual>());
    for (OWLNamedIndividual m : males.get(g - 1))
      for (OWLNamedIndividual f : females.get(g - 1))
        if (Math.random() < probpair) {
          int children = (int) (Math.random() * childrennumber);
          for (int c = 0; c < children; c++) {
            final OWLNamedIndividual child;
            if (Math.random() < 0.5)
              child = addMale(g);
            else
              child = addFemale(g);
            om.addAxiom(onto, fac.getOWLObjectPropertyAssertionAxiom(hasChild, m, child));
            om.addAxiom(onto, fac.getOWLObjectPropertyAssertionAxiom(hasChild, f, child));
          }
        }
  }

  private static final void export(final String pathToFile) {
    System.out.println("Writing to " + pathToFile);
    FileOutputStream s = null;
    try {
      s = new FileOutputStream(new File(pathToFile));
      om.saveOntology(onto, s);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (s != null)
        try {
          s.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }
    System.out.println("done");
  }

}
