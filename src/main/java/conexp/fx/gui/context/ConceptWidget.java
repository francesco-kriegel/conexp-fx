package conexp.fx.gui.context;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2019 Francesco Kriegel
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

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;

import conexp.fx.core.collections.Collections3;
import conexp.fx.core.context.Concept;
import conexp.fx.core.math.GuavaIsomorphism;
import conexp.fx.core.util.OWLUtil;
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
                    return OWLUtil.toString(
                        OWLManager.getOWLDataFactory().getOWLObjectIntersectionOf(
                            Collections3.transform(
                                p.getValue().getIntent(),
                                GuavaIsomorphism.create(x -> (OWLClassExpression) x, null))));
                  final String s = p.getValue().getIntent().toString();
                  return s.substring(1, s.length() - 1);
                }))
                .build())
        .items(dataset.concepts)
        .build();
    this.setCenter(table);
  }
}
