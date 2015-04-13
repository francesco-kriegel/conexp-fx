package conexp.fx.gui.dataset;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import org.semanticweb.owlapi.model.IRI;

import conexp.fx.core.dl.OWLInterpretation;
import conexp.fx.core.dl.Signature;
import conexp.fx.core.util.FileFormat;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.assistent.ModelAssistent;
import conexp.fx.gui.task.BlockingTask;

public class RDFDataset extends Dataset {

  private ObservableList<IRI[]> triples;
  private List<IRI>             roles;

  public RDFDataset(final File rdfFile, final FileFormat format) {
    super(null, rdfFile, format);
    this.triples = FXCollections.observableArrayList();
    ConExpFX.instance.exe.submit(new BlockingTask("Importing " + file.getName()) {

      @Override
      protected void _call() {
        this.updateProgress(30, 100);
        try {
          readTriples();
          this.updateProgress(80, 100);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    views.add(new DatasetView<List<IRI[]>>("Triples", createTriplesListView(), triples));
    defaultActiveViews.add("Triples");
    actions.add(new DatasetAction("New Model...", () -> new ModelAssistent(this).showAndWait()));
  }

  private final ListView<IRI[]> createTriplesListView() {
    final ListView<IRI[]> listView = new ListView<IRI[]>();
    listView.setItems(triples);
    listView.setCellFactory(l -> new ListCell<IRI[]>() {

      @Override
      protected void updateItem(final IRI[] value, final boolean empty) {
        if (!empty)
          this.setText(value[0] + " " + value[1] + " " + value[2]);
      }
    });
    return listView;
  }

  public final void readTriples() throws IOException {
//    triples =
    Files
        .lines(FileSystems.getDefault().getPath(file.getAbsolutePath()))
        .map(line -> line.replace("<", "").replace(">", ""))
        .map(line -> line.split(" "))
        .filter(tuple -> tuple.length > 2)
        .map(triple -> new IRI[] { IRI.create(triple[0]), IRI.create(triple[1]), IRI.create(triple[2]) })
        .forEach(triples::add);
//            .collect(Collectors.toList());
    roles = triples.parallelStream().map(triple -> triple[1]).distinct().sorted().collect(Collectors.toList());
  }

  public final List<IRI[]> getTriples() {
    return triples;
  }

  public final List<IRI> getRoles() {
    return roles;
  }

  public final OWLInterpretation extractInterpretation(
      final List<IRI> selectedConceptNames,
      final List<IRI> selectedRoleNames,
      final IRI selectedIsARoleName) {
    final Signature signature = new Signature(null);
    signature.getConceptNames().addAll(selectedConceptNames);
    signature.getRoleNames().addAll(selectedRoleNames);
    signature.getIndividualNames().addAll(
        getTriples()
            .parallelStream()
            .filter(triple -> triple[1].equals(selectedIsARoleName) && signature.getConceptNames().contains(triple[2]))
            .map(triple -> triple[0])
            .collect(Collectors.toSet()));
//          signature.getIndividualNames().addAll(
//              triples.parallelStream().filter(
//                  triple -> signature.getRoleNames().contains(
//                      triple[1])).map(
//                  triple -> triple[0]).collect(
//                  Collectors.toSet()));
//          signature.getIndividualNames().addAll(
//              triples.parallelStream().filter(
//                  triple -> signature.getRoleNames().contains(
//                      triple[1])).map(
//                  triple -> triple[2]).collect(
//                  Collectors.toSet()));
    final OWLInterpretation i = new OWLInterpretation(signature);
    getTriples().stream().forEach(
        triple -> {
          if (triple[1].equals(selectedIsARoleName)) {
            if (signature.getConceptNames().contains(triple[2]) && signature.getIndividualNames().contains(triple[0])) {
              i.addConceptNameAssertion(triple[2], triple[0]);
            }
          } else if (signature.getRoleNames().contains(triple[1]) && signature.getIndividualNames().contains(triple[0])
              && signature.getIndividualNames().contains(triple[2])) {
            i.addRoleNameAssertion(triple[1], triple[0], triple[2]);
          }
        });
    return i;
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
