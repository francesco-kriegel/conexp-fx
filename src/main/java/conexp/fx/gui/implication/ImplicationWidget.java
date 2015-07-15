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
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumnBuilder;
import javafx.scene.control.TableView;
import javafx.scene.control.TableViewBuilder;
import javafx.scene.control.ToolBar;
import javafx.scene.control.ToolBarBuilder;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class ImplicationWidget<G, M> extends BorderPane {

  private final FCADataset<G, M>             fca;
  private final TableView<Implication<G, M>> table;
  private final ToolBar                      toolBar;

  @SuppressWarnings({ "deprecation", "unchecked" })
  public ImplicationWidget(final FCADataset<G, M> fcaInstance) {
    super();
    this.fca = fcaInstance;

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
    final Slider supportSlider = new Slider(0, 100, 1);
    supportSlider.setBlockIncrement(5);
    supportSlider.setSnapToTicks(true);
    supportSlider.setMajorTickUnit(1);
    final Label supportValue = new Label();
    supportValue.textProperty().bind(
        Bindings.createStringBinding(() -> ((int) supportSlider.getValue()) + "%", supportSlider.valueProperty()));
    final HBox supportBox = new HBox(supportLabel, supportSlider, supportValue);
    this.table = TableViewBuilder
        .<Implication<G, M>> create()
        .columns(
            TableColumnBuilder
                .<Implication<G, M>, Integer> create()
                .text("Support")
                .cellValueFactory(
                    new Callback<CellDataFeatures<Implication<G, M>, Integer>, ObservableValue<Integer>>() {

                      @Override
                      public ObservableValue<Integer> call(CellDataFeatures<Implication<G, M>, Integer> param) {
                        return new ObservableValueBase<Integer>() {

                          @Override
                          public Integer getValue() {
                            return param.getValue().getSupport().size();
                          }
                        };
                      }
                    })
                .build(),
            TableColumnBuilder
                .<Implication<G, M>, Integer> create()
                .text("Confidence")
                .cellValueFactory(
                    new Callback<CellDataFeatures<Implication<G, M>, Integer>, ObservableValue<Integer>>() {

                      @Override
                      public ObservableValue<Integer> call(CellDataFeatures<Implication<G, M>, Integer> param) {
                        return new ObservableValueBase<Integer>() {

                          @Override
                          public Integer getValue() {
                            return (int) (100d * param.getValue().getConfidence());
                          }
                        };
                      }
                    })
                .build(),
            TableColumnBuilder
                .<Implication<G, M>, String> create()
                .text("Premise")
                .cellValueFactory(new Callback<CellDataFeatures<Implication<G, M>, String>, ObservableValue<String>>() {

                  @Override
                  public ObservableValue<String> call(CellDataFeatures<Implication<G, M>, String> param) {
                    if (!param.getValue().getPremise().isEmpty()
                        && param.getValue().getPremise().iterator().next() instanceof OWLClassExpression)
                      return new SimpleStringProperty(
                          OWLtoString.toString(
                              OWLManager.getOWLDataFactory().getOWLObjectIntersectionOf(
                                  Collections3.transform(
                                      param.getValue().getPremise(),
                                      Isomorphism.create(x -> (OWLClassExpression) x, null)))));
                    return new SimpleStringProperty(param.getValue().getPremise().toString());
                  }
                })
                .build(),
            TableColumnBuilder
                .<Implication<G, M>, String> create()
                .text("Conclusion")
                .cellValueFactory(new Callback<CellDataFeatures<Implication<G, M>, String>, ObservableValue<String>>() {

                  @Override
                  public ObservableValue<String> call(CellDataFeatures<Implication<G, M>, String> param) {
                    if (!param.getValue().getConclusion().isEmpty()
                        && param.getValue().getConclusion().iterator().next() instanceof OWLClassExpression)
                      return new SimpleStringProperty(
                          OWLtoString.toString(
                              OWLManager.getOWLDataFactory().getOWLObjectIntersectionOf(
                                  Collections3.transform(
                                      param.getValue().getConclusion(),
                                      Isomorphism.create(x -> (OWLClassExpression) x, null)))));
                    return new SimpleStringProperty(param.getValue().getConclusion().toString());
                  }
                })
                .build())
        .items(fca.implications)
        .build();
    table.itemsProperty().bind(
        Bindings.createObjectBinding(
            () -> fca.implications.filtered(
                impl -> impl.getConfidence() >= confidenceSlider.getValue() / 100d
                    && (int) (100d * (((double) impl.getSupport().size())
                        / ((double) fca.context.rowHeads().size()))) >= (int) supportSlider.getValue()),
            fca.implications,
            confidenceSlider.valueProperty(),
            supportSlider.valueProperty()));
    this.setCenter(table);
    final Button computeButton = ButtonBuilder.create().text("Refresh").onAction(new EventHandler<ActionEvent>() {

      @Override
      public final void handle(final ActionEvent event) {
        fcaInstance.calcImplications();
      }
    }).build();

    this.toolBar = ToolBarBuilder.create().items(computeButton, supportBox, confidenceBox).build();
    this.setTop(toolBar);
    this.table.getFocusModel().focusedItemProperty().addListener(new ChangeListener<Implication<G, M>>() {

      @Override
      public void changed(
          ObservableValue<? extends Implication<G, M>> observable,
          Implication<G, M> oldValue,
          Implication<G, M> newValue) {
        if (newValue != null)
          fca.conceptGraph.highlight(true, fca.conceptGraph.highlightRequests.implication(newValue));
      }
    });
  }
}
