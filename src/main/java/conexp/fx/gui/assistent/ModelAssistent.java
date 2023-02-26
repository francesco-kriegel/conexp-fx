package conexp.fx.gui.assistent;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2023 Francesco Kriegel
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.Collections2;

import conexp.fx.gui.ConExpFX;
//import conexp.fx.gui.assistent.ModelAssistent.Result;
import conexp.fx.gui.dataset.RDFDataset;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@Deprecated
public class ModelAssistent {}
//public class ModelAssistent extends Assistent<Result> {
//
//  public static class Result {
//
//    public List<IRI> selectedConceptNames;
//    public List<IRI> selectedRoleNames;
//    public IRI       selectedIsARoleName;
//  }
//
//  private final RDFDataset dataset;
//  final ListView<IRI>      conceptListView  = new ListView<IRI>();
//  final ListView<IRI>      roleListView     = new ListView<IRI>();
//  final ChoiceBox<IRI>     isaRoleChoiceBox = new ChoiceBox<IRI>();
//
//  public ModelAssistent(final RDFDataset dataset) {
//    super(
//        ConExpFX.instance.primaryStage,
//        "Model Wizard",
//        "Select Model Signature",
//        "Extract a Model from a Dataset of RDF Triples (" + dataset.file.getName() + ")",
//        null,
//        r -> null);
//    this.dataset = dataset;
//    initialize();
//    this.resultProperty.set(new Result());
//    Platform.runLater(() -> {
//      final Optional<IRI> typeRole =
//          dataset.getRoles().parallelStream().filter(role -> role.toString().contains("type")).findAny();
//      if (typeRole.isPresent())
//        isaRoleChoiceBox.getSelectionModel().select(typeRole.get());
//      else
//        isaRoleChoiceBox.getSelectionModel().selectFirst();
//    });
//  }
//
//  public void showAndWait() {
//    stage.showAndWait();
//  }
//
//  @Override
//  protected void onNext() {
//    resultProperty.get().selectedConceptNames = conceptListView.getSelectionModel().getSelectedItems();
//    resultProperty.get().selectedRoleNames = roleListView.getSelectionModel().getSelectedItems();
//    resultProperty.get().selectedIsARoleName = isaRoleChoiceBox.getSelectionModel().getSelectedItem();
//    dataset.createDLModel(
//        conceptListView.getSelectionModel().getSelectedItems(),
//        roleListView.getSelectionModel().getSelectedItems(),
//        isaRoleChoiceBox.getSelectionModel().getSelectedItem());
//  }
//
//  @Override
//  protected Node createInitialNode() {
//    final BorderPane pane = new BorderPane();
//    pane.setPadding(new Insets(4));
//    final Label isARoleLabel = new Label("IS-A Role Name");
//    isARoleLabel.setPadding(new Insets(4));
//    final Label conceptLabel = new Label("Concept Names");
//    conceptLabel.setPadding(new Insets(4, 4, 1, 4));
//    final Label roleLabel = new Label("Role Names");
//    roleLabel.setPadding(new Insets(4, 4, 1, 4));
//    conceptListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//    roleListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//    try {
//      isaRoleChoiceBox.setItems(FXCollections.observableArrayList(dataset.getRoles()));
//    } catch (NullPointerException e) {
//      System.out.println();
//    }
//    final HBox rbox = new HBox(isARoleLabel, isaRoleChoiceBox);
//    final VBox vbox = new VBox(rbox, conceptLabel, conceptListView, roleLabel, roleListView);
//    pane.setCenter(vbox);
//    isaRoleChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//      final List<IRI> concepts = dataset
//          .getTriples()
//          .parallelStream()
//          .filter(triple -> IRI.create(triple.getPredicate().stringValue()).equals(newValue))
//          .map(triple -> IRI.create(triple.getObject().stringValue()))
//          .distinct()
//          .sorted()
//          .collect(Collectors.toList());
//      conceptListView.setItems(FXCollections.observableArrayList(concepts));
//      roleListView.setItems(
//          FXCollections.observableArrayList(Collections2.filter(dataset.getRoles(), role -> !role.equals(newValue))));
//      conceptListView.getSelectionModel().selectAll();
//      roleListView.getSelectionModel().selectAll();
//    });
//    return pane;
//  }
//
//  @Override
//  protected void createPages() {}
//
//}
