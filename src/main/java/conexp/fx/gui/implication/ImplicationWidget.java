package conexp.fx.gui.implication;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumnBuilder;
import javafx.scene.control.TableView;
import javafx.scene.control.TableViewBuilder;
import javafx.scene.control.ToolBar;
import javafx.scene.control.ToolBarBuilder;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import conexp.fx.core.implication.Implication;
import conexp.fx.gui.FCAInstance;

public class ImplicationWidget<G, M> extends BorderPane {

  private final FCAInstance<G, M>            fca;
  private final TableView<Implication<G, M>> table;
  private final ToolBar                      toolBar;

  @SuppressWarnings({ "deprecation", "unchecked" })
  public ImplicationWidget(final FCAInstance<G, M> fcaInstance) {
    super();
    this.fca = fcaInstance;
    this.table =
        TableViewBuilder
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
                    .<Implication<G, M>, String> create()
                    .text("Premise")
                    .cellValueFactory(
                        new Callback<CellDataFeatures<Implication<G, M>, String>, ObservableValue<String>>() {

                          @Override
                          public ObservableValue<String> call(CellDataFeatures<Implication<G, M>, String> param) {
                            return new SimpleStringProperty(param.getValue().getPremise().toString());
                          }
                        })
                    .build(),
                TableColumnBuilder
                    .<Implication<G, M>, String> create()
                    .text("Conclusion")
                    .cellValueFactory(
                        new Callback<CellDataFeatures<Implication<G, M>, String>, ObservableValue<String>>() {

                          @Override
                          public ObservableValue<String> call(CellDataFeatures<Implication<G, M>, String> param) {
                            return new SimpleStringProperty(param.getValue().getConclusion().toString());
                          }
                        })
                    .build())
            .items(fca.implications)
            .build();
    this.setCenter(table);
    final Button computeButton = ButtonBuilder.create().text("Compute").onAction(new EventHandler<ActionEvent>() {

      @Override
      public final void handle(final ActionEvent event) {
        fcaInstance.calcImplications();
      }
    }).build();
    final Button exploreButton = ButtonBuilder.create().text("Explore").onAction(new EventHandler<ActionEvent>() {

      @Override
      public final void handle(final ActionEvent event) {
        new AttributeExploration((FCAInstance<String, String>) fcaInstance);
      }
    }).disable(!fcaInstance.editable).build();
    this.toolBar = ToolBarBuilder.create().items(computeButton, exploreButton).build();
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
