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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;

import conexp.fx.core.collections.IntPair;
import conexp.fx.gui.properties.BoundedIntPairProperty;
import conexp.fx.gui.properties.SimpleIntPairProperty;
import conexp.fx.gui.util.ColorScheme;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;

public abstract class CellPane<TCellPane extends CellPane<TCellPane, TCell>, TCell extends Cell<TCell, TCellPane>>
    extends GridPane {

  public final ReadOnlyStringProperty              id;
  protected final StackPane                        contentAndInteractionStackPane = new StackPane() {

                                                                                    @Deprecated
                                                                                    public final BaseBounds
                                                                                        impl_computeGeomBounds(
                                                                                            final BaseBounds baseBounds,
                                                                                            final BaseTransform baseTransform) {
                                                                                      synchronized (this) {
                                                                                        while (true)
                                                                                          try {
                                                                                            return super.impl_computeGeomBounds(
                                                                                                baseBounds,
                                                                                                baseTransform);
                                                                                          } catch (ConcurrentModificationException e) {
                                                                                            // System.err.println("ignore
                                                                                            // " + e.toString() + " in
                                                                                            // <CellPane>.contentAndInteractionStackPane.impl_computeGeomBounds(...)");
                                                                                          }
                                                                                      }
                                                                                    };
                                                                                  };
  protected final GridPane                         contentPane                    = new GridPane() {

                                                                                    @Deprecated
                                                                                    public final BaseBounds
                                                                                        impl_computeGeomBounds(
                                                                                            final BaseBounds baseBounds,
                                                                                            final BaseTransform baseTransform) {
                                                                                      synchronized (this) {
                                                                                        while (true)
                                                                                          try {
                                                                                            return super.impl_computeGeomBounds(
                                                                                                baseBounds,
                                                                                                baseTransform);
                                                                                          } catch (ConcurrentModificationException e) {
                                                                                            // System.err.println("ignore
                                                                                            // " + e.toString() + " in
                                                                                            // <CellPane>.contentPane.impl_computeGeomBounds(...)");
                                                                                          }
                                                                                      }
                                                                                    };
                                                                                  };
  protected final GridPane                         interactionPane                = new GridPane() {

                                                                                    @Deprecated
                                                                                    public final BaseBounds
                                                                                        impl_computeGeomBounds(
                                                                                            final BaseBounds baseBounds,
                                                                                            final BaseTransform baseTransform) {
                                                                                      synchronized (this) {
                                                                                        while (true)
                                                                                          try {
                                                                                            return super.impl_computeGeomBounds(
                                                                                                baseBounds,
                                                                                                baseTransform);
                                                                                          } catch (ConcurrentModificationException e) {
                                                                                            // System.err.println("ignore
                                                                                            // " + e.toString() + " in
                                                                                            // <CellPane>.interactionPane.impl_computeGeomBounds(...)");
                                                                                          }
                                                                                      }
                                                                                    };
                                                                                  };
  private final EventHandler<MouseEvent>           dehighlightEventHandler        = new EventHandler<MouseEvent>() {

                                                                                    @Override
                                                                                    public void
                                                                                        handle(MouseEvent event) {
                                                                                      dehighlight();
                                                                                    }
                                                                                  };
  public final IntegerProperty                     maxRows                        = new SimpleIntegerProperty();
  public final IntegerProperty                     maxColumns                     = new SimpleIntegerProperty();
  public final BooleanProperty                     autoSizeRows                   = new SimpleBooleanProperty(false);
  public final BooleanProperty                     autoSizeColumns                = new SimpleBooleanProperty(false);
  public final IntegerProperty                     rowHeightDefault               = new SimpleIntegerProperty();
  public final IntegerProperty                     columnWidthDefault             = new SimpleIntegerProperty();
  public final IntegerProperty                     textSizeDefault                = new SimpleIntegerProperty();
  public final DoubleProperty                      zoomFactor                     = new SimpleDoubleProperty(1);
  public final DoubleProperty                      maximalTextWidth               = new SimpleDoubleProperty(0);
  public final IntegerBinding                      rowHeight                      = new IntegerBinding() {

                                                                                    {
                                                                                      super.bind(
                                                                                          zoomFactor,
                                                                                          rowHeightDefault,
                                                                                          maximalTextWidth);
                                                                                    }

                                                                                    protected int computeValue() {
                                                                                      if (autoSizeRows.get())
                                                                                        return (int) maximalTextWidth
                                                                                            .get() + 10;
                                                                                //                                                                                        return Math.min(
                                                                                //                                                                                            250,
                                                                                //                                                                                            (int) maximalTextWidth.get()
                                                                                //                                                                                                + 10);
                                                                                      return (int) (zoomFactor.get()
                                                                                          * (double) rowHeightDefault
                                                                                              .get());
                                                                                    }
                                                                                  };
  public final IntegerBinding                      columnWidth                    = new IntegerBinding() {

                                                                                    {
                                                                                      super.bind(
                                                                                          zoomFactor,
                                                                                          columnWidthDefault,
                                                                                          maximalTextWidth);
                                                                                    }

                                                                                    protected int computeValue() {
                                                                                      if (autoSizeColumns.get())
                                                                                        return (int) maximalTextWidth
                                                                                            .get() + 10;
                                                                                //                                                                                        return Math.min(
                                                                                //                                                                                            250,
                                                                                //                                                                                            (int) maximalTextWidth.get()
                                                                                //                                                                                                + 10);
                                                                                      return (int) (zoomFactor.get()
                                                                                          * columnWidthDefault.get());
                                                                                    };
                                                                                  };
  public final IntegerBinding                      textSize                       = new IntegerBinding() {

                                                                                    {
                                                                                      super.bind(
                                                                                          zoomFactor,
                                                                                          textSizeDefault);
                                                                                    }

                                                                                    protected int computeValue() {
                                                                                      return (int) (zoomFactor.get()
                                                                                          * (double) textSizeDefault
                                                                                              .get());
                                                                                    };
                                                                                  };
  protected final DoubleBinding                    prefHeight                     = new DoubleBinding() {

                                                                                    {
                                                                                      super.bind(rowHeight, maxRows);
                                                                                    }

                                                                                    @Override
                                                                                    protected double computeValue() {
                                                                                      return maxRows.doubleValue()
                                                                                          * rowHeight.doubleValue();
                                                                                    }
                                                                                  };
  protected final DoubleBinding                    prefWidth                      = new DoubleBinding() {

                                                                                    {
                                                                                      super.bind(
                                                                                          columnWidth,
                                                                                          maxColumns);
                                                                                    }

                                                                                    @Override
                                                                                    protected double computeValue() {
                                                                                      return maxColumns.doubleValue()
                                                                                          * columnWidth.doubleValue();
                                                                                    }
                                                                                  };
  public final IntegerBinding                      visibleRows                    = new IntegerBinding() {

                                                                                    {
                                                                                      super.bind(
                                                                                          contentAndInteractionStackPane
                                                                                              .heightProperty(),
                                                                                          maxRows,
                                                                                          rowHeight);
                                                                                    }

                                                                                    protected int computeValue() {
                                                                                      if (rowHeight.intValue() == 0)
                                                                                        return 0;
                                                                                      return Math.min(
                                                                                          contentAndInteractionStackPane
                                                                                              .heightProperty()
                                                                                              .intValue()
                                                                                              / rowHeight.intValue(),
                                                                                          maxRows.intValue());
                                                                                    };
                                                                                  };
  public final IntegerBinding                      visibleColumns                 = new IntegerBinding() {

                                                                                    {
                                                                                      super.bind(
                                                                                          contentAndInteractionStackPane
                                                                                              .widthProperty(),
                                                                                          maxColumns,
                                                                                          columnWidth);
                                                                                    }

                                                                                    protected int computeValue() {
                                                                                      if (columnWidth.intValue() == 0)
                                                                                        return 0;
                                                                                      return Math.min(
                                                                                          contentAndInteractionStackPane
                                                                                              .widthProperty()
                                                                                              .intValue()
                                                                                              / columnWidth.intValue(),
                                                                                          maxColumns.intValue());
                                                                                    };
                                                                                  };
  public final ObjectProperty<InteractionMode>     interactionMode                =
      new SimpleObjectProperty<InteractionMode>();
  public final ObjectProperty<ColorScheme>         colorScheme                    =
      new SimpleObjectProperty<ColorScheme>(ColorScheme.DEFAULT);
  public final BooleanProperty                     animate                        = new SimpleBooleanProperty();
  public final BooleanProperty                     highlight                      = new SimpleBooleanProperty();
  protected final BooleanProperty                  isDragging                     = new SimpleBooleanProperty(false);
  protected final BooleanProperty                  isDropping                     = new SimpleBooleanProperty(false);
  protected final RowScrollBar                     rowScrollBar                   = new RowScrollBar();
  protected final ColumnScrollBar                  columnScrollBar                = new ColumnScrollBar();
  protected int                                    actualRows                     = 0;
  protected int                                    actualColumns                  = 0;
  protected final SetMultimap<Integer, TCell>      rows                           =
      Multimaps.synchronizedSetMultimap(HashMultimap.<Integer, TCell> create());
  protected final SetMultimap<Integer, TCell>      columns                        =
      Multimaps.synchronizedSetMultimap(HashMultimap.<Integer, TCell> create());
  public final SimpleMapProperty<Integer, Integer> rowMap                         =
      new SimpleMapProperty<Integer, Integer>(FXCollections.observableMap(new ConcurrentHashMap<Integer, Integer>()));
  public final SimpleMapProperty<Integer, Integer> columnMap                      =
      new SimpleMapProperty<Integer, Integer>(FXCollections.observableMap(new ConcurrentHashMap<Integer, Integer>()));
  public final SimpleMapProperty<Integer, Double>  rowOpacityMap                  =
      new SimpleMapProperty<Integer, Double>(FXCollections.observableMap(new ConcurrentHashMap<Integer, Double>()));
  public final SimpleMapProperty<Integer, Double>  columnOpacityMap               =
      new SimpleMapProperty<Integer, Double>(FXCollections.observableMap(new ConcurrentHashMap<Integer, Double>()));
  protected final BoundedIntPairProperty           minCoordinates                 =
      new BoundedIntPairProperty(IntPair.zero(), new ObjectBinding<IntPair>() {

        // {
        // super.bind();
        // }
        @Override
        protected IntPair computeValue() {
          return IntPair.zero();
        }
      }, new ObjectBinding<IntPair>() {

        {
          super.bind(rowScrollBar.maxProperty(), columnScrollBar.maxProperty());
        }

        @Override
        protected IntPair computeValue() {
          return IntPair.valueOf((int) rowScrollBar.getMax(), (int) columnScrollBar.getMax());
        }
      });
  public final IntegerBinding                      minRow                         = new IntegerBinding() {

                                                                                    {
                                                                                      super.bind(
                                                                                          rowScrollBar.valueProperty());
                                                                                    }

                                                                                    protected int computeValue() {
                                                                                      return rowScrollBar
                                                                                          .valueProperty()
                                                                                          .intValue();
                                                                                    };
                                                                                  };
  public final IntegerBinding                      maxRow                         = new IntegerBinding() {

                                                                                    {
                                                                                      super.bind(minRow, visibleRows);
                                                                                    }

                                                                                    protected int computeValue() {
                                                                                      return minRow.intValue()
                                                                                          + visibleRows.intValue() - 1;
                                                                                    };
                                                                                  };
  public final IntegerBinding                      minColumn                      = new IntegerBinding() {

                                                                                    {
                                                                                      super.bind(
                                                                                          columnScrollBar
                                                                                              .valueProperty());
                                                                                    }

                                                                                    protected int computeValue() {
                                                                                      return columnScrollBar
                                                                                          .valueProperty()
                                                                                          .intValue();
                                                                                    };
                                                                                  };
  public final IntegerBinding                      maxColumn                      = new IntegerBinding() {

                                                                                    {
                                                                                      super.bind(
                                                                                          minColumn,
                                                                                          visibleColumns);
                                                                                    }

                                                                                    protected int computeValue() {
                                                                                      return minColumn.intValue()
                                                                                          + visibleColumns.intValue()
                                                                                          - 1;
                                                                                    };
                                                                                  };
  protected final RowConstraints                   rowConstraints                 = new RowConstraints();
  protected final ColumnConstraints                columnConstraints              = new ColumnConstraints();

  protected final class RowScrollBar extends ScrollBar {

    private RowScrollBar() {
      super();
      this.setOrientation(Orientation.VERTICAL);
      this.minHeightProperty().bind(rowHeight);
      this.prefHeightProperty().bind(prefHeight);
      this.maxHeightProperty().bind(prefHeight);
      this.minWidthProperty().bind(columnWidth);
      this.prefWidthProperty().bind(columnWidth);
      this.maxWidthProperty().bind(columnWidth);
      this.setMin(0);
      this.visibleProperty().bind(new BooleanBinding() {

        {
          super.bind(visibleRows, maxRows);
        }

        @Override
        protected boolean computeValue() {
          return visibleRows.get() < maxRows.get();
        }
      });
      this.maxProperty().bind(new DoubleBinding() {

        {
          super.bind(maxRows, visibleRows);
        }

        @Override
        protected double computeValue() {
          return maxRows.doubleValue() - visibleRows.doubleValue();
        }
      });
      this.setUnitIncrement(1);
      this.blockIncrementProperty().bind(new IntegerBinding() {

        {
          super.bind(visibleRows);
        }

        @Override
        protected int computeValue() {
          return (int) visibleRows.doubleValue() - 1;
        }
      });
      this.setValue(0);
      this.visibleAmountProperty().bind(new DoubleBinding() {

        {
          super.bind(maxProperty(), visibleRows, maxRows);
        }

        @Override
        protected double computeValue() {
          if (maxRows.get() == 0)
            return 1;
          return (visibleRows.doubleValue() / maxRows.doubleValue()) * maxProperty().doubleValue();
        }
      });
//      this.valueProperty().addListener(new ChangeListener<Number>() {
//
//        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//          final int deltaValue = newValue.intValue() - oldValue.intValue();
//          if (deltaValue != 0)
//            scrollRows(deltaValue);
//        }
//      });
      this.addEventHandler(MouseEvent.MOUSE_ENTERED, dehighlightEventHandler);
      this.addEventHandler(MouseEvent.MOUSE_EXITED, dehighlightEventHandler);
    }
  }

  protected final class ColumnScrollBar extends ScrollBar {

    private ColumnScrollBar() {
      super();
      this.setOrientation(Orientation.HORIZONTAL);
      this.minWidthProperty().bind(columnWidth);
      this.prefWidthProperty().bind(prefWidth);
      this.maxWidthProperty().bind(prefWidth);
      this.minHeightProperty().bind(rowHeight);
      this.prefHeightProperty().bind(rowHeight);
      this.maxHeightProperty().bind(rowHeight);
      this.setMin(0);
      this.visibleProperty().bind(new BooleanBinding() {

        {
          super.bind(visibleColumns, maxColumns);
        }

        @Override
        protected boolean computeValue() {
          return visibleColumns.get() < maxColumns.get();
        }
      });
      this.maxProperty().bind(new DoubleBinding() {

        {
          super.bind(maxColumns, visibleColumns);
        }

        @Override
        protected double computeValue() {
          return maxColumns.doubleValue() - visibleColumns.doubleValue();
        }
      });
      this.setUnitIncrement(1);
      this.blockIncrementProperty().bind(new IntegerBinding() {

        {
          super.bind(visibleColumns);
        }

        @Override
        protected int computeValue() {
          return (int) visibleColumns.doubleValue() - 1;
        }
      });
      this.setValue(0);
      this.visibleAmountProperty().bind(new DoubleBinding() {

        {
          super.bind(maxProperty(), visibleColumns, maxColumns);
        }

        @Override
        protected double computeValue() {
          if (maxColumns.get() == 0)
            return 1;
          return (visibleColumns.doubleValue() / maxColumns.doubleValue()) * maxProperty().doubleValue();
        }
      });
//      this.valueProperty().addListener(new ChangeListener<Number>() {
//
//        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//          final int deltaValue = newValue.intValue() - oldValue.intValue();
//          if (deltaValue != 0)
//            scrollColumns(deltaValue);
//        }
//      });
      this.addEventHandler(MouseEvent.MOUSE_ENTERED, dehighlightEventHandler);
      this.addEventHandler(MouseEvent.MOUSE_EXITED, dehighlightEventHandler);
    }
  }

  public final boolean interactive;

  protected CellPane(final String id, final InteractionMode interactionMode) {
    this(id, interactionMode, true);
  }

  protected CellPane(final String id, final InteractionMode interactionMode, final boolean interactive) {
    super();
    this.id = new ReadOnlyStringWrapper(id).getReadOnlyProperty();
    this.interactionMode.set(interactionMode);
    this.interactive = interactive;
    this.zoomFactor.addListener(new ChangeListener<Number>() {

      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        maximalTextWidth.set(maximalTextWidth.get() * (newValue.doubleValue() / oldValue.doubleValue()));
      }
    });
    minCoordinates.addListener(new ChangeListener<IntPair>() {

      @Override
      public void changed(ObservableValue<? extends IntPair> observable, IntPair oldValue, IntPair newValue) {
        if (rowScrollBar.getValue() != newValue.x())
          rowScrollBar.setValue(Math.min(newValue.x(), rowScrollBar.getMax()));
        if (columnScrollBar.getValue() != newValue.y())
          columnScrollBar.setValue(Math.min(newValue.y(), columnScrollBar.getMax()));
      }
    });
    rowScrollBar.valueProperty().addListener(new ChangeListener<Number>() {

      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (minCoordinates.get().x() != newValue.intValue())
          minCoordinates.set(newValue.intValue(), minCoordinates.get().y());
      }
    });
    columnScrollBar.valueProperty().addListener(new ChangeListener<Number>() {

      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (minCoordinates.get().y() != newValue.intValue())
          minCoordinates.set(minCoordinates.get().x(), newValue.intValue());
      }
    });
    interactionPane.setOnScroll(event -> {
      if (event.getDeltaX() > 0)
        columnScrollBar.decrement();
      else if (event.getDeltaX() < 0)
        columnScrollBar.increment();
      if (event.getDeltaY() > 0)
        rowScrollBar.decrement();
      else if (event.getDeltaY() < 0)
        rowScrollBar.increment();
    });
//    interactionPane.setOnSwipeDown(rowScrollBar.getOnSwipeDown());
//    interactionPane.setOnSwipeUp(rowScrollBar.getOnSwipeUp());
//    interactionPane.setOnSwipeLeft(columnScrollBar.getOnSwipeLeft());
//    interactionPane.setOnSwipeRight(columnScrollBar.getOnSwipeRight());
    minCoordinates.addListener(new ChangeListener<IntPair>() {

      @Override
      public void changed(ObservableValue<? extends IntPair> observable, IntPair oldValue, IntPair newValue) {
        scroll(newValue.x() - oldValue.x(), newValue.y() - oldValue.y());
      }
    });
    this.minHeightProperty().bind(rowHeight);
    this.prefHeightProperty().bind(prefHeight);
    this.maxHeightProperty().bind(prefHeight);
    this.contentPane.minHeightProperty().bind(rowHeight);
    this.contentPane.prefHeightProperty().bind(prefHeight);
    this.contentPane.maxHeightProperty().bind(prefHeight);
    this.interactionPane.minHeightProperty().bind(rowHeight);
    this.interactionPane.prefHeightProperty().bind(prefHeight);
    this.interactionPane.maxHeightProperty().bind(prefHeight);
    this.contentAndInteractionStackPane.minHeightProperty().bind(rowHeight);
    this.contentAndInteractionStackPane.prefHeightProperty().bind(prefHeight);
    this.contentAndInteractionStackPane.maxHeightProperty().bind(prefHeight);
    this.rowConstraints.minHeightProperty().bind(rowHeight);
    this.rowConstraints.maxHeightProperty().bind(rowHeight);
    this.minWidthProperty().bind(columnWidth);
    this.prefWidthProperty().bind(prefWidth);
    this.maxWidthProperty().bind(prefWidth);
    this.contentPane.minWidthProperty().bind(columnWidth);
    this.contentPane.prefWidthProperty().bind(prefWidth);
    this.contentPane.maxWidthProperty().bind(prefWidth);
    this.interactionPane.minWidthProperty().bind(columnWidth);
    this.interactionPane.prefWidthProperty().bind(prefWidth);
    this.interactionPane.maxWidthProperty().bind(prefWidth);
    this.contentAndInteractionStackPane.minWidthProperty().bind(columnWidth);
    this.contentAndInteractionStackPane.prefWidthProperty().bind(prefWidth);
    this.contentAndInteractionStackPane.maxWidthProperty().bind(prefWidth);
    this.columnConstraints.minWidthProperty().bind(columnWidth);
    this.columnConstraints.maxWidthProperty().bind(columnWidth);
    this.visibleColumns.addListener(new ChangeListener<Number>() {

      public void
          changed(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
        final int deltaColumns = newValue.intValue() - oldValue.intValue();
        adjustColumns(deltaColumns);
      }
    });
    this.visibleRows.addListener(new ChangeListener<Number>() {

      public void
          changed(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
        final int deltaRows = newValue.intValue() - oldValue.intValue();
        adjustRows(deltaRows);
      }
    });
    interactionPane.addEventHandler(MouseEvent.MOUSE_EXITED, dehighlightEventHandler);
    this.contentAndInteractionStackPane.getChildren().addAll(contentPane, interactionPane);
    this.add(contentAndInteractionStackPane, 0, 0);
    this.add(rowScrollBar, 1, 0);
    this.add(columnScrollBar, 0, 1);
    contentAndInteractionStackPane.toFront();
    interactionPane.toFront();
  }

  public final StackPane getContentAndInteractionStackPane() {
    return contentAndInteractionStackPane;
  }

  public final GridPane getContentPane() {
    return contentPane;
  }

  public final GridPane getInteractionPane() {
    return interactionPane;
  }

  public RowScrollBar getRowScrollBar() {
    return rowScrollBar;
  }

  public ColumnScrollBar getColumnScrollBar() {
    return columnScrollBar;
  }

  public final Set<TCell> getCellsByGridRow(final int gridRow) {
    return rows.get(gridRow);
  }

  public final Set<TCell> getCellsByGridColumn(final int gridColumn) {
    return columns.get(gridColumn);
  }

  public final TCell getCellByGridCoordinates(final IntPair coordinates) {
    return getCellByGridCoordinates(coordinates.x().intValue(), coordinates.y().intValue());
  }

  public final TCell getCellByGridCoordinates(final int row, final int column) {
    return Iterables.getOnlyElement(Sets.intersection(getCellsByGridRow(row), getCellsByGridColumn(column)));
  }

  private final class ContentRowPredicate implements Predicate<TCell> {

    private final int contentRow;

    private ContentRowPredicate(int contentRow) {
      super();
      this.contentRow = contentRow;
    }

    @Override
    public boolean apply(TCell cell) {
      return cell.contentCoordinates.get().x().intValue() == contentRow;
    }
  }

  private final class ContentColumnPredicate implements Predicate<TCell> {

    private final int contentColumn;

    private ContentColumnPredicate(int contentColumn) {
      super();
      this.contentColumn = contentColumn;
    }

    @Override
    public boolean apply(TCell cell) {
      return cell.contentCoordinates.get().y().intValue() == contentColumn;
    }
  }

  public final Collection<TCell> getCellsByContentRow(final int contentRow) {
    return Collections2.filter(rows.values(), new ContentRowPredicate(contentRow));
  }

  public final Collection<TCell> getCellsByContentColumn(final int contentColumn) {
    return Collections2.filter(rows.values(), new ContentColumnPredicate(contentColumn));
  }

  public final TCell getCellByContentCoordinates(final IntPair coordinates) {
    return getCellByContentCoordinates(coordinates.x().intValue(), coordinates.y().intValue());
  }

  public final TCell getCellByContentCoordinates(final int row, final int column) {
    return Iterables.getOnlyElement(Iterables.filter(getCellsByContentRow(row), new Predicate<TCell>() {

      @Override
      public boolean apply(TCell cell) {
        return getCellsByContentColumn(column).contains(cell);
      }
    }));
  }

  public final void bind(final CellPane<?, ?> anotherSuperPane, final InteractionMode interactionMode) {
    if (interactionMode.isRowsEnabled()) {
      this.maxRows.bind(anotherSuperPane.maxRows);
      this.rowHeightDefault.bind(anotherSuperPane.rowHeightDefault);
      this.animate.bind(anotherSuperPane.animate);
      this.isDragging.bindBidirectional(anotherSuperPane.isDragging);
      this.isDropping.bindBidirectional(anotherSuperPane.isDropping);
      this.highlightRowMap.bindBidirectional(anotherSuperPane.highlightRowMap);
      this.rowOpacityMap.bindBidirectional(anotherSuperPane.rowOpacityMap);
      this.dragRowMap.bindBidirectional(anotherSuperPane.dragRowMap);
      this.rowMap.bindBidirectional(anotherSuperPane.rowMap);
      this.rowScrollBar.valueProperty().bindBidirectional(anotherSuperPane.rowScrollBar.valueProperty());
    }
    if (interactionMode.isColumnsEnabled()) {
      this.maxColumns.bind(anotherSuperPane.maxColumns);
      this.columnWidthDefault.bind(anotherSuperPane.columnWidthDefault);
      this.animate.bind(anotherSuperPane.animate);
      this.isDragging.bindBidirectional(anotherSuperPane.isDragging);
      this.isDropping.bindBidirectional(anotherSuperPane.isDropping);
      this.highlightColumnMap.bindBidirectional(anotherSuperPane.highlightColumnMap);
      this.columnOpacityMap.bindBidirectional(anotherSuperPane.columnOpacityMap);
      this.dragColumnMap.bindBidirectional(anotherSuperPane.dragColumnMap);
      this.columnMap.bindBidirectional(anotherSuperPane.columnMap);
      this.columnScrollBar.valueProperty().bindBidirectional(anotherSuperPane.columnScrollBar.valueProperty());
    }
  }

  public final void updateContent() {
    for (final TCell cell : rows.values())
      cell.updateContent();
  }

  /**
   * This method creates a new instance of a TCell. In a concrete implementation it suffices to simply return a new
   * TCell, i.e. <code>return new TCell(...)</code>.
   * 
   * @param gridRow
   *          the grid row
   * @param gridColumn
   *          the grid column
   * @return the t cell
   */
  protected abstract TCell createCell(final int gridRow, final int gridColumn);

  protected void addCell(final int gridRow, final int gridColumn) {
    createCell(gridRow, gridColumn);
  }

  private final void removeCell(final TCell cell) {
    cell.dispose();
  }

  private final void adjustRows(final int deltaValue) {
    if (deltaValue > 0) {
      final int upShift = Math.max(0, maxRow.get() + deltaValue - maxRows.get());
      if (upShift > 0)
        rowScrollBar.setValue(Math.max(0, rowScrollBar.getValue() - upShift));
      for (int i = 0; i < deltaValue; i++)
        appendRow();
    } else if (deltaValue < 0)
      for (int i = 0; i < -deltaValue; i++)
        removeRow();
  }

  private final void appendRow() {
    this.contentPane.getRowConstraints().add(rowConstraints);
    this.interactionPane.getRowConstraints().add(rowConstraints);
    final int interactionRow = actualRows++;
    for (int interactionColumn = 0; interactionColumn < actualColumns; interactionColumn++)
      addCell(interactionRow, interactionColumn);
  }

  private final void removeRow() {
    final int row = --actualRows;
    if (row != -1) {
      for (final TCell cell : getCellsByGridRow(row))
        removeCell(cell);
      this.contentPane.getRowConstraints().remove(row);
      this.interactionPane.getRowConstraints().remove(row);
    } else
      actualRows++;
  }

  private final void adjustColumns(int deltaValue) {
    if (deltaValue > 0) {
      final int leftShift = Math.max(0, maxColumn.get() + deltaValue - maxColumns.get());
      if (leftShift > 0)
        columnScrollBar.setValue(Math.max(0, columnScrollBar.getValue() - leftShift));
      for (int i = 0; i < deltaValue; i++)
        appendColumn();
    } else if (deltaValue < 0)
      for (int i = 0; i < -deltaValue; i++)
        removeColumn();
  }

  private final void appendColumn() {
    this.contentPane.getColumnConstraints().add(columnConstraints);
    this.interactionPane.getColumnConstraints().add(columnConstraints);
    final int interactionColumn = actualColumns++;
    for (int interactionRow = 0; interactionRow < actualRows; interactionRow++)
      addCell(interactionRow, interactionColumn);
  }

  private final void removeColumn() {
    final int column = --actualColumns;
    if (column != -1) {
      for (final TCell cell : getCellsByGridColumn(column))
        removeCell(cell);
      this.contentPane.getColumnConstraints().remove(column);
      this.interactionPane.getColumnConstraints().remove(column);
    } else
      actualColumns++;
  }

  protected final SimpleIntPairProperty scrollDeltaCoordinates = new SimpleIntPairProperty();

  protected enum MovementDirection {
    UP,
    UP_LEFT,
    LEFT,
    DOWN_LEFT,
    DOWN,
    DOWN_RIGHT,
    RIGHT,
    UP_RIGHT;

    protected static final MovementDirection valueOf(int rowDelta, int columnDelta) {
      if (rowDelta > 0 && columnDelta == 0)
        return UP;
      if (rowDelta > 0 && columnDelta > 0)
        return UP_LEFT;
      if (rowDelta == 0 && columnDelta > 0)
        return LEFT;
      if (rowDelta < 0 && columnDelta > 0)
        return DOWN_LEFT;
      if (rowDelta < 0 && columnDelta == 0)
        return DOWN;
      if (rowDelta < 0 && columnDelta < 0)
        return DOWN_RIGHT;
      if (rowDelta == 0 && columnDelta < 0)
        return RIGHT;
      if (rowDelta > 0 && columnDelta < 0)
        return UP_RIGHT;
      return null;
    }
  }

  private final IntPair flipRow(final IntPair gridCoordinates) {
    return IntPair.valueOf(flipRow(gridCoordinates.x()), gridCoordinates.y());
  }

  private final int flipRow(final int row) {
    return maxRow.get() - row;
  }

  private final IntPair flipColumn(final IntPair gridCoordinates) {
    return IntPair.valueOf(gridCoordinates.x(), flipColumn(gridCoordinates.y()));
  }

  private final int flipColumn(final int column) {
    return maxColumn.get() - column;
  }

  private final IntPair flipBoth(final IntPair gridCoordinates) {
    return IntPair.valueOf(flipRow(gridCoordinates.x()), flipColumn(gridCoordinates.y()));
  }

  protected final void scroll(int rowDelta, int columnDelta) {
    switch (MovementDirection.valueOf(rowDelta, columnDelta)) {
    case UP:
      for (int row = 0; row < actualRows; row++)
        for (TCell cell : getCellsByGridRow(row))
          cell.scrollDeltaCoordinatesQueue.add(new IntPair(-rowDelta, -columnDelta));
      break;
    case UP_LEFT:
      ArrayList<TCell> cantorianSortedCells = Lists.newArrayList(rows.values());
      Collections.sort(cantorianSortedCells, new Comparator<TCell>() {

        @Override
        public int compare(TCell o1, TCell o2) {
          return IntPair.POSITIVE_CANTORIAN_COMPARATOR.compare(o1.gridCoordinates.get(), o2.gridCoordinates.get());
        }
      });
      for (TCell cell : cantorianSortedCells)
        cell.scrollDeltaCoordinatesQueue.add(new IntPair(-rowDelta, -columnDelta));
      cantorianSortedCells.clear();
      break;
    case LEFT:
      for (int column = 0; column < actualColumns; column++)
        for (TCell cell : getCellsByGridColumn(column))
          cell.scrollDeltaCoordinatesQueue.add(new IntPair(-rowDelta, -columnDelta));
      break;
    case DOWN_LEFT:
      cantorianSortedCells = Lists.newArrayList(rows.values());
      Collections.sort(cantorianSortedCells, new Comparator<TCell>() {

        @Override
        public int compare(TCell o1, TCell o2) {
          return IntPair.POSITIVE_CANTORIAN_COMPARATOR
              .compare(flipRow(o1.gridCoordinates.get()), flipRow(o2.gridCoordinates.get()));
        }
      });
      for (TCell cell : cantorianSortedCells)
        cell.scrollDeltaCoordinatesQueue.add(new IntPair(-rowDelta, -columnDelta));
      cantorianSortedCells.clear();
      break;
    case DOWN:
      for (int row = actualRows - 1; row >= 0; row--)
        for (TCell cell : getCellsByGridRow(row))
          cell.scrollDeltaCoordinatesQueue.add(new IntPair(-rowDelta, -columnDelta));
      break;
    case DOWN_RIGHT:
      cantorianSortedCells = Lists.newArrayList(rows.values());
      Collections.sort(cantorianSortedCells, new Comparator<TCell>() {

        @Override
        public int compare(TCell o1, TCell o2) {
          return IntPair.POSITIVE_CANTORIAN_COMPARATOR
              .compare(flipBoth(o1.gridCoordinates.get()), flipBoth(o2.gridCoordinates.get()));
        }
      });
      for (TCell cell : cantorianSortedCells)
        cell.scrollDeltaCoordinatesQueue.add(new IntPair(-rowDelta, -columnDelta));
      cantorianSortedCells.clear();
      break;
    case RIGHT:
      for (int column = actualColumns - 1; column >= 0; column--)
        for (TCell cell : getCellsByGridColumn(column))
          cell.scrollDeltaCoordinatesQueue.add(new IntPair(-rowDelta, -columnDelta));
      break;
    case UP_RIGHT:
      cantorianSortedCells = Lists.newArrayList(rows.values());
      Collections.sort(cantorianSortedCells, new Comparator<TCell>() {

        @Override
        public int compare(TCell o1, TCell o2) {
          return IntPair.POSITIVE_CANTORIAN_COMPARATOR
              .compare(flipColumn(o1.gridCoordinates.get()), flipColumn(o2.gridCoordinates.get()));
        }
      });
      for (TCell cell : cantorianSortedCells)
        cell.scrollDeltaCoordinatesQueue.add(new IntPair(-rowDelta, -columnDelta));
      cantorianSortedCells.clear();
      break;
    default:
      scrollDeltaCoordinates.set(-rowDelta, -columnDelta);
    }
  }

  protected final SimpleMapProperty<Integer, Integer> dragRowMap    =
      new SimpleMapProperty<Integer, Integer>(FXCollections.observableMap(new ConcurrentHashMap<Integer, Integer>()));
  protected final SimpleMapProperty<Integer, Integer> dragColumnMap =
      new SimpleMapProperty<Integer, Integer>(FXCollections.observableMap(new ConcurrentHashMap<Integer, Integer>()));

  protected final void drag(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {
    isDropping.set(false);
    isDragging.set(true);
    if (sourceRow != -1 && interactionMode.get().isRowsEnabled())
      dragRowMap.putAll(getRowChanges(sourceRow, targetRow, ChangeType.DRAG));
    if (sourceColumn != -1 && interactionMode.get().isColumnsEnabled())
      dragColumnMap.putAll(getColumnChanges(sourceColumn, targetColumn, ChangeType.DRAG));
  }

  public final void drop(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {
    isDragging.set(false);
    isDropping.set(true);
    if (sourceRow != -1 && interactionMode.get().isRowsEnabled())
      rowMap.putAll(getRowChanges(sourceRow, targetRow, ChangeType.DROP));
    if (sourceColumn != -1 && interactionMode.get().isColumnsEnabled())
      columnMap.putAll(getColumnChanges(sourceColumn, targetColumn, ChangeType.DROP));
    if (sourceRow != -1 && interactionMode.get().isRowsEnabled())
      dragRowMap.clear();
    if (sourceColumn != -1 && interactionMode.get().isColumnsEnabled())
      dragColumnMap.clear();
    Platform.runLater(new Runnable() {

      @Override
      public void run() {
        Platform.runLater(new Runnable() {

          @Override
          public void run() {
            isDropping.set(false);
          }
        });
      }
    });
  }

  private enum ChangeType {
    DRAG,
    DROP;
  }

  private final Map<Integer, Integer> getRowChanges(final int sourceRow, final int targetRow, final ChangeType type) {
    final Map<Integer, Integer> changes = new HashMap<Integer, Integer>();
    final int sgnRow = (int) Math.signum(targetRow - sourceRow);
    for (int row = 0; row < actualRows; row++) {
      int newDragRow = row;
      if (row == sourceRow)
        newDragRow = targetRow;
      else if (row * sgnRow >= (sourceRow + sgnRow) * sgnRow && row * sgnRow <= targetRow * sgnRow)
        newDragRow = row - sgnRow;
      switch (type) {
      case DRAG:
        changes.put(row, newDragRow);
        break;
      case DROP:
        changes.put(
            minRow.get() + newDragRow,
            rowMap.containsKey(minRow.get() + row) ? rowMap.get(minRow.get() + row) : minRow.get() + row);
      }
    }
    return changes;
  }

  private final Map<Integer, Integer>
      getColumnChanges(final int sourceColumn, final int targetColumn, final ChangeType type) {
    final Map<Integer, Integer> changes = new HashMap<Integer, Integer>();
    final int sgnColumn = (int) Math.signum(targetColumn - sourceColumn);
    for (int column = 0; column < actualColumns; column++) {
      int newDragColumn = column;
      if (column == sourceColumn)
        newDragColumn = targetColumn;
      else if (column * sgnColumn >= (sourceColumn + sgnColumn) * sgnColumn
          && column * sgnColumn <= targetColumn * sgnColumn)
        newDragColumn = column - sgnColumn;
      switch (type) {
      case DRAG:
        changes.put(column, newDragColumn);
        break;
      case DROP:
        changes.put(
            minColumn.get() + newDragColumn,
            columnMap.containsKey(minColumn.get() + column) ? columnMap.get(minColumn.get() + column)
                : minColumn.get() + column);
      }
    }
    return changes;
  }

  protected final SimpleMapProperty<Integer, Boolean> highlightRowMap    =
      new SimpleMapProperty<Integer, Boolean>(FXCollections.observableMap(new ConcurrentHashMap<Integer, Boolean>()));
  protected final SimpleMapProperty<Integer, Boolean> highlightColumnMap =
      new SimpleMapProperty<Integer, Boolean>(FXCollections.observableMap(new ConcurrentHashMap<Integer, Boolean>()));
  public final SimpleBooleanProperty                  highlightConcept   = new SimpleBooleanProperty(false);

  public final void highlight(TCell cell) {
    if (highlight.get())
      switch (interactionMode.get()) {
      case NONE:
        break;
      case ROWS:
        highlight(Collections.singleton(cell.gridCoordinates.get().x().intValue()), null);
        break;
      case COLUMNS:
        highlight(null, Collections.singleton(cell.gridCoordinates.get().y().intValue()));
        break;
      case ROWS_AND_COLUMNS:
        highlight(
            Collections.singleton(cell.gridCoordinates.get().x().intValue()),
            Collections.singleton(cell.gridCoordinates.get().y().intValue()));
        break;
      }
  }

  public final void highlight(final Collection<Integer> rows, final Collection<Integer> columns) {
    if (highlight.get()) {
      dehighlight(
          rows == null ? Collections.<Integer> emptySet() : rows,
          columns == null ? Collections.<Integer> emptySet() : columns);
      if (rows != null)
        for (Integer row : rows)
          highlightRowMap.put(row, true);
      if (columns != null)
        for (Integer column : columns)
          highlightColumnMap.put(column, true);
    }
  }

  public final void dehighlight() {
    highlightRowMap.clear();
    highlightColumnMap.clear();
  }

  protected final void dehighlight(Collection<Integer> ignoreRows, Collection<Integer> ignoreColumns) {
    highlightRowMap.keySet().retainAll(ignoreRows);
    highlightColumnMap.keySet().retainAll(ignoreColumns);
  }

  public void resetGridPositions() {
    for (TCell cell : rows.values())
      cell.resetGridPosition();
  }
}
