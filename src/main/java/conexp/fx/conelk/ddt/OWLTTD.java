package conexp.fx.conelk.ddt;

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
