package conexp.fx.core.math;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import java.util.Collection;
import java.util.List;

import javafx.geometry.Point3D;
import be.humphreys.simplevoronoi.GraphEdge;
import be.humphreys.simplevoronoi.Voronoi;

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
