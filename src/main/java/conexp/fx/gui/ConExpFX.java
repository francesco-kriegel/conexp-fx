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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuBuilder;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPaneBuilder;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.TextBuilder;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
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
import conexp.fx.gui.util.AppUserModelIdUtility;
import conexp.fx.gui.util.FXControls;

public class ConExpFX extends Application {

  public final static void main(String[] args) {
    System.setProperty("file.encoding", "UTF-8");
    if (System.getProperty("os.name").toLowerCase().startsWith("windows"))
      AppUserModelIdUtility.setCurrentProcessExplicitAppUserModelID("peter.panther.conexp.conexp-fx");
    launch(args);
  }

  final class ErrorDialog extends FXDialog {

    public ErrorDialog(final Exception e) {
      super(primaryStage, Style.ERROR, e.getMessage(), e.toString(), null);
    }
  }

  private final class CFXMenuBar {

    private final MenuBar leftBar     = new MenuBar();
    private final MenuBar rightBar    = new MenuBar();
    private final Menu    contextMenu = MenuBuilder.create().text("_Context").build();
    private final Menu    helpMenu    = MenuBuilder.create().text("?").build();

    public CFXMenuBar() {
      super();
      buildLeftBar();
      buildRightBar();
      rootPane.setTop(HBoxBuilder.create().children(leftBar, rightBar).build());
    }

    private final void buildLeftBar() {
      HBox.setHgrow(leftBar, Priority.ALWAYS);
      buildContextMenu();
      buildHelpMenu();
      leftBar.getMenus().addAll(contextMenu, helpMenu);
    }

    private final void buildRightBar() {
      final Menu fullScreenMenu =
          MenuBuilder
              .create()
              .graphic(
                  TextBuilder
                      .create()
                      .text("Fullscreen")
                      .fontSmoothingType(FontSmoothingType.LCD)
                      .fill(Color.WHITE)
                      .onMouseClicked(new EventHandler<Event>() {

                        public final void handle(final Event event) {
                          primaryStage.setFullScreen(!primaryStage.isFullScreen());
                        }
                      })
                      .build())
              .build();
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
      rightBar.getMenus().addAll(fullScreenMenu);
    }

    private final void buildContextMenu() {
      final MenuItem newMenuItem =
          FXControls.newMenuItem("New", "image/16x16/new_page.png", new EventHandler<ActionEvent>() {

            public final void handle(final ActionEvent event) {
              showConstructAssistent();
            }
          });
      final MenuItem openMenuItem =
          FXControls.newMenuItem("Open", "image/16x16/folder.png", new EventHandler<ActionEvent>() {

            public final void handle(final ActionEvent event) {
              showOpenFileDialog();
            }
          });
      final MenuItem saveMenuItem =
          FXControls.newMenuItem("Save", "image/16x16/save.png", true, new EventHandler<ActionEvent>() {

            public final void handle(final ActionEvent event) {
              activeInstance.get().save();
            }
          });
      final MenuItem saveAsMenuItem =
          FXControls.newMenuItem("Save As", "image/16x16/save.png", true, new EventHandler<ActionEvent>() {

            public final void handle(final ActionEvent event) {
              activeInstance.get().saveAs();
            }
          });
      final MenuItem texMenuItem =
          FXControls.newMenuItem("TeX-Export", "image/16x16/briefcase.png", true, new EventHandler<ActionEvent>() {

            public final void handle(final ActionEvent event) {
              activeInstance.get().exportTeX();
            }
          });
      final MenuItem exportMenuItem =
          FXControls.newMenuItem("Export", "image/16x16/briefcase.png", true, new EventHandler<ActionEvent>() {

            public final void handle(final ActionEvent event) {
              activeInstance.get().export();
            }
          });
      texMenuItem.disableProperty().bind(exportMenuItem.disableProperty());
      final Menu historyMenu =
          MenuBuilder.create().text("History").graphic(FXControls.newImageView("image/16x16/clock.png")).build();
      final MenuItem exitMenuItem =
          FXControls.newMenuItem("Exit", "image/16x16/delete.png", new EventHandler<ActionEvent>() {

            public final void handle(final ActionEvent event) {
              stop();
            }
          });
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
            saveMenuItem.disableProperty().bind(new BooleanBinding() {

              {
                bind(newSelectedTab.unsavedChanges);
              }

              protected final boolean computeValue() {
                return !newSelectedTab.unsavedChanges.get();
              }
            });
            saveAsMenuItem.setDisable(false);
            exportMenuItem.setDisable(false);
          }
        }
      });
      historyMenu.disableProperty().bind(fileHistory.emptyProperty());
      fileHistory.addListener(new ListChangeListener<File>() {

        public final void onChanged(final ListChangeListener.Change<? extends File> c) {
          historyMenu.getItems().clear();
          historyMenu.getItems().addAll(Collections2.transform(fileHistory, new Function<File, MenuItem>() {

            public final MenuItem apply(final File file) {
              return MenuItemBuilder
                  .create()
                  .graphic(LabelBuilder.create().text(file.toString()).build())
                  .onAction(new EventHandler<ActionEvent>() {

                    public final void handle(final ActionEvent event) {
                      Platform.runLater(new Runnable() {

                        public final void run() {
                          if (file.exists() && file.isFile())
                            openFFile(FileFormat.of(file, FileFormat.CFX, FileFormat.CXT));
                        }
                      });
                    }
                  })
                  .build();
            }
          }));
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
        final MenuItem helpMenuItem =
            FXControls.newMenuItem("Help", "image/16x16/help.png", new EventHandler<ActionEvent>() {

              public final void handle(final ActionEvent event) {
                try {
                  Desktop.getDesktop().browse(new URI("http://francesco.kriegel.bplaced.de/conexp-fx/conexp-fx.html"));
                } catch (Exception e) {
                  new ErrorDialog(e).showAndWait();
                }
              }
            });
        helpMenu.getItems().add(helpMenuItem);
      }
      final MenuItem infoMenuItem =
          FXControls.newMenuItem("Info", "image/16x16/info.png", new EventHandler<ActionEvent>() {

            public final void handle(final ActionEvent event) {
              new InfoDialog(ConExpFX.this).showAndWait();
            }
          });
      helpMenu.getItems().addAll(infoMenuItem);
    }
  }

  public final XMLFile                             configuration  = initConfiguration();
  public final ListProperty<File>                  fileHistory    = new SimpleListProperty<File>(
                                                                      FXCollections.<File> observableArrayList());
  public File                                      lastDirectory;
  public final ThreadPoolExecutor                  tpe            = new ThreadPoolExecutor(
                                                                      Runtime.getRuntime().availableProcessors(),
                                                                      Runtime.getRuntime().availableProcessors(),
                                                                      60,
                                                                      TimeUnit.SECONDS,
                                                                      new LinkedBlockingQueue<Runnable>());
  private final ObservableList<FCAInstance<?, ?>>  fcaInstances   = FXCollections.observableArrayList();
  public final ObjectBinding<FCAInstance<?, ?>>    activeInstance = new ObjectBinding<FCAInstance<?, ?>>() {

                                                                    @Override
                                                                    protected FCAInstance<?, ?> computeValue() {
                                                                      // TODO Auto-generated method stub
                                                                      return null;
                                                                    }
                                                                  };
  public final ObservableList<MatrixContext<?, ?>> contexts       =
                                                                      FXCollections
                                                                          .observableList(Lists
                                                                              .transform(
                                                                                  fcaInstances,
                                                                                  new Function<FCAInstance<?, ?>, MatrixContext<?, ?>>() {

                                                                                    public final MatrixContext<?, ?>
                                                                                        apply(final FCAInstance tab) {
                                                                                      return tab.context;
                                                                                    }
                                                                                  }));
  public final ObservableList<MatrixContext<?, ?>> orders         = FXCollections.observableList(Collections3.filter(
                                                                      contexts,
                                                                      new Predicate<MatrixContext<?, ?>>() {

                                                                        public final boolean apply(
                                                                            final MatrixContext<?, ?> context) {
                                                                          return context.isHomogen();
                                                                        }
                                                                      }));

  public Stage                                     primaryStage;
  private final BorderPane                         rootPane       = new BorderPane();
  private final TreeView<Label>                    treeView       = new TreeView<Label>();
  private final SplitPane                          contentPane    = new SplitPane();
  private SplitPane                                splitPane;

  public final void start(final Stage primaryStage) {
    Platform.setImplicitExit(true);
    this.primaryStage = primaryStage;
    this.primaryStage.initStyle(StageStyle.DECORATED);
    this.primaryStage.setTitle("Concept Explorer FX");
    this.primaryStage.getIcons().add(new Image(ConExpFX.class.getResourceAsStream("image/conexp-fx.png")));
    this.primaryStage.setScene(new Scene(rootPane, 1280, 800));
    this.primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

      public final void handle(final KeyEvent event) {
        if (event.getCode().equals(KeyCode.F11))
          ConExpFX.this.primaryStage.setFullScreen(!ConExpFX.this.primaryStage.isFullScreen());
      }
    });
    this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

      public final void handle(final WindowEvent event) {
        stop();
      }
    });
    final Screen screen = Screen.getPrimary();
    final Rectangle2D bounds = screen.getVisualBounds();
    primaryStage.setX(bounds.getMinX());
    primaryStage.setY(bounds.getMinY());
    primaryStage.setWidth(bounds.getWidth());
    primaryStage.setHeight(bounds.getHeight());
//    primaryStage.setFullScreen(true);
//    this.rootPane.getStylesheets().add("conexp/fx/gui/style/style.css");
    new CFXMenuBar();
//    this.tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>()
//    {
//
//      
//      public final void
//          changed(final ObservableValue<? extends Tab> observable, final Tab oldValue, final Tab newValue)
//      {}
//    });
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
              case "Status":
                contentPane.getItems().add(fca.statusWidget);
                break;
              }
          }
        }
        Platform.runLater(new Runnable() {

          @Override
          public void run() {
            final double pos = contentPane.getItems().isEmpty() ? 0d : 1d / (double) contentPane.getItems().size();
            for (int i = 0; i < contentPane.getItems().size(); i++)
              contentPane.setDividerPosition(i, pos * (double) (i + 1));
          }
        });
      }
    });
    this.fcaInstances.addListener(new ListChangeListener<FCAInstance<?, ?>>() {

      @Override
      public final void onChanged(final ListChangeListener.Change<? extends FCAInstance<?, ?>> c) {
        c.next();
        if (c.wasAdded())
          for (FCAInstance<?, ?> fca : c.getAddedSubList()) {
            final Label label = new Label(fca.id.get());
            label.setUserData(fca);
            label.textProperty().bind(fca.id);
            label.setStyle("-fx-font-weight: bold;");
            final TreeItem treeItem =
                new TreeItem(label, ImageViewBuilder
                    .create()
                    .image(new Image(ConExpFX.class.getResourceAsStream("image/context.gif")))
                    .build());
            treeView.getRoot().getChildren().add(treeItem);// , new Label(tab.fca.id.get())
            treeItem.getChildren().addAll(
                new TreeItem("Context"),
                new TreeItem("Lattice"),
                new TreeItem("Concepts"),
                new TreeItem("Implications"),
                new TreeItem("Status"));
          }
        if (c.wasRemoved())
          for (FCAInstance<?, ?> fca : c.getRemoved()) {

          }
      }

    });
    this.splitPane = SplitPaneBuilder.create().orientation(Orientation.HORIZONTAL).items(treeView, contentPane).build();
    this.rootPane.setCenter(splitPane);
    this.primaryStage.show();
    Platform.runLater(new Runnable() {

      public final void run() {
        ConExpFX.this.splitPane.setDividerPositions(new double[] { 0.1d });
        readConfiguration();
      }
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
      fileHistory.addAll(Lists.transform(
          configuration.get("file_history").getStringListValue(),
          new Function<String, File>() {

            public final File apply(final String string) {
              return new File(string);
            }
          }));
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
    for (FCAInstance conExpTab : fcaInstances) {
      if (conExpTab.file != null) {
        configuration.get("last_opened_files").getStringListValue().add(conExpTab.file.toString());
//        if (tabPane.getSelectionModel().getSelectedItem().equals(tab))
//          configuration.put("last_active_file", new StringData("last_active_file", conExpTab.fca.file.toString()));
      }
    }
    configuration.put(
        "file_history",
        new StringListData("file_history", "file", Lists.transform(fileHistory, new Function<File, String>() {

          public final String apply(final File file) {
            return file.toString();
          }
        })));
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
    final Pair<File, FileFormat> ffile = showOpenFileDialog("Open Formal Context File", FileFormat.CXT, FileFormat.CFX);
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
