package conexp.fx.gui.concept;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumnBuilder;
import javafx.scene.control.TableView;
import javafx.scene.control.TableViewBuilder;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import conexp.fx.core.context.Concept;
import conexp.fx.gui.dataset.FCADataset;

public class ConceptWidget<G, M> extends BorderPane {

  private final FCADataset<G, M>        fca;
  private final TableView<Concept<G, M>> table;

  public ConceptWidget(final FCADataset<G, M> fcaInstance) {
    super();
    this.fca = fcaInstance;
    this.table =
        TableViewBuilder
            .<Concept<G, M>> create()
            .columns(
                TableColumnBuilder
                    .<Concept<G, M>, Integer> create()
                    .text("Support")
                    .cellValueFactory(
                        new Callback<CellDataFeatures<Concept<G, M>, Integer>, ObservableValue<Integer>>() {

                          @Override
                          public final ObservableValue<Integer> call(
                              final CellDataFeatures<Concept<G, M>, Integer> param) {
                            return new ObservableValueBase<Integer>() {

                              @Override
                              public final Integer getValue() {
                                return param.getValue().getExtent().size();
                              }

                            };
                          }
                        })
                    .build(),
                TableColumnBuilder
                    .<Concept<G, M>, Integer> create()
                    .text("Support")
                    .cellValueFactory(
                        new Callback<CellDataFeatures<Concept<G, M>, Integer>, ObservableValue<Integer>>() {

                          @Override
                          public ObservableValue<Integer> call(CellDataFeatures<Concept<G, M>, Integer> param) {
                            return new ObservableValueBase<Integer>() {

                              @Override
                              public Integer getValue() {
                                return (int) ((100d * (double) param.getValue().getExtent().size()) / ((double) fca.context
                                    .rowHeads()
                                    .size()));
                              }
                            };
                          }
                        })
                    .build(),
                TableColumnBuilder
                    .<Concept<G, M>, String> create()
                    .text("Extent")
                    .cellValueFactory(new Callback<CellDataFeatures<Concept<G, M>, String>, ObservableValue<String>>() {

                      @Override
                      public final ObservableValue<String> call(final CellDataFeatures<Concept<G, M>, String> arg0) {
                        final String s = arg0.getValue().getExtent().toString();
                        return new SimpleStringProperty(s.substring(1, s.length() - 1));
                      }
                    })
                    .build(),
                TableColumnBuilder
                    .<Concept<G, M>, Integer> create()
                    .text("Attributes")
                    .cellValueFactory(
                        new Callback<CellDataFeatures<Concept<G, M>, Integer>, ObservableValue<Integer>>() {

                          @Override
                          public ObservableValue<Integer> call(CellDataFeatures<Concept<G, M>, Integer> param) {
                            return new ObservableValueBase<Integer>() {

                              @Override
                              public Integer getValue() {
                                return param.getValue().intent().size();
                              }
                            };
                          }
                        })
                    .build(),
                TableColumnBuilder
                    .<Concept<G, M>, String> create()
                    .text("Intent")
                    .cellValueFactory(new Callback<CellDataFeatures<Concept<G, M>, String>, ObservableValue<String>>() {

                      @Override
                      public final ObservableValue<String> call(final CellDataFeatures<Concept<G, M>, String> arg0) {
                        final String s = arg0.getValue().getIntent().toString();
                        return new SimpleStringProperty(s.substring(1, s.length() - 1));
                      }
                    })
                    .build())
            .items(fca.concepts)
            .build();
    this.setCenter(table);
  }
}
