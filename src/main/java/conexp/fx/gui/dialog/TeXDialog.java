package conexp.fx.gui.dialog;

import conexp.fx.core.exporter.TeXExporter.ContextTeXPackage;
import conexp.fx.core.exporter.TeXExporter.DiagramTeXPackage;
import conexp.fx.core.exporter.TeXExporter.FitScale;
import conexp.fx.core.exporter.TeXExporter.ScaleEnum;
import conexp.fx.core.exporter.TeXExporter.TeXOptions;

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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxBuilder;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioButtonBuilder;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToggleGroupBuilder;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import jfxtras.scene.control.ListSpinner;

public final class TeXDialog<G, M> extends FXDialog<TeXOptions> {

  public TeXDialog(final Stage stage) {
    super(stage, FXDialog.Style.WARN, "TeX Export Wizard", "TeX Export Wizard Options", new VBox(), 270);
    VBox box = (VBox) pane.getCenter();
    box.setPadding(new Insets(0, 10, 0, 10));
    box.setSpacing(4);
    value =
        new TeXOptions(null, false, true, false, ContextTeXPackage.None, DiagramTeXPackage.ConExpFX, new FitScale(
            80,
            120));
    final CheckBox arrowsCheckBox = CheckBoxBuilder.create().text(
        "Arrow Relations").build();
    final CheckBox labelsCheckBox = CheckBoxBuilder.create().text(
        "Concept Labels").selected(
        true).build();
    final CheckBox standAloneCheckBox = CheckBoxBuilder.create().disable(
        true).text(
        "Stand-Alone Document").build();
    final RadioButton noneContextButton = RadioButtonBuilder.create().text(
        "Context Package: None").selected(
        true).userData(
        ContextTeXPackage.None).build();
    final RadioButton ganterContextButton = RadioButtonBuilder.create().text(
        "Context Package: Ganter").userData(
        ContextTeXPackage.Ganter).build();
    final RadioButton tabularContextButton = RadioButtonBuilder.create().text(
        "Context Package: Tabular").userData(
        ContextTeXPackage.Tabular).build();
    final RadioButton noneDiagramButton = RadioButtonBuilder.create().text(
        "Diagram Package: None").userData(
        DiagramTeXPackage.None).build();
    final RadioButton ganterDiagramButton = RadioButtonBuilder.create().text(
        "Diagram Package: Ganter").userData(
        DiagramTeXPackage.Ganter).build();
    final RadioButton conExpFXDiagramButton = RadioButtonBuilder.create().text(
        "Diagram Package: ConExpFX").selected(
        true).userData(
        DiagramTeXPackage.ConExpFX).build();
    final RadioButton fitButton = RadioButtonBuilder.create().text(
        "Diagram Scale: Fit").userData(
        ScaleEnum.Fit).build();
    final RadioButton fitWidthButton = RadioButtonBuilder.create().text(
        "Diagram Scale: Fit Width").userData(
        ScaleEnum.FitWidth).build();
    final RadioButton fitHeightButton = RadioButtonBuilder.create().text(
        "Diagram Scale: Fit Height").userData(
        ScaleEnum.FitHeight).build();
    final RadioButton fitRatioButton = RadioButtonBuilder.create().text(
        "Diagram Scale: Fit Ratio").selected(
        true).userData(
        ScaleEnum.FitRatio).build();
    final ListSpinner<Integer> widthSpinner = new ListSpinner<Integer>(1, 1000);
    final ListSpinner<Integer> heightSpinner = new ListSpinner<Integer>(1, 1000);
    widthSpinner.valueProperty().set(
        80);
    heightSpinner.valueProperty().set(
        120);
    widthSpinner.withPostfix("mm");
    heightSpinner.withPostfix("mm");
//      widthSpinner.withAlignment(Pos.CENTER);
//      heightSpinner.withAlignment(Pos.CENTER);
//      widthSpinner.withArrowDirection(ArrowDirection.HORIZONTAL);
//      heightSpinner.withArrowDirection(ArrowDirection.VERTICAL);
//      widthSpinner.withArrowPosition(ArrowPosition.SPLIT);
//      heightSpinner.withArrowPosition(ArrowPosition.SPLIT);
    widthSpinner.withEditable(true);
    widthSpinner.withStringConverter(new IntegerStringConverter());
    heightSpinner.withEditable(true);
    heightSpinner.withStringConverter(new IntegerStringConverter());
    widthSpinner.setMinWidth(100);
    widthSpinner.setMaxWidth(100);
    heightSpinner.setMinWidth(100);
    heightSpinner.setMaxWidth(100);
    final ToggleGroup contextGroup = ToggleGroupBuilder.create().toggles(
        noneContextButton,
        ganterContextButton,
        tabularContextButton).build();
    final ToggleGroup diagramGroup = ToggleGroupBuilder.create().toggles(
        noneDiagramButton,
        ganterDiagramButton,
        conExpFXDiagramButton).build();
    final ToggleGroup scaleGroup = ToggleGroupBuilder.create().toggles(
        fitButton,
        fitWidthButton,
        fitHeightButton,
        fitRatioButton).build();
    arrowsCheckBox.selectedProperty().addListener(
        new ChangeListener<Boolean>() {

          @Override
          public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            value.arrows = newValue;
          }
        });
    labelsCheckBox.selectedProperty().addListener(
        new ChangeListener<Boolean>() {

          @Override
          public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            value.labels = newValue;
          }
        });
    standAloneCheckBox.selectedProperty().addListener(
        new ChangeListener<Boolean>() {

          @Override
          public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            value.standAlone = newValue;
          }
        });
    contextGroup.selectedToggleProperty().addListener(
        new ChangeListener<Toggle>() {

          @Override
          public void changed(ObservableValue<? extends Toggle> observable, Toggle oldToggle, Toggle newToggle) {
            value.contextTeXPackage = (ContextTeXPackage) newToggle.getUserData();
          }
        });
    diagramGroup.selectedToggleProperty().addListener(
        new ChangeListener<Toggle>() {

          @Override
          public void changed(ObservableValue<? extends Toggle> observable, Toggle oldToggle, Toggle newToggle) {
            value.diagramTeXPackage = (DiagramTeXPackage) newToggle.getUserData();
          }
        });
    scaleGroup.selectedToggleProperty().addListener(
        new ChangeListener<Toggle>() {

          @Override
          public void changed(ObservableValue<? extends Toggle> observable, Toggle oldToggle, Toggle newToggle) {
            value.scale = ((ScaleEnum) newToggle.getUserData()).toOption(
                widthSpinner.valueProperty().get(),
                heightSpinner.valueProperty().get());
            widthSpinner.disableProperty().set(
                (ScaleEnum) newToggle.getUserData() == ScaleEnum.FitHeight);
            heightSpinner.disableProperty().set(
                (ScaleEnum) newToggle.getUserData() == ScaleEnum.FitWidth);
          }
        });
    widthSpinner.valueProperty().addListener(
        new ChangeListener<Integer>() {

          @Override
          public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
            value.scale = ((ScaleEnum) scaleGroup.selectedToggleProperty().get().getUserData()).toOption(
                newValue,
                heightSpinner.valueProperty().get());
          }
        });
    heightSpinner.valueProperty().addListener(
        new ChangeListener<Integer>() {

          @Override
          public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
            value.scale = ((ScaleEnum) scaleGroup.selectedToggleProperty().get().getUserData()).toOption(
                widthSpinner.valueProperty().get(),
                newValue);
          }
        });
    box.getChildren().addAll(
        VBoxBuilder.create().padding(
            new Insets(2, 0, 2, 0)).spacing(
            4).children(
            arrowsCheckBox,
            labelsCheckBox,
            standAloneCheckBox).build());
    box.getChildren().addAll(
        VBoxBuilder.create().padding(
            new Insets(2, 0, 2, 0)).spacing(
            4).children(
            noneContextButton,
            ganterContextButton,
            tabularContextButton).build());
    box.getChildren().addAll(
        VBoxBuilder.create().padding(
            new Insets(2, 0, 2, 0)).spacing(
            4).children(
            noneDiagramButton,
            ganterDiagramButton,
            conExpFXDiagramButton).build());
    box.getChildren().addAll(
        VBoxBuilder.create().padding(
            new Insets(2, 0, 0, 0)).spacing(
            4).children(
            fitButton,
            fitWidthButton,
            fitHeightButton,
            fitRatioButton).build());
    box.getChildren().addAll(
        HBoxBuilder.create().padding(
            new Insets(0, 0, 2, 0)).spacing(
            4).children(
            widthSpinner,
            heightSpinner).build());
  }
}
