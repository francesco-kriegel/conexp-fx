package conexp.fx.gui.dialog;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import javafx.stage.Stage;

public final class ErrorDialog extends FXDialog<Void> {

  public ErrorDialog(final Stage parent, final Exception e) {
    super(parent, Style.ERROR, e.getMessage(), e.toString(), null);
  }
}
