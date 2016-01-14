package conexp.fx.gui.implication;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;

import conexp.fx.core.collections.Collections3;
import conexp.fx.core.implication.Implication;
import conexp.fx.core.math.Isomorphism;
import conexp.fx.core.util.OWLtoString;
import conexp.fx.gui.dataset.FCADataset;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumnBuilder;
import javafx.scene.control.TableView;
import javafx.scene.control.TableViewBuilder;
import javafx.scene.control.ToolBar;
import javafx.scene.control.ToolBarBuilder;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class ImplicationWidget<G, M> extends BorderPane {

  private final FCADataset<G, M>             dataset;
  private final TableView<Implication<G, M>> table;
  private final ToolBar                      toolBar;

  @SuppressWarnings({ "deprecation", "unchecked" })
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
                      if (!p.getValue().getPremise().isEmpty()
                          && p.getValue().getPremise().iterator().next() instanceof OWLClassExpression)
                        return OWLtoString.toString(
                            OWLManager.getOWLDataFactory().getOWLObjectIntersectionOf(
                                Collections3.transform(
                                    p.getValue().getPremise(),
                                    Isomorphism.create(x -> (OWLClassExpression) x, null))));
                      return p.getValue().getPremise().toString();
                    }))
                    .build(),
                TableColumnBuilder
                    .<Implication<G, M>, String> create()
                    .text("Conclusion")
                    .cellValueFactory(p -> Bindings.createObjectBinding(() -> {
                      if (!p.getValue().getConclusion().isEmpty()
                          && p.getValue().getConclusion().iterator().next() instanceof OWLClassExpression)
                        return OWLtoString.toString(
                            OWLManager.getOWLDataFactory().getOWLObjectIntersectionOf(
                                Collections3.transform(
                                    p.getValue().getConclusion(),
                                    Isomorphism.create(x -> (OWLClassExpression) x, null))));
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
    final Button computeButton =
        ButtonBuilder.create().text("Refresh").onAction(__ -> dataset.calcImplications()).build();
    this.toolBar = ToolBarBuilder.create().items(computeButton, supportBox, confidenceBox).build();
    this.setTop(toolBar);
    this.table.getFocusModel().focusedItemProperty().addListener((__, ___, newValue) -> {
      if (newValue != null)
        dataset.conceptGraph.highlight(true, dataset.conceptGraph.highlightRequests.implication(newValue));
    });
  }
}
