package conexp.fx.gui.graph.transformation;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import conexp.fx.core.math.Isomorphism;
import javafx.geometry.Point3D;

public class RotationXY extends Isomorphism<Point3D, Point3D> {

  private final double  angle;
  private final Point3D center;

  public RotationXY(final Point3D center, final double angle) {
    super();
    this.angle = angle;
    this.center = center;
  }

  @Override
  public final Point3D apply(final Point3D p) {
    final double dx = p.getX() - center.getX();
    final double dy = p.getY() - center.getY();
    final double r = Math.sqrt(dx * dx + dy * dy);
    final double w = Math.atan2(dy, dx) + angle;
    return new Point3D(r * Math.cos(w) + center.getX(), r * Math.sin(w) + center.getY(), p.getZ());
  }

  @Override
  public final Point3D invert(final Point3D q) {
    final double dx = q.getX() - center.getX();
    final double dy = q.getY() - center.getY();
    final double r = Math.sqrt(dx * dx + dy * dy);
    final double w = Math.atan2(dy, dx) - angle;
    return new Point3D(r * Math.cos(w) + center.getX(), r * Math.sin(w) + center.getY(), q.getZ());
  }
}
