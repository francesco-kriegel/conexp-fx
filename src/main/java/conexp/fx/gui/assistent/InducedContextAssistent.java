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

import java.util.Arrays;

//import org.semanticweb.owlapi.model.IRI;
//import org.semanticweb.owlapi.model.OWLClassExpression;

import conexp.fx.core.builder.Request;
import conexp.fx.core.builder.Requests.Source;
import conexp.fx.core.builder.Requests.Type;
import conexp.fx.core.context.Context;
import conexp.fx.core.context.MatrixContext;
//import conexp.fx.core.dl.deprecated.Constructor;
import conexp.fx.gui.ConExpFX;
//import conexp.fx.gui.assistent.InducedContextAssistent.Result;
import conexp.fx.gui.dataset.DLDataset;
import conexp.fx.gui.dataset.FCADataset;
import conexp.fx.gui.task.TimeTask;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jfxtras.scene.control.ListSpinner;

@Deprecated
public class InducedContextAssistent {}
//public class InducedContextAssistent extends Assistent<Result> {
//
//  public static class Result {
//
//    public Integer       selectedRoleDepth;
//    public Integer       selectedMaxCardinality;
//    public Constructor[] selectedConstructors;
//  }
//
//  private final DLDataset             dataset;
//  private final ListSpinner<Integer>  roleDepthSpinner      = new ListSpinner<Integer>(0, 3);
//  private final ListSpinner<Integer>  maxCardinalitySpinner = new ListSpinner<Integer>(1, 10);
//  private final ListView<Constructor> constructorListView   =
//      new ListView<Constructor>(FXCollections.observableList(Arrays.asList(Constructor.values())));
//
//  public InducedContextAssistent(DLDataset dataset) {
//    super(
//        ConExpFX.instance.primaryStage,
//        "Induced Context Wizard",
//        "Description Logic",
//        "Select Description Logic Constructors",
//        null,
//        r -> null);
//    this.dataset = dataset;
//    initialize();
//    this.resultProperty.set(new Result());
////      this.resultProperty.bind(new ObjectBinding<Result>() {
////
////        {
////          super.bind(
////              roleDepthSpinner.valueProperty(),
////              maxCardinalitySpinner.valueProperty(),
////              constructorListView.getSelectionModel().getSelectedItems());
////        }
////
////        @Override
////        protected Result computeValue() {
////          final Result result = new Result();
////          result.selectedConceptNames = conceptListView.getSelectionModel().getSelectedItems();
////          result.selectedRoleNames = roleListView.getSelectionModel().getSelectedItems();
////          result.selectedIsARoleName = isaRoleChoiceBox.getSelectionModel().getSelectedItem();
////          result.selectedRoleDepth = roleDepthSpinner.getValue();
////          result.selectedMaxCardinality = maxCardinalitySpinner.getValue();
////          result.selectedConstructors = constructorListView.getSelectionModel().getSelectedItems().toArray(
////              new Constructor[] {});
////          return result;
////        }
////      });
//  }
//
//  @Override
//  protected Node createInitialNode() {
//    final BorderPane pane = new BorderPane();
//    pane.setPadding(new Insets(4));
//    roleDepthSpinner.setValue(1);
//    maxCardinalitySpinner.setValue(3);
//    final Label roleDepthLabel = new Label("Role Depth");
//    roleDepthLabel.setPadding(new Insets(4));
//    final Label maxCardinalityLabel = new Label("Maximal Cardinality in Number Restrictions");
//    maxCardinalityLabel.setPadding(new Insets(4));
//    final Label constructorLabel = new Label("Constructors");
//    constructorLabel.setPadding(new Insets(4, 4, 1, 4));
//    roleDepthLabel.setMinWidth(100);
//    maxCardinalityLabel.setMinWidth(100);
//    roleDepthLabel.minWidthProperty().bind(maxCardinalityLabel.widthProperty());
//    final HBox rbox = new HBox(roleDepthLabel, roleDepthSpinner);
//    final HBox cbox = new HBox(maxCardinalityLabel, maxCardinalitySpinner);
//    final VBox vbox = new VBox(rbox, cbox, constructorLabel, constructorListView);
//    pane.setCenter(vbox);
//    constructorListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//    constructorListView.getSelectionModel().select(Constructor.CONJUNCTION);
//    constructorListView.getSelectionModel().select(Constructor.EXISTENTIAL_RESTRICTION);
//    return pane;
//  }
//
//  @Override
//  protected void createPages() {}
//
//  @Override
//  protected void onNext() {
//    this.resultProperty.get().selectedRoleDepth = roleDepthSpinner.getValue();
//    this.resultProperty.get().selectedMaxCardinality = maxCardinalitySpinner.getValue();
//    this.resultProperty.get().selectedConstructors =
//        constructorListView.getSelectionModel().getSelectedItems().toArray(new Constructor[] {});
//    ConExpFX.instance.executor.execute(new TimeTask<Void>("Creating new Induced Context") {
//
//      @Override
//      protected Void call() {
//        updateProgress(0d, 1d);
//        if (isCancelled())
//          return null;
//        final Context<IRI, OWLClassExpression> inducedContext = dataset.interpretation.getInducedContext(
//            dataset.interpretation.getDomain(),
//            resultProperty.get().selectedRoleDepth,
//            resultProperty.get().selectedMaxCardinality,
//            resultProperty.get().selectedConstructors);
//        ConExpFX.instance.treeView.addDataset(
//            new FCADataset<IRI, OWLClassExpression>(
//                dataset,
//                new Request<IRI, OWLClassExpression>(Type.INDUCED_CONTEXT, Source.NULL) {
//
//                  @Override
//                  public MatrixContext<IRI, OWLClassExpression> createContext() {
//                    MatrixContext<IRI, OWLClassExpression> cxt = new MatrixContext<IRI, OWLClassExpression>(false);
//                    return cxt;
//                  }
//
//                  @Override
//                  public void setContent() {
//                    this.context.rowHeads().addAll(inducedContext.rowHeads());
//                    this.context.colHeads().addAll(inducedContext.colHeads());
//                    context.addAll(inducedContext);
//                  }
//                }));
//        updateProgress(1d, 1d);
//        return null;
//      }
//    });
//  }
//
//}
