package conexp.fx.gui.assistent;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
