package conexp.fx.core.concurrent;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Worker.State;

public abstract class BlockingTaskNoFX extends FutureTask<Void> {

  public static final BlockingTask      NULL                = new BlockingTask("") {

                                                              protected void _call() {}
                                                            };
  private long                          runTimeMillis       = 0l;
  protected final ObjectProperty<State> stateProperty       = new SimpleObjectProperty<State>();
  protected final StringProperty        titleProperty       = new SimpleStringProperty("");
  protected final StringProperty        messageProperty     = new SimpleStringProperty("");
  protected final DoubleProperty        currentWorkProperty = new SimpleDoubleProperty(0d);
  protected final DoubleProperty        totalWorkProperty   = new SimpleDoubleProperty(1d);
  protected final DoubleBinding         progressProperty    = new DoubleBinding() {

                                                              {
                                                                bind(currentWorkProperty, totalWorkProperty);
                                                              }

                                                              @Override
                                                              protected double computeValue() {
                                                                final double c = currentWorkProperty.get();
                                                                final double t = totalWorkProperty.get();
                                                                if (t <= 0 || c <= 0)
                                                                  return 0;
                                                                return c / t;
                                                              }
                                                            };

  public BlockingTaskNoFX(final String title) {
    super(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        System.err.println("unwanted call");
        return null;
      }
    });
    // TODO: Setting value for callable field via reflection
    // maybe this is a bad practise, try to change it to a better solution!
    try {
      final Class<?> clazz = this.getClass().getSuperclass().getSuperclass();
      final Field callableField = clazz.getDeclaredField("callable");
      callableField.setAccessible(true);
      callableField.set(this, new Callable<Void>() {

        @Override
        public Void call() throws Exception {
          return BlockingTaskNoFX.this.call();
        }
      });
    } catch (NoSuchFieldException | IllegalAccessException | SecurityException e) {
      e.printStackTrace();
    }
    updateTitle(title);
    ready();
  }

  public final long runTimeMillis() {
    return runTimeMillis;
  }

  public final Void call() {
    final long startTimeMillis = System.currentTimeMillis();
    scheduled();
    running();
    updateProgress(0d, 1d);
    try {
      _call();
    } catch (IllegalStateException e) {
//    	e.printStackTrace();
    	System.err.println(e + " @ "+titleProperty.get());
      Platform.runLater(new Runnable() {

        @Override
        public void run() {
          call();
        }
      });
    } catch (Exception e) {
      runTimeMillis = System.currentTimeMillis() - startTimeMillis;
      updateMessage("failed (" + e.toString() + ": " + e.getMessage() + ")");
      failed();
      done();
    } finally {
      runTimeMillis = System.currentTimeMillis() - startTimeMillis;
      updateMessage("succeeded (" + runTimeMillis + "ms)");
      updateProgress(1d, 1d);
      succeeded();
      done();
    }
    return null;
  }

  /**
   * Implement this method to run the desired computation, i.e. encapsulate an algorithm.
   */
  protected abstract void _call();

  /**
   * Overwrite this method for a cancelable task implementation.
   */
  protected void _cancel() {}

  public final void cancel() {
    _cancel();
    cancelled();
    done();
  }

  protected final void ready() {
    stateProperty.set(State.READY);
  }

  protected final void scheduled() {
    stateProperty.set(State.SCHEDULED);
  }

  protected final void running() {
    stateProperty.set(State.RUNNING);
  }

  protected final void succeeded() {
    stateProperty.set(State.SUCCEEDED);
  }

  protected final void failed() {
    stateProperty.set(State.FAILED);
  }

  protected final void cancelled() {
    stateProperty.set(State.CANCELLED);
  }

  protected void updateTitle(final String title) {
    titleProperty.set(title);
  }

  protected void updateMessage(final String message) {
    messageProperty.set(message);
  }

  protected void updateProgress(final double current, final double total) {
    currentWorkProperty.set(current);
    totalWorkProperty.set(total);
  }

  public final DoubleExpression progressProperty() {
    return progressProperty;
  }

  public final ObjectExpression<State> stateProperty() {
    return stateProperty;
  }

  public final StringExpression messageProperty() {
    return messageProperty;
  }

  public final StringExpression titleProperty() {
    return titleProperty;
  }
}
