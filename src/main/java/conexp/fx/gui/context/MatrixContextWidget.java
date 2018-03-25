package conexp.fx.gui.context;

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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.semanticweb.owlapi.model.OWLClassExpression;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import conexp.fx.core.collections.Pair;
import conexp.fx.core.collections.relation.RelationEvent;
import conexp.fx.core.collections.relation.RelationEventHandler;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.context.MatrixContext.Incidence;
import conexp.fx.core.util.Constants;
import conexp.fx.core.util.IdGenerator;
import conexp.fx.core.util.OWLUtil;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.cellpane.Cell;
import conexp.fx.gui.cellpane.CellPane;
import conexp.fx.gui.cellpane.InteractionMode;
import conexp.fx.gui.dataset.FCADataset;
import conexp.fx.gui.graph.ConceptGraph;
import conexp.fx.gui.util.ColorScheme;
import conexp.fx.gui.util.LaTeX;
import conexp.fx.gui.util.Platform2;
import de.tudresden.inf.tcs.fcalib.Implication;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Slider;
import javafx.scene.control.SliderBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class MatrixContextWidget<G, M> extends BorderPane {

  public final class RowHeaderPane extends CellPane<RowHeaderPane, RowHeaderCell> {

    private RowHeaderPane(final boolean interactive) {
      super("DomainPane", InteractionMode.ROWS, interactive);
      this.rowHeightDefault.bind(MatrixContextWidget.this.cellSizeDefault);
      this.columnWidthDefault.bind(MatrixContextWidget.this.rowHeaderSizeDefault);
      this.zoomFactor.bind(MatrixContextWidget.this.zoomFactor);
      this.textSizeDefault.bind(MatrixContextWidget.this.textSizeDefault);
      this.animate.bind(MatrixContextWidget.this.animate);
      this.autoSizeRows.set(false);
      this.autoSizeColumns.set(true);
      this.maxColumns.set(1);
      this.maxRows.set(context.rowHeads().size());
//      final RelationEventHandler<G, M> eventHandler = new RelationEventHandler<G, M>() {
//
//        public final void handle(final RelationEvent<G, M> event) {
//          Platform2.runOnFXThread(new Runnable() {
//
//            public void run() {
////              System.out.println("updating rows");
//              maxRows.set(context.rowHeads().size());
////              updateContent();
//            }
//          });
//        }
//      };
//      context.addEventHandler(eventHandler, RelationEvent.ROWS);
//      context.addEventHandler(eventHandler, RelationEvent.ROWS_ADDED);
//      context.addEventHandler(eventHandler, RelationEvent.ROWS_REMOVED);
      context.addEventHandler(
          __ -> Platform2.runOnFXThread(() -> maxRows.set(context.rowHeads().size())),
          RelationEvent.ROWS);
      if (dataset != null)
        this.interactionPane.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {

          public final void handle(final MouseEvent event) {
            dataset.conceptGraph.highlight(false, dataset.conceptGraph.highlightRequests.dehighlight());
          }
        });
      zoomFactor.addListener((__, ___, ____) -> updateContent());
    }

    protected final RowHeaderCell createCell(final int row, final int column) {
      return new RowHeaderCell(row);
    }

    public final void highlightConcept(final Collection<Integer> domainIndices) {
      highlight(domainIndices, null);
    }

    protected final Map<G, Node> decorations = new ConcurrentHashMap<>();

    public final void addDecoration(final G row, final Node decoration) {
      decorations.put(row, decoration);
    }
  }

  public final class ColHeaderPane extends CellPane<ColHeaderPane, ColHeaderCell> {

    private ColHeaderPane(final boolean interactive) {
      super("CodomainPane", InteractionMode.COLUMNS, interactive);
      this.rowHeightDefault.bind(MatrixContextWidget.this.colHeaderSizeDefault);
      this.columnWidthDefault.bind(MatrixContextWidget.this.cellSizeDefault);
      this.zoomFactor.bind(MatrixContextWidget.this.zoomFactor);
      this.textSizeDefault.bind(MatrixContextWidget.this.textSizeDefault);
      this.animate.bind(MatrixContextWidget.this.animate);
      this.autoSizeRows.set(true);
      this.autoSizeColumns.set(false);
      this.maxRows.set(1);
      this.maxColumns.set(context.colHeads().size());
      final RelationEventHandler<G, M> eventHandler = new RelationEventHandler<G, M>() {

        public final void handle(final RelationEvent<G, M> event) {
          Platform2.runOnFXThread(new Runnable() {

            public void run() {
//              System.out.println("updating columns");
              maxColumns.set(context.colHeads().size());
//              updateContent();
            }
          });
        }
      };
      context.addEventHandler(eventHandler, RelationEvent.COLUMNS);
//      context.addEventHandler(eventHandler, RelationEvent.COLUMNS_ADDED);
//      context.addEventHandler(eventHandler, RelationEvent.COLUMNS_REMOVED);
      if (dataset != null)
        this.interactionPane.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {

          public final void handle(final MouseEvent event) {
            dataset.conceptGraph.highlight(false, dataset.conceptGraph.highlightRequests.dehighlight());
          }
        });
      zoomFactor.addListener((__, ___, ____) -> updateContent());
    }

    protected final ColHeaderCell createCell(final int row, final int column) {
      return new ColHeaderCell(column);
    }

    public final void highlightConcept(final Collection<Integer> codomainIndices) {
      highlight(null, codomainIndices);
    }
  }

  public final class ContextPane extends CellPane<ContextPane, ContextCell> {

    private ContextPane(final boolean interactive) {
      super("FormalContextPane", InteractionMode.ROWS_AND_COLUMNS, interactive);
      this.textSizeDefault.bind(MatrixContextWidget.this.incidenceSizeDefault);
      this.zoomFactor.bind(MatrixContextWidget.this.zoomFactor);
      this.bind(rowHeaderPane, InteractionMode.ROWS);
      this.bind(colHeaderPane, InteractionMode.COLUMNS);
      if (dataset != null)
        this.rowMap.addListener(new MapChangeListener<Integer, Integer>() {

          public final void onChanged(
              final javafx.collections.MapChangeListener.Change<? extends Integer, ? extends Integer> change) {
            dataset.unsavedChanges.set(true);
          }
        });
      if (dataset != null)
        this.columnMap.addListener(new MapChangeListener<Integer, Integer>() {

          public final void onChanged(
              final javafx.collections.MapChangeListener.Change<? extends Integer, ? extends Integer> change) {
            dataset.unsavedChanges.set(true);
          }
        });
      if (dataset != null)
        this.interactionPane.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {

          public final void handle(final MouseEvent event) {
            dataset.conceptGraph.highlight(false, dataset.conceptGraph.highlightRequests.dehighlight());
          }
        });
      zoomFactor.addListener((__, ___, ____) -> updateContent());
    }

    protected final ContextCell createCell(final int row, final int column) {
      return new ContextCell(row, column);
    }
  }

  public final class RowHeaderCell extends Cell<RowHeaderCell, RowHeaderPane> {

    private ImageView view = ImageViewBuilder.create().build();

    private RowHeaderCell(final int row) {
      super(rowHeaderPane, row, 0, Pos.CENTER_RIGHT, TextAlignment.RIGHT, false, null, false);
      this.contentPane.get().getChildren().add(view);
      this.contentPane.get().text.setOpacity(0);
      if (cellPane.interactive) {
        final ContextMenu contextMenu = new ContextMenu();
        final MenuItem editItem = new MenuItem("Edit");
        final MenuItem removeItem = new MenuItem("Remove");
        final MenuItem selectItem = new MenuItem("Select");
        final MenuItem insertItem = new MenuItem("Insert");
        if (dataset != null && dataset.editable)
          contextMenu.getItems().addAll(editItem, removeItem, selectItem, insertItem);
        else
          contextMenu.getItems().addAll(removeItem, selectItem);
        insertItem.setOnAction(__ -> {
          ((FCADataset<String, String>) dataset).addObject(
              (dataset.context.isHomogen() ? "Element " : "Object ") + IdGenerator.getNextId(dataset),
              contentCoordinates.get().x());
          rowHeaderPane.rowOpacityMap.keySet().stream().sorted().filter(i -> i >= contentCoordinates.get().x()).forEach(
              i -> rowHeaderPane.rowOpacityMap.put(i + 1, rowHeaderPane.rowOpacityMap.remove(i)));
        });
        editItem.setOnAction(__ -> {
          if (MatrixContextWidget.this.dataset.editable) {
            final G object = context.rowHeads().get(contentCoordinates.get().x());
            final TextField textField = TextFieldBuilder.create().text((String) object).build();
            textField.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
              switch (event.getCode()) {
              case ENTER:
                dataset.renameObject(object, (G) textField.getText().trim());
              case ESCAPE:
                interactionPane.get().getChildren().remove(textField);
              }
            });
            interactionPane.get().getChildren().add(textField);
            textField.focusedProperty().addListener(
                (observable, oldValue, newValue) -> new Timer().schedule(new TimerTask() {

              public final void run() {
                Platform.runLater(() -> textField.selectAll());
              }
            }, 20));
            textField.requestFocus();
          } else {
            System.out.println("no instance of MatrixContextWidget");
          }
        });
        removeItem.setOnAction(__ -> {
          dataset.removeObject(context.rowHeads().get(contentCoordinates.get().x()));
          rowHeaderPane.rowOpacityMap.keySet().stream().sorted().filter(i -> i > contentCoordinates.get().x()).forEach(
              i -> rowHeaderPane.rowOpacityMap.put(i - 1, rowHeaderPane.rowOpacityMap.remove(i)));
        });
        selectItem.setOnAction(__ -> select());
        this.interactionPane.get().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
          switch (event.getButton()) {
          case PRIMARY:
            select();
            break;
          case SECONDARY:
            contextMenu.show(interactionPane.getValue(), event.getScreenX(), event.getScreenY());
          }
        });
      }
      if (dataset != null)
        this.interactionPane.get().addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
          if (contextPane.highlight.get())
            dataset.conceptGraph.highlight(
                true,
                dataset.conceptGraph.highlightRequests.object(context.rowHeads().get(contentCoordinates.get().x())));
        });
      if (cellPane.autoSizeRows.get() || cellPane.autoSizeColumns.get())
        view.layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {

          @Override
          public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
            final double decorationWidth =
                cellPane.decorations.containsKey(context.rowHeads().get(contentCoordinates.get().x()))
                    ? cellPane.decorations
                        .get(context.rowHeads().get(contentCoordinates.get().x()))
                        .layoutBoundsProperty()
                        .get()
                        .getWidth()
                    : 0d;
            final double width = newValue.getWidth() + decorationWidth;
            if (width > cellPane.maximalTextWidth.get())
              cellPane.maximalTextWidth.set(width);
          }
        });
      context.addEventHandler(event -> Platform2.runOnFXThread(RowHeaderCell.this::updateContent), RelationEvent.ROWS);
      updateContent();
    }

    private final void select() {
      synchronized (rowHeaderPane.rowOpacityMap) {
        if (rowHeaderPane.rowOpacityMap.containsKey(contentCoordinates.get().x())) {
          if (dataset != null)
            dataset.conceptGraph.highlight(false, dataset.conceptGraph.highlightRequests.dehighlight());
          rowHeaderPane.rowOpacityMap.remove(contentCoordinates.get().x());
          if (dataset != null)
            dataset.selectObject(context.rowHeads().get(contentCoordinates.get().x()));
        } else {
          if (dataset != null)
            dataset.conceptGraph.highlight(false, dataset.conceptGraph.highlightRequests.dehighlight());
          rowHeaderPane.rowOpacityMap.put(contentCoordinates.get().x(), Constants.HIDE_OPACITY);
          if (dataset != null)
            dataset.ignoreObject(context.rowHeads().get(contentCoordinates.get().x()));
        }
      }
//    if (rowHeaderPane.rowOpacityMap.containsKey(contentCoordinates.get().x())) {
//    rowHeaderPane.rowOpacityMap.remove(contentCoordinates.get().x());
////  final G object = context.getDomain().get(contentCoordinates.get().x());
////  context.selectObject(object);
//  } else {
//    rowHeaderPane.rowOpacityMap.put(contentCoordinates.get().x(), Constants.HIDE_OPACITY);
////  final G object = context.getDomain().get(contentCoordinates.get().x());
////  context.deselectObject(object);
//  }
    }

    public final void updateContent() {
      try {
        final String string = context.rowHeads().get(contentCoordinates.get().x()).toString();
        textContent.set(string);
        view.setImage(LaTeX.toFXImage(string, (float) (16d * zoomFactor.get())));
        if (cellPane.decorations.containsKey(context.rowHeads().get(contentCoordinates.get().x()))) {
          interactionPane.get().getChildren().removeAll(cellPane.decorations.values());
          interactionPane
              .get()
              .setRight(cellPane.decorations.get(context.rowHeads().get(contentCoordinates.get().x())));
        }
      } catch (IndexOutOfBoundsException __) {}
    }
  }

  public final class ColHeaderCell extends Cell<ColHeaderCell, ColHeaderPane> {

    private ImageView view = ImageViewBuilder.create().build();

    private ColHeaderCell(final int column) {
      super(colHeaderPane, 0, column, Pos.CENTER_LEFT, TextAlignment.LEFT, true, null, false);
      this.contentPane.get().getChildren().add(view);
      this.contentPane.get().text.setOpacity(0);
      if (cellPane.interactive) {
        final ContextMenu contextMenu = new ContextMenu();
        final MenuItem editItem = new MenuItem("Edit");
        final MenuItem removeItem = new MenuItem("Remove");
        final MenuItem selectItem = new MenuItem("Select");
        final MenuItem insertItem = new MenuItem("Insert");
        if (dataset != null && dataset.editable)
          contextMenu.getItems().addAll(editItem, removeItem, selectItem, insertItem);
        else
          contextMenu.getItems().addAll(removeItem, selectItem);
        insertItem.setOnAction(__ -> {
          ((FCADataset<String, String>) dataset).addAttribute(
              (dataset.context.isHomogen() ? "Element " : "Attribute ") + IdGenerator.getNextId(dataset),
              contentCoordinates.get().y());
          colHeaderPane.columnOpacityMap
              .keySet()
              .stream()
              .sorted()
              .filter(i -> i >= contentCoordinates.get().y())
              .forEach(i -> colHeaderPane.columnOpacityMap.put(i + 1, colHeaderPane.columnOpacityMap.remove(i)));
        });
        editItem.setOnAction(event -> {
          if (MatrixContextWidget.this.dataset.editable) {
            final M attribute = context.colHeads().get(contentCoordinates.get().y());
            final TextField textField = TextFieldBuilder.create().text((String) attribute).build();
            textField.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
              switch (keyEvent.getCode()) {
              case ENTER:
                dataset.renameAttribute(attribute, (M) textField.getText().trim());
              case ESCAPE:
                interactionPane.get().getChildren().remove(textField);
              }
            });
            textField.rotateProperty().set(-90);
            textField.setMinSize(colHeaderPane.rowHeight.get(), cellSize.get());
            textField.setMaxSize(colHeaderPane.rowHeight.get(), cellSize.get());
            interactionPane.get().getChildren().add(textField);
            textField.focusedProperty().addListener(
                (observable, oldValue, newValue) -> new Timer().schedule(new TimerTask() {

              public final void run() {
                Platform.runLater(() -> textField.selectAll());
              }
            }, 20));
            textField.requestFocus();
          }
        });
        removeItem.setOnAction(event -> {
          dataset.removeAttribute(context.colHeads().get(contentCoordinates.get().y()));
          colHeaderPane.columnOpacityMap.remove(contentCoordinates.get().y());
          colHeaderPane.columnOpacityMap
              .keySet()
              .stream()
              .sorted()
              .filter(i -> i > contentCoordinates.get().y())
              .forEach(i -> colHeaderPane.columnOpacityMap.put(i - 1, colHeaderPane.columnOpacityMap.remove(i)));
        });
        selectItem.setOnAction(event -> select());
//      if (!tab.fca.context.selectedAttributes().contains(tab.fca.context.colHeads().get(contentCoordinates.get().y())))
//        colHeaderPane.columnOpacityMap.put(contentCoordinates.get().y(), Constants.HIDE_OPACITY);
        this.interactionPane.get().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
          switch (event.getButton()) {
          case PRIMARY:
            select();
            break;
          case SECONDARY:
            contextMenu.show(interactionPane.getValue(), event.getScreenX(), event.getScreenY());
          }
        });
      }
      if (dataset != null)
        this.interactionPane.get().addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
          if (contextPane.highlight.get()) {
            final M m = context.colHeads().get(contentCoordinates.get().y());
            if (context.selectedAttributes().contains(m))
              dataset.conceptGraph.highlight(true, dataset.conceptGraph.highlightRequests.attribute(m));
          }
        });
      if (cellPane.autoSizeRows.get() || cellPane.autoSizeColumns.get())
        view.layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {

          @Override
          public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
            final double width = newValue.getWidth();
            if (width > cellPane.maximalTextWidth.get())
              cellPane.maximalTextWidth.set(width);
          }
        });
      context
          .addEventHandler(event -> Platform2.runOnFXThread(ColHeaderCell.this::updateContent), RelationEvent.COLUMNS);
      updateContent();
    }

    private void select() {
      synchronized (colHeaderPane.columnOpacityMap) {
        if (colHeaderPane.columnOpacityMap.containsKey(contentCoordinates.get().y())) {
          if (dataset != null)
            dataset.conceptGraph.highlight(false, dataset.conceptGraph.highlightRequests.dehighlight());
          colHeaderPane.columnOpacityMap.remove(contentCoordinates.get().y());
          if (dataset != null)
            dataset.selectAttribute(context.colHeads().get(contentCoordinates.get().y()));
        } else {
          if (dataset != null)
            dataset.conceptGraph.highlight(false, dataset.conceptGraph.highlightRequests.dehighlight());
          colHeaderPane.columnOpacityMap.put(contentCoordinates.get().y(), Constants.HIDE_OPACITY);
          if (dataset != null)
            dataset.ignoreAttribute(context.colHeads().get(contentCoordinates.get().y()));
        }
      }
    }

    public final void updateContent() {
      try {
        final M m = context.colHeads().get(contentCoordinates.get().y());
        final String string =
            m instanceof OWLClassExpression ? OWLUtil.toString((OWLClassExpression) m) : m.toString();
        textContent.set(string);
        view.setImage(LaTeX.toFXImage(string, (float) (16d * zoomFactor.get())));
      } catch (IndexOutOfBoundsException __) {}
    }
  }

  public final class ContextCell extends Cell<ContextCell, ContextPane> {

    private ContextCell(final int row, final int column) {
      super(contextPane, row, column, Pos.CENTER, TextAlignment.CENTER, false, null, false);
      if (dataset != null)
        this.interactionPane.get().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

          public final void handle(final MouseEvent event) {
            final G g = context.rowHeads().get(contentCoordinates.get().x());
            final M m = context.colHeads().get(contentCoordinates.get().y());
            dataset.conceptGraph.highlight(false, dataset.conceptGraph.highlightRequests.dehighlight());
            dataset.flip(g, m);
            dataset.unsavedChanges.set(true);
          }
        });
      else
        this.interactionPane.get().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
          final G g = context.rowHeads().get(contentCoordinates.get().x());
          final M m = context.colHeads().get(contentCoordinates.get().y());
          if (context.contains(g, m))
            context.remove(g, m);
          else
            context.add(g, m);
        });
      if (dataset != null)
        this.interactionPane.get().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {

          public final void handle(final MouseEvent event) {
            if (contextPane.highlight.get()) {
              final G g = context.rowHeads().get(contentCoordinates.get().x());
              final M m = context.colHeads().get(contentCoordinates.get().y());
              if (context.selectedAttributes().contains(m))
                if (textContent.get().equals(Constants.CROSS_CHARACTER))
                  dataset.conceptGraph.highlight(true, dataset.conceptGraph.highlightRequests.incidence(g, m));
                else if (textContent.get().equals(Constants.DOWN_ARROW_CHARACTER))
                  dataset.conceptGraph.highlight(true, dataset.conceptGraph.highlightRequests.downArrow(g, m));
                else if (textContent.get().equals(Constants.UP_ARROW_CHARACTER))
                  dataset.conceptGraph.highlight(true, dataset.conceptGraph.highlightRequests.upArrow(g, m));
                else if (textContent.get().equals(Constants.BOTH_ARROW_CHARACTER))
                  dataset.conceptGraph.highlight(true, dataset.conceptGraph.highlightRequests.bothArrow(g, m));
                else if (textContent.get().equals(Constants.NO_CROSS_CHARACTER))
                  dataset.conceptGraph.highlight(true, dataset.conceptGraph.highlightRequests.nonIncidence(g, m));
            }
          }
        });
      context.addEventHandler(event -> updateContent(), RelationEvent.ANY);
//          RelationEvent.ENTRIES_ADDED,
//          RelationEvent.ENTRIES_REMOVED,
//          RelationEvent.ALL_CHANGED,
//          RelationEvent.SELECTION_CHANGED);
      updateContent();
    }

    @SuppressWarnings("incomplete-switch")
    public final void updateContent() {
      try {
        final G g = context.rowHeads().get(contentCoordinates.get().x());
        final M m = context.colHeads().get(contentCoordinates.get().y());
        if (context.selectedAttributes().contains(m) && context.selectedObjects().contains(g)) {
          final Pair<Incidence, Incidence> p = context.selection.getValue(g, m, showArrows.get(), showPaths.get());
          final Incidence first = p.first();
          final Incidence second = p.second();
          ContextCell.this.textContent.set(
              second != null && first == Incidence.NO_CROSS ? Constants.NO_CROSS_CHARACTER_BOLD : first.toString());
          switch (first) {
          case BOTH_ARROW:
            ContextCell.this.contentPane.get().text.setRotate(-45d);
            break;
          case DOWN_ARROW:
          case UP_ARROW:
          case CROSS:
          case NO_CROSS:
            ContextCell.this.contentPane.get().text.setRotate(0d);
            break;
          }
          if (second == null)
            ContextCell.this.contentPane.get().text.setFill(Color.BLACK);
          else
            switch (second) {
            case BOTH_PATH:
              ContextCell.this.contentPane.get().text.setFill(ConceptGraph.COLOR_INTERVAL);
              break;
            case DOWN_PATH:
              ContextCell.this.contentPane.get().text.setFill(ConceptGraph.COLOR_LOWER);
              break;
            case UP_PATH:
              ContextCell.this.contentPane.get().text.setFill(ConceptGraph.COLOR_UPPER);
              break;
            }
        } else {
          ContextCell.this.textContent.set(context.getValue(g, m, false).first().toString());
          ContextCell.this.contentPane.get().text.setRotate(0d);
        }
      } catch (IndexOutOfBoundsException __) {}
    }
  }

  protected final FCADataset<G, M>    dataset;
  protected final MatrixContext<G, M> context;
  protected final GridPane            centerPane            = new GridPane();
  public final RowHeaderPane          rowHeaderPane;
  public final ColHeaderPane          colHeaderPane;
  public final ContextPane            contextPane;
  protected final ScrollBar           rowScrollBar;
  protected final ScrollBar           colScrollBar;
//  public final ListSpinner<Integer> zoomSpinner =new ListSpinner<Integer>(-4, 4, 1);
  public final Slider                 zoomSlider            = SliderBuilder
                                                                .create()
                                                                .min(-4d)
                                                                .max(4d)
                                                                .value(0d)
                                                                // .showTickMarks(true)
                                                                // .minorTickCount(17)
                                                                // .majorTickUnit(1d)
                                                                // .snapToTicks(true)
                                                                .blockIncrement(0.25d)
                                                                .build();
  public final DoubleProperty         zoomFactor            = new SimpleDoubleProperty(0.01d);
  public final IntegerProperty        rowHeaderSizeDefault  = new SimpleIntegerProperty(40);
  public final IntegerProperty        colHeaderSizeDefault  = new SimpleIntegerProperty(40);
  public final IntegerProperty        cellSizeDefault       = new SimpleIntegerProperty(20);
  public final IntegerProperty        textSizeDefault       = new SimpleIntegerProperty(16);
  public final IntegerProperty        incidenceSizeDefault  = new SimpleIntegerProperty(20);
  public final IntegerBinding         cellSize              = new IntegerBinding() {

                                                              {
                                                                super.bind(zoomFactor, cellSizeDefault);
                                                              }

                                                              protected int computeValue() {
                                                                return (int) (zoomFactor.get()
                                                                    * cellSizeDefault.doubleValue());
                                                              };
                                                            };
  public final IntegerBinding         textSize              = new IntegerBinding() {

                                                              {
                                                                super.bind(zoomFactor, textSizeDefault);
                                                              }

                                                              protected int computeValue() {
                                                                return (int) (zoomFactor.get()
                                                                    * textSizeDefault.doubleValue());
                                                              };
                                                            };
  public final IntegerBinding         incidenceSize         = new IntegerBinding() {

                                                              {
                                                                super.bind(zoomFactor, textSizeDefault);
                                                              }

                                                              protected int computeValue() {
                                                                return (int) (zoomFactor.get()
                                                                    * incidenceSizeDefault.doubleValue());
                                                              };
                                                            };
  public final BooleanProperty        animate               = new SimpleBooleanProperty(false);
  public final BooleanProperty        showArrows            = new SimpleBooleanProperty();
  public final BooleanProperty        showPaths             = new SimpleBooleanProperty();
  public final ToggleButton           highlightToggleButton = new ToggleButton();
  public final DoubleBinding          height;

  public MatrixContextWidget(final FCADataset<G, M> fcaInstance) {
    this(fcaInstance, true);
  }

  /**
   * @param dataset
   * @param withToolbar
   * @param orContext
   *          may only be set if fcaInstance is null, otherwise unexpected behaviour may occur.
   */
  @SafeVarargs
  public MatrixContextWidget(
      final FCADataset<G, M> dataset,
      final boolean withToolbar,
      final MatrixContext<G, M>... orContext) {
    this(dataset, withToolbar, true, orContext);
  }

  @SafeVarargs
  public MatrixContextWidget(
      final FCADataset<G, M> dataset,
      final boolean withToolbar,
      final boolean interactive,
      final MatrixContext<G, M>... orContext) {
    super();
    this.dataset = dataset;
    if (dataset == null && orContext[0] != null)
      this.context = orContext[0];
    else
      this.context = dataset.context;
    this.rowHeaderPane = new RowHeaderPane(interactive);
    this.colHeaderPane = new ColHeaderPane(interactive);
    this.contextPane = new ContextPane(interactive);
    this.rowScrollBar = contextPane.getRowScrollBar();
    this.colScrollBar = contextPane.getColumnScrollBar();
    centerPane.setHgap(4);
    centerPane.setVgap(4);
    rowHeaderPane.colorScheme.setValue(ColorScheme.JAVA_FX);
    colHeaderPane.colorScheme.setValue(ColorScheme.JAVA_FX);
    contextPane.colorScheme.setValue(ColorScheme.JAVA_FX);
    final RowConstraints firstRowConstraints = new RowConstraints();
    firstRowConstraints.minHeightProperty().bind(colHeaderPane.rowHeight);
    firstRowConstraints.maxHeightProperty().bind(colHeaderPane.rowHeight);
    final RowConstraints secondRowConstraints = new RowConstraints();
    secondRowConstraints.minHeightProperty().bind(cellSize);
    secondRowConstraints.prefHeightProperty().bind(new IntegerBinding() {

      {
        super.bind(rowHeaderPane.maxRows, cellSize);
      }

      protected final int computeValue() {
        return context.rowHeads().size() * cellSize.get();
      }
    });
    final RowConstraints thirdRowConstraints = new RowConstraints();
    thirdRowConstraints.minHeightProperty().bind(cellSize);
    thirdRowConstraints.maxHeightProperty().bind(cellSize);
    centerPane.getRowConstraints().addAll(firstRowConstraints, secondRowConstraints, thirdRowConstraints);
    final ColumnConstraints firstColumnConstraints = new ColumnConstraints();
    firstColumnConstraints.minWidthProperty().bind(rowHeaderPane.columnWidth);
    firstColumnConstraints.maxWidthProperty().bind(rowHeaderPane.columnWidth);
    final ColumnConstraints secondColumnConstraints = new ColumnConstraints();
    secondColumnConstraints.minWidthProperty().bind(cellSize);
    secondColumnConstraints.maxWidthProperty().bind(new IntegerBinding() {

      {
        super.bind(colHeaderPane.maxColumns, cellSize);
      }

      protected final int computeValue() {
        return context.colHeads().size() * cellSize.get();
      }
    });
    final ColumnConstraints thirdColumnConstraints = new ColumnConstraints();
    thirdColumnConstraints.minWidthProperty().bind(cellSize);
    thirdColumnConstraints.maxWidthProperty().bind(cellSize);
    centerPane.getColumnConstraints().addAll(firstColumnConstraints, secondColumnConstraints, thirdColumnConstraints);
    centerPane.add(contextPane.getContentAndInteractionStackPane(), 1, 1);
    centerPane.add(rowHeaderPane.getContentAndInteractionStackPane(), 0, 1);
    centerPane.add(colHeaderPane.getContentAndInteractionStackPane(), 1, 0);
    centerPane.add(rowScrollBar, 2, 1);
    centerPane.add(colScrollBar, 1, 2);
    this.setCenter(centerPane);
    if (withToolbar)
      createToolBar();
    else
      zoomFactor.set(Math.pow(2d, 0));
    final ChangeListener<Boolean> updateContentListener = new ChangeListener<Boolean>() {

      public final void
          changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue, final Boolean newValue) {
        contextPane.updateContent();
      }
    };
    showArrows.addListener(updateContentListener);
    showPaths.addListener(updateContentListener);
    height = new DoubleBinding() {

      {
        bind(
            colHeaderPane.heightProperty(),
            contextPane.heightProperty(),
            colHeaderPane.rowHeight,
            colHeaderPane.visibleRows,
            contextPane.rowHeight,
            contextPane.visibleRows);
      }

      @Override
      protected double computeValue() {
//        System.out.println(colHeaderPane.rowHeight.get());
//        System.out.println(colHeaderPane.visibleRows.get());
//        System.out.println(contextPane.rowHeight.get());
//        System.out.println(contextPane.visibleRows.get());
        final int h = colHeaderPane.rowHeight.get() * colHeaderPane.visibleRows.get()
            + contextPane.rowHeight.get() * contextPane.visibleRows.get();
//        System.out.println(h);
        return h;
      }
    };
    rowHeaderPane.toFront();
    colHeaderPane.toFront();
    final Timeline t = new Timeline();
    t.getKeyFrames().add(new KeyFrame(
        Duration.millis(1000),
        // new EventHandler<ActionEvent>() {
        //
        // public final void handle(final ActionEvent event) {
        // final Timeline s = new Timeline();
        // s.getKeyFrames().add(
        // new KeyFrame(Duration.millis(1000), new KeyValue(zoomSlider.valueProperty(), 0.9d, Interpolator.EASE_OUT)));
        // Platform.runLater(new Runnable() {
        //
        //
        // public final void run() {
        // s.play();
        // }
        // });
        // }
        // },
        new KeyValue(zoomSlider.valueProperty(), 0d, Interpolator.EASE_IN)));
    Platform.runLater(new Runnable() {

      public void run() {
        t.play();
      }
    });
    if (dataset != null && dataset.editable) {

      final Button domainButton = ButtonBuilder
          .create()
          // .text(conExpTab.fca.context.isHomogen() ? "New Element" : "New Object")
          .onAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {
              ((FCADataset<String, String>) dataset).addObject(
                  (dataset.context.isHomogen() ? "Element " : "Object ") + IdGenerator.getNextId(dataset),
                  -1);
            }
          })
          .build();
      final ImageView view =
          ImageViewBuilder.create().image(new Image(ConExpFX.class.getResourceAsStream("image/16x16/add.png"))).build();
      view.scaleXProperty().bind(zoomFactor);
      view.scaleYProperty().bind(zoomFactor);
      domainButton.setGraphic(view);
      domainButton.minWidthProperty().bind(rowHeaderPane.columnWidth);
      domainButton.maxWidthProperty().bind(rowHeaderPane.columnWidth);
      domainButton.minHeightProperty().bind(cellSize);
      domainButton.maxHeightProperty().bind(cellSize);
      domainButton.styleProperty().bind(new StringBinding() {

        {
          super.bind(textSize);
        }

        @Override
        protected String computeValue() {
          return "-fx-padding: 0; -fx-font-size: " + textSize.get() + ";";
        }
      });
      centerPane.add(domainButton, 0, 2);
      final Button codomainButton = ButtonBuilder
          .create()
          // .text(conExpTab.fca.context.isHomogen() ? "New Element" : "New Attribute")
          .onAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {
              ((FCADataset<String, String>) dataset).addAttribute(
                  (dataset.context.isHomogen() ? "Element " : "Attribute ") + IdGenerator.getNextId(dataset),
                  -1);
            }
          })
          .build();
      final ImageView view2 =
          ImageViewBuilder.create().image(new Image(ConExpFX.class.getResourceAsStream("image/16x16/add.png"))).build();
      view2.scaleXProperty().bind(zoomFactor);
      view2.scaleYProperty().bind(zoomFactor);
      codomainButton.setGraphic(view2);
      codomainButton.rotateProperty().set(-90);
      codomainButton.minWidthProperty().bind(colHeaderPane.rowHeight);
      codomainButton.maxWidthProperty().bind(colHeaderPane.rowHeight);
      codomainButton.minHeightProperty().bind(cellSize);
      codomainButton.maxHeightProperty().bind(cellSize);
      codomainButton.translateXProperty().bind(new DoubleBinding() {

        {
          super.bind(colHeaderPane.rowHeight, cellSize);
        }

        @Override
        protected double computeValue() {
          return -(colHeaderPane.rowHeight.get() - cellSize.get()) / 2d;
        }
      });
      codomainButton.styleProperty().bind(new StringBinding() {

        {
          super.bind(textSize);
        }

        @Override
        protected String computeValue() {
          return "-fx-padding: 0; -fx-font-size: " + textSize.get() + ";";
        }
      });
      centerPane.add(codomainButton, 2, 0);
    }
  }

  private final void createToolBar() {
    zoomFactor.bind(new DoubleBinding() {

      {
        bind(zoomSlider.valueProperty());
      }

      protected double computeValue() {
        return Math.pow(2d, zoomSlider.valueProperty().get());
      }
    });
    final ToggleButton arrowsToggleButton =
        new ToggleButton(Constants.DOWN_ARROW_CHARACTER + Constants.UP_ARROW_CHARACTER);
    arrowsToggleButton.setSelected(false);
    arrowsToggleButton.setMinHeight(24);
    showArrows.bind(arrowsToggleButton.selectedProperty());
    final ToggleButton pathsToggleButton =
        new ToggleButton(Constants.DOWN_ARROW_CHARACTER + Constants.DOWN_ARROW_CHARACTER);
    pathsToggleButton.setDisable(true);
    pathsToggleButton.setSelected(false);
    pathsToggleButton.setMinHeight(24);
    showPaths.bind(pathsToggleButton.selectedProperty());
    arrowsToggleButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {

      @Override
      public final void handle(final ActionEvent event) {
        if (showArrows.get()) {
          pathsToggleButton.setDisable(false);
        } else {
          pathsToggleButton.setSelected(false);
          pathsToggleButton.setDisable(true);
        }
      }
    });
    highlightToggleButton.setSelected(false);
    highlightToggleButton.setMinHeight(24);
    highlightToggleButton.setGraphic(
        ImageViewBuilder.create().image(new Image(ConExpFX.class.getResourceAsStream("image/16x16/flag.png"))).build());
    rowHeaderPane.highlight.bind(highlightToggleButton.selectedProperty());
    colHeaderPane.highlight.bind(highlightToggleButton.selectedProperty());
    contextPane.highlight.bind(highlightToggleButton.selectedProperty());
    arrowsToggleButton.setStyle("-fx-background-radius: 5 0 0 5, 5 0 0 5, 4 0 0 4, 3 0 0 3;");
    pathsToggleButton.setStyle("-fx-background-radius: 0, 0, 0, 0");
    highlightToggleButton.setStyle("-fx-background-radius: 0 5 5 0, 0 5 5 0, 0 4 4 0, 0 3 3 0;");
    HBox showBox = new HBox();
    showBox.setPadding(new Insets(0d));
    showBox.getChildren().addAll(arrowsToggleButton, pathsToggleButton, highlightToggleButton);
    final ToolBar toolBar = new ToolBar();
    toolBar.getItems().addAll(zoomSlider, showBox);
    this.setTop(toolBar);
    toolBar.toFront();
  }

  protected final void update() {
    rowHeaderPane.updateContent();
    colHeaderPane.updateContent();
    contextPane.updateContent();
  }

  public final void dehighlight() {
    rowHeaderPane.dehighlight();
    colHeaderPane.dehighlight();
    contextPane.dehighlight();
    contextPane.highlightConcept.set(false);
  }

  public final void highlight(final Concept<G, M> concept) {
    final Collection<Integer> domainIndices = context.rowHeads().indicesOf(concept.extent(), true);
    final Collection<Integer> codomainIndices = context.colHeads().indicesOf(concept.intent(), true);
    contextPane.highlightConcept.set(true);
    rowHeaderPane.highlightConcept(Collections2.transform(domainIndices, new Function<Integer, Integer>() {

      public final Integer apply(final Integer index) {
        for (Entry<Integer, Integer> entry : contextPane.rowMap.get().entrySet())
          if (entry.getValue().equals(index))
            return entry.getKey();
        return index;
      }
    }));
    colHeaderPane.highlightConcept(Collections2.transform(codomainIndices, new Function<Integer, Integer>() {

      public final Integer apply(final Integer index) {
        for (Entry<Integer, Integer> entry : contextPane.columnMap.get().entrySet())
          if (entry.getValue().equals(index))
            return entry.getKey();
        return index;
      }
    }));
  }

  public final void highlightImplication(final Implication<M> implication) {

  }

  public final void addRowDecoration(final G row, final Node decoration) {
    rowHeaderPane.addDecoration(row, decoration);
  }
}
