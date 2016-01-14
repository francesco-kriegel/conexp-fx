package conexp.fx.gui.dialog;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import javafx.stage.Stage;

public final class ErrorDialog extends FXDialog<Void> {

  public ErrorDialog(final Stage parent, final Throwable e) {
    this(parent, e.getMessage(), e.toString());
    e.printStackTrace();
  }

  public ErrorDialog(final Stage parent, final String title, final String message) {
    super(parent, Style.ERROR, title, message, null);
  }

}
