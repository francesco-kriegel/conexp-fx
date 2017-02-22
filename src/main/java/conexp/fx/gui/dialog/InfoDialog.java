package conexp.fx.gui.dialog;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.awt.Desktop;
import java.net.URI;

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
import conexp.fx.gui.ConExpFX;

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
    final Label label =
        LabelBuilder
            .create()
            .text("Concept Explorer FX\r\n" + "(c) 2010-2015, Francesco Kriegel, TU Dresden\r\n" + "Apache License 2.0")
            .build();
    final ImageView icon =
        imageView("image/conexp-fx.png", 64, "http://lat.inf.tu-dresden.de/~francesco/conexp-fx/conexp-fx.html");
    final HBox title =
        HBoxBuilder
            .create()
            .spacing(4)
            .padding(InsetsBuilder.create().left(0).top(0).right(0).bottom(0).build())
            .children(icon, label)
            .build();
    final VBox contact =
        VBoxBuilder
            .create()
            .spacing(0)
            .padding(InsetsBuilder.create().left(0).top(0).right(0).bottom(0).build())
            .children(email, homepage)
            .build();
    final HBox icons1 =
        HBoxBuilder
            .create()
            .spacing(5)
            .padding(InsetsBuilder.create().left(0).top(0).right(0).bottom(0).build())
            .children(
                imageView("image/logo_tu_black.png", 40, "http://tu-dresden.de/en"),
                imageView("image/ganter.gif", 40, "http://tu-dresden.de/Members/bernhard.ganter"))
            .build();
    final HBox icons2 =
        HBoxBuilder
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
