package conexp.fx.gui.dataset;

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import conexp.fx.core.util.FileFormat;
import conexp.fx.gui.ConExpFX;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public abstract class Dataset {

  public final class DatasetTreeItem extends TreeItem<Control> {

    public DatasetTreeItem(ConExpFX.DatasetTreeView treeView) {
      super();
      final Label label = new Label(id.get());
      label.textProperty().bind(
          Bindings.createStringBinding(() -> id.get() + (unsavedChanges.get() ? "*" : ""), id, unsavedChanges));
      label.setStyle("-fx-font-weight: bold;");
      final ProgressIndicator progressIndicator = new ProgressIndicator();
      progressIndicator.setMinSize(12, 12);
      progressIndicator.setMaxSize(12, 12);
      progressIndicator.setPadding(new Insets(0d));
      progressIndicator.progressProperty().bind(
          Bindings.createDoubleBinding(
              () -> ConExpFX.instance.executor.datasetProgressBinding(Dataset.this).get() == 1d ? 1d : -1d,
              ConExpFX.instance.executor.datasetProgressBinding(Dataset.this)));
      progressIndicator.visibleProperty().bind(progressIndicator.progressProperty().lessThan(1d));
      this.setValue(label);
      final Hyperlink closeLink = new Hyperlink("x");
      closeLink.setOnAction(e -> treeView.close(Dataset.this));
      closeLink.setPadding(new Insets(0));
      final HBox hBox = new HBox(closeLink, progressIndicator);
      hBox.setSpacing(4d);
      hBox.setAlignment(Pos.CENTER);
      label.setGraphic(hBox);
      label.setContentDisplay(ContentDisplay.RIGHT);
      this.setGraphic(new ImageView(new Image(ConExpFX.class.getResourceAsStream("image/context.gif"))));
      views.forEach(view -> getChildren().add(view.getTreeItem()));
      actions.forEach(action -> {
        this.getChildren().add(action.getTreeItem());
      });
    }

    public final Dataset getDataset() {
      return Dataset.this;
    }

  }

  public File                       file;
  public FileFormat                 format;
  public final StringProperty       id                 = new SimpleStringProperty("");
  public final BooleanProperty      unsavedChanges     = new SimpleBooleanProperty(false);
  public final List<DatasetView<?>> views              = new ArrayList<DatasetView<?>>();
  public final Set<String>          defaultActiveViews = new HashSet<String>();
  public final List<DatasetAction>  actions            = new ArrayList<DatasetAction>();
  public final Dataset              parent;
  public Dataset.DatasetTreeItem    treeItem;

  protected Dataset(final Dataset parent) {
    super();
    this.parent = parent;
  }

  public Dataset(final Dataset parent, final File file, final FileFormat format) {
    this(parent);
    this.file = file;
    this.format = format;
    this.id.set(file.getName());
  }

  public final Dataset.DatasetTreeItem getTreeItem() {
    return treeItem;
  }

  public final void addToTree(final ConExpFX.DatasetTreeView treeView) {
    this.treeItem = new Dataset.DatasetTreeItem(treeView);
    treeView.getParentItem(Dataset.this).getChildren().add(treeItem);
    treeView.getParentItem(Dataset.this).setExpanded(true);
    treeItem.setExpanded(true);
//    views.forEach(view -> {
//      if (defaultActiveViews.contains(view.getId()))
//        treeView.getSelectionModel().select(view.getTreeItem());
//    });
  }

  public abstract void save();

  public abstract void saveAs();

  public abstract void export();

  public abstract void close();

}
