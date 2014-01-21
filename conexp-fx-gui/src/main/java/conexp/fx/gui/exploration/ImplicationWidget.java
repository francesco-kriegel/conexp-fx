package conexp.fx.gui.exploration;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ListView;
import javafx.scene.control.ListViewBuilder;
import javafx.scene.control.ToolBar;
import javafx.scene.control.ToolBarBuilder;
import javafx.scene.layout.BorderPane;
import conexp.fx.core.context.Implication;
import conexp.fx.gui.tab.CFXTab;

public class ImplicationWidget<G, M> extends BorderPane {

  private final CFXTab<G, M>             tab;
  private final ListView<Implication<M>> list;
  private final ToolBar                  toolBar;

  public ImplicationWidget(final CFXTab<G, M> tab) {
    super();
    this.tab = tab;
    this.list = ListViewBuilder.<Implication<M>> create().items(tab.fca.implications).build();
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
  }
}
