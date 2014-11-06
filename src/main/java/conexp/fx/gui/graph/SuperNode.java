package conexp.fx.gui.graph;

/*
 * #%L
 * Concept Explorer FX - Graphical User Interface
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


import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcBuilder;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CircleBuilder;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;

public class SuperNode extends Group {

  public enum State {
    CIRCLE,
    ARC;
  }

  private State state;

  public final State getState() {
    return state;
  }

  public SuperNode(final double radius, final Paint fill) {
    toCircle(radius, fill);
  }

  public SuperNode(
      final double innerRadius,
      final double outerRadius,
      final double startAngle,
      final double length,
      final Paint fill) {
    toArc(innerRadius, outerRadius, startAngle, length, fill);
  }

  public void toCircle(final double radius, final Paint fill) {
    getChildren().clear();
    getChildren().add(
        CircleBuilder
            .create()
            .radius(radius)
            .fill(fill)
            .strokeType(StrokeType.OUTSIDE)
            .stroke(Color.BLACK)
            .strokeWidth(1d)
            .build());
    state = State.CIRCLE;
  }

  public void toArc(
      final double innerRadius,
      final double outerRadius,
      final double startAngle,
      final double length,
      final Paint fill) {
    getChildren().clear();
    getChildren().add(newBackRectangle(2 * outerRadius));
    getChildren().add(newCircularSegment(innerRadius, outerRadius, startAngle, length, fill));
    state = State.ARC;
  }

  private final Rectangle newBackRectangle(final double size) {
    final Rectangle back = RectangleBuilder.create().build();
    back.setWidth(size);
    back.setHeight(size);
    back.setFill(Color.TRANSPARENT);
    return back;
  }

  private final Shape newCircularSegment(
      final double innerRadius,
      final double outerRadius,
      final double startAngle,
      final double length,
      final Paint fill) {
    final Circle inner = CircleBuilder.create().centerX(outerRadius).centerY(outerRadius).radius(innerRadius).build();
    final Arc outer =
        ArcBuilder
            .create()
            .centerX(outerRadius)
            .centerY(outerRadius)
            .radiusX(outerRadius)
            .radiusY(outerRadius)
            .startAngle(startAngle)
            .type(ArcType.ROUND)
            .length(length)
            .build();
    final Shape segmentArc = Shape.subtract(outer, inner);
    segmentArc.setFill(fill);
    segmentArc.setStrokeType(StrokeType.CENTERED);
    segmentArc.setStroke(Color.BLACK);
    segmentArc.setStrokeWidth(0.5d);
    return segmentArc;
  }
}