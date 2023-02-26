package conexp.fx.gui.context;

//import org.semanticweb.owlapi.apibinding.OWLManager;
//import org.semanticweb.owlapi.model.OWLClassExpression;

import conexp.fx.core.collections.Collections3;
import conexp.fx.core.context.Implication;
import conexp.fx.core.math.GuavaIsomorphism;
//import conexp.fx.core.util.OWLUtil;
import conexp.fx.gui.dataset.FCADataset;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2023 Francesco Kriegel
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

import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumnBuilder;
import javafx.scene.control.TableView;
import javafx.scene.control.TableViewBuilder;
import javafx.scene.control.ToolBar;
import javafx.scene.control.ToolBarBuilder;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

@SuppressWarnings({ "deprecation", "unchecked" })
public class ImplicationWidget<G, M> extends BorderPane {

  private final FCADataset<G, M>             dataset;
  private final TableView<Implication<G, M>> table;
  private final ToolBar                      toolBar;

  public ImplicationWidget(final FCADataset<G, M> dataset) {
    super();
    this.dataset = dataset;
    final Label confidenceLabel = new Label("Confidence");
    final Slider confidenceSlider = new Slider(0, 100, 100);
    confidenceSlider.setBlockIncrement(5);
    confidenceSlider.setSnapToTicks(true);
    confidenceSlider.setMajorTickUnit(1);
    final Label confidenceValue = new Label();
    confidenceValue.textProperty().bind(
        Bindings
            .createStringBinding(() -> ((int) confidenceSlider.getValue()) + "%", confidenceSlider.valueProperty()));
    final HBox confidenceBox = new HBox(confidenceLabel, confidenceSlider, confidenceValue);
    final Label supportLabel = new Label("Support");
    final Slider supportSlider = new Slider(0, 100, 0);
    supportSlider.setBlockIncrement(5);
    supportSlider.setSnapToTicks(true);
    supportSlider.setMajorTickUnit(1);
    final Label supportValue = new Label();
    supportValue.textProperty().bind(
        Bindings.createStringBinding(() -> ((int) supportSlider.getValue()) + "%", supportSlider.valueProperty()));
    final HBox supportBox = new HBox(supportLabel, supportSlider, supportValue);
    this.table =
        TableViewBuilder
            .<Implication<G, M>> create()
            .columns(
                TableColumnBuilder
                    .<Implication<G, M>, Integer> create()
                    .text("Support")
                    .cellValueFactory(p -> Bindings.createObjectBinding(() -> p.getValue().getSupport().size()))
                    .build(),
                TableColumnBuilder
                    .<Implication<G, M>, Integer> create()
                    .text("Confidence")
                    .cellValueFactory(
                        p -> Bindings.createObjectBinding(() -> (int) (100d * p.getValue().getConfidence())))
                    .build(),
                TableColumnBuilder
                    .<Implication<G, M>, String> create()
                    .text("Premise")
                    .cellValueFactory(p -> Bindings.createObjectBinding(() -> {
//                      if (!p.getValue().getPremise().isEmpty()
//                          && p.getValue().getPremise().iterator().next() instanceof OWLClassExpression)
//                        return OWLUtil.toString(
//                            OWLManager.getOWLDataFactory().getOWLObjectIntersectionOf(
//                                Collections3.transform(
//                                    p.getValue().getPremise(),
//                                    GuavaIsomorphism.create(x -> (OWLClassExpression) x, null))));
                      return p.getValue().getPremise().toString();
                    }))
                    .build(),
                TableColumnBuilder
                    .<Implication<G, M>, String> create()
                    .text("Conclusion")
                    .cellValueFactory(p -> Bindings.createObjectBinding(() -> {
//                      if (!p.getValue().getConclusion().isEmpty()
//                          && p.getValue().getConclusion().iterator().next() instanceof OWLClassExpression)
//                        return OWLUtil.toString(
//                            OWLManager.getOWLDataFactory().getOWLObjectIntersectionOf(
//                                Collections3.transform(
//                                    p.getValue().getConclusion(),
//                                    GuavaIsomorphism.create(x -> (OWLClassExpression) x, null))));
                      return p.getValue().getConclusion().toString();
                    }))
                    .build())
            .items(dataset.implications)
            .build();
    // TODO: the following line disables sorting functionality when clicking on column heads!
    table.itemsProperty().bind(
        Bindings.createObjectBinding(
            () -> dataset.implications.filtered(
                impl -> impl.getConfidence() >= confidenceSlider.getValue() / 100d
                    && (int) (100d * (((double) impl.getSupport().size())
                        / ((double) dataset.context.rowHeads().size()))) >= (int) supportSlider.getValue()),
            dataset.implications,
            confidenceSlider.valueProperty(),
            supportSlider.valueProperty()));
    this.setCenter(table);
    this.toolBar = ToolBarBuilder.create().items(supportBox, confidenceBox).build();
    this.setTop(toolBar);
    this.table.getFocusModel().focusedItemProperty().addListener((__, ___, newValue) -> {
      if (newValue != null)
        dataset.conceptGraph.highlight(true, dataset.conceptGraph.highlightRequests.implication(newValue));
    });
  }
}
