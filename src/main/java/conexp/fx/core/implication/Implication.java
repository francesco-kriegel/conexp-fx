package conexp.fx.core.implication;

import java.util.Set;

public class Implication<G, M> extends de.tudresden.inf.tcs.fcalib.Implication<M> {

  private final Set<G> support;

  public Implication(final Set<M> p, final Set<M> c, final Set<G> s) {
    super(p, c);
    this.support = s;
  }

  public Set<G> getSupport() {
    return support;
  }

}
