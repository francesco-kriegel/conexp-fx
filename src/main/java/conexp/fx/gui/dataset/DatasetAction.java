package conexp.fx.gui.dataset;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2020 Francesco Kriegel
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
