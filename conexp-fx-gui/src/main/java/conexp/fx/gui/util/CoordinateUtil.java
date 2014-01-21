package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX - Graphical User Interface
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


import javafx.geometry.Pos;
import javafx.scene.Node;

public final class CoordinateUtil {

  public static final double getLocalX(Node node, final double sceneX) {
    final double minX = node.localToScene(node.getBoundsInLocal()).getMinX();
    final double localX = sceneX - minX;
    return localX;
  }

  public static final double getLocalY(Node node, final double sceneY) {
    final double minY = node.localToScene(node.getBoundsInLocal()).getMinY();
    final double localY = sceneY - minY;
    return localY;
  }

  public static final double getScreenX(Node node) {
    final double windowX = node.getScene().getWindow().getX();
    final double sceneX = node.getScene().getX();
    final double nodeX = node.localToScene(node.getBoundsInLocal()).getMinX();
    return windowX + sceneX + nodeX;
  }

  public static final double getScreenY(Node node) {
    final double windowY = node.getScene().getWindow().getY();
    final double sceneY = node.getScene().getY();
    final double nodeY = node.localToScene(node.getBoundsInLocal()).getMinY();
    return windowY + sceneY + nodeY;
  }

  public static final Pos contraryPosition(Pos position) {
    switch (position) {
    case BOTTOM_LEFT:
      return Pos.TOP_RIGHT;
    case BOTTOM_CENTER:
      return Pos.TOP_CENTER;
    case BOTTOM_RIGHT:
      return Pos.TOP_LEFT;
    case CENTER_LEFT:
      return Pos.CENTER_RIGHT;
    case CENTER:
      return Pos.CENTER;
    case CENTER_RIGHT:
      return Pos.CENTER_LEFT;
    case TOP_LEFT:
      return Pos.BOTTOM_RIGHT;
    case TOP_CENTER:
      return Pos.BOTTOM_CENTER;
    case TOP_RIGHT:
      return Pos.BOTTOM_LEFT;
    default:
      return position;
    }
  }

}
