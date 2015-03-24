package conexp.fx.gui.task;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
