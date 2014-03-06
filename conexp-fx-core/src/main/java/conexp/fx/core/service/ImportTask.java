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
    updateMessage("Setting Content...");
    this.fcaInstance.request.setContent();
//    updateMessage("Reducing Context...");
//    this.fcaInstance.context.initHandlers(true, MatrixContext.AutomaticMode.REDUCE);
//    final int rows = this.fcaInstance.context.rowHeads().size();
//    final int cols = this.fcaInstance.context.colHeads().size();
//    this.fcaInstance.context.initHandlers(true, MatrixContext.AutomaticMode.fromSize(rows,cols));
  }
}
