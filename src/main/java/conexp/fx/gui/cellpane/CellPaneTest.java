package conexp.fx.gui.cellpane;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2019 Francesco Kriegel
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
      super(THE_CELL_PANE, gridRow, gridColumn, Pos.CENTER, TextAlignment.CENTER, false, null, true);
    }

    @Override
    protected final void updateContent() {
//      final String text = TestCell.this.contentCoordinates.get().toString();
      textContent.set("X");
    }

  }

}
