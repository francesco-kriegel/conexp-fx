package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2020 Francesco Kriegel
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


import java.util.Arrays;
import java.util.List;

import conexp.fx.gui.ConExpFX;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ChoiceBoxBuilder;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import jfxtras.scene.control.ListSpinner;

public final class FXControls
{
  public static final ImageView newImageView(final String image)
  {
    return new ImageView(new Image(ConExpFX.class.getResourceAsStream(image)));
  }

  public static final MenuItem newMenuItem(
      final String text,
      final String image,
      final EventHandler<ActionEvent> onAction)
  {
    return MenuItemBuilder.create().text(text).graphic(newImageView(image)).onAction(onAction).build();
  }

  public static final MenuItem newMenuItem(
      final String text,
      final String image,
      final boolean disable,
      final EventHandler<ActionEvent> onAction)
  {
    return MenuItemBuilder.create().text(text).graphic(newImageView(image)).disable(disable).onAction(onAction).build();
  }

  public static final Text newText(final StringBinding textBinding)
  {
    final Text text = new Text();
    text.textProperty().bind(textBinding);
    return text;
  }

  public static final <E> ListSpinner<E> newListSpinner(final E initial, final List<E> values)
  {
    return new ListSpinner<E>(FXCollections.observableList(values), initial);
  }

  @SuppressWarnings("unchecked")
  public static final <E> ListSpinner<E> newListSpinner(final E initial, final E... values)
  {
    return newListSpinner(initial, Arrays.asList(values));
  }

  public static final <E> ChoiceBox<E> newChoiceBox(final E initial, final List<E> values)
  {
    return ChoiceBoxBuilder
        .<E> create()
        .items(FXCollections.observableList(values))
        .value(initial)
        .minWidth(150)
        .maxWidth(150)
        .build();
  }

  @SuppressWarnings("unchecked")
  public static final <E> ChoiceBox<E> newChoiceBox(final E initial, final E... values)
  {
    return newChoiceBox(initial, Arrays.asList(values));
  }
}
