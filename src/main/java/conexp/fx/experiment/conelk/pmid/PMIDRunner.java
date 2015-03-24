package conexp.fx.experiment.conelk.pmid;

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

import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import conexp.fx.experiment.conelk.TBoxer;

public class PMIDRunner {

  public static final void main(String[] args) throws OWLOntologyCreationException {
    Map<String, OWLOntology> tBoxes = new TBoxer(new PMIDDataset()).getAllTBoxes();
    for (Entry<String, OWLOntology> entry : tBoxes.entrySet()) {
      System.out.println(entry.getValue());
    }
  }

}
