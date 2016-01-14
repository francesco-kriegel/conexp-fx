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

import java.util.Collection;
import java.util.ConcurrentModificationException;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;

public final class SynchronizedPane extends Pane {

  public SynchronizedPane() {
    super();
  }

  public final void add(final Node... c) {
    synchronized (this) {
      for (Node n : c)
//        try {
        getChildren().add(n);
//        } catch (IllegalArgumentException e) {
////          System.err.println("ignore " + e.toString() + " in <SynchronizedPane>.add(...)");
//        }
    }
  }

  public final void add(final Collection<? extends Node> c) {
    synchronized (this) {
      for (Node n : c)
//        try {
        getChildren().add(n);
//        } catch (IllegalArgumentException e) {
////          System.err.println("ignore " + e.toString() + " in <SynchronizedPane>.add(...)");
//        }
    }
  }

  public final void remove(final Node... c) {
    synchronized (this) {
      getChildren().removeAll(c);
    }
  }

  public final void remove(final Collection<? extends Node> c) {
    synchronized (this) {
      getChildren().removeAll(c);
    }
  }

  public final void clear() {
    synchronized (this) {
      getChildren().clear();
    }
  }

  @Deprecated
  public final BaseBounds impl_computeGeomBounds(final BaseBounds baseBounds, final BaseTransform baseTransform) {
    synchronized (this) {
//      while (true)
      try {
        return super.impl_computeGeomBounds(baseBounds, baseTransform);
      } catch (Exception e) {
        return null;
////          System.err.println("ignore " + e.toString() + " in <SynchronizedPane>.impl_computeGeomBounds(...)");
      }
    }
  }
}
