package conexp.fx.core.math;

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


import java.util.Collection;
import java.util.List;

import be.humphreys.simplevoronoi.GraphEdge;
import be.humphreys.simplevoronoi.Voronoi;
import javafx.geometry.Point3D;

public class VoronoiGenerator {

  public static final <G, M> List<GraphEdge> generate(
      final Collection<Point3D> points,
      final double minX,
      final double maxX,
      final double minY,
      final double maxY) {
    final float minDistanceBetweenSites = 0.00001f;
    final int size = points.size();
    final double[] latValues = new double[size];
    final double[] lngValues = new double[size];
    int i = 0;
    for (Point3D p : points) {
      latValues[i] = p.getX();
      lngValues[i] = p.getY();
      i++;
    }
    return new Voronoi(minDistanceBetweenSites).generateVoronoi(latValues, lngValues, minX, maxX, minY, maxY);
  }
}
