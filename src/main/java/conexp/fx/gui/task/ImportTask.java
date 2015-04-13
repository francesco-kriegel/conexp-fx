package conexp.fx.gui.task;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import conexp.fx.core.context.MatrixContext;
import conexp.fx.gui.dataset.FCADataset;

public final class ImportTask<G, M> extends BlockingTask {

  /**
   * 
   */
  private final FCADataset<G, M> fcaInstance;

  public ImportTask(FCADataset<G, M> fcaInstance) {
    super(fcaInstance.id.get() + " - Import Task");
    this.fcaInstance = fcaInstance;
  }

  protected final void _call() {
    updateMessage("Setting Content...");
    this.fcaInstance.request.setContent();
    updateMessage("Reducing Context...");
    this.fcaInstance.context.initHandlers(true, MatrixContext.AutomaticMode.REDUCE);
//    final int rows = this.fcaInstance.context.rowHeads().size();
//    final int cols = this.fcaInstance.context.colHeads().size();
//    this.fcaInstance.context.initHandlers(true, MatrixContext.AutomaticMode.fromSize(rows,cols));
  }
}
