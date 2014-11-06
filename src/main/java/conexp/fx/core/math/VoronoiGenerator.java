package conexp.fx.core.math;

/*
 * #%L
 * Concept Explorer FX - Core
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
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
