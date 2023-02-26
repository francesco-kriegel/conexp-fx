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

import java.util.Collection;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

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
