package conexp.fx.gui.assistent;

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
