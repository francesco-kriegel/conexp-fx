/**
 * @author Francesco Kriegel (francesco.kriegel@gmx.de)
 */
package conexp.fx.gui;

/*
 * #%L
 * Concept Explorer FX - Graphical User Interface
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;

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
import javafx.scene.control.Label;
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
import javafx.scene.image.ImageView;
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

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import conexp.fx.core.builder.Request;
import conexp.fx.core.builder.Requests;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.util.FileFormat;
import conexp.fx.core.xml.StringData;
import conexp.fx.core.xml.StringListData;
import conexp.fx.core.xml.XMLFile;
import conexp.fx.gui.assistent.ConstructAssistent;
import conexp.fx.gui.dialog.FXDialog;
import conexp.fx.gui.dialog.FXDialog.Result;
import conexp.fx.gui.dialog.FXDialog.Style;
import conexp.fx.gui.task.BlockingExecutor;
import conexp.fx.gui.task.ExecutorStatusBar;
import conexp.fx.gui.util.AppUserModelIdUtility;
import conexp.fx.gui.util.FXControls;

public class ConExpFX extends Application {

  public static ConExpFX __;

  public final static void main(String[] args) {
    System.setProperty("file.encoding", "UTF-8");
    if (System.getProperty("os.name").toLowerCase().startsWith("windows"))
      AppUserModelIdUtility.setCurrentProcessExplicitAppUserModelID("peter.panther.conexp.conexp-fx");
//    AquaFx.style();
    launch(args);
  }

  public final class ErrorDialog extends FXDialog<Void> {

    public ErrorDialog(final Exception e) {
      super(primaryStage, Style.ERROR, e.getMessage(), e.toString(), null);
    }
  }

  private final class CFXMenuBar {

    private final MenuBar menuBar     = new MenuBar();
    private final Menu    contextMenu = new Menu("_Context");
    private final Menu    viewMenu    = new Menu("_View");
    private final Menu    helpMenu    = new Menu("?");

    public CFXMenuBar() {
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
//      final Menu fullScreenMenu =
//          MenuBuilder
//              .create()
//              .graphic(
//                  TextBuilder
//                      .create()
//                      .text("Fullscreen")
//                      .fontSmoothingType(FontSmoothingType.LCD)
//                      .fill(Color.WHITE)
//                      .onMouseClicked(new EventHandler<Event>() {
//
//                        public final void handle(final Event event) {
//                          primaryStage.setFullScreen(!primaryStage.isFullScreen());
//                        }
//                      })
//                      .build())
//              .build();
//      final ProgressBar progressBar = ProgressBarBuilder.create().minHeight(10).maxHeight(10).build();
//      progressBar.progressProperty().bind(new DoubleBinding() {
//
//        {
//          new Timer().schedule(new TimerTask() {
//
//            @Override
//            public void run() {
//              invalidate();
//            }
//          }, 1000, 1000);
//        }
//
//        @Override
//        protected double computeValue() {
//          final double load =
//              ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage()
//                  / (double) ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
//          System.out.println(load);
//          return load;
//        }
//
//      });
//      final Menu progressMenu = MenuBuilder.create().graphic(progressBar).build();
//      rightBar.getMenus().addAll(fullScreenMenu);
      viewMenu.getItems().add(
          FXControls.newMenuItem(
              "Fullscreen",
              "image/16x16/new_page.png",
              e -> primaryStage.setFullScreen(!primaryStage.isFullScreen())));
    }

    private final void buildContextMenu() {
      final MenuItem newMenuItem =
          FXControls.newMenuItem("New", "image/16x16/new_page.png", e -> showConstructAssistent());
      final MenuItem openMenuItem = FXControls.newMenuItem("Open", "image/16x16/folder.png", e -> showOpenFileDialog());
      final MenuItem saveMenuItem =
          FXControls.newMenuItem("Save", "image/16x16/save.png", true, e -> activeInstance.get().save());
      final MenuItem saveAsMenuItem =
          FXControls.newMenuItem("Save As", "image/16x16/save.png", true, e -> activeInstance.get().saveAs());
      final MenuItem texMenuItem =
          FXControls
              .newMenuItem("TeX-Export", "image/16x16/briefcase.png", true, e -> activeInstance.get().exportTeX());
      final MenuItem exportMenuItem =
          FXControls.newMenuItem("Export", "image/16x16/briefcase.png", true, e -> activeInstance.get().export());
      texMenuItem.disableProperty().bind(exportMenuItem.disableProperty());
      final Menu historyMenu = new Menu("History", FXControls.newImageView("image/16x16/clock.png"));
//          MenuBuilder.create().text("History").graphic(FXControls.newImageView("image/16x16/clock.png")).build();
      final MenuItem exitMenuItem = FXControls.newMenuItem("Exit", "image/16x16/delete.png", e -> stop());
      activeInstance.addListener(new ChangeListener<FCAInstance<?, ?>>() {

        public final void changed(
            final ObservableValue<? extends FCAInstance<?, ?>> observable,
            final FCAInstance<?, ?> oldSelectedTab,
            final FCAInstance<?, ?> newSelectedTab) {
          saveMenuItem.disableProperty().unbind();
          if (newSelectedTab == null) {
            saveMenuItem.setDisable(true);
            saveAsMenuItem.setDisable(true);
            exportMenuItem.setDisable(true);
          } else {
            saveMenuItem
                .disableProperty()
                .bind(
                    Bindings.createBooleanBinding(
                        () -> !newSelectedTab.unsavedChanges.get(),
                        newSelectedTab.unsavedChanges));
//            saveMenuItem.disableProperty().bind(new BooleanBinding() {
//
//              {
//                bind(newSelectedTab.unsavedChanges);
//              }
//
//              protected final boolean computeValue() {
//                return !newSelectedTab.unsavedChanges.get();
//              }
//            });
            saveAsMenuItem.setDisable(false);
            exportMenuItem.setDisable(false);
          }
        }
      });
      historyMenu.disableProperty().bind(fileHistory.emptyProperty());
      fileHistory.addListener(new ListChangeListener<File>() {

        public final void onChanged(final ListChangeListener.Change<? extends File> c) {
          historyMenu.getItems().clear();
          historyMenu.getItems().addAll(Collections2.transform(fileHistory, file -> MenuItemBuilder.create()
//                      .graphic(LabelBuilder.create()
              .text(file.toString())
//                          .build())
              .onAction(e -> Platform.runLater(() -> {
                if (file.exists() && file.isFile())
                  openFFile(FileFormat.of(file, FileFormat.CFX, FileFormat.CXT));
              }))
              .build()));
        }
      });
      contextMenu.getItems().addAll(
          newMenuItem,
          openMenuItem,
          saveMenuItem,
          saveAsMenuItem,
          texMenuItem,
          exportMenuItem,
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
            new ErrorDialog(e).showAndWait();
          }
        });
        helpMenu.getItems().add(helpMenuItem);
      }
      final MenuItem infoMenuItem =
          FXControls.newMenuItem("Info", "image/16x16/info.png", e -> new InfoDialog(ConExpFX.this).showAndWait());
      helpMenu.getItems().addAll(infoMenuItem);
    }
  }

  private final class CFXTreeView {

    private final ToolBar         toolBar  = new ToolBar();
    private final TreeView<Label> treeView = new TreeView<Label>();

    public CFXTreeView() {
      super();
      final Button newButton = new Button("New", FXControls.newImageView("image/16x16/new_page.png"));
      newButton.setOnAction(e -> showConstructAssistent());
      final Button openButton = new Button("Open", FXControls.newImageView("image/16x16/folder.png"));
      openButton.setOnAction(e -> showOpenFileDialog());
      toolBar.getItems().addAll(newButton, openButton);
      this.treeView.setRoot(new TreeItem<>());
      this.treeView.setShowRoot(false);
      this.treeView.selectionModelProperty().get().setSelectionMode(SelectionMode.MULTIPLE);
      this.treeView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TreeItem<?>>() {

        @Override
        public void onChanged(javafx.collections.ListChangeListener.Change<? extends TreeItem<?>> c) {
          c.next();
          contentPane.getItems().clear();
          for (TreeItem<?> item : c.getList()) {
            if (item != null && item.getValue() != null && item.getValue() instanceof String) {
              FCAInstance<?, ?> fca = (FCAInstance<?, ?>) ((Label) item.getParent().getValue()).getUserData();
              activeInstance.set(fca);
              if (fca != null)
                switch ((String) item.getValue()) {
                case "Context":
                  contentPane.getItems().add(fca.contextWidget);
                  break;
                case "Lattice":
                  contentPane.getItems().add(fca.conceptGraph);
                  break;
                case "Concepts":
                  contentPane.getItems().add(fca.conceptWidget);
                  break;
                case "Implications":
                  contentPane.getItems().add(fca.implicationWidget);
                  break;
//                case "Status":
//                  contentPane.getItems().add(fca.statusWidget);
//                  break;
                }
            }
          }
          Platform.runLater(() -> {
            final double pos = contentPane.getItems().isEmpty() ? 0d : 1d / (double) contentPane.getItems().size();
            for (int i = 0; i < contentPane.getItems().size(); i++)
              contentPane.setDividerPosition(i, pos * (double) (i + 1));
          });
        }
      });
      ConExpFX.this.fcaInstances.addListener(new ListChangeListener<FCAInstance<?, ?>>() {

        @Override
        public final void onChanged(final ListChangeListener.Change<? extends FCAInstance<?, ?>> c) {
          c.next();
          if (c.wasAdded())
            for (FCAInstance<?, ?> fca : c.getAddedSubList()) {
              final Label label = new Label(fca.id.get());
              label.setUserData(fca);
              label.textProperty().bind(fca.id);
              label.setStyle("-fx-font-weight: bold;");
              final TreeItem<Label> treeItem =
                  new TreeItem<Label>(label, new ImageView(new Image(ConExpFX.class
                      .getResourceAsStream("image/context.gif"))));
              treeView.getRoot().getChildren().add(treeItem);// , new Label(tab.fca.id.get())
              final TreeItem treeItem2 = new TreeItem("Lattice");
              treeItem.getChildren().addAll(
                  new TreeItem("Context"),
                  treeItem2,
                  new TreeItem("Concepts"),
                  new TreeItem("Implications")
//                  ,new TreeItem("Status")
                  );
              treeView.getSelectionModel().select(treeItem2);
            }
          if (c.wasRemoved())
            for (FCAInstance<?, ?> fca : c.getRemoved()) {

            }
        }

      });
      ConExpFX.this.datasets.addListener(new ListChangeListener<Dataset>() {

        @Override
        public void onChanged(javafx.collections.ListChangeListener.Change<? extends Dataset> c) {
          while (c.next())
            if (c.wasAdded())
              for (Dataset d : c.getAddedSubList()) {
                final Label label = new Label(d.id.get());
                label.textProperty().bind(d.id);
                label.setUserData(d);
                label.setStyle("-fx-font-weight: bold;");
                final TreeItem treeItem =
                    new TreeItem(label, new ImageView(
                        new Image(ConExpFX.class.getResourceAsStream("image/context.gif"))));
                treeView.getRoot().getChildren().add(treeItem);
                treeView.getSelectionModel().select(treeItem);
              }
            else if (c.wasRemoved())
              for (Dataset d : c.getRemoved()) {

              }
        }
      });
      ConExpFX.this.splitPane.getItems().add(new BorderPane(treeView, toolBar, null, null, null));
    }

  }

  public final BlockingExecutor                    exe               = new BlockingExecutor();
  public final XMLFile                             configuration     = initConfiguration();
  public final ListProperty<File>                  fileHistory       = new SimpleListProperty<File>(
                                                                         FXCollections.<File> observableArrayList());
  public File                                      lastDirectory;
  private final ObservableList<FCAInstance<?, ?>>  fcaInstances      = FXCollections.observableArrayList();
  private final ObservableList<Dataset>            datasets          = FXCollections.observableArrayList();
  public final ObjectProperty<FCAInstance<?, ?>>   activeInstance    =
                                                                         new SimpleObjectProperty<FCAInstance<?, ?>>(
                                                                             null);
  public final ObservableList<MatrixContext<?, ?>> contexts          = FXCollections.observableList(Lists.transform(
                                                                         fcaInstances,
                                                                         tab -> tab.context));
  public final ObservableList<MatrixContext<?, ?>> orders            = FXCollections.observableList(Collections3
                                                                         .filter(
                                                                             contexts,
                                                                             context -> context.isHomogen()));

  public Stage                                     primaryStage;
  private final StackPane                          stackPane         = new StackPane();
  private final BorderPane                         rootPane          = new BorderPane();
  private final AnchorPane                         overlayPane       = new AnchorPane();
  private final SplitPane                          contentPane       = new SplitPane();
  private final SplitPane                          splitPane         = new SplitPane();
  public final ExecutorStatusBar                   executorStatusBar = new ExecutorStatusBar(overlayPane);

  public final void start(final Stage primaryStage) {
    Platform.setImplicitExit(true);
    this.primaryStage = primaryStage;
    this.primaryStage.initStyle(StageStyle.DECORATED);
    this.primaryStage.setTitle("Concept Explorer FX");
    this.primaryStage.getIcons().add(new Image(ConExpFX.class.getResourceAsStream("image/conexp-fx.png")));
    stackPane.getChildren().addAll(splitPane, overlayPane);
    overlayPane.setMouseTransparent(true);
    this.primaryStage.setScene(new Scene(rootPane, 1280, 800));
    this.primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.getCode().equals(KeyCode.F11))
        ConExpFX.this.primaryStage.setFullScreen(!ConExpFX.this.primaryStage.isFullScreen());
    });
    this.primaryStage.setOnCloseRequest(e -> stop());
    final Screen screen = Screen.getPrimary();
    final Rectangle2D bounds = screen.getVisualBounds();
    primaryStage.setX(bounds.getMinX());
    primaryStage.setY(bounds.getMinY());
    primaryStage.setWidth(bounds.getWidth());
    primaryStage.setHeight(bounds.getHeight());
//    primaryStage.setFullScreen(true);
//    this.rootPane.getStylesheets().add("conexp/fx/gui/style/style.css");
    this.splitPane.setOrientation(Orientation.HORIZONTAL);
    new CFXMenuBar();
    new CFXTreeView();
    this.splitPane.getItems().add(contentPane);
    this.rootPane.setCenter(stackPane);
    executorStatusBar.bindTo(exe);
    this.rootPane.setBottom(executorStatusBar.statusBar);
    this.primaryStage.show();
    Platform.runLater(() -> {
      ConExpFX.this.splitPane.setDividerPositions(new double[] { 0.1d });
      readConfiguration();
    });
  }

  private final XMLFile initConfiguration() {
    try {
      File file = new File("conexp-fx.xml");
      try {
        file.getParentFile().isDirectory();
      } catch (NullPointerException e) {
        file = new File(File.createTempFile("conexp-fx", "tmp").getParent(), "conexp-fx.xml");
      }
      System.out.println(file.getAbsolutePath());
      if (!file.exists())
        XMLFile.createEmptyConfiguration(file);
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
          if (last_opened_file.endsWith(".cfx"))
            newFCAInstance(new Requests.Import.ImportCFX(new File(last_opened_file)));
          else if (last_opened_file.endsWith(".cxt"))
            newFCAInstance(new Requests.Import.ImportCXT(new File(last_opened_file)));
//    if (configuration.containsKey("last_active_file"))
//      for (final FCAInstance conExpTab : fcaInstances) {
//        if (conExpTab.file != null
//            && conExpTab.file.toString().equals(configuration.get("last_active_file").getStringValue()))
//          tabPane.getSelectionModel().select(tab);
//      }
  }

  private final void writeConfiguration() throws IOException {
    if (lastDirectory != null)
      configuration.put("last_directory", new StringData("last_directory", lastDirectory.toString()));
    configuration.put("last_opened_files", new StringListData("last_opened_files", "last_opened_file"));
    for (FCAInstance<?, ?> conExpTab : fcaInstances) {
      if (conExpTab.file != null) {
        configuration.get("last_opened_files").getStringListValue().add(conExpTab.file.toString());
//        if (tabPane.getSelectionModel().getSelectedItem().equals(tab))
//          configuration.put("last_active_file", new StringData("last_active_file", conExpTab.fca.file.toString()));
      }
    }
    configuration.put(
        "file_history",
        new StringListData("file_history", "file", Lists.transform(fileHistory, File::toString)));
    configuration.store();
  }

  public final <G, M> void newFCAInstance(final Request<G, M> request) {
    final FCAInstance<G, M> fca = new FCAInstance<G, M>(this, request);
    fcaInstances.add(fca);
  }

  private final void showConstructAssistent() {
    new ConstructAssistent(this).showAndWait();
  }

  private final void showOpenFileDialog() {
    final Pair<File, FileFormat> ffile =
        showOpenFileDialog("Open Formal Context File", FileFormat.CXT, FileFormat.CFX, FileFormat.TTL);
    if (ffile != null)
      openFFile(ffile);
  }

  @SuppressWarnings("incomplete-switch")
  private void openFFile(final Pair<File, FileFormat> ffile) {
    switch (ffile.second()) {
    case CFX:
      newFCAInstance(new Requests.Import.ImportCFX(ffile.first()));
      break;
    case CXT:
      newFCAInstance(new Requests.Import.ImportCXT(ffile.first()));
      break;
    case TTL:
      final Dataset dataset = new Dataset(ffile.x(), ffile.y());
      datasets.add(dataset);
    }
  }

  public synchronized final Pair<File, FileFormat> showOpenFileDialog(
      final String title,
      final FileFormat... fileFormats) {
    final FileChooser fc = new FileChooser();
    fc.setTitle(title);
    if (lastDirectory != null)
      fc.setInitialDirectory(lastDirectory);
    for (FileFormat ff : fileFormats)
      fc.getExtensionFilters().add(ff.extensionFilter);
    final File file = fc.showOpenDialog(primaryStage);
    if (file == null)
      return null;
    lastDirectory = file.getParentFile();
    return FileFormat.of(file, fileFormats);
  }

  public final void stop() {
    askForUnsavedChanges();
    try {
      writeConfiguration();
      primaryStage.close();
      System.exit(0);
    } catch (IOException e) {
      System.err.println("The following error can be ignored:");
      e.printStackTrace();
      primaryStage.close();
      System.exit(1);
    }
  }

  private final void askForUnsavedChanges() {
    for (FCAInstance<?, ?> tab : fcaInstances)
      if (tab.unsavedChanges.get()
          && new FXDialog(primaryStage, Style.QUESTION, "Unsaved Changes", tab.id.get()
              + " has unsaved changes. Do you want to save?", null).showAndWait().equals(Result.YES))
        tab.save();
  }
}
