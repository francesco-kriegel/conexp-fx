package conexp.fx.core.algorithm.exploration;

import java.util.HashSet;
import java.util.Set;

import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.implication.ImplicationSet;
import de.tudresden.inf.tcs.fcalib.Implication;

public class AttributeExploration<G, M> {

  protected final MatrixContext<G, M> cxt;
  protected final Expert<G, M>        expert;

  protected final ImplicationSet<M>   impls;
//  protected final List<Set<M>>         intents;
  protected final MatrixContext<G, M> counterexamples;
  protected Set<M>                    b;

  public AttributeExploration(final MatrixContext<G, M> context, final Expert<G, M> expert) {
    super();
    this.cxt = context;
    cxt.pushAllChangedEvent();
    this.expert = expert;
    this.impls = new ImplicationSet<M>();
    this.b = new HashSet<M>();
//    this.intents = new LinkedList<Set<M>>();
    this.counterexamples = this.cxt.clone();
//    this.counterexamples = new MatrixContext<G, M>(false);
//    this.counterexamples.colHeads().addAll(cxt.colHeads());
  }

  public void startExploration() {
    for (Implication<M> impl = nextImplication(); impl != null; impl = nextImplication()) {
      System.out.println("next implication: " + impl);
      final Counterexample<G, M> counterexample = expert.askForCounterexample(impl);
      if (counterexample == null) {
        addImplication(impl);
        final Set<M> c = impls.closure(b);
        System.out.println(b + " with its linClosure: " + c);
        if (b.equals(c))
          return;
        b = c;
      } else
        addCounterexample(counterexample);
    }
  }

  protected Implication<M> nextImplication() {
    final Implication<M> implication = new Implication<M>();
    implication.getPremise().addAll(b);
    final Set<M> bprime;
    if (counterexamples.isEmpty())
      bprime = counterexamples.colHeads();
    else
      bprime = counterexamples.intent(b);
    System.out.println(b + " with its prime: " + bprime);
    implication.getConclusion().addAll(bprime);
    implication.getConclusion().removeAll(b);
    return implication;
  }

  protected void addImplication(Implication<M> impl) {
    if (impl.getConclusion().isEmpty())
      ;
//      intents.add(impl.getPremise());
    else
      impls.add(impl);
  }

  protected void addCounterexample(Counterexample<G, M> counterexample) {
    counterexamples.rowHeads().add(counterexample.getObject());
    counterexamples.row(counterexample.getObject()).addAll(counterexample.getAttributes());
  }

}
