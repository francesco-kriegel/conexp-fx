package conexp.fx.gui.cellpane;

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

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import conexp.fx.core.collections.IntPair;
import conexp.fx.core.util.IdGenerator;
import conexp.fx.gui.util.Platform2;
import javafx.animation.FadeTransition;
import javafx.animation.FadeTransitionBuilder;
import javafx.animation.FillTransition;
import javafx.animation.FillTransitionBuilder;
import javafx.animation.TranslateTransition;
import javafx.animation.TranslateTransitionBuilder;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadowBuilder;
import javafx.scene.effect.Effect;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBuilder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

public abstract class Cell<TCell extends Cell<TCell, TCellPane>, TCellPane extends CellPane<TCellPane, TCell>> {

  public static final DataFormat CELL_COORDINATES_DATA_FORMAT = new DataFormat("CellCoordinates");

  public enum MouseEventType {
    SCROLL,
    DRAG;
  }

  public static final class CellCoordinates implements Serializable {

    private static final long   serialVersionUID = 8412756602066000809L;
    public final String         cellPaneId;
    public final MouseEventType mouseEventType;
    public int                  gridRow;
    public int                  gridColumn;
    public int                  contentRow;
    public int                  contentColumn;

    protected CellCoordinates(
        String cellPaneId,
        final MouseEventType mouseEventType,
        int gridRow,
        int gridColumn,
        int contentRow,
        int contentColumn) {
      super();
      this.cellPaneId = cellPaneId;
      this.mouseEventType = mouseEventType;
      this.gridRow = gridRow;
      this.gridColumn = gridColumn;
      this.contentRow = contentRow;
      this.contentColumn = contentColumn;
    }
  }

  public Color                                        dehighlightColor            = Color.TRANSPARENT;
  protected final TCellPane                           cellPane;
  public final ReadOnlyLongProperty                   id                          =
      new ReadOnlyLongWrapper(IdGenerator.getNextId()).getReadOnlyProperty();
  public final ReadOnlyObjectProperty<IntPair>        gridCoordinates;
  public final ObjectBinding<IntPair>                 contentCoordinates;
  protected final ObjectBinding<IntPair>              snapToCoordinates;
  protected final ObservableList<IntPair>             scrollDeltaCoordinatesQueue =
      FXCollections.observableList(Collections.synchronizedList(new LinkedList<IntPair>()));
  public final DoubleProperty                         width                       = new SimpleDoubleProperty();
  public final DoubleProperty                         height                      = new SimpleDoubleProperty();
  public final IntegerProperty                        textSize                    = new SimpleIntegerProperty();
  public final StringBinding                          textStyle                   = new StringBinding() {

                                                                                    {
                                                                                      super.bind(textSize);
                                                                                    }

                                                                                    @Override
                                                                                    public String computeValue() {
                                                                                      return "-fx-font-size: "
                                                                                          + textSize.get() + ";";
                                                                                    }
                                                                                  };
  public final StringProperty                         textContent                 = new SimpleStringProperty();
  public final DoubleProperty                         opacity                     = new SimpleDoubleProperty();
  public final BooleanProperty                        highlight                   = new SimpleBooleanProperty();
  public final BooleanProperty                        animate                     = new SimpleBooleanProperty();
  protected final ObjectProperty<CellInteractionPane> interactionPane             =
      new SimpleObjectProperty<CellInteractionPane>(new CellInteractionPane());
  public final ObjectProperty<CellContentPane>        contentPane                 =
      new SimpleObjectProperty<CellContentPane>(new CellContentPane());
  private boolean                                     runningLoop                 = false;
  private ObjectProperty<TranslateTransition>         translateTransition         =
      new SimpleObjectProperty<TranslateTransition>(TranslateTransitionBuilder.create().build());
  private ObjectProperty<FillTransition>              fillTransition              =
      new SimpleObjectProperty<FillTransition>(FillTransitionBuilder.create().build());
  private ObjectProperty<FadeTransition>              fadeTransition              =
      new SimpleObjectProperty<FadeTransition>(FadeTransitionBuilder.create().build());
  private ChangeListener<IntPair>                     snapToCoordinatesChangeListener;
  private ListChangeListener<IntPair>                 scrollDeltaCoordinatesQueueChangeListener;
  private ChangeListener<IntPair>                     contentCoordinatesChangeListener;
  private ChangeListener<String>                      textContentChangeListener;
  private ChangeListener<Number>                      opacityChangeListener;
  private ChangeListener<Boolean>                     highlightChangeListener;
  private ChangeListener<TranslateTransition>         translateTransitionChangeListener;
  private ChangeListener<FillTransition>              fillTransitionChangeListener;
  private ChangeListener<FadeTransition>              fadeTransitionChangeListener;
  private EventHandler<MouseEvent>                    mouseEnteredEventHandler;
  private EventHandler<MouseEvent>                    mouseExitedEventHandler;
  private EventHandler<MouseEvent>                    dragDetectedEventHandler;
  private EventHandler<DragEvent>                     dragOverEventHandler;
  private EventHandler<DragEvent>                     dragEnteredEventHandler;
  private EventHandler<DragEvent>                     dragExitedEventHandler;
  private EventHandler<DragEvent>                     dragDroppedEventHandler;
  private EventHandler<DragEvent>                     dragDoneEventHandler;
  private ChangeListener<IntPair>                     scrollDeltaCoordinatesChangeListener;

  public class CellContentPane extends StackPane {

    public final Rectangle background = RectangleBuilder.create().fill(dehighlightColor).build();
    public final Text      text       = TextBuilder.create().build();                            // .fontSmoothingType(FontSmoothingType.GRAY)
  }

  public class CellInteractionPane extends BorderPane {

    public final Rectangle interactionRectangle = RectangleBuilder.create().fill(Color.TRANSPARENT).build();
  }

  @SuppressWarnings("unchecked")
  public Cell(
      final TCellPane cellPane,
      final int gridRow,
      final int gridColumn,
      final Pos alignment,
      final TextAlignment textAlignment,
      final boolean rotated,
      final EventHandler<ActionEvent> onFinishedEventHandler,
      final boolean createTextSizeListener) {
    super();
    this.cellPane = cellPane;
    this.width.bind(cellPane.columnWidth);
    this.height.bind(cellPane.rowHeight);
    this.textSize.bind(cellPane.textSize);
    this.highlight.bind(new BooleanBinding() {

      {
        super.bind(cellPane.highlightRowMap, cellPane.highlightColumnMap);
      }

      protected boolean computeValue() {
        final Boolean highlightedRow = cellPane.highlightRowMap.get(gridRow);
        final Boolean highlightedColumn = cellPane.highlightColumnMap.get(gridColumn);
        if (cellPane.highlightConcept.get())
          return (highlightedRow == null ? false : highlightedRow)
              && (highlightedColumn == null ? false : highlightedColumn);
        else
          return (highlightedRow == null ? false : highlightedRow)
              || (highlightedColumn == null ? false : highlightedColumn);
      };
    });
    this.animate.bind(cellPane.animate);
    this.gridCoordinates = new ReadOnlyObjectWrapper<IntPair>(new IntPair(gridRow, gridColumn)).getReadOnlyProperty();
    this.snapToCoordinates = new ObjectBinding<IntPair>() {

      {
        super.bind(cellPane.dragRowMap, cellPane.dragColumnMap);
      }

      @Override
      protected IntPair computeValue() {
        final Integer row = cellPane.dragRowMap.get(gridRow);
        final Integer column = cellPane.dragColumnMap.get(gridColumn);
        return IntPair.valueOf(row == null ? gridRow : row, column == null ? gridColumn : column);
      }
    };
    this.contentCoordinates = new ObjectBinding<IntPair>() {

      {
        super.bind(gridCoordinates, cellPane.minRow, cellPane.minColumn, cellPane.rowMap, cellPane.columnMap);
      }

      @Override
      protected IntPair computeValue() {
        final int gridRow = cellPane.minRow.get() + gridCoordinates.get().x();
        final int gridColumn = cellPane.minColumn.get() + gridCoordinates.get().y();
        final Integer contentRow = cellPane.rowMap.get(gridRow);
        final Integer contentColumn = cellPane.columnMap.get(gridColumn);
        return IntPair
            .valueOf(contentRow == null ? gridRow : contentRow, contentColumn == null ? gridColumn : contentColumn);
      }
    };
    this.opacity.bind(new DoubleBinding() {

      {
        super.bind(contentCoordinates, cellPane.rowOpacityMap, cellPane.columnOpacityMap);
      }

      @Override
      protected double computeValue() {
        final Double rowOpacity = cellPane.rowOpacityMap.get(contentCoordinates.get().x());
        final Double columnOpacity = cellPane.columnOpacityMap.get(contentCoordinates.get().y());
        return Math.min(rowOpacity == null ? 1 : rowOpacity, columnOpacity == null ? 1 : columnOpacity);
      }
    });
    this.contentPane.get().setAlignment(alignment);
    this.contentPane.get().setOpacity(Constants.HIDE_OPACITY);
    this.contentPane.get().getChildren().addAll(contentPane.get().background, contentPane.get().text);
    this.contentPane.get().text.setTextAlignment(textAlignment);
    this.contentPane.get().text.styleProperty().bind(textStyle);
    this.interactionPane.get().setCenter(interactionPane.get().interactionRectangle);
    this.interactionPane.get().minWidthProperty().bind(width);
    this.interactionPane.get().maxWidthProperty().bind(width);
    this.interactionPane.get().interactionRectangle.widthProperty().bind(width);
    this.interactionPane.get().minHeightProperty().bind(height);
    this.interactionPane.get().maxHeightProperty().bind(height);
    this.interactionPane.get().interactionRectangle.heightProperty().bind(height);
    if (rotated) {
      this.contentPane.get().minWidthProperty().bind(height);
      this.contentPane.get().maxWidthProperty().bind(height);
      this.contentPane.get().background.widthProperty().bind(height);
      this.contentPane.get().minHeightProperty().bind(width);
      this.contentPane.get().maxHeightProperty().bind(width);
      this.contentPane.get().background.heightProperty().bind(width);
      this.createRotation();
    } else {
      this.contentPane.get().minWidthProperty().bind(width);
      this.contentPane.get().maxWidthProperty().bind(width);
      this.contentPane.get().background.widthProperty().bind(width);
      this.contentPane.get().minHeightProperty().bind(height);
      this.contentPane.get().maxHeightProperty().bind(height);
      this.contentPane.get().background.heightProperty().bind(height);
    }
    this.contentPane.get().text.effectProperty().bind(new ObjectBinding<Effect>() {

      {
        super.bind(animate);
      }

      @Override
      protected Effect computeValue() {
        if (animate.get())
          return DropShadowBuilder
              .create()
              .radius(1)
              .blurType(BlurType.GAUSSIAN)
              .color(Color.ALICEBLUE)
              .spread(1)
              .build();
        else
          return null;
      }
    });
    this.createPropertyListeners();
    this.createMouseHandlers();
    if (cellPane.interactive)
      this.createDragAndDropHandlers();
    cellPane.rows.put(gridRow, (TCell) Cell.this);
    cellPane.columns.put(gridColumn, (TCell) Cell.this);
//    Platform.runLater(new Runnable() {
//
//      @Override
//      public void run() {
    cellPane.contentPane.add(getContentPane(), gridColumn, gridRow);
    cellPane.interactionPane.add(getInteractionPane(), gridColumn, gridRow);
//    updateContent();
    fade(
        Constants.HIDE_OPACITY,
        opacity.getValue() == null ? Constants.SHOW_OPACITY : opacity.get(),
        TransitionType.SMOOTH,
        onFinishedEventHandler);
//      }
//    });
    if (createTextSizeListener)
      if (cellPane.autoSizeRows.get() || cellPane.autoSizeColumns.get())
        contentPane.get().text.layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {

          @Override
          public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
            final double width = newValue.getWidth();
            if (width > cellPane.maximalTextWidth.get())
              cellPane.maximalTextWidth.set(width);
          }
        });
  }

  private final void createRotation() {
    final Rotate rotate = new Rotate(-90);
    final Translate translate = new Translate(0, 0);
    rotate.pivotXProperty().bind(new DoubleBinding() {

      {
        super.bind(height);
      }

      protected double computeValue() {
        return height.get() / 2d;
      };
    });
    rotate.pivotYProperty().bind(new DoubleBinding() {

      {
        super.bind(width);
      }

      protected double computeValue() {
        return width.get() / 2d;
      };
    });
    translate.yProperty().bind(new DoubleBinding() {

      {
        super.bind(width, height);
      }

      protected double computeValue() {
        return (width.get() - height.get()) / 2d;
      };
    });
    this.contentPane.get().getTransforms().addAll(rotate, translate);
//    this.contentPane.get().setRotate(-90);
//    final Translate translate = new Translate();
//    translate.xProperty().bind(new DoubleBinding() {
//
//      {
//        super.bind(width, height);
//      }
//
//      @Override
//      protected double computeValue() {
//        return (width.get() - height.get()) / 2d;
//      }
//    });
//    this.contentPane.get().getTransforms().add(translate);
  }

  private final void createPropertyListeners() {
    snapToCoordinatesChangeListener = new ChangeListener<IntPair>() {

      @Override
      public void changed(ObservableValue<? extends IntPair> observable, IntPair oldValue, final IntPair newValue) {
        if (cellPane.isDragging.get() && !cellPane.isDropping.get())
          snapToGrid(newValue, TransitionType.SMOOTH, null);
        else {
          snapToGrid(newValue, TransitionType.DISCRETE, null);
          updateContent();
        }
      }
    };
    scrollDeltaCoordinatesChangeListener = new ChangeListener<IntPair>() {

      @Override
      public void changed(ObservableValue<? extends IntPair> observable, IntPair oldValue, IntPair newValue) {
        scrollDeltaCoordinatesQueue.add(newValue);
      }
    };
    scrollDeltaCoordinatesQueueChangeListener = new ListChangeListener<IntPair>() {

      @Override
      public void onChanged(Change<? extends IntPair> c) {
        if (c.next() && c.wasAdded())
          scrollLoop();
      }
    };
    contentCoordinatesChangeListener = new ChangeListener<IntPair>() {

      @Override
      public void changed(ObservableValue<? extends IntPair> observable, IntPair oldValue, IntPair newValue) {
        if (!cellPane.isDragging.get() && !cellPane.isDropping.get())
          updateContent();
      }
    };
//    this.textStyle.addListener(new ChangeListener<String>() {
//
//      @Override
//      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//        System.out.println("newTextStyle: " + newValue);
////        contentPane.get().text.setStyle(newValue);
//        updateContent();
//      }
//    });
    textContentChangeListener = new ChangeListener<String>() {

      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
//        if (cellPane.isDropping.get())
        Platform2.runOnFXThread(new Runnable() {

          @Override
          public void run() {
            contentPane.get().text.setText(newValue);
          }
        });
//        else
//          fadeOut(TransitionType.DEFAULT, new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent event) {
//              contentPane.get().text.setText(newValue);
//              fadeIn(TransitionType.DEFAULT, null);
//            }
//          });
      }
    };
    opacityChangeListener = new ChangeListener<Number>() {

      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        fade(oldValue.doubleValue(), newValue.doubleValue(), TransitionType.DEFAULT, null);
      }
    };
    highlightChangeListener = new ChangeListener<Boolean>() {

      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        highlight(
            newValue.booleanValue(),
            // cellPane.isDragging.get() ? TransitionType.DISCRETE : newValue
            // ?
            TransitionType.DISCRETE
//                : TransitionType.DEFAULT
            ,
            null);
      }
    };
    translateTransitionChangeListener = new ChangeListener<TranslateTransition>() {

      @Override
      public void changed(
          ObservableValue<? extends TranslateTransition> observable,
          TranslateTransition oldValue,
          TranslateTransition newValue) {
        try {
          oldValue.jumpTo(Constants.ANIMATION_DURATION);
          oldValue.stop();
        } catch (NullPointerException e) {}
        try {
          newValue.play();
        } catch (NullPointerException e) {}
      }
    };
    fillTransitionChangeListener = new ChangeListener<FillTransition>() {

      @Override
      public void changed(
          ObservableValue<? extends FillTransition> observable,
          FillTransition oldValue,
          FillTransition newValue) {
        oldValue.jumpTo(Constants.ANIMATION_DURATION);
        oldValue.stop();
        newValue.play();
      }
    };
    fadeTransitionChangeListener = new ChangeListener<FadeTransition>() {

      @Override
      public void changed(
          ObservableValue<? extends FadeTransition> observable,
          FadeTransition oldValue,
          FadeTransition newValue) {
        oldValue.jumpTo(Constants.ANIMATION_DURATION);
        oldValue.stop();
        newValue.play();
      }
    };
    this.snapToCoordinates.addListener(snapToCoordinatesChangeListener);
    cellPane.scrollDeltaCoordinates.addListener(scrollDeltaCoordinatesChangeListener);
    this.scrollDeltaCoordinatesQueue.addListener(scrollDeltaCoordinatesQueueChangeListener);
    this.contentCoordinates.addListener(contentCoordinatesChangeListener);
    this.textContent.addListener(textContentChangeListener);
    this.opacity.addListener(opacityChangeListener);
    this.highlight.addListener(highlightChangeListener);
    this.translateTransition.addListener(translateTransitionChangeListener);
    this.fillTransition.addListener(fillTransitionChangeListener);
    this.fadeTransition.addListener(fadeTransitionChangeListener);
  }

  private final void createMouseHandlers() {
    mouseEnteredEventHandler = new EventHandler<MouseEvent>() {

      @SuppressWarnings("unchecked")
      public void handle(MouseEvent event) {
//        interactionPane.get().interactionRectangle.setStroke(Color.RED);
//        interactionPane.get().interactionRectangle.setStrokeType(StrokeType.INSIDE);
//        interactionPane.get().interactionRectangle.setStrokeWidth(1);
        cellPane.highlight((TCell) Cell.this);
      }
    };
//    mouseExitedEventHandler = new EventHandler<MouseEvent>() {
//
//      public void handle(MouseEvent event) {
//        interactionPane.get().interactionRectangle.setStroke(Color.TRANSPARENT);
//      }
//    };
    this.getInteractionPane().setOnMouseEntered(mouseEnteredEventHandler);
//    this.getInteractionPane().setOnMouseExited(mouseExitedEventHandler);
  }

  private final void createDragAndDropHandlers() {
    dragDetectedEventHandler = new EventHandler<MouseEvent>() {

      @SuppressWarnings("unchecked")
      public void handle(MouseEvent event) {
        cellPane.highlight((TCell) Cell.this);
        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.put(
            CELL_COORDINATES_DATA_FORMAT,
            new CellCoordinates(
                cellPane.id.get(),
                event.getButton().equals(MouseButton.PRIMARY) ? MouseEventType.SCROLL : MouseEventType.DRAG,
                gridCoordinates.get().x().intValue(),
                gridCoordinates.get().y().intValue(),
                contentCoordinates.get().x().intValue(),
                contentCoordinates.get().y().intValue()));
        contentPane.get().startDragAndDrop(TransferMode.MOVE).setContent(clipboardContent);
        event.consume();
      }
    };
    dragOverEventHandler = new EventHandler<DragEvent>() {

      public void handle(final DragEvent event) {
        final String sourceCellPaneId =
            ((CellCoordinates) event.getDragboard().getContent(CELL_COORDINATES_DATA_FORMAT)).cellPaneId;
        final String targetCellPaneId = cellPane.id.get();
        if (sourceCellPaneId.equals(targetCellPaneId))
          event.acceptTransferModes(TransferMode.MOVE);
        else
          event.acceptTransferModes(TransferMode.NONE);
        event.consume();
      }
    };
    dragEnteredEventHandler = new EventHandler<DragEvent>() {

      @SuppressWarnings("unchecked")
      @Override
      public void handle(DragEvent event) {
        final Dragboard dragboard = event.getDragboard();
        final CellCoordinates sourceCellCoordinates =
            (CellCoordinates) dragboard.getContent(CELL_COORDINATES_DATA_FORMAT);
        final String sourceId = sourceCellCoordinates.cellPaneId;
        final String targetId = cellPane.id.get();
        if (sourceId.equals(targetId))
          switch (sourceCellCoordinates.mouseEventType) {
          case SCROLL:
            cellPane.highlight((TCell) Cell.this);
            final int rowDelta = gridCoordinates.get().x() - sourceCellCoordinates.gridRow;
            final int columnDelta = gridCoordinates.get().y() - sourceCellCoordinates.gridColumn;
            sourceCellCoordinates.gridRow = gridCoordinates.get().x();
            sourceCellCoordinates.gridColumn = gridCoordinates.get().y();
            dragboard.setContent(
                Collections.<DataFormat, Object> singletonMap(CELL_COORDINATES_DATA_FORMAT, sourceCellCoordinates));
            cellPane.minCoordinates.add(-rowDelta, -columnDelta);
//            final boolean down = rowDelta < 0;
//            final boolean right = columnDelta < 0;
//            for (int i = 0; i < Math.abs(rowDelta); i++)
//              if (down)
//                cellPane.rowScrollBar.increment();
//              else
//                cellPane.rowScrollBar.decrement();
//            for (int i = 0; i < Math.abs(columnDelta); i++)
//              if (right)
//                cellPane.columnScrollBar.increment();
//              else
//                cellPane.columnScrollBar.decrement();
            break;
          case DRAG:
            cellPane.drag(
                sourceCellCoordinates.gridRow,
                sourceCellCoordinates.gridColumn,
                gridCoordinates.get().x().intValue(),
                gridCoordinates.get().y().intValue());
          }
        event.consume();
      }
    };
    dragExitedEventHandler = new EventHandler<DragEvent>() {

      public void handle(DragEvent event) {
        event.consume();
      }
    };
    dragDroppedEventHandler = new EventHandler<DragEvent>() {

      public void handle(DragEvent event) {
        cellPane.dehighlight();
        final Dragboard dragboard = event.getDragboard();
        final CellCoordinates sourceCellCoordinates =
            (CellCoordinates) dragboard.getContent(CELL_COORDINATES_DATA_FORMAT);
        switch (sourceCellCoordinates.mouseEventType) {
        case SCROLL:
          break;
        case DRAG:
          cellPane.drop(
              sourceCellCoordinates.gridRow,
              sourceCellCoordinates.gridColumn,
              gridCoordinates.get().x().intValue(),
              gridCoordinates.get().y().intValue());
        }
        event.setDropCompleted(true);
        event.consume();
      }
    };
    dragDoneEventHandler = new EventHandler<DragEvent>() {

      public void handle(DragEvent event) {
        // TODO Why is this handler never called upon drag done?
        System.out.println("drag done!");
        event.consume();
      }
    };
    this.getInteractionPane().setOnDragDetected(dragDetectedEventHandler);
    this.getInteractionPane().setOnDragOver(dragOverEventHandler);
    this.getInteractionPane().setOnDragEntered(dragEnteredEventHandler);
    this.getInteractionPane().setOnDragExited(dragExitedEventHandler);
    this.getInteractionPane().setOnDragDropped(dragDroppedEventHandler);
    this.getInteractionPane().setOnDragDone(dragDoneEventHandler);
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj instanceof Cell && ((Cell<?, ?>) obj).id == this.id;
  }

  @Override
  public int hashCode() {
    return (int) id.get();
  }

  public final CellContentPane getContentPane() {
    return contentPane.get();
  }

  public final CellInteractionPane getInteractionPane() {
    return interactionPane.get();
  }

  /**
   * Abstract method to update textual content (and possibly other properties) based on e.g. content coordinates
   * property <code>this.contentCoordinates.get()</code>. Just call <code>this.textContent.set(String)</code> within the
   * implementation body to update the text.
   */
  protected abstract void updateContent();

  public final void dispose() {
    fadeOut(TransitionType.SMOOTH, new EventHandler<ActionEvent>() {

      @Override
      @SuppressWarnings("unchecked")
      public void handle(ActionEvent event) {
        cellPane.contentPane.getChildren().remove(getContentPane());
        cellPane.interactionPane.getChildren().remove(getInteractionPane());
        cellPane.rows.remove(gridCoordinates.get().x(), (TCell) Cell.this);
        cellPane.columns.remove(gridCoordinates.get().y(), (TCell) Cell.this);
        contentCoordinates.dispose();
        snapToCoordinates.dispose();
        scrollDeltaCoordinatesQueue.clear();
        textStyle.dispose();
        snapToCoordinates.removeListener(snapToCoordinatesChangeListener);
        cellPane.scrollDeltaCoordinates.removeListener(scrollDeltaCoordinatesChangeListener);
        scrollDeltaCoordinatesQueue.removeListener(scrollDeltaCoordinatesQueueChangeListener);
        contentCoordinates.removeListener(contentCoordinatesChangeListener);
        textContent.removeListener(textContentChangeListener);
        opacity.removeListener(opacityChangeListener);
        highlight.removeListener(highlightChangeListener);
        translateTransition.removeListener(translateTransitionChangeListener);
        fillTransition.removeListener(fillTransitionChangeListener);
        fadeTransition.removeListener(fadeTransitionChangeListener);
        if (cellPane.interactive) {
          getInteractionPane().removeEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnteredEventHandler);
//        getInteractionPane().removeEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedEventHandler);
          getInteractionPane().removeEventHandler(MouseEvent.DRAG_DETECTED, dragDetectedEventHandler);
          getInteractionPane().removeEventHandler(DragEvent.DRAG_OVER, dragOverEventHandler);
          getInteractionPane().removeEventHandler(DragEvent.DRAG_ENTERED, dragEnteredEventHandler);
          getInteractionPane().removeEventHandler(DragEvent.DRAG_EXITED, dragExitedEventHandler);
          getInteractionPane().removeEventHandler(DragEvent.DRAG_DROPPED, dragDroppedEventHandler);
          getInteractionPane().removeEventHandler(DragEvent.DRAG_DONE, dragDoneEventHandler);
        }
      }
    });
  }

  private final void scrollLoop() {
    if (!animate.get())
      return;
    if (runningLoop)
      return;
    runningLoop = true;
    _scrollLoop();
  }

  private final void _scrollLoop() {
    final IntPair deltaCoordinates = cummulateScrollDeltaCoordinates();
    final Integer deltaX = deltaCoordinates.x();
    final Integer deltaY = deltaCoordinates.y();
    if (deltaX != 0 || deltaY != 0) {
      snapToGrid(
          gridCoordinates.get().x() + deltaX,
          gridCoordinates.get().y() + deltaY,
          TransitionType.SMOOTH,
          new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
              Platform.runLater(new Runnable() {

                @Override
                public void run() {
                  resetGridPosition();
                }
              });
            }
          });
      _scrollLoop();
    } else
      runningLoop = false;
  }

  protected void resetGridPosition() {
    snapToGrid(gridCoordinates.get(), TransitionType.DISCRETE, null);
  }

  private final IntPair cummulateScrollDeltaCoordinates() {
    int deltaRowSum = 0;
    int deltaColumnSum = 0;
    synchronized (scrollDeltaCoordinatesQueue) {
      Iterator<IntPair> iterator = scrollDeltaCoordinatesQueue.iterator();
      while (iterator.hasNext()) {
        final IntPair nextDeltaCoordinates = iterator.next();
        deltaRowSum += nextDeltaCoordinates.x();
        deltaColumnSum += nextDeltaCoordinates.y();
        iterator.remove();
      }
    }
    return IntPair.valueOf(deltaRowSum, deltaColumnSum);
  }

  private final void snapToGrid(
      final IntPair coordinates,
      final TransitionType translationType,
      final EventHandler<ActionEvent> onFinishedEventHandler) {
    snapToGrid(coordinates.x(), coordinates.y(), translationType, onFinishedEventHandler);
  }

  private final void snapToGrid(
      final int row,
      final int column,
      final TransitionType translationType,
      final EventHandler<ActionEvent> onFinishedEventHandler) {
    translateTo(column * width.get(), row * height.get(), translationType, onFinishedEventHandler);
  }

  private final void translateTo(
      final double minXInParent,
      final double minYInParent,
      final TransitionType translationType,
      final EventHandler<ActionEvent> onFinishedEventHandler) {
    translateTransition.get().jumpTo(Constants.ANIMATION_DURATION);
    translateTransition.get().stop();
    final Bounds boundsInLocal = contentPane.get().getBoundsInLocal();
    final Bounds boundsInParent = contentPane.get().getBoundsInParent();
    final double deltaX = minXInParent - (boundsInParent.getMinX() - boundsInLocal.getMinX());
    final double deltaY = minYInParent - (boundsInParent.getMinY() - boundsInLocal.getMinY());
    translateBy(deltaX, deltaY, translationType, onFinishedEventHandler);
  }

  private final void translateBy(
      final double x,
      final double y,
      final TransitionType translationType,
      final EventHandler<ActionEvent> onFinishedEventHandler) {
    final boolean smooth = translationType == TransitionType.SMOOTH
        || (translationType == TransitionType.DEFAULT && Cell.this.animate.get());
    translateTransition.set(
        TranslateTransitionBuilder
            .create()
            .duration(smooth ? Constants.ANIMATION_DURATION : Duration.ONE)
            .byX(x)
            .byY(y)
            .node(contentPane.get())
            .onFinished(onFinishedEventHandler)
            .build());
  }

  protected final void toFront() {
    contentPane.get().toFront();
  }

  private final void highlight(
      final boolean highlight,
      final TransitionType translationType,
      final EventHandler<ActionEvent> onFinishedEventHandler) {
    if (highlight)
      toFront();
    final boolean smooth = translationType == TransitionType.SMOOTH
        || (translationType == TransitionType.DEFAULT && Cell.this.animate.get());
    fillTransition.set(
        FillTransitionBuilder
            .create()
            .fromValue(highlight ? dehighlightColor : cellPane.colorScheme.get().getColor(4))
            .toValue(highlight ? cellPane.colorScheme.get().getColor(4) : dehighlightColor)
            .duration(smooth ? Constants.ANIMATION_DURATION : Duration.ONE)
            .shape(contentPane.get().background)
            .onFinished(onFinishedEventHandler)
            .build());
  }

  private final void
      fadeIn(final TransitionType translationType, final EventHandler<ActionEvent> onFinishedEventHandler) {
    fade(Constants.HIDE_OPACITY, Constants.SHOW_OPACITY, translationType, onFinishedEventHandler);
  }

  private final void
      fadeOut(final TransitionType translationType, final EventHandler<ActionEvent> onFinishedEventHandler) {
    fade(Constants.SHOW_OPACITY, Constants.HIDE_OPACITY, translationType, onFinishedEventHandler);
  }

  private final void fade(
      final double fromValue,
      final double toValue,
      final TransitionType translationType,
      final EventHandler<ActionEvent> onFinishedEventHandler) {
    final boolean smooth = translationType == TransitionType.SMOOTH
        || (translationType == TransitionType.DEFAULT && Cell.this.animate.get());
    fadeTransition.set(
        FadeTransitionBuilder
            .create()
            .node(contentPane.get())
            .duration(smooth ? Constants.ANIMATION_DURATION : Duration.ONE)
            .fromValue(fromValue)
            .toValue(toValue)
            .onFinished(onFinishedEventHandler)
            .build());
  }
}
