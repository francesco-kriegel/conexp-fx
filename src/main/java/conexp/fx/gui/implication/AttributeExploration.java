package conexp.fx.gui.implication;

import java.util.Iterator;

import conexp.fx.core.algorithm.nextclosure.NextImplication;
import conexp.fx.gui.FCAInstance;
import de.tudresden.inf.tcs.fcalib.Implication;

public class AttributeExploration {

  private final FCAInstance<String, String> tab;
  private Iterator<Implication<String>>     impls;

  public AttributeExploration(final FCAInstance<String, String> fcaInstance) {
    super();
    this.tab = fcaInstance;
    initialize();
    showNextImplication();
  }

  private void initialize() {
    NextImplication<String, String> nextImpl = new NextImplication<String, String>(tab.context);
    impls = nextImpl.iterator();
  }

  private void showNextImplication() {
    if (!impls.hasNext())
      return;
    final ExplorationDialog<String, String> explorationDialog =
        new ExplorationDialog<String, String>(tab, impls.next());
    if (explorationDialog.showAndReturn() == 0)
      showNextImplication();
  }
}
