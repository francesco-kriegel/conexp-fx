package conexp.fx.experiment.conelk.ddt;

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

import org.semanticweb.owlapi.model.OWLOntology;

import conexp.fx.experiment.conelk.Dataset;


public class DDTDataset extends Dataset {

  @Override
  public String getName() {
    return "DDT";
  }

  @Override
  public Map<String, OWLOntology> getData() {
    // TODO Auto-generated method stub
    return null;
  }

}