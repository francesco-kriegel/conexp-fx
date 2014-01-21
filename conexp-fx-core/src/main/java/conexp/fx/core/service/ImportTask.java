package conexp.fx.core.service;

import conexp.fx.core.concurrent.BlockingTask;

final class ImportTask<G, M> extends BlockingTask {

  /**
   * 
   */
  private final FCAInstance<G, M> fcaInstance;

  public ImportTask(FCAInstance<G, M> fcaInstance) {
    super("Import Task");
    this.fcaInstance = fcaInstance;
  }

  protected final void _call() {
    this.fcaInstance.request.setContent();
  }
}