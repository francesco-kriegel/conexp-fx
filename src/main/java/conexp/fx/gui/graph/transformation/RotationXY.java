package conexp.fx.gui.graph.transformation;

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
