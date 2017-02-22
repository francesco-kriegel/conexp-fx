package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import java.util.Arrays;
import java.util.List;

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
import conexp.fx.gui.ConExpFX;

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
