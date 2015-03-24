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

import org.semanticweb.owlapi.model.IRI;

public final class OWLTTD {

  public static final String NAMESPACE  = "http://bidd.nus.edu.sg/group/cjttd#";

  public static final Iri    DRUG       = new Iri("drug");
  public static final Iri    DISEASE    = new Iri("disease");
  public static final Iri    TARGET     = new Iri("target");

  public static final Iri    HAS_NAME   = new Iri("hasName");
  public static final Iri    HAS_TYPE   = new Iri("hasType");

  public static final Iri    HEALS      = new Iri("heals");
  public static final Iri    LOCATED_AT = new Iri("locatedAt");
  public static final Iri    BINDS_TO   = new Iri("bindsTo");
  public static final Iri    MAPS_TO    = new Iri("mapsTo");

  public static class Iri extends IRI {

    private static final long serialVersionUID = 8259805184219210275L;

    public Iri(String name) {
      super(NAMESPACE, name);
    }

  }

}
