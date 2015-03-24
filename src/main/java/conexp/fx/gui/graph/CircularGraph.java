package conexp.fx.gui.graph;

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


import java.util.Random;
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

  public void show(final ConceptLattice<G, M> lattice) {
    final Stage primaryStage = new Stage();
    final Pane rootPane = new StackPane();
    primaryStage.setScene(new Scene(rootPane, 1280, 800));
    primaryStage.show();
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
    int rnd = (int) (new Random().nextFloat() * 23f);
    int h = o.hashCode() * rnd;
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
