package conexp.fx.gui.context;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import conexp.fx.core.util.IdGenerator;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.dataset.FCADataset;

public class StringMatrixContextWidget extends MatrixContextWidget<String, String> {

  public StringMatrixContextWidget(final FCADataset<String, String> fcaInstance) {
    super(fcaInstance);
    final Button domainButton = ButtonBuilder.create()
//            .text(conExpTab.fca.context.isHomogen() ? "New Element" : "New Object")
        .onAction(new EventHandler<ActionEvent>() {

          public void handle(ActionEvent event) {
            fcaInstance.addObject((fcaInstance.context.isHomogen() ? "Element " : "Object ") + IdGenerator.getNextId());
          }
        })
        .build();
    final ImageView view =
        ImageViewBuilder.create().image(new Image(ConExpFX.class.getResourceAsStream("image/16x16/add.png"))).build();
    view.scaleXProperty().bind(zoomFactor);
    view.scaleYProperty().bind(zoomFactor);
    domainButton.setGraphic(view);
    domainButton.minWidthProperty().bind(rowHeaderPane.columnWidth);
    domainButton.maxWidthProperty().bind(rowHeaderPane.columnWidth);
    domainButton.minHeightProperty().bind(cellSize);
    domainButton.maxHeightProperty().bind(cellSize);
    domainButton.styleProperty().bind(new StringBinding() {

      {
        super.bind(textSize);
      }

      @Override
      protected String computeValue() {
        return "-fx-padding: 0; -fx-font-size: " + textSize.get() + ";";
      }
    });
    centerPane.add(domainButton, 0, 2);
    final Button codomainButton = ButtonBuilder.create()
//            .text(conExpTab.fca.context.isHomogen() ? "New Element" : "New Attribute")
        .onAction(new EventHandler<ActionEvent>() {

          public void handle(ActionEvent event) {
            fcaInstance.addAttribute((fcaInstance.context.isHomogen() ? "Element " : "Attribute ")
                + IdGenerator.getNextId());
          }
        })
        .build();
    final ImageView view2 =
        ImageViewBuilder.create().image(new Image(ConExpFX.class.getResourceAsStream("image/16x16/add.png"))).build();
    view2.scaleXProperty().bind(zoomFactor);
    view2.scaleYProperty().bind(zoomFactor);
    codomainButton.setGraphic(view2);
    codomainButton.rotateProperty().set(-90);
    codomainButton.minWidthProperty().bind(colHeaderPane.rowHeight);
    codomainButton.maxWidthProperty().bind(colHeaderPane.rowHeight);
    codomainButton.minHeightProperty().bind(cellSize);
    codomainButton.maxHeightProperty().bind(cellSize);
    codomainButton.translateXProperty().bind(new DoubleBinding() {

      {
        super.bind(colHeaderPane.rowHeight, cellSize);
      }

      @Override
      protected double computeValue() {
        return -(colHeaderPane.rowHeight.get() - cellSize.get()) / 2d;
      }
    });
    codomainButton.styleProperty().bind(new StringBinding() {

      {
        super.bind(textSize);
      }

      @Override
      protected String computeValue() {
        return "-fx-padding: 0; -fx-font-size: " + textSize.get() + ";";
      }
    });
    centerPane.add(codomainButton, 2, 0);
  }
}
