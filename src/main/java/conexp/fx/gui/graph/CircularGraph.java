package conexp.fx.gui.graph;

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

import java.util.Set;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;

public class CircularGraph<G, M> {

  private final Stage primaryStage;

  public CircularGraph(final ConceptLattice<G, M> lattice) {
    super();
    this.primaryStage = new Stage();
    final Pane rootPane = new StackPane();
    this.primaryStage.setScene(new Scene(rootPane, 1280, 800));
    final Multimap<Concept<G, M>, Area> layers = computeLayers(lattice, 360d, 36d);
    final Multimap<Concept<G, M>, SuperNode> nodes = HashMultimap.<Concept<G, M>, SuperNode> create();
    for (Concept<G, M> c : layers.keySet())
      for (Area a : layers.get(c)) {
        final SuperNode n = new SuperNode(a.y, a.y + a.h, a.x, a.w, fromHashCode(c));
        nodes.put(c, n);
        rootPane.getChildren().add(n);
        addListener(c, n, nodes);
      }
  }

  public final void show() {
    this.primaryStage.show();
  }

  private void addListener(final Concept<G, M> c, final SuperNode n, final Multimap<Concept<G, M>, SuperNode> nodes) {
    ((Shape) n.getChildren().get(1)).addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {

      @Override
      public void handle(MouseEvent event) {
        for (SuperNode node : nodes.get(c))
          ((Shape) node.getChildren().get(1)).setFill(Color.RED);
      }
    });
    ((Shape) n.getChildren().get(1)).addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {

      @Override
      public void handle(MouseEvent event) {
        for (SuperNode node : nodes.get(c))
          ((Shape) node.getChildren().get(1)).setFill(fromHashCode(c));
      }
    });
  }

  private Color fromHashCode(final Object o) {
//    int rnd = (int) (new Random().nextFloat() * 23f);
    int h = o.hashCode();// * rnd;
    int m = 192;
    int r = (h * 7) % m;
    int g = (h * 17) % m;
    int b = (h * 37) % m;
    if (r < 0)
      r += m;
    if (g < 0)
      g += m;
    if (b < 0)
      b += m;
    r += (256 - m);
    g += (256 - m);
    b += (256 - m);
    return Color.rgb(r, g, b);
  }

  private class Area {

    private final double x, y, w, h;

    public Area(double x, double y, double w, double h) {
      super();
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
    }

  }

  private Multimap<Concept<G, M>, Area> computeLayers(
      final ConceptLattice<G, M> lattice,
      final double width,
      final double layerHeight) {
    final Multimap<Concept<G, M>, Area> layers = HashMultimap.<Concept<G, M>, Area> create();
    final Concept<G, M> top = lattice.context.topConcept();
    final Area parent = new Area(0, 0, width, layerHeight);
    layers.put(top, parent);
    addNextLayer(lattice, layers, parent, lattice.col(top));
    return layers;
  }

  private void addNextLayer(
      final ConceptLattice<G, M> lattice,
      final Multimap<Concept<G, M>, Area> layers,
      final Area parent,
      final Set<Concept<G, M>> concepts) {
    double l = parent.w / (double) concepts.size();
    double t = parent.x;
    for (Concept<G, M> c : concepts) {
      final Area a = new Area(t, parent.y + parent.h, l, parent.h);
      layers.put(c, a);
      addNextLayer(lattice, layers, a, lattice.col(c));
      t += l;
    }
  }
}
