package conexp.fx.core.service;

import com.google.common.collect.Iterators;

import conexp.fx.core.algorithm.lattice.IPred;
import conexp.fx.core.algorithm.nextclosure.NextConcept;
import conexp.fx.core.concurrent.BlockingTask;
import conexp.fx.core.layout.GeneticLayouter;
import conexp.fx.core.service.FCAInstance.InitLevel;
import conexp.fx.core.util.Constants;

final class InitializationTask<G, M> extends BlockingTask {

  /**
   * 
   */
  private final FCAInstance<G, M> fcaInstance;
  private final FCAInstance.InitLevel lvl;

  public InitializationTask(FCAInstance<G, M> fcaInstance, final FCAInstance.InitLevel lvl) {
    super("Displayability Test");
    this.fcaInstance = fcaInstance;
    this.lvl = lvl;
  }

  protected final void _call() {
    updateMessage("checking whether there are more than 200 concepts...");
    if (isDisplayable()) {
      if (lvl.isOrNeedsLevel(InitLevel.CONCEPTS)) {
        this.fcaInstance.executor.submit(NextConcept.concepts(this.fcaInstance.lattice));
        if (lvl.isOrNeedsLevel(InitLevel.LAYOUT))
          this.fcaInstance.executor.submit(new SeedsAndLabelsTask<G, M>(this.fcaInstance));
        if (lvl.isOrNeedsLevel(InitLevel.LATTICE))
          this.fcaInstance.executor.submit(IPred.neighborhood(this.fcaInstance.lattice));
        if (lvl.isOrNeedsLevel(InitLevel.LAYOUT))
          this.fcaInstance.executor.submit(GeneticLayouter.seeds(
              this.fcaInstance.layout,
              false,
              Constants.GENERATIONS,
              Constants.POPULATION,
              this.fcaInstance.tab == null ? false : this.fcaInstance.tab.threeDimensions(),
              this.fcaInstance.conflictDistance,
              this.fcaInstance.tpe));
      }
    }
  }

  private final boolean isDisplayable() {
    if (this.fcaInstance.context.rowHeads().size() > 100)
      return false;
    if (this.fcaInstance.context.colHeads().size() > 100)
      return false;
    try {
      Iterators.get(new NextConcept<G, M>(this.fcaInstance.context).iterator(), Constants.MAX_CONCEPTS + 1);
    } catch (IndexOutOfBoundsException e) {
      return true;
    }
    return false;
  }
}