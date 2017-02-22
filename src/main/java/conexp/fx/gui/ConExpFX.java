/**
 * @author Francesco Kriegel (francesco.kriegel@gmx.de)
 */
package conexp.fx.gui;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import conexp.fx.core.builder.Requests;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.Pair;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.util.FileFormat;
import conexp.fx.core.xml.StringData;
import conexp.fx.core.xml.StringListData;
import conexp.fx.core.xml.XMLFile;
import conexp.fx.gui.assistent.ConstructAssistent;
import conexp.fx.gui.assistent.ExportAssistent;
import conexp.fx.gui.dataset.Dataset;
import conexp.fx.gui.dataset.Dataset.DatasetTreeItem;
import conexp.fx.gui.dataset.DatasetView;
import conexp.fx.gui.dataset.FCADataset;
import conexp.fx.gui.dataset.RDFDataset;
import conexp.fx.gui.dialog.ErrorDialog;
import conexp.fx.gui.dialog.FXDialog;
import conexp.fx.gui.dialog.FXDialog.Answer;
import conexp.fx.gui.dialog.FXDialog.Style;
import conexp.fx.gui.dialog.InfoDialog;
import conexp.fx.gui.task.BlockingExecutor;
import conexp.fx.gui.task.ExecutorStatusBar;
import conexp.fx.gui.task.TimeTask;
import conexp.fx.gui.util.AppUserModelIdUtility;
import conexp.fx.gui.util.FXControls;
import conexp.fx.gui.util.Platform2;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ConExpFX extends Application {

  public static ConExpFX instance;

  public final static void main(String[] args) {
    System.setProperty("file.encoding", "UTF-8");
    if (System.getProperty("os.name").toLowerCase().startsWith("windows"))
      AppUserModelIdUtility.setCurrentProcessExplicitAppUserModelID("conexp-fx");
//    AquaFx.style();
    launch(args);
  }

  public static final void execute(final TimeTask<?> task) {
    instance.executor.execute(task);
  }

  public static final ExecutorService getThreadPool() {
    return instance.executor.tpe;
  }

  private final class CFXMenuBar {

    private final MenuBar menuBar     = new MenuBar();
    private final Menu    contextMenu = new Menu("_Context");
    private final Menu    viewMenu    = new Menu("_View");
    private final Menu    helpMenu    = new Menu("?");

    private CFXMenuBar() {
      super();
      HBox.setHgrow(menuBar, Priority.ALWAYS);
      buildContextMenu();
      buildViewMenu();
      buildHelpMenu();
      menuBar.getMenus().addAll(contextMenu, viewMenu, helpMenu);
      menuBar.setUseSystemMenuBar(true);
      rootPane.setTop(menuBar);
    }

    private final void buildViewMenu() {
      viewMenu.getItems().add(
          FXControls.newMenuItem(
              "Fullscreen",
              "image/16x16/new_page.png",
              e -> primaryStage.setFullScreen(!primaryStage.isFullScreen())));
    }

    private final void buildContextMenu() {
      final MenuItem newMenuItem =
          FXControls.newMenuItem("New", "image/16x16/new_page.png", e -> new ConstructAssistent().showAndWait());
      final MenuItem openMenuItem = FXControls.newMenuItem("Open", "image/16x16/folder.png", e -> showOpenFileDialog());
      final MenuItem saveMenuItem =
          FXControls.newMenuItem("Save", "image/16x16/save.png", true, e -> treeView.getActiveDataset().get().save());
      final MenuItem saveAsMenuItem = FXControls
          .newMenuItem("Save As", "image/16x16/save.png", true, e -> treeView.getActiveDataset().get().saveAs());
      final MenuItem exportMenuItem = FXControls.newMenuItem("Export", "image/16x16/briefcase.png", true, e -> {
        if (treeView.getActiveDataset().get() instanceof FCADataset)
          new ExportAssistent(primaryStage, (FCADataset<?, ?>) treeView.getActiveDataset().get()).showAndWait();
      });
      final MenuItem closeMenuItem =
          FXControls.newMenuItem("Close", "image/16x16/delete.png", true, e -> treeView.closeActiveDataset());
      final Menu historyMenu = new Menu("History", FXControls.newImageView("image/16x16/clock.png"));
      final MenuItem exitMenuItem = FXControls.newMenuItem("Exit", "image/16x16/delete.png", e -> stop());
      treeView.getActiveDataset().addListener(new ChangeListener<Dataset>() {

        public final void changed(
            final ObservableValue<? extends Dataset> observable,
            final Dataset oldSelectedTab,
            final Dataset newSelectedTab) {
          Platform.runLater(() -> {
            saveMenuItem.disableProperty().unbind();
            if (newSelectedTab == null) {
              saveMenuItem.setDisable(true);
              saveAsMenuItem.setDisable(true);
              exportMenuItem.setDisable(true);
              closeMenuItem.setDisable(true);
            } else {
              saveMenuItem.disableProperty().bind(
                  Bindings
                      .createBooleanBinding(() -> !newSelectedTab.unsavedChanges.get(), newSelectedTab.unsavedChanges));
              saveAsMenuItem.setDisable(false);
              exportMenuItem.setDisable(false);
              closeMenuItem.setDisable(false);
            }
          });
        }
      });
      historyMenu.disableProperty().bind(fileHistory.emptyProperty());
      fileHistory.addListener(new ListChangeListener<File>() {

        @SuppressWarnings("deprecation")
        public final void onChanged(final ListChangeListener.Change<? extends File> c) {
          historyMenu.getItems().clear();
          historyMenu.getItems().addAll(
              Collections2.transform(
                  fileHistory,
                  file -> MenuItemBuilder.create().text(file.toString()).onAction(e -> Platform.runLater(() -> {
                    if (file.exists() && file.isFile())
                      openFile(FileFormat.of(file));
                  })).build()));
        }
      });
      contextMenu.getItems().addAll(
          newMenuItem,
          openMenuItem,
          saveMenuItem,
          saveAsMenuItem,
          exportMenuItem,
          closeMenuItem,
          new SeparatorMenuItem(),
          historyMenu,
          new SeparatorMenuItem(),
          exitMenuItem);

    }

    private final void buildHelpMenu() {
      if (Desktop.isDesktopSupported()) {
        final MenuItem helpMenuItem = FXControls.newMenuItem("Help", "image/16x16/help.png", ev -> {
          try {
            Desktop.getDesktop().browse(new URI("http://lat.inf.tu-dresden.de/~francesco/conexp-fx/conexp-fx.html"));
          } catch (Exception e) {
            new ErrorDialog(primaryStage, e).showAndWait();
          }
        });
        helpMenu.getItems().add(helpMenuItem);
      }
      final MenuItem infoMenuItem =
          FXControls.newMenuItem("Info", "image/16x16/info.png", e -> new InfoDialog(ConExpFX.this).showAndWait());
      helpMenu.getItems().addAll(infoMenuItem);
    }
  }

  public final class DatasetTreeView extends TreeView<Control> {

    private final ObservableList<Dataset> datasets      = FXCollections.observableArrayList();
    public final ObjectProperty<Dataset>  activeDataset = new SimpleObjectProperty<Dataset>(null);
    private final ToolBar                 toolBar       = new ToolBar();

    private DatasetTreeView() {
      super();
      final Button newButton = new Button("New", FXControls.newImageView("image/16x16/new_page.png"));
      newButton.setOnAction(e -> new ConstructAssistent().showAndWait());
      final Button openButton = new Button("Open", FXControls.newImageView("image/16x16/folder.png"));
      openButton.setOnAction(e -> showOpenFileDialog());
      toolBar.getItems().addAll(newButton, openButton);
      this.setRoot(new TreeItem<>());
      this.setShowRoot(false);
      this.selectionModelProperty().get().setSelectionMode(SelectionMode.MULTIPLE);
      activeDataset.bind(Bindings.createObjectBinding(() -> {
//        final Object foo = instance == null ? 0 : instance;
//        synchronized (foo) {
          Dataset active = null;
          final Iterator<TreeItem<Control>> it = getSelectionModel().getSelectedItems().iterator();
          if (it.hasNext()) {
            TreeItem<?> selectedItem = it.next();
            if (selectedItem.isLeaf())
              selectedItem = selectedItem.getParent();
            if (selectedItem instanceof DatasetTreeItem) {
              active = ((DatasetTreeItem) selectedItem).getDataset();
              while (it.hasNext()) {
                selectedItem = it.next();
                if (selectedItem.isLeaf())
                  selectedItem = selectedItem.getParent();
                if (selectedItem instanceof Dataset.DatasetTreeItem) {
                  if (!active.equals(((Dataset.DatasetTreeItem) selectedItem).getDataset())) {
                    active = null;
                    break;
                  }
                }
              }
            }
          }
          return active;
//        }
      }, this.getSelectionModel().getSelectedItems()));
      this.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TreeItem<Control>>() {

        @Override
        public synchronized void onChanged(ListChangeListener.Change<? extends TreeItem<Control>> c) {
//          synchronized (instance) {
            while (c.next()) {
              if (c.wasAdded())
                c
                    .getAddedSubList()
                    .stream()
                    .filter(item -> item instanceof DatasetView<?>.DatasetViewTreeItem)
                    .map(item -> (DatasetView<?>.DatasetViewTreeItem) item)
                    .forEach(item -> contentPane.getItems().add(item.getDatasetView().getContentNode()));
              if (c.wasRemoved())
                c
                    .getRemoved()
                    .stream()
                    .filter(item -> item instanceof DatasetView<?>.DatasetViewTreeItem)
                    .map(item -> (DatasetView<?>.DatasetViewTreeItem) item)
                    .forEach(item -> contentPane.getItems().remove(item.getDatasetView().getContentNode()));
            }
            Platform2.runOnFXThreadAndWaitTryCatch(() -> {
//              synchronized (instance) {
                final double pos = contentPane.getItems().isEmpty() ? 0d : 1d / (double) contentPane.getItems().size();
                for (int i = 0; i < contentPane.getItems().size(); i++)
                  contentPane.setDividerPosition(i, pos * (double) (i + 1));
//              }
            });
//          }
        }
      });
      datasets.addListener(new ListChangeListener<Dataset>() {

        @Override
        public void onChanged(ListChangeListener.Change<? extends Dataset> c) {
//          synchronized (instance) {
            while (c.next()) {
              if (c.wasAdded())
                c.getAddedSubList().forEach(dataset -> dataset.addToTree(DatasetTreeView.this));
              if (c.wasRemoved())
                c.getRemoved().forEach(dataset -> {
                  dataset.views.forEach(view -> contentPane.getItems().remove(view.getContentNode()));
                  final TreeItem<Control> parentItem = getParentItem(dataset);
                  parentItem
                      .getChildren()
                      .parallelStream()
                      .filter(treeItem -> treeItem instanceof Dataset.DatasetTreeItem)
                      .map(treeItem -> (Dataset.DatasetTreeItem) treeItem)
                      .filter(treeItem -> treeItem.getDataset().equals(dataset))
                      .findAny()
                      .ifPresent(treeItem -> parentItem.getChildren().remove(treeItem));
                });
            }
//          }
        }
      });
      ConExpFX.this.splitPane.getItems().add(new BorderPane(this, toolBar, null, null, null));
    }

    public final ObservableList<Dataset> getDatasets() {
      return datasets;
    }

    public final ObjectProperty<Dataset> getActiveDataset() {
      return activeDataset;
    }

    public final void addDataset(final Dataset dataset) {
      Platform2.runOnFXThread(() -> {
//        synchronized (instance) {
          datasets.add(dataset);
//        }
      });
    }

    public final void close(final Dataset dataset) {
      askForUnsavedChanges(dataset);
      getSelectionModel().clearSelection();
      datasets.remove(dataset);
      execute(
          TimeTask.create(
              dataset,
              "Closing " + dataset.id.get(),
              () -> Platform2.runOnFXThread(() -> executor.cancel(dataset))));
    }

    public final void closeActiveDataset() {
      if (activeDataset.isNotNull().get())
        close(activeDataset.get());
    }

    public final TreeItem<Control> getParentItem(final Dataset dataset) {
      if (dataset.parent != null)
        return dataset.parent.treeItem;
      return getRoot();
    }
  }

  public Stage                                     primaryStage;
  private final StackPane                          stackPane         = new StackPane();
  private final BorderPane                         rootPane          = new BorderPane();
  private final AnchorPane                         overlayPane       = new AnchorPane();
  private final SplitPane                          contentPane       = new SplitPane();
  private final SplitPane                          splitPane         = new SplitPane();
  public final DatasetTreeView                     treeView          = new DatasetTreeView();
  public final ExecutorStatusBar                   executorStatusBar = new ExecutorStatusBar(overlayPane);

  public final BlockingExecutor                    executor          = new BlockingExecutor();
  public final XMLFile                             configuration     = initConfiguration();
  public final ListProperty<File>                  fileHistory       =
      new SimpleListProperty<File>(FXCollections.observableArrayList());
  public File                                      lastDirectory;
  public final ObservableList<MatrixContext<?, ?>> contexts          = FXCollections.observableList(
      Lists.transform(
          Collections3.filter(treeView.getDatasets(), dataset -> dataset instanceof FCADataset),
          dataset -> ((FCADataset<?, ?>) dataset).context));
  public final ObservableList<MatrixContext<?, ?>> orders            =
      FXCollections.observableList(Collections3.filter(contexts, context -> context.isHomogen()));

  public final void start(final Stage primaryStage) {
    ConExpFX.instance = this;
    Platform.setImplicitExit(true);
    this.primaryStage = primaryStage;
    this.primaryStage.initStyle(StageStyle.DECORATED);
    this.primaryStage.setTitle("Concept Explorer FX");
    this.primaryStage.getIcons().add(new Image(ConExpFX.class.getResourceAsStream("image/conexp-fx.png")));
    this.primaryStage.setScene(new Scene(rootPane, 1280, 800));
    this.primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.getCode().equals(KeyCode.F11))
        ConExpFX.this.primaryStage.setFullScreen(!ConExpFX.this.primaryStage.isFullScreen());
    });
    this.primaryStage.setOnCloseRequest(e -> stop());
    final Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
    this.primaryStage.setX(bounds.getMinX());
    this.primaryStage.setY(bounds.getMinY());
    this.primaryStage.setWidth(bounds.getWidth());
    this.primaryStage.setHeight(bounds.getHeight());
//  this.primaryStage.setFullScreen(true);
    this.stackPane.getChildren().addAll(splitPane, overlayPane);
    this.overlayPane.setMouseTransparent(true);
    this.rootPane.setCenter(stackPane);
    this.rootPane.setBottom(executorStatusBar.statusBar);
//  this.rootPane.getStylesheets().add("conexp/fx/gui/style/style.css");
    this.splitPane.setOrientation(Orientation.HORIZONTAL);
    this.splitPane.getItems().add(contentPane);
    new CFXMenuBar();
    this.executorStatusBar.setOnMouseExitedHandler(this.primaryStage.getScene());
    this.executorStatusBar.bindTo(executor);
    this.primaryStage.show();
    Platform.runLater(() -> {
      ConExpFX.this.splitPane.setDividerPositions(new double[] {
          0.1618d
      });
      readConfiguration();
    });
  }

  private final XMLFile initConfiguration() {
    try {
      File file = new File(System.getProperty("user.home"), ".conexp-fx.xml");
      if (!file.exists())
        try {
          XMLFile.createEmptyConfiguration(file);
        } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Cannot create file " + file.getAbsolutePath());
          System.out.println("Creating temporary file instead.");
          file = new File(File.createTempFile("conexp-fx", "tmp").getParent(), "conexp-fx.xml");
        }
      if (!file.exists())
        XMLFile.createEmptyConfiguration(file);
      System.out.println("configuration file: " + file.getAbsolutePath());
      return new XMLFile(file);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private final void readConfiguration() {
    if (configuration.containsKey("file_history"))
      fileHistory.addAll(Lists.transform(configuration.get("file_history").getStringListValue(), File::new));
    if (configuration.containsKey("last_directory")
        && new File(configuration.get("last_directory").getStringValue()).exists()
        && new File(configuration.get("last_directory").getStringValue()).isDirectory())
      lastDirectory = new File(configuration.get("last_directory").getStringValue());
    if (configuration.containsKey("last_opened_files"))
      for (String last_opened_file : configuration.get("last_opened_files").getStringListValue())
        if (new File(last_opened_file).exists() && new File(last_opened_file).isFile())
          openFile(FileFormat.of(new File(last_opened_file)));
  }

  private final void writeConfiguration() throws IOException {
    if (lastDirectory != null)
      configuration.put("last_directory", new StringData("last_directory", lastDirectory.toString()));
    configuration.put("last_opened_files", new StringListData("last_opened_files", "last_opened_file"));
    for (Dataset d : treeView.datasets)
      if (d.file != null)
        configuration.get("last_opened_files").getStringListValue().add(d.file.toString());
    configuration
        .put("file_history", new StringListData("file_history", "file", Lists.transform(fileHistory, File::toString)));
    configuration.store();
  }

  private final void showOpenFileDialog() {
    final Pair<File, FileFormat> ffile = showOpenFileDialog(
        "Open Dataset",
        FileFormat.CXT,
        FileFormat.CFX,
        FileFormat.CSVB,
        FileFormat.NT,
        FileFormat.CSVT);
    if (ffile != null)
      openFile(ffile);
  }

  public synchronized final Pair<File, FileFormat>
      showOpenFileDialog(final String title, final FileFormat... fileFormats) {
    final FileChooser fc = new FileChooser();
    fc.setTitle(title);
    if (lastDirectory != null)
      fc.setInitialDirectory(lastDirectory);
    for (FileFormat ff : fileFormats)
      fc.getExtensionFilters().add(ff.extensionFilter);
    final File file = fc.showOpenDialog(primaryStage);
    if (file == null)
      return null;
    FileFormat fileFormat = null;
    for (FileFormat ff : fileFormats)
      if (fc.getSelectedExtensionFilter().equals(ff.extensionFilter)) {
        fileFormat = ff;
        break;
      }
    if (fileFormat == null)
      return null;
    lastDirectory = file.getParentFile();
    return FileFormat.of(file, fileFormat);
  }

  @SuppressWarnings("incomplete-switch")
  private void openFile(final Pair<File, FileFormat> ffile) {
    fileHistory.remove(ffile.first());
    fileHistory.add(0, ffile.first());
    switch (ffile.second()) {
    case CFX:
      treeView.addDataset(new FCADataset<String, String>(null, new Requests.Import.ImportCFX(ffile.first())));
      break;
    case CXT:
      treeView.addDataset(new FCADataset<String, String>(null, new Requests.Import.ImportCXT(ffile.first())));
      break;
    case CSVB:
      treeView.addDataset(new FCADataset<String, String>(null, new Requests.Import.ImportCSVB(ffile.first())));
      break;
    case NT:
      treeView.addDataset(new RDFDataset(ffile.first(), ffile.second()));
      break;
    case CSVT:
      treeView.addDataset(new RDFDataset(ffile.first(), ffile.second()));
    }
  }

  private final void askForUnsavedChanges() {
    treeView.getDatasets().forEach(this::askForUnsavedChanges);
  }

  private final void askForUnsavedChanges(final Dataset dataset) {
    if (dataset.unsavedChanges.get() && new FXDialog<Void>(
        primaryStage,
        Style.QUESTION,
        "Unsaved Changes",
        dataset.id.get() + " has unsaved changes. Do you want to save?",
        null).showAndWait().result().equals(Answer.YES))
      dataset.save();
  }

  public final void stop() {
    askForUnsavedChanges();
    try {
      writeConfiguration();
      primaryStage.close();
      System.exit(0);
    } catch (IOException e) {
      System.err.println("Could not write configuration to " + configuration.getFile());
      e.printStackTrace();
      primaryStage.close();
      System.exit(1);
    }
  }

}
