package conexp.fx.gui.assistent;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.io.File;
import java.io.IOException;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxBuilder;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioButtonBuilder;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToggleGroupBuilder;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import jfxtras.scene.control.ListSpinner;
import conexp.fx.core.exporter.TeXExporter;
import conexp.fx.core.exporter.TeXExporter.ContextTeXPackage;
import conexp.fx.core.exporter.TeXExporter.DiagramTeXPackage;
import conexp.fx.core.exporter.TeXExporter.FitScale;
import conexp.fx.core.exporter.TeXExporter.ScaleEnum;
import conexp.fx.core.exporter.TeXExporter.TeXOptions;
import conexp.fx.core.util.FileFormat;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.dataset.FCADataset;

@SuppressWarnings("deprecation")
public class ExportAssistent extends Assistent<String> {

  public final class TeXExportPage extends AssistentPage<TeXOptions> {

    private final FCADataset<?, ?> fcaInstance;

    public TeXExportPage(final FCADataset<?, ?> fcaInstance) {
      super("TeX Export", "Exports a Formal Context and its Concept Lattice to LaTeX", null, r -> null);
      this.fcaInstance = fcaInstance;
      this.resultProperty.set(new TeXOptions(null, false, false, false, null, null, null));
      this.contentProperty.set(createContentNode());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void onNext() {
      try {
        new TeXExporter(
            fcaInstance.context,
            fcaInstance.contextWidget.rowHeaderPane.rowMap,
            fcaInstance.contextWidget.colHeaderPane.columnMap,
            fcaInstance.layout,
            this.resultProperty.get()).export();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    protected Node createContentNode() {
      VBox box = new VBox();
      box.setPadding(new Insets(0, 10, 0, 10));
      box.setSpacing(4);
      this.resultProperty.set(new TeXOptions(
          null,
          false,
          true,
          false,
          ContextTeXPackage.None,
          DiagramTeXPackage.ConExpFX,
          new FitScale(80, 120)));
      final CheckBox arrowsCheckBox = CheckBoxBuilder.create().text("Arrow Relations").build();
      final CheckBox labelsCheckBox = CheckBoxBuilder.create().text("Concept Labels").selected(true).build();
      final CheckBox standAloneCheckBox = CheckBoxBuilder.create().disable(true).text("Stand-Alone Document").build();
      final RadioButton noneContextButton =
          RadioButtonBuilder
              .create()
              .text("Context Package: None")
              .selected(true)
              .userData(ContextTeXPackage.None)
              .build();
      final RadioButton ganterContextButton =
          RadioButtonBuilder.create().text("Context Package: Ganter").userData(ContextTeXPackage.Ganter).build();
      final RadioButton tabularContextButton =
          RadioButtonBuilder.create().text("Context Package: Tabular").userData(ContextTeXPackage.Tabular).build();
      final RadioButton noneDiagramButton =
          RadioButtonBuilder.create().text("Diagram Package: None").userData(DiagramTeXPackage.None).build();
      final RadioButton ganterDiagramButton =
          RadioButtonBuilder.create().text("Diagram Package: Ganter").userData(DiagramTeXPackage.Ganter).build();
      final RadioButton conExpFXDiagramButton =
          RadioButtonBuilder
              .create()
              .text("Diagram Package: ConExpFX")
              .selected(true)
              .userData(DiagramTeXPackage.ConExpFX)
              .build();
      final RadioButton fitButton =
          RadioButtonBuilder.create().text("Diagram Scale: Fit").userData(ScaleEnum.Fit).build();
      final RadioButton fitWidthButton =
          RadioButtonBuilder.create().text("Diagram Scale: Fit Width").userData(ScaleEnum.FitWidth).build();
      final RadioButton fitHeightButton =
          RadioButtonBuilder.create().text("Diagram Scale: Fit Height").userData(ScaleEnum.FitHeight).build();
      final RadioButton fitRatioButton =
          RadioButtonBuilder
              .create()
              .text("Diagram Scale: Fit Ratio")
              .selected(true)
              .userData(ScaleEnum.FitRatio)
              .build();
      final ListSpinner<Integer> widthSpinner = new ListSpinner<Integer>(1, 1000);
      final ListSpinner<Integer> heightSpinner = new ListSpinner<Integer>(1, 1000);
      widthSpinner.valueProperty().set(80);
      heightSpinner.valueProperty().set(120);
      widthSpinner.withPostfix("mm");
      heightSpinner.withPostfix("mm");
//        widthSpinner.withAlignment(Pos.CENTER);
//        heightSpinner.withAlignment(Pos.CENTER);
//        widthSpinner.withArrowDirection(ArrowDirection.HORIZONTAL);
//        heightSpinner.withArrowDirection(ArrowDirection.VERTICAL);
//        widthSpinner.withArrowPosition(ArrowPosition.SPLIT);
//        heightSpinner.withArrowPosition(ArrowPosition.SPLIT);
      widthSpinner.withEditable(true);
      widthSpinner.withStringConverter(new IntegerStringConverter());
      heightSpinner.withEditable(true);
      heightSpinner.withStringConverter(new IntegerStringConverter());
      widthSpinner.setMinWidth(100);
      widthSpinner.setMaxWidth(100);
      heightSpinner.setMinWidth(100);
      heightSpinner.setMaxWidth(100);
      final ToggleGroup contextGroup =
          ToggleGroupBuilder.create().toggles(noneContextButton, ganterContextButton, tabularContextButton).build();
      final ToggleGroup diagramGroup =
          ToggleGroupBuilder.create().toggles(noneDiagramButton, ganterDiagramButton, conExpFXDiagramButton).build();
      final ToggleGroup scaleGroup =
          ToggleGroupBuilder.create().toggles(fitButton, fitWidthButton, fitHeightButton, fitRatioButton).build();
      arrowsCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          TeXExportPage.this.resultProperty.get().arrows = newValue;
        }
      });
      labelsCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          TeXExportPage.this.resultProperty.get().labels = newValue;
        }
      });
      standAloneCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          TeXExportPage.this.resultProperty.get().standAlone = newValue;
        }
      });
      contextGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

        @Override
        public void changed(ObservableValue<? extends Toggle> observable, Toggle oldToggle, Toggle newToggle) {
          TeXExportPage.this.resultProperty.get().contextTeXPackage = (ContextTeXPackage) newToggle.getUserData();
        }
      });
      diagramGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

        @Override
        public void changed(ObservableValue<? extends Toggle> observable, Toggle oldToggle, Toggle newToggle) {
          TeXExportPage.this.resultProperty.get().diagramTeXPackage = (DiagramTeXPackage) newToggle.getUserData();
        }
      });
      scaleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

        @Override
        public void changed(ObservableValue<? extends Toggle> observable, Toggle oldToggle, Toggle newToggle) {
          TeXExportPage.this.resultProperty.get().scale =
              ((ScaleEnum) newToggle.getUserData()).toOption(widthSpinner.valueProperty().get(), heightSpinner
                  .valueProperty()
                  .get());
          widthSpinner.disableProperty().set((ScaleEnum) newToggle.getUserData() == ScaleEnum.FitHeight);
          heightSpinner.disableProperty().set((ScaleEnum) newToggle.getUserData() == ScaleEnum.FitWidth);
        }
      });
      widthSpinner.valueProperty().addListener(new ChangeListener<Integer>() {

        @Override
        public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
          TeXExportPage.this.resultProperty.get().scale =
              ((ScaleEnum) scaleGroup.selectedToggleProperty().get().getUserData()).toOption(newValue, heightSpinner
                  .valueProperty()
                  .get());
        }
      });
      heightSpinner.valueProperty().addListener(new ChangeListener<Integer>() {

        @Override
        public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
          TeXExportPage.this.resultProperty.get().scale =
              ((ScaleEnum) scaleGroup.selectedToggleProperty().get().getUserData()).toOption(widthSpinner
                  .valueProperty()
                  .get(), newValue);
        }
      });
      box.getChildren().addAll(
          VBoxBuilder
              .create()
              .padding(new Insets(2, 0, 2, 0))
              .spacing(4)
              .children(arrowsCheckBox, labelsCheckBox, standAloneCheckBox)
              .build());
      box.getChildren().addAll(
          VBoxBuilder
              .create()
              .padding(new Insets(2, 0, 2, 0))
              .spacing(4)
              .children(noneContextButton, ganterContextButton, tabularContextButton)
              .build());
      box.getChildren().addAll(
          VBoxBuilder
              .create()
              .padding(new Insets(2, 0, 2, 0))
              .spacing(4)
              .children(noneDiagramButton, ganterDiagramButton, conExpFXDiagramButton)
              .build());
      box.getChildren().addAll(
          VBoxBuilder
              .create()
              .padding(new Insets(2, 0, 0, 0))
              .spacing(4)
              .children(fitButton, fitWidthButton, fitHeightButton, fitRatioButton)
              .build());
      box
          .getChildren()
          .addAll(
              HBoxBuilder
                  .create()
                  .padding(new Insets(0, 0, 2, 0))
                  .spacing(4)
                  .children(widthSpinner, heightSpinner)
                  .build());
      return box;
    }

  }

  private final FCADataset fcaInstance;

  public ExportAssistent(final Stage owner, final FCADataset<?, ?> fcaInstance) {
    super(owner, "Export Wizard", "Export Wizard", "Exports a Formal Context", null, r -> r.equals("TEX") ? "TEX"
        : null);
    this.fcaInstance = fcaInstance;
    initialize();
  }

  @Override
  protected Node createInitialNode() {
    final BorderPane pane = new BorderPane();
    final ToggleGroup toggleGroup = new ToggleGroup();
    final RadioButton htmlButton = new RadioButton("HTML");
    final RadioButton pdfButton = new RadioButton("PDF");
    final RadioButton pngButton = new RadioButton("PNG");
    final RadioButton svgButton = new RadioButton("SVG");
    final RadioButton texButton = new RadioButton("LaTeX");
    htmlButton.setToggleGroup(toggleGroup);
    pdfButton.setToggleGroup(toggleGroup);
    pngButton.setToggleGroup(toggleGroup);
    svgButton.setToggleGroup(toggleGroup);
    texButton.setToggleGroup(toggleGroup);
    htmlButton.setUserData("HTML");
    pdfButton.setUserData("PDF");
    pngButton.setUserData("PNG");
    svgButton.setUserData("SVG");
    texButton.setUserData("TEX");
    final VBox toggleBox = new VBox(htmlButton, pdfButton, pngButton, svgButton, texButton);
    this.resultProperty.bind(new ObjectBinding<String>() {

      {
        super.bind(toggleGroup.selectedToggleProperty());
      }

      @Override
      protected String computeValue() {
        final Toggle toggle = toggleGroup.selectedToggleProperty().get();
        if (toggle == null)
          return null;
        return (String) toggle.getUserData();
      }

    });
    pane.setPadding(new Insets(4));
    pane.setCenter(toggleBox);
    return pane;
  }

  @Override
  protected void createPages() {
    this.availablePages.put("TEX", new TeXExportPage(fcaInstance));
  }

  @Override
  protected void onNext() {
    if (!this.resultProperty.get().equals("TEX")) {
      final FileChooser fc = new FileChooser();
      fc.setInitialDirectory(ConExpFX.instance.lastDirectory);
      final File result = fc.showSaveDialog(owner);
      if (result != null)
        fcaInstance.export(FileFormat.valueOf(this.resultProperty.get()), result);
    }
  }

}
