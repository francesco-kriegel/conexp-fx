package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
