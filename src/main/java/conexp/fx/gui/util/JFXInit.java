package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
