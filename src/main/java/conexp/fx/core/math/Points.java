package conexp.fx.core.math;

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

import java.util.Collection;
import java.util.NoSuchElementException;

import com.sun.javafx.geom.Line2D;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

public final class Points {

//  public static final Point3D ZERO  = new Point3D(0, 0, 0);
//  public static final Point3D ONE_X = new Point3D(1, 0, 0);
//  public static final Point3D ONE_Y = new Point3D(0, 1, 0);
//  public static final Point3D ONE_Z = new Point3D(0, 0, 1);

  public static final Point2D
      projectOnCircle(final double cx, final double cy, final double r, final double px, final double py) {
    final double dx = px - cx;
    final double dy = py - cy;
    final double l = Math.sqrt(dx * dx + dy * dy);
    final double f = r / l;
    final double x = cx + f * dx;
    final double y = cx + f * dy;
    return new Point2D(x, y);
  }

  public static final Point3D absoluteSum(final Collection<Point3D> c) {
    double x = 0d;
    double y = 0d;
    double z = 0d;
    for (Point3D p : c) {
      x += Math.abs(p.getX());
      y += Math.abs(p.getY());
      z += Math.abs(p.getZ());
    }
    return new Point3D(x, y, z);
  }

  public static final Point3D rotate(final Point3D point, final double angle) {
    final double x = point.getX() * Math.cos(angle) + point.getZ() * Math.sin(angle);
    final double y = point.getY();
    final double z = -point.getX() * Math.sin(angle) + point.getZ() * Math.cos(angle);
    return new Point3D(x, y, z);
  }

//  public static final Point3D plus(final Point3D p, final Point3D q) {
//    return Point3DBuilder.create().x(p.getX() + q.getX()).y(p.getY() + q.getY()).z(p.getZ() + q.getZ()).build();
//  }
//
//  public static final Point3D plus(final Point3D p, final double dx, final double dy) {
//    if (dy == 0d && dy == 0d)
//      return p;
//    else
//      return new Point3D(p.getX() + dx, p.getY() + dy, p.getZ());
//  }
//
//  public static final Point2D plus(final Point2D p, final double dx, final double dy) {
//    if (dy == 0d && dy == 0d)
//      return p;
//    else
//      return new Point2D(p.getX() + dx, p.getY() + dy);
//  }
//
//  public static final Point3D plus(final Point3D p, final double dx, final double dy, final double dz) {
//    if (dy == 0d && dy == 0d && dz == 0d)
//      return p;
//    else
//      return new Point3D(p.getX() + dx, p.getY() + dy, p.getZ() + dz);
//  }
//
//  public static final Point3D minus(final Point3D p, final Point3D q) {
//    return Point3DBuilder.create().x(p.getX() - q.getX()).y(p.getY() - q.getY()).z(p.getZ() - q.getZ()).build();
//  }
//
//  public static final Point3D multiply(final double f, final Point3D p) {
//    return Point3DBuilder.create().x(p.getX() * f).y(p.getY() * f).z(p.getZ() * f).build();
//  }
//
//  public static final Point3D divide(final Point3D p, final double f) {
//    return Point3DBuilder.create().x(p.getX() / f).y(p.getY() / f).z(p.getZ() / f).build();
//  }

//  public static final double scalarProduct(final Point3D p, final Point3D q) {
//    return p.getX() * q.getX() + p.getY() * q.getY() + p.getZ() * q.getZ();
//  }

//  public static final Point3D crossProduct(final Point3D p, final Point3D q) {
//    return Point3DBuilder
//        .create()
//        .x(p.getY() * q.getZ() - p.getZ() * q.getY())
//        .y(p.getZ() * q.getX() - p.getX() * q.getZ())
//        .z(p.getX() * q.getY() - p.getY() * q.getX())
//        .build();
//  }

//  public static final double length(final Point3D p) {
//    return Math.sqrt(scalarProduct(p, p));
//  }

  public static final double pointSegmentDistance(final Point3D p, final Point3D q1, final Point3D q2) {
    if (p.equals(q1) || p.equals(q2))
      return 0;
    if (q1.equals(q2))
      return p.distance(q1);
    // line segment: g = q1 + t * (q2 - q1), 0 <= t <= 1
    final Point3D q = q2.subtract(q1);
    // projection on line: P(p,g) = q1 + t * q
    final double t = p.subtract(q1).dotProduct(q) / q.dotProduct(q);
    if (t < 0)
      return p.distance(q1);
    else if (t > 1)
      return p.distance(q2);
    else
      return p.distance(q1.add(q.multiply(t)));
  }

  public static final boolean intersectX(final Point3D p1, final Point3D p2, final Point3D q1, final Point3D q2) {
    return Math.max(p1.getX(), p2.getX()) >= Math.min(q1.getX(), q2.getX())
        && Math.max(q1.getX(), q2.getX()) >= Math.min(p1.getX(), p2.getX());
  }

  public static final boolean intersectY(final Point3D p1, final Point3D p2, final Point3D q1, final Point3D q2) {
    return Math.max(p1.getY(), p2.getY()) >= Math.min(q1.getY(), q2.getY())
        && Math.max(q1.getY(), q2.getY()) >= Math.min(p1.getY(), p2.getY());
  }

  public static final boolean intersectXY(Point3D p1, Point3D p2, Point3D q1, Point3D q2) {
    return new Line2D((float) p1.getX(), (float) p1.getY(), (float) p2.getX(), (float) p2.getY())
        .intersectsLine(new Line2D((float) q1.getX(), (float) q1.getY(), (float) q2.getX(), (float) q2.getY()));
  }

  public static final Point2D projectXY(final Point3D p) {
    return new Point2D(p.getX(), p.getY());
  }

  public static final boolean
      intersectingLineSegments(final Point3D p1, final Point3D p2, final Point3D q1, final Point3D q2) {
    // line segment: g = p1 + s * (p2 - p1), 0 <= s <= 1
    // line segment: h = q1 + t * (q2 - q1), 0 <= t <= 1
    final Point3D p = p2.subtract(p1);
    final Point3D q = q2.subtract(q1);
    // equation: p1 + s * p = q1 + t * q
    // ==> s * p = (q1 - p1) + t * q
    // ==> t * q = (p1 - q1) + s * p
    // ==> s * (p X q) = (q1 - p1) X q
    // ==> t * (q X p) = (p1 - q1) X p
    // g intersects h iff there is a solution (s,t) in [0,1]x[0,1]
    final Point3D c1 = p.crossProduct(q);
    final Point3D c2 = q1.subtract(p1).crossProduct(q);
    final Point3D d1 = q.crossProduct(p);
    final Point3D d2 = p1.subtract(q1).crossProduct(p);
    // ==> s * c1 = c2
    // ==> t * d1 = d2
    try {
      final double s = solve(c1, c2);
      if (s < 0 || s > 1)
        return false;
      final double t = solve(d1, d2);
      if (t >= 0 && t <= 1)
        return true;
    } catch (NoSuchElementException e) {}
    return false;
  }

  /**
   * @param p
   * @param q
   * @return double value t such that t * p == q
   * @throws NoSuchElementException
   */
  public static final double solve(final Point3D p, final Point3D q) throws NoSuchElementException {
    if (p.normalize().equals(q.normalize())) {
      if (p.equals(Point3D.ZERO))
        return 1;
      return q.magnitude() / p.magnitude();
    }
//    if (p.getX() == 0 && q.getX() != 0)
//      throw new NoSuchElementException();
//    if (p.getY() == 0 && q.getY() != 0)
//      throw new NoSuchElementException();
//    if (p.getZ() == 0 && q.getZ() != 0)
//      throw new NoSuchElementException();
//    if (q.getX() == 0 && q.getY() == 0 && q.getZ() == 0)
//      return 1d;
//    if (p.getX() != 0 && q.getX() != 0) {
//      final double t = q.getX() / p.getX();
//      if (t * p.getY() == q.getY() && t * p.getZ() == q.getZ())
//        return t;
//    } else if (p.getY() != 0 && q.getY() != 0) {
//      final double t = q.getY() / p.getY();
//      if (t * p.getX() == q.getX() && t * p.getZ() == q.getZ())
//        return t;
//    } else if (p.getZ() != 0 && q.getZ() != 0) {
//      final double t = q.getZ() / p.getZ();
//      if (t * p.getX() == q.getX() && t * p.getY() == q.getY())
//        return t;
//    }
    throw new NoSuchElementException();
  }

  public static final boolean
      parallelLineSegments(final Point3D p1, final Point3D p2, final Point3D q1, final Point3D q2) {
    final Point3D p = p2.subtract(p1);
    final Point3D q = q2.subtract(q1);
    // length(minus(divide(p, length(p)), divide(q, length(q))))
    if (p.normalize().crossProduct(q.normalize()).magnitude() < 0.01d)
      return true;
    return false;
  }

  public static final double cosAngle(final Point3D p1, final Point3D p2, final Point3D q1, final Point3D q2) {
    final Point3D p = p2.subtract(p1);
    final Point3D q = q2.subtract(q1);
    return p.normalize().dotProduct(q.normalize());
  }

//  public static final Function<Point3D, Double>  X_PROJECTION  = new Function<Point3D, Double>() {
//
//                                                                 @Override
//                                                                 public final Double apply(final Point3D point) {
//                                                                   return point.getX();
//                                                                 }
//                                                               };
//  public static final Function<Point3D, Double>  Y_PROJECTION  = new Function<Point3D, Double>() {
//
//                                                                 @Override
//                                                                 public final Double apply(final Point3D point) {
//                                                                   return point.getY();
//                                                                 }
//                                                               };
//  public static final Function<Point3D, Double>  Z_PROJECTION  = new Function<Point3D, Double>() {
//
//                                                                 @Override
//                                                                 public final Double apply(final Point3D point) {
//                                                                   return point.getZ();
//                                                                 }
//                                                               };
//  public static final Function<Point3D, Point3D> XY_PROJECTION = new Function<Point3D, Point3D>() {
//
//                                                                 @Override
//                                                                 public final Point3D apply(final Point3D point) {
//                                                                   return Point3DBuilder
//                                                                       .create()
//                                                                       .x(point.getX())
//                                                                       .y(point.getY())
//                                                                       .z(0)
//                                                                       .build();
//                                                                 }
//                                                               };

  /**
   * Returns t such that p = q1 + t * (q2 - q1)
   * 
   * @param p
   * @param q1
   * @param q2
   * @return
   */
  public static final double solve(final Point3D p, final Point3D q1, final Point3D q2) {
    final Point3D q = q2.subtract(q1);
    final Point3D x = p.subtract(q1);
    return x.dotProduct(q) / q.dotProduct(q);
  }

  public static final Point3D projectToLine(final Point3D p, final Point3D q1, final Point3D q2) {
    final double t = solve(p, q1, q2);
    return q1.add(q2.subtract(q1).multiply(t));
  }

  public static final Point3D projectToLineSegment(final Point3D p, final Point3D q1, final Point3D q2) {
    final double t = Math.min(Math.max(solve(p, q1, q2), 0), 1);
    return q1.add(q2.subtract(q1).multiply(t));
  }

  public static final Point3D shortestVectorFromLineSegment(final Point3D p, final Point3D q1, final Point3D q2) {
    return p.subtract(projectToLineSegment(p, q1, q2));
  }

}
