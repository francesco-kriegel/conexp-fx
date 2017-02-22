package conexp.fx.gui.dataset;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;

public class DatasetView<T> {

  private final String              id;
  private final Node                contentNode;
  private final T                   data;
  private final DatasetViewTreeItem treeItem;

  public DatasetView(final String id, final Node contentNode, final T data) {
    super();
    this.id = id;
    this.contentNode = contentNode;
    this.data = data;
    this.treeItem = new DatasetViewTreeItem();
  }

  public String getId() {
    return id;
  }

  public Node getContentNode() {
    return contentNode;
  }

  public T getData() {
    return data;
  }

  public DatasetViewTreeItem getTreeItem() {
    return treeItem;
  }

  public final class DatasetViewTreeItem extends TreeItem<Control> {

    public DatasetViewTreeItem() {
      super(new Label(id));
    }

    public final DatasetView<T> getDatasetView() {
      return DatasetView.this;
    }
  }

}
