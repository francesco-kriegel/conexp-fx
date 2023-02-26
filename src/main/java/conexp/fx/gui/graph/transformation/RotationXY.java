package conexp.fx.gui.graph.transformation;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2023 Francesco Kriegel
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


import conexp.fx.core.math.GuavaIsomorphism;
import javafx.geometry.Point3D;

public class RotationXY extends GuavaIsomorphism<Point3D, Point3D> {

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
