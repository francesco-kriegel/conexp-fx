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
