package conexp.fx.gui.concept;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;

import conexp.fx.core.collections.Collections3;
import conexp.fx.core.context.Concept;
import conexp.fx.core.math.Isomorphism;
import conexp.fx.core.util.OWLtoString;
import conexp.fx.gui.dataset.FCADataset;
import javafx.beans.binding.Bindings;
import javafx.scene.control.TableColumnBuilder;
import javafx.scene.control.TableView;
import javafx.scene.control.TableViewBuilder;
import javafx.scene.layout.BorderPane;

public class ConceptWidget<G, M> extends BorderPane {

  private final FCADataset<G, M>         dataset;
  private final TableView<Concept<G, M>> table;

  @SuppressWarnings({ "deprecation", "unchecked" })
  public ConceptWidget(final FCADataset<G, M> dataset) {
    super();
    this.dataset = dataset;
    this.table = TableViewBuilder
        .<Concept<G, M>> create()
        .columns(
            TableColumnBuilder
                .<Concept<G, M>, Integer> create()
                .text("Support")
                .cellValueFactory(p -> Bindings.createObjectBinding(() -> p.getValue().getExtent().size()))
                .build(),
            TableColumnBuilder
                .<Concept<G, M>, Integer> create()
                .text("Support")
                .cellValueFactory(
                    p -> Bindings.createObjectBinding(
                        () -> (int) ((100d * (double) p.getValue().getExtent().size())
                            / ((double) dataset.context.rowHeads().size()))))
                .build(),
            TableColumnBuilder
                .<Concept<G, M>, String> create()
                .text("Extent")
                .cellValueFactory(p -> Bindings.createObjectBinding(() -> {
                  final String s = p.getValue().getExtent().toString();
                  return s.substring(1, s.length() - 1);
                }))
                .build(),
            TableColumnBuilder
                .<Concept<G, M>, Integer> create()
                .text("Attributes")
                .cellValueFactory(p -> Bindings.createObjectBinding(() -> p.getValue().intent().size()))
                .build(),
            TableColumnBuilder
                .<Concept<G, M>, String> create()
                .text("Intent")
                .cellValueFactory(p -> Bindings.createObjectBinding(() -> {
                  if (!p.getValue().getIntent().isEmpty()
                      && p.getValue().getIntent().iterator().next() instanceof OWLClassExpression)
                    return OWLtoString.toString(
                        OWLManager.getOWLDataFactory().getOWLObjectIntersectionOf(
                            Collections3.transform(
                                p.getValue().getIntent(),
                                Isomorphism.create(x -> (OWLClassExpression) x, null))));
                  final String s = p.getValue().getIntent().toString();
                  return s.substring(1, s.length() - 1);
                }))
                .build())
        .items(dataset.concepts)
        .build();
    this.setCenter(table);
  }
}
