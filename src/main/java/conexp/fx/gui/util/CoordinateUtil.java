package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
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
