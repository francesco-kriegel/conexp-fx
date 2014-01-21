package conexp.fx.core.algorithm.exploration;

import de.tudresden.inf.tcs.fcalib.Implication;

public interface Expert<G, M> {

  public Counterexample<G, M> askForCounterexample(final Implication<M> impl);

}
