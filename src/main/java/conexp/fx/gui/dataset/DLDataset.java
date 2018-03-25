package conexp.fx.gui.dataset;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
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

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import conexp.fx.core.collections.Pair;
import conexp.fx.core.dl.OWLInterpretation;
import conexp.fx.core.util.IdGenerator;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.assistent.InducedContextAssistent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class DLDataset extends Dataset {

  public class DomainWidget {

    private final BorderPane contentPane;

    public DomainWidget() {
      super();
      this.contentPane = new BorderPane();
      final ListView<IRI> listView = new ListView<IRI>();
      listView.getItems().addAll(interpretation.getDomain());
      contentPane.setCenter(listView);
    }

  }

  public class ConceptWidget {

    private final BorderPane contentPane;

    public ConceptWidget() {
      super();
      this.contentPane = new BorderPane();
      final Label conceptLabel = new Label("Concept Name");
      final ChoiceBox<IRI> conceptChoiceBox = new ChoiceBox<IRI>();
      conceptChoiceBox.getItems().addAll(interpretation.getSignature().getConceptNames());
      final ListView<IRI> listView = new ListView<IRI>();
      conceptChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        listView.getItems().clear();
        listView.getItems().addAll(interpretation.getConceptNameExtension(newValue));
      });
      contentPane.setTop(new HBox(conceptLabel, conceptChoiceBox));
      contentPane.setCenter(listView);
      conceptChoiceBox.getSelectionModel().selectFirst();
    }
  }

  public class RoleWidget {

    private final BorderPane contentPane;

    public RoleWidget() {
      super();
      this.contentPane = new BorderPane();
      final Label roleLabel = new Label("Role Name");
      final ChoiceBox<IRI> roleChoiceBox = new ChoiceBox<IRI>();
      roleChoiceBox.getItems().addAll(interpretation.getSignature().getRoleNames());
      final ListView<Pair<IRI, IRI>> listView = new ListView<Pair<IRI, IRI>>();
      roleChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        listView.getItems().clear();
        listView.getItems().addAll(interpretation.getRoleNameExtension(newValue));
      });
      contentPane.setTop(new HBox(roleLabel, roleChoiceBox));
      contentPane.setCenter(listView);
      roleChoiceBox.getSelectionModel().selectFirst();
    }
  }

  public final OWLInterpretation interpretation;

  public DLDataset(final Dataset parent, final OWLInterpretation interpretation) {
    super(parent);
    this.interpretation = interpretation;
    this.id.set("Model " + IdGenerator.getNextId(ConExpFX.instance));
    this.views
        .add(new DatasetView<Set<IRI>>("Individuals", new DomainWidget().contentPane, interpretation.getDomain()));
    this.views.add(
        new DatasetView<Set<IRI>>(
            "Concept Names",
            new ConceptWidget().contentPane,
            interpretation.getSignature().getConceptNames()));
    this.views.add(
        new DatasetView<Set<IRI>>(
            "Role Names",
            new RoleWidget().contentPane,
            interpretation.getSignature().getRoleNames()));
    this.defaultActiveViews.add("Individuals");
    this.actions
        .add(new DatasetAction("New Induced Context...", () -> new InducedContextAssistent(this).showAndWait()));
  }

  @Override
  public void save() {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveAs() {
    // TODO Auto-generated method stub

  }

  @Override
  public void export() {
    // TODO Auto-generated method stub

  }

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

}
