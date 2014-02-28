package conexp.fx.gui.exploration;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ListViewBuilder;
import javafx.scene.control.ToolBar;
import javafx.scene.control.ToolBarBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
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
                      setText(impl.toString());
                      setUserData(impl);
                    }
                  }
                };
                cell.setOnMouseEntered(new EventHandler<MouseEvent>() {

                  @Override
                  public void handle(MouseEvent event) {
                    tab.conceptGraph.highlight(
                        true,
                        tab.conceptGraph.highlightRequests.implication((Implication<M>) cell.getUserData()));
                  }
                });
                return cell;
              }
            })
            .items(tab.fca.implications)
            .build();
    this.setCenter(list);
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
//    this.list.getFocusModel().focusedItemProperty().addListener(new ChangeListener<Implication<M>>(){
//    	@Override
//    	public final void changed(final ObservableValue<? extends Implication<M>> value,
//    			final Implication<M> wasFocused, final Implication<M> isFocused) {
//    		tab.conceptGraph.highlight(true, tab.conceptGraph.highlightRequests.implication(isFocused));
//    	}
//    });
  }
}
