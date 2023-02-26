package conexp.fx.gui.graph.transformation;

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
