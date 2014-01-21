package conexp.fx.core.algorithm.exploration;

import de.tudresden.inf.tcs.fcalib.Implication;

public class NoExpert<G, M> implements Expert<G, M> {

	@Override
	public Counterexample<G, M> askForCounterexample(Implication<M> impl) {
		return null;
	}

}
