package conexp.fx.gui.graph.transformation;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import conexp.fx.core.math.GuavaIsomorphism;
import javafx.geometry.Point3D;

public final class PolarTransformation extends GuavaIsomorphism<Point3D, Point3D> {

//  private final double minX;
  private final double width;
  private final double centerX;

  public PolarTransformation(final double minX, final double width) {
//    this.minX = minX;
    this.width = width;
    this.centerX = minX + width / 2d;
  }

  @Override
  public final Point3D apply(final Point3D p) {
    final double a = ((p.getX() - centerX) / width) * (1.8d * Math.PI);
    final double r = p.getY();
    return new Point3D(r * Math.sin(a), r * Math.cos(a), 0);
  }

  @Override
  public final Point3D invert(final Point3D q) {
    final double a = Math.atan2(q.getX(), q.getY());
    final double r = Math.sqrt(q.getX() * q.getX() + q.getY() * q.getY());
    final double x = centerX + ((a * width) / (1.8d * Math.PI));
    final double y = r;
    return new Point3D(x, y, 0);
  }
}
