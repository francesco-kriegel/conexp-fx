package conexp.fx.conelk.ddt;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.semanticweb.owlapi.model.IRI;

public final class TTD {

  public static final String NAMESPACE  = "http://bidd.nus.edu.sg/group/cjttd#";

  public static final Uri    DRUG       = new Uri("drug");
  public static final Uri    DISEASE    = new Uri("disease");
  public static final Uri    TARGET     = new Uri("target");

  public static final Uri    HAS_NAME   = new Uri("hasName");
  public static final Uri    HAS_TYPE   = new Uri("hasType");

  public static final Uri    HEALS      = new Uri("heals");
  public static final Uri    LOCATED_AT = new Uri("locatedAt");
  public static final Uri    BINDS_TO   = new Uri("bindsTo");
  public static final Uri    MAPS_TO    = new Uri("mapsTo");

  public static class Uri extends URIImpl implements URI {

    private static final long serialVersionUID = 1685789721680024800L;

    public Uri(final String name) {
      super(NAMESPACE + name);
    }

  }

}
