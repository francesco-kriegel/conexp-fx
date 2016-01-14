package conexp.fx.gui.dataset;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import javafx.geometry.Insets;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TreeItem;

public class DatasetAction {

  private final String                id;
  private final Runnable              action;
  private final DatasetActionTreeItem treeItem;

  public DatasetAction(final String id, final Runnable action) {
    super();
    this.id = id;
    this.action = action;
    this.treeItem = new DatasetActionTreeItem();
  }

  public String getId() {
    return id;
  }

  public void run() {
    action.run();
  }

  public DatasetActionTreeItem getTreeItem() {
    return treeItem;
  }

  public final class DatasetActionTreeItem extends TreeItem<Control> {

    public DatasetActionTreeItem() {
      super();
      final Hyperlink hyperlink = new Hyperlink(id);
      hyperlink.setPadding(new Insets(0));
      hyperlink.setOnAction(e -> run());
      setValue(hyperlink);
    }

    public final DatasetAction getDatasetAction() {
      return DatasetAction.this;
    }
  }

}
