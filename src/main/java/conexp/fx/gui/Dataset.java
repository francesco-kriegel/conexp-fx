package conexp.fx.gui;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import conexp.fx.core.util.FileFormat;

public class Dataset {

  public class DatasetView {

  }

  public File                    file;
  public FileFormat              format;
  public final StringProperty    id             = new SimpleStringProperty("");
  public final BooleanProperty   unsavedChanges = new SimpleBooleanProperty(false);
  public final List<DatasetView> views          = new LinkedList<DatasetView>();

  public Dataset(final File file, final FileFormat format) {
    super();
    this.file = file;
    this.format = format;
    this.id.set(file.getName());
  }

}
