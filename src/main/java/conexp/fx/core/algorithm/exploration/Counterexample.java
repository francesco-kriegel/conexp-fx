package conexp.fx.core.algorithm.exploration;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Counterexample<G, M> {

  private final G      object;
  private final Set<M> attributes;

  @SafeVarargs
  public Counterexample(final G object, final M... attributes) {
    this(object, Arrays.asList(attributes));
  }

  public Counterexample(final G object, final Collection<M> attributes) {
    super();
    this.object = object;
    this.attributes = new HashSet<M>(attributes);
  }

  public G getObject() {
    return object;
  }

  public Set<M> getAttributes() {
    return attributes;
  }
}
