package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2022 Francesco Kriegel
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
