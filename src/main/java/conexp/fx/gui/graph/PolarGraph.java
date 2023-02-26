package conexp.fx.gui.graph;

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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.ujmp.core.util.RandomSimple;

import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.core.layout.ChainDecomposer;
import conexp.fx.core.math.Math3;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class PolarGraph<G, M> {

  private final Stage      primaryStage;
  private final AnchorPane rootPane;

  public PolarGraph(final ConceptLattice<G, M> lattice) {
    super();
    this.primaryStage = new Stage();
    this.rootPane = new AnchorPane();
    this.rootPane.setCenterShape(true);
    this.primaryStage.setScene(new Scene(rootPane, 1280, 800));
    this.initialize(lattice);
  }

  private void initialize(final ConceptLattice<G, M> lattice) {
    final ChainDecomposer<Set<Integer>> chainDecomposer = new ChainDecomposer<Set<Integer>>(
        lattice.context.selection._reduced.clone().attributeQuasiOrder().neighborhood());
    final Random rng = new RandomSimple();
    final Map<M, Point2D> seeds = new HashMap<M, Point2D>();
    final Map<Concept<G, M>, Point2D> positions = new HashMap<Concept<G, M>, Point2D>();
    final Set<Set<Set<Integer>>> chains = chainDecomposer.randomChainDecomposition();
    final int w = chains.size();
    if (w == 1) {
      final Point2D seed = new Point2D(1d, 0d);
      for (M m : Collections2.transform(Iterables.getOnlyElement(chains), lattice.context.selection._firstAttribute))
        seeds.put(m, seed);
    } else {
      int i = 0;
      for (Set<Set<Integer>> chain : chains) {
        final Point2D seed = new Point2D(1d, ((double) i) / ((double) w) * 2d * Math.PI);
        for (M m : Collections2.transform(chain, lattice.context.selection._firstAttribute))
          seeds.put(m, seed);
        i++;
      }
    }
    for (Concept<G, M> concept : lattice.rowHeads())
      if (!concept.equals(lattice.context.bottomConcept())) {
        positions.put(
            concept,
            PolarPoints.polarSum(
                seeds
                    .entrySet()
                    .stream()
                    .filter(e -> concept.intent().contains(e.getKey()))
                    .map(e -> e.getValue())
                    .collect(Collectors.toList())));
      }
    positions.values().forEach(this::addCircleAt);
    for (Concept<G, M> c1 : lattice.rowHeads())
      if (!c1.equals(lattice.context.bottomConcept()))
        for (Concept<G, M> c2 : lattice.col(c1))
          if (!c2.equals(lattice.context.bottomConcept()))
            drawLine(positions.get(c1), positions.get(c2));
  }

  private final void drawLine(final Point2D p, final Point2D q) {
    final Point2D _p = transform(p);
    final Point2D _q = transform(q);
    final Arc e = new Arc(
        rootPane.getWidth() / 2d,
        rootPane.getHeight() / 2d,
        100d * p.getX(),
        100d * q.getX(),
        p.getY(),
        q.getY() - p.getY());

    rootPane.getChildren().add(e);
//    rootPane.getChildren().add(new Line(_p.getX(), _p.getY(), _q.getX(), _q.getY()));
  }

  private final void addCircleAt(final Point2D p) {
    final Point2D q = transform(p);
    rootPane.getChildren().add(new Circle(q.getX(), q.getY(), 10d, Color.CHARTREUSE));
  }

  private Point2D transform(final Point2D p) {
    final Point2D q = PolarPoints.toCartesian(p);
    return new Point2D(rootPane.getWidth() / 2d + 100d * q.getX(), rootPane.getHeight() / 2d + 100d * q.getY());
  }

//  private final Point2D getPolarSum(Stream<Point2D> points) {
//    final DoubleAdder radius = new DoubleAdder();
//    final DoubleAdder angle = new DoubleAdder();
//    final LongAdder count = new LongAdder();
//    points.forEach(p -> {
//      radius.add(p.getX());
//      angle.add(p.getY());
//      count.increment();
//    });
//    return new Point2D(radius.doubleValue(), angle.doubleValue() / count.doubleValue());
//  }

  public static final class PolarPoints {

    public static final double angleDistance(final double a, final double b) {
      return Math.min(Math3.modulo(a - b, 360d), Math3.modulo(b - a, 360d));
    }

    public static final double averageAngle(final double a, final double b) {
      if (a == b)
        return a;
      final double _a = Math3.modulo(a, 360d);
      final double _b = Math3.modulo(b, 360d);
      if (_a == _b)
        return _a;
      final double d = angleDistance(_a, _b) / 2d;
      if (Math3.modulo(_b - _a, 360d) < Math3.modulo(_a - _b, 360d))
        return Math3.modulo(_a + d, 360d);
      else
        return Math3.modulo(_b + d, 360d);
    }

    public static final Point2D polarSum(final Point2D... ps) {
      return polarSum(Arrays.asList(ps));
    }

    public static final Point2D polarSum(final Collection<Point2D> ps) {
      final double r = ps.stream().map(p -> p.getX()).reduce(0d, Double::sum);
      final double a =
          toPolar(ps.stream().map(PolarPoints::toCartesian).reduce(new Point2D(0, 0), (p, q) -> p.add(q))).getY();
      return new Point2D(r, a);
    }

    public static final Point2D toPolar(final Point2D q) {
      final double r = Math.sqrt(q.getX() * q.getX() + q.getY() * q.getY());
      final double a = Math.atan2(q.getX(), q.getY());
      return new Point2D(r, a);
    }

    public static final Point2D toCartesian(final Point2D p) {
      final double x = p.getX() * Math.sin(p.getY());
      final double y = p.getX() * Math.cos(p.getY());
      return new Point2D(x, y);
    }
  }

  public final void show() {
    this.primaryStage.show();
  }

//  public static final void main(String[] args) {
////    System.out.println(angleDistance(300, 60));
////    System.out.println(angleDistance(60, 300));
////    System.out.println(angleDistance(300 + 360, 60 + 720));
////    System.out.println(angleDistance(60 - 720, 300));
////    System.out.println(averageAngle(300, 60));
////    System.out.println(averageAngle(60, 300));
////    System.out.println(averageAngle(300 + 360, 60 + 720));
////    System.out.println(averageAngle(60 - 720, 300));
////    System.out.println(averageAngle(0, 10));
////    System.out.println(averageAngle(10, 0));
////    System.out.println(averageAngle(0, 350));
////    System.out.println(averageAngle(350, 0));
////    System.out.println(averageAngle(180, 270));
//  }
}
