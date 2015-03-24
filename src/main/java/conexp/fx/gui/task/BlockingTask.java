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

import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;

public abstract class BlockingTask extends Task<Void> {

  public static final BlockingTask NULL          = new BlockingTask("") {

                                                   protected void _call() {}
                                                 };
  public final LongProperty        runTimeMillis = new SimpleLongProperty(0l);

  public BlockingTask(final String title) {
    super();
    updateTitle(title);
  }

  @Override
  public final Void call() {
    final long startTimeMillis = System.currentTimeMillis();
    scheduled();
    running();
    updateProgress(0d, 1d);
    final Thread t = new Thread(() -> {
      while (true) {
        Platform.runLater(() -> runTimeMillis.set(System.currentTimeMillis() - startTimeMillis));
        try {
          Thread.sleep(250);
        } catch (Exception e) {}
      }
    });
    t.start();
    try {
      _call();
    } catch (Exception e) {
      e.printStackTrace();
    }
    t.stop();
    Platform.runLater(() -> runTimeMillis.set(System.currentTimeMillis() - startTimeMillis));
    updateProgress(1d, 1d);
    updateMessage("succeeded (" + runTimeMillis + "ms)");
    succeeded();
    done();
    return null;
  }

  protected abstract void _call();
}
