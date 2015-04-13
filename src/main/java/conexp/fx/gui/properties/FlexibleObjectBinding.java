package conexp.fx.gui.properties;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;

public abstract class FlexibleObjectBinding<X> extends ObjectBinding<X> {

  public final void bind(final Observable o) {
    super.bind(o);
  }

  public final void unbind(final Observable o) {
    super.unbind(o);
  }
}
