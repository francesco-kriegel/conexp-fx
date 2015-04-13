package conexp.fx.gui.assistent;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

import com.google.common.base.Function;

public abstract class AssistentPage<T>
{
  public final StringProperty       titleProperty;
  public final StringProperty       textProperty;
  public final ObjectProperty<Node> contentProperty;
  public final ObjectProperty<T>    resultProperty = new SimpleObjectProperty<T>();
  public final StringBinding        nextPageIdBinding;

  public AssistentPage(
      final String title,
      final String text,
      final Node content,
      final Function<T, String> nextPageIdFunction)
  {
    super();
    this.titleProperty = new SimpleStringProperty(title);
    this.textProperty = new SimpleStringProperty(text);
    this.contentProperty = new SimpleObjectProperty<Node>(content);
    this.nextPageIdBinding = new StringBinding()
    {
      {
        super.bind(resultProperty);
      }

      @Override
      protected String computeValue()
      {
        if (nextPageIdFunction == null || resultProperty.isNull().get())
          return null;
        return nextPageIdFunction.apply(resultProperty.get());
      }
    };
  }

  protected abstract void onNext();
}
