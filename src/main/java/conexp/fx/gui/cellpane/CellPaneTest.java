package conexp.fx.gui.cellpane;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public final class CellPaneTest extends Application {

  public static final void main(String[] args) {
    launch(args);
  }

  @Override
  public final void start(Stage stage) throws Exception {
    stage.setScene(new Scene(THE_CELL_PANE, 1200, 800));
    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

      @Override
      public void handle(WindowEvent event) {
        System.exit(0);
      }
    });
    stage.show();
  }

  private static final TestCellPane THE_CELL_PANE = new TestCellPane();

  private static final class TestCellPane extends CellPane<TestCellPane, TestCell> {

    protected TestCellPane() {
      super("TheCellPane", InteractionMode.ROWS_AND_COLUMNS);
      rowHeightDefault.set(20);
      columnWidthDefault.set(20);
      textSizeDefault.set(16);
      animate.set(true);
      maxRows.set(1000);
      maxColumns.set(1000);
    }

    @Override
    protected final TestCell createCell(final int gridRow, final int gridColumn) {
      return new TestCell(gridRow, gridColumn);
    }

  }

  private static final class TestCell extends Cell<TestCell, TestCellPane> {

    public TestCell(final int gridRow, final int gridColumn) {
      super(THE_CELL_PANE, gridRow, gridColumn, Pos.CENTER, TextAlignment.CENTER, false, null);
    }

    @Override
    protected final void updateContent() {
//      final String text = TestCell.this.contentCoordinates.get().toString();
      textContent.set("X");
    }

  }

}
