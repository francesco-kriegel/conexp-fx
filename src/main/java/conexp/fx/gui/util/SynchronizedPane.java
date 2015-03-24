package conexp.fx.gui.util;

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


import java.util.Collection;
import java.util.ConcurrentModificationException;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;

public final class SynchronizedPane
  extends Pane
{
  public SynchronizedPane()
  {
    super();
  }

  public final void add(final Node... c)
  {
    synchronized (this) {
      for (Node n : c)
        try {
          getChildren().add(n);
        } catch (IllegalArgumentException e) {
//          System.err.println("ignore " + e.toString() + " in <SynchronizedPane>.add(...)");
        }
    }
  }

  public final void add(final Collection<? extends Node> c)
  {
    synchronized (this) {
      for (Node n : c)
        try {
          getChildren().add(n);
        } catch (IllegalArgumentException e) {
//          System.err.println("ignore " + e.toString() + " in <SynchronizedPane>.add(...)");
        }
    }
  }

  public final void remove(final Node... c)
  {
    synchronized (this) {
      getChildren().removeAll(c);
    }
  }

  public final void remove(final Collection<? extends Node> c)
  {
    synchronized (this) {
      getChildren().removeAll(c);
    }
  }

  public final void clear()
  {
    synchronized (this) {
      getChildren().clear();
    }
  }

  @Deprecated
  public final BaseBounds impl_computeGeomBounds(final BaseBounds baseBounds, final BaseTransform baseTransform)
  {
    synchronized (this) {
      while (true)
        try {
          return super.impl_computeGeomBounds(baseBounds, baseTransform);
        } catch (ConcurrentModificationException e) {
//          System.err.println("ignore " + e.toString() + " in <SynchronizedPane>.impl_computeGeomBounds(...)");
        }
    }
  }
}
