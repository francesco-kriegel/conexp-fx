package conexp.fx.gui.zoom;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import conexp.fx.gui.util.Colors;
import conexp.fx.gui.util.CoordinateUtil;
import javafx.animation.FadeTransitionBuilder;
import javafx.animation.KeyFrame;
import javafx.animation.TimelineBuilder;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraintsBuilder;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.util.Duration;

public class ZoomWidget extends Label {

  protected final Node                  owner;
  private boolean                       currentlyShown     = false;
  private boolean                       hideRequested      = false;
  private final Popup                   zoomPopup;
  private final ZoomPopupPane           zoomPopupPane;

  protected final ObjectProperty<Color> color1             = new SimpleObjectProperty<Color>(
                                                               Colors.fromCSSColorString("#99bcfd"));
  protected final ObjectProperty<Color> color2             = new SimpleObjectProperty<Color>(
                                                               Colors.fromCSSColorString("#e2ecfe"));
  protected final ObjectProperty<Color> color3             = new SimpleObjectProperty<Color>(
                                                               Colors.fromCSSColorString("#99bcfd"));

  public final DoubleProperty           zoomFactorProperty = new SimpleDoubleProperty(1);

  public ZoomWidget(final Node owner, final Image image) {
    super();
    this.owner = owner;
    this.setAlignment(Pos.TOP_LEFT);
    this.setGraphic(ImageViewBuilder
        .create()
        .image(image)
        .preserveRatio(true)
        .fitWidth(32)
        .smooth(true)
        .cache(true)
        .build());
    zoomPopupPane = new ZoomPopupPane();
    zoomPopup = new Popup();
    zoomPopup.getContent().add(zoomPopupPane);
    zoomFactorProperty.bind(zoomPopupPane.sizeSlider.valueProperty());
    addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {

      public void handle(MouseEvent event) {
//        if (!currentlyShown) {
//          currentlyShown = true;
//          zoomPopupPane.setOpacity(0);
        zoomPopupPane.setVisible(true);
        zoomPopup.show(
            owner,
            CoordinateUtil.getScreenX(ZoomWidget.this) - 40,
            CoordinateUtil.getScreenY(ZoomWidget.this) - 20);
//          FadeTransitionBuilder
//              .create()
//              .duration(Duration.millis(100))
//              .fromValue(0)
//              .toValue(1)
//              .node(zoomPopupPane)
//              .build()
//              .play();
//        }
      };
    });
    zoomPopup.setAutoHide(true);
//    zoomPopupPane.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
//
//      public void handle(MouseEvent event) {
//        hideRequested = false;
//      };
//    });
//    zoomPopupPane.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
//
//      public void handle(MouseEvent event) {
//        hideRequested = true;
//        TimelineBuilder.create().keyFrames(new KeyFrame(Duration.ONE, new EventHandler<ActionEvent>() {
//
//          public void handle(ActionEvent event) {
//            if (hideRequested) {
////              FadeTransitionBuilder
////                  .create()
////                  .duration(Duration.millis(100))
////                  .fromValue(1)
////                  .toValue(0)
////                  .node(zoomPopupPane)
////                  .onFinished(new EventHandler<ActionEvent>() {
////
////                    @Override
////                    public void handle(ActionEvent event) {
//              zoomPopup.hide();
////                      zoomPopupPane.setVisible(false);
//              currentlyShown = false;
////                    }
////                  })
////                  .build()
////                  .play();
//            }
//          }
//        })).delay(Duration.millis(200)).build().play();
//      };
//    });
  }

  protected final class ZoomPopupPane extends GridPane {

    private final Button   minusButton;
    private final Label    label;
    private final Text     text;
    private final Button   plusButton;
    protected final Slider sizeSlider;

    private final int      size1 = 26;
    private final int      size2 = 42;

    public ZoomPopupPane() {
      super();
      this.setHgap(4);
      this.setVgap(4);
      this.getStyleClass().add("tooltip");
//      this.styleProperty().bind(new StringBinding() {
//
//        {
//          super.bind(color2, color3);
//        }
//
//        @Override
//        protected String computeValue() {
//          return "-fx-background-color: linear-gradient(" + Colors.toCSSColorString(color2.get()) + ","
//              + Colors.toCSSColorString(color3.get()) + "); -fx-background-radius: 0 0 0 0;";
//        }
//      });
      this.getColumnConstraints().addAll(
          ColumnConstraintsBuilder.create().minWidth(size1).maxWidth(size1).build(),
          ColumnConstraintsBuilder.create().minWidth(size2).maxWidth(size2).build(),
          ColumnConstraintsBuilder.create().minWidth(size1).maxWidth(size1).build());
      this.minusButton = new Button("-");
      this.plusButton = new Button("+");
      this.minusButton.setMinSize(size1, size1);
      this.minusButton.setMaxSize(size1, size1);
      this.plusButton.setMinSize(size1, size1);
      this.plusButton.setMaxSize(size1, size1);
      this.label = new Label();
      this.text = new Text("100%");
//      this.text.setFontSmoothingType(FontSmoothingType.GRAY);
      this.text.setTextAlignment(TextAlignment.CENTER);
      this.label.setGraphic(text);
      this.label.setAlignment(Pos.CENTER);
      this.label.setMinWidth(size2);
      this.label.setPrefWidth(size2);
      this.label.setMaxWidth(size2);
      this.sizeSlider = new Slider();
      this.minusButton.setOnAction(new EventHandler<ActionEvent>() {

        @Override
        public void handle(ActionEvent event) {
          sizeSlider.decrement();
        }
      });
      this.plusButton.setOnAction(new EventHandler<ActionEvent>() {

        @Override
        public void handle(ActionEvent event) {
          if (sizeSlider.getValue() == 2)
            sizeSlider.setValue(4);
          else
            sizeSlider.increment();
        }
      });
      this.add(minusButton, 0, 0);
      this.add(label, 1, 0);
      this.add(plusButton, 2, 0);
      this.sizeSlider.setOrientation(Orientation.HORIZONTAL);
      this.sizeSlider.setMin(0.1);
      this.sizeSlider.setMax(2);
      this.sizeSlider.setValue(1);
      this.sizeSlider.setBlockIncrement(0.1);
      this.add(sizeSlider, 0, 1, 3, 1);
      text.textProperty().bind(new StringBinding() {

        {
          super.bind(sizeSlider.valueProperty());
        }

        @Override
        protected String computeValue() {
          return (int) (100 * sizeSlider.valueProperty().get()) + "%";
        }
      });
    }
  }
}
