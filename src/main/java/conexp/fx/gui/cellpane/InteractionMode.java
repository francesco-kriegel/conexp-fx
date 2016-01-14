package conexp.fx.gui.cellpane;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


public enum InteractionMode {
  NONE,
  ROWS,
  COLUMNS,
  ROWS_AND_COLUMNS;

  public boolean isRowsEnabled() {
    return this == ROWS || this == ROWS_AND_COLUMNS;
  }

  public boolean isColumnsEnabled() {
    return this == COLUMNS || this == ROWS_AND_COLUMNS;
  }

}
