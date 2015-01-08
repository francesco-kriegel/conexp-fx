package conexp.fx.gui;

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
