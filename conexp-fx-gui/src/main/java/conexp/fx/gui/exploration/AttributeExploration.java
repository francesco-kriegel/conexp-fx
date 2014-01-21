package conexp.fx.gui.exploration;

import java.util.Iterator;

import conexp.fx.core.algorithm.nextclosure.NextImplication;
import conexp.fx.gui.tab.CFXTab;
import de.tudresden.inf.tcs.fcalib.Implication;

public class AttributeExploration {

  private final CFXTab<String, String>  tab;
  private Iterator<Implication<String>> impls;

  public AttributeExploration(final CFXTab<String, String> tab) {
    super();
    this.tab = tab;
    initialize();
    showNextImplication();
  }

  private void initialize() {
    NextImplication<String, String> nextImpl = new NextImplication<String, String>(tab.fca.context);
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
