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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import conexp.fx.core.util.FileFormat;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.ConExpFX.DatasetTreeView;

public abstract class Dataset {

  public File                       file;
  public FileFormat                 format;
  public final StringProperty       id                 = new SimpleStringProperty("");
  public final BooleanProperty      unsavedChanges     = new SimpleBooleanProperty(false);
  public final List<DatasetView<?>> views              = new ArrayList<DatasetView<?>>();
  public final Set<String>          defaultActiveViews = new HashSet<String>();
  public final List<DatasetAction>  actions            = new ArrayList<DatasetAction>();
  public final Dataset              parent;
  public DatasetTreeItem            treeItem;

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

  public final DatasetTreeItem getTreeItem() {
    return treeItem;
  }

  public final void addToTree(final DatasetTreeView treeView) {
    this.treeItem = new DatasetTreeItem();
    treeView.getParentItem(Dataset.this).getChildren().add(treeItem);
    treeView.getParentItem(Dataset.this).setExpanded(true);
    treeItem.setExpanded(true);
    views.forEach(view -> {
      if (defaultActiveViews.contains(view.getId()))
        treeView.getSelectionModel().select(view.getTreeItem());
    });
  }

  public abstract void save();

  public abstract void saveAs();

  public abstract void export();

  public abstract void close();

  public final class DatasetTreeItem extends TreeItem<Control> {

    public DatasetTreeItem() {
      super();
      final Label label = new Label(id.get());
      label.textProperty().bind(
          Bindings.createStringBinding(() -> id.get() + (unsavedChanges.get() ? "*" : ""), id, unsavedChanges));
      label.setStyle("-fx-font-weight: bold;");
      this.setValue(label);
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

}
