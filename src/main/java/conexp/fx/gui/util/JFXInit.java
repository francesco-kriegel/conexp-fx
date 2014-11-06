package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX - Core
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class JFXInit extends Application {

  public static void initializeJavaFXPlatform() {
    new Thread(new Runnable() {

      @Override
      public void run() {
        JFXInit._init();
      }
    }).start();
  }

  public static void shutdownJavaFXPlatform() {
    try {
      instance.stop();
    } catch (Exception e) {}
    System.exit(0);
  }

  private static final void _init() {
    launch();
  }

  private static JFXInit instance = null;

  @Override
  public void start(Stage primaryStage) throws Exception {
    Platform.setImplicitExit(true);
    instance = this;
  }
}