package conexp.fx.gui.dialog;

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

import java.awt.Desktop;
import java.net.URI;

import conexp.fx.gui.ConExpFX;
import javafx.geometry.InsetsBuilder;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.HyperlinkBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;

public final class InfoDialog extends FXDialog<Void> {

  private final ConExpFX cfx;

  public InfoDialog(final ConExpFX cfx) {
    super(cfx.primaryStage, Style.INFO, "Info", "", content(cfx));
    this.cfx = cfx;
  }

  private static VBox content(final ConExpFX cfx) {
    final Hyperlink homepage =
        HyperlinkBuilder.create().text("http://lat.inf.tu-dresden.de/~francesco").onAction(ev -> {
          try {
            Desktop.getDesktop().browse(new URI("http://lat.inf.tu-dresden.de/~francesco"));
          } catch (Exception e) {
            new ErrorDialog(cfx.primaryStage, e).showAndWait();
          }
        }).build();
    final Hyperlink email = HyperlinkBuilder.create().text("mailto:francesco.kriegel@tu-dresden.de").onAction(ev -> {
      try {
        Desktop.getDesktop().mail(new URI("mailto:francesco.kriegel@tu-dresden.de"));
      } catch (Exception e) {
        new ErrorDialog(cfx.primaryStage, e).showAndWait();
      }
    }).build();
    final Label label = LabelBuilder
        .create()
        .text("Concept Explorer FX\r\n" + "(c) 2010-2018, Francesco Kriegel, TU Dresden\r\n" + "GNU General Public License v3 (GPL-3)")
        .build();
    final ImageView icon =
        imageView("image/conexp-fx.png", 64, "http://lat.inf.tu-dresden.de/~francesco/conexp-fx/conexp-fx.html");
    final HBox title = HBoxBuilder
        .create()
        .spacing(4)
        .padding(InsetsBuilder.create().left(0).top(0).right(0).bottom(0).build())
        .children(icon, label)
        .build();
    final VBox contact = VBoxBuilder
        .create()
        .spacing(0)
        .padding(InsetsBuilder.create().left(0).top(0).right(0).bottom(0).build())
        .children(email, homepage)
        .build();
    final HBox icons1 = HBoxBuilder
        .create()
        .spacing(5)
        .padding(InsetsBuilder.create().left(0).top(0).right(0).bottom(0).build())
        .children(
            imageView("image/logo_tu_black.png", 40, "http://tu-dresden.de/en"),
            imageView("image/ganter.gif", 40, "http://tu-dresden.de/Members/bernhard.ganter"))
        .build();
    final HBox icons2 = HBoxBuilder
        .create()
        .spacing(5)
        .padding(InsetsBuilder.create().left(0).top(0).right(0).bottom(0).build())
        .children(
            imageView("image/Javafx_logo_color.png", 40, "http://docs.oracle.com/javafx/"),
            imageView("image/apache.png", 40, "http://www.apache.org/licenses/LICENSE-2.0.html"),
            imageView("image/mavenlogo_builtby_w.gif", 40, "http://maven.apache.org/"))
        .build();
    return VBoxBuilder
        .create()
        .spacing(5)
        .padding(InsetsBuilder.create().left(10).top(10).right(10).bottom(10).build())
        .children(title, contact, icons1, icons2)
        .build();
  }

  private static ImageView imageView(final String image, final int h, final String url) {
    final Image i = new Image(ConExpFX.class.getResourceAsStream(image));
    final double r = i.getWidth() / i.getHeight();
    return ImageViewBuilder.create().onMouseClicked(ev -> {
      try {
        Desktop.getDesktop().browse(new URI(url));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).image(i).fitHeight(h).fitWidth(r * h).build();
  }
}
