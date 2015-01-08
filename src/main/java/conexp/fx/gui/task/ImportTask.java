package conexp.fx.gui.task;

import conexp.fx.core.context.MatrixContext;
import conexp.fx.gui.FCAInstance;

public final class ImportTask<G, M> extends BlockingTask {

  /**
   * 
   */
  private final FCAInstance<G, M> fcaInstance;

  public ImportTask(FCAInstance<G, M> fcaInstance) {
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
