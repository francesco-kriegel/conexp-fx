package conexp.fx.gui.exploration;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ListViewBuilder;
import javafx.scene.control.TabBuilder;
import javafx.scene.control.TabPaneBuilder;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumnBuilder;
import javafx.scene.control.TableView;
import javafx.scene.control.TableViewBuilder;
import javafx.scene.control.ToolBar;
import javafx.scene.control.ToolBarBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import conexp.fx.core.collections.relation.RelationEvent;
import conexp.fx.core.collections.relation.RelationEventHandler;
import conexp.fx.core.context.Concept;
import conexp.fx.gui.tab.CFXTab;
import de.tudresden.inf.tcs.fcalib.Implication;

public class ImplicationWidget<G, M> extends BorderPane {

  private final CFXTab<G, M>             tab;
  private final ListView<Implication<M>> list;
  private final ToolBar                  toolBar;

  public ImplicationWidget(final CFXTab<G, M> tab) {
    super();
    this.tab = tab;
    this.list =
        ListViewBuilder
            .<Implication<M>> create()
            .cellFactory(new Callback<ListView<Implication<M>>, ListCell<Implication<M>>>() {

              @Override
              public ListCell<Implication<M>> call(ListView<Implication<M>> list) {
                final ListCell<Implication<M>> cell = new ListCell<Implication<M>>() {

                  @Override
                  protected void updateItem(Implication<M> impl, boolean empty) {
                    super.updateItem(impl, empty);
                    if (impl != null) {
                      setText(tab.fca.context.colAnd(impl.getPremise()).size() + ": " + impl.toString());
                      setUserData(impl);
                    }
                  }
                };
                cell.setOnMouseEntered(new EventHandler<MouseEvent>() {

                  @Override
                  public void handle(MouseEvent event) {
                    final Object userData = cell.getUserData();
                    if (userData != null)
                      tab.conceptGraph.highlight(
                          true,
                          tab.conceptGraph.highlightRequests.implication((Implication<M>) userData));
                  }
                });
                return cell;
              }
            })
            .items(tab.fca.implications)
            .build();
    final ObservableList<Concept<G, M>> l = FXCollections.<Concept<G, M>> observableArrayList();
//    tab.fca.lattice.addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {
//
//      @Override
//      public void handle(RelationEvent<Concept<G, M>, Concept<G, M>> event) {
//        System.out.println("add");
////        synchronized (tab.fca.layout.generators) {
////          l.addAll(event.getRows());
////        }
//      }
//    }, RelationEvent.ROWS_ADDED);
//    tab.fca.lattice.addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {
//
//      @Override
//      public void handle(RelationEvent<Concept<G, M>, Concept<G, M>> event) {
//        System.out.println("clear");
////        synchronized (tab.fca.layout.generators) {
////          l.clear();
////        }
//      }
//    }, RelationEvent.ROWS_CLEARED);
//    tab.fca.lattice.addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {
//
//      @Override
//      public void handle(RelationEvent<Concept<G, M>, Concept<G, M>> event) {
//        System.out.println("remove");
////        synchronized (tab.fca.layout.generators) {
////          l.removeAll(event.getRows());
////        }
//      }
//    }, RelationEvent.ROWS_REMOVED);
//    tab.fca.lattice.addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {
//
//      @Override
//      public void handle(RelationEvent<Concept<G, M>, Concept<G, M>> event) {
//        System.out.println("set");
////        synchronized (tab.fca.layout.generators) {
////          for (Pair<Concept<G, M>, Concept<G, M>> x : event.getSetRows()) {
////            l.remove(x.first());
////            l.add(x.second());
////          }
////        }
//      }
//    }, RelationEvent.ROWS_SET);
    tab.fca.lattice.addEventHandler(new RelationEventHandler<Concept<G, M>, Concept<G, M>>() {

      @Override
      public final void handle(final RelationEvent<Concept<G, M>, Concept<G, M>> event) {
        l.clear();
//        Platform.runLater(new Runnable() {
//
//          @Override
//          public final void run() {
            l.addAll(tab.fca.lattice.colHeads());
//          }
//        });
      }
    }, RelationEvent.ALL_CHANGED); //
//    final ListView<Concept<G, M>> clist =
//        ListViewBuilder
//            .<Concept<G, M>> create()
//            .cellFactory(new Callback<ListView<Concept<G, M>>, ListCell<Concept<G, M>>>() {
//
//              @Override
//              public ListCell<Concept<G, M>> call(ListView<Concept<G, M>> list) {
//                final ListCell<Concept<G, M>> cell = new ListCell<Concept<G, M>>() {
//
//                  @Override
//                  protected void updateItem(Concept<G, M> impl, boolean empty) {
//                    super.updateItem(impl, empty);
//                    if (impl != null) {
//                      setText(impl.getExtent().size() + ": " + impl.toString());
//                      setUserData(impl);
//                    }
//                  }
//                };
//                cell.setOnMouseEntered(new EventHandler<MouseEvent>() {
//
//                  @Override
//                  public void handle(MouseEvent event) {
//                    final Object userData = cell.getUserData();
//                    if (userData != null)
//                      tab.conceptGraph.highlight(
//                          true,
//                          tab.conceptGraph.highlightRequests.concept((Concept<G, M>) userData));
//                  }
//                });
//                return cell;
//              }
//            })
//            .items(l)
//            .build();
    final TableView<Concept<G, M>> ctable =
        TableViewBuilder
            .<Concept<G, M>> create()
            .columns(
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
            .items(l)
            .build();
    this.setCenter(TabPaneBuilder
        .create()
        .side(Side.LEFT)
        .tabs(
            TabBuilder.create().text("C").content(ctable).closable(false).build(),
            TabBuilder.create().text("I").content(list).closable(false).build())
        .build());
    final Button computeButton = ButtonBuilder.create().text("Compute").onAction(new EventHandler<ActionEvent>() {

      @Override
      public final void handle(final ActionEvent event) {
        tab.fca.calcImplications();
      }
    }).build();
    final Button exploreButton = ButtonBuilder.create().text("Explore").onAction(new EventHandler<ActionEvent>() {

      @SuppressWarnings("unchecked")
      @Override
      public final void handle(final ActionEvent event) {
        new AttributeExploration((CFXTab<String, String>) tab);
      }
    }).disable(!tab.isStringTab()).build();
    this.toolBar = ToolBarBuilder.create().items(computeButton, exploreButton).build();
    this.setTop(toolBar);
    // this.list.getFocusModel().focusedItemProperty().addListener(new
    // ChangeListener<Implication<M>>(){
    // @Override
    // public final void changed(final ObservableValue<? extends
    // Implication<M>> value,
    // final Implication<M> wasFocused, final Implication<M> isFocused) {
    // tab.conceptGraph.highlight(true,
    // tab.conceptGraph.highlightRequests.implication(isFocused));
    // }
    // });
  }
}
