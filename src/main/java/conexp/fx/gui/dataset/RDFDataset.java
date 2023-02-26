package conexp.fx.gui.dataset;

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

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
//import org.semanticweb.owlapi.model.IRI;

import conexp.fx.core.builder.Requests;
//import conexp.fx.core.dl.deprecated.OWLInterpretation;
import conexp.fx.core.importer.RDFImporter;
import conexp.fx.core.util.FileFormat;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.assistent.ModelAssistent;
import conexp.fx.gui.task.TimeTask;
import info.aduna.iteration.Iterations;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

@Deprecated
public class RDFDataset extends Dataset {

  private final Repository                repository = new SailRepository(new MemoryStore());
  private final ObservableList<Statement> statements = FXCollections.observableArrayList();

  public RDFDataset(final File file, final FileFormat format) {
    super(null, file, format);
    try {
      repository.initialize();
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
    views.add(new DatasetView<Repository>("Triples", createTriplesView(), repository));
    views.add(new DatasetView<Repository>("Query", createQueryView(), repository));
    defaultActiveViews.add("Triples");
    actions.add(new DatasetAction("New SPARQL Context...", () -> {
      return;
    }));
//    actions.add(new DatasetAction("New DL Model...", () -> new ModelAssistent(this).showAndWait()));
    initialize();
  }

  public final void initialize() {
    if (format.equals(FileFormat.CSVT))
      ConExpFX.execute(TimeTask.create(this, "Importing RDF Dataset", () -> RDFImporter.readCSV(repository, file)));
    else
      ConExpFX.execute(TimeTask.create(this, "Importing RDF Dataset", () -> RDFImporter.read(repository, file)));
    ConExpFX.execute(TimeTask.create(this, "Preparing Statements View", () -> {
      try {
        RepositoryConnection connection = repository.getConnection();
        Iterations.addAll(connection.getStatements(null, null, null, true), statements);
        connection.close();

      } catch (RepositoryException e) {
        throw new RuntimeException(e);
      }
    }));
  }

  private final TableView<Statement> createTriplesView() {
    TableView<Statement> table = new TableView<>(statements);
    final TableColumn<Statement, String> subjectColumn = new TableColumn<Statement, String>("Subject");
    final TableColumn<Statement, String> predicateColumn = new TableColumn<Statement, String>("Predicate");
    final TableColumn<Statement, String> objectColumn = new TableColumn<Statement, String>("Object");
    subjectColumn.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().getSubject().stringValue()));
    predicateColumn.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().getPredicate().stringValue()));
    objectColumn.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().getObject().stringValue()));
    table.getColumns().addAll(subjectColumn, predicateColumn, objectColumn);
    return table;
  }

  private final Pane createQueryView() {
    final BorderPane repositoryView = new BorderPane();
    final TextArea queryArea = new TextArea();
    queryArea.setFont(Font.font(java.awt.Font.MONOSPACED, 14));
    final Button queryButton = new Button("Query");
    repositoryView.setTop(new BorderPane(queryArea, null, queryButton, null, null));
    final TableView<BindingSet> table = new TableView<BindingSet>(FXCollections.observableArrayList());
    repositoryView.setCenter(table);
    queryButton.setOnAction(__ -> {
      try {
        final RepositoryConnection connection = repository.getConnection();
        final TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryArea.getText());
        final TupleQueryResult result = query.evaluate();
        table.getColumns().clear();
        result.getBindingNames().forEach(b -> {
          final TableColumn<BindingSet, String> column = new TableColumn<BindingSet, String>(b);
          column.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().getValue(b).stringValue()));
          table.getColumns().add(column);
        });
        table.getItems().clear();
        while (result.hasNext())
          table.getItems().add(result.next());
        result.close();
        connection.close();
      } catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
        throw new RuntimeException(e);
      }
    });
    return repositoryView;
  }

  public final ObservableList<Statement> getTriples() {
    return statements;
  }

//  public final Set<IRI> getRoles() {
//    return statements.parallelStream().map(s -> IRI.create(s.getPredicate().stringValue())).collect(Collectors.toSet());
//  }

  public final void createFormalContextFromSPARQLQuery(final String query) {
    ConExpFX.execute(TimeTask.create(this, "Extracting SPARQL Context", () -> {
      ConExpFX.instance.treeView.addDataset(
          new FCADataset<>(RDFDataset.this, new Requests.Import.ImportSPARQLFromRepository(repository, query)));
    }));
  }

//  public final void
//      createDLModel(List<IRI> selectedConceptNames, List<IRI> selectedRoleNames, IRI selectedIsARoleName) {
////    ConExpFX.execute(TimeTask.create(this, "Extracting DL Model", () -> {
////      final OWLInterpretation i =
////          RDFImporter.extractInterpretation(statements, selectedConceptNames, selectedRoleNames, selectedIsARoleName);
////      ConExpFX.instance.treeView.addDataset(new DLDataset(RDFDataset.this, i));
////    }));
//  }

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
