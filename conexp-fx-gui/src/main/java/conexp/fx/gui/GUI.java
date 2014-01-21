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
import javafx.geometry.InsetsBuilder;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.HyperlinkBuilder;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuBuilder;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
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
import conexp.fx.core.service.FCAService;
import conexp.fx.core.util.FileFormat;
import conexp.fx.core.util.windows7.AppUserModelIdUtility;
import conexp.fx.core.xml.StringData;
import conexp.fx.core.xml.StringListData;
import conexp.fx.core.xml.XMLFile;
import conexp.fx.gui.assistent.ConstructAssistent;
import conexp.fx.gui.dialog.FXDialog;
import conexp.fx.gui.dialog.FXDialog.Result;
import conexp.fx.gui.dialog.FXDialog.Style;
import conexp.fx.gui.tab.CFXTab;
import conexp.fx.gui.util.FXControls;

public class GUI extends Application {

  public final static void main(String[] args) {
    System.setProperty("file.encoding", "UTF-8");
    if (System.getProperty("os.name").toLowerCase().startsWith("windows"))
      AppUserModelIdUtility.setCurrentProcessExplicitAppUserModelID("peter.panther.conexp.conexp-fx");
    launch(args);
  }

  private final class InfoDialog extends FXDialog {

    private InfoDialog() {
      super(primaryStage, Style.INFO, "Info", "Concept Explorer FX\t\t(c) 2013, Francesco Kriegel\r\n\r\n"
          + "You may use this software for private or educational purposes at no charge.", HBoxBuilder
          .create()
          .spacing(10)
          .padding(InsetsBuilder.create().left(10).top(10).right(10).bottom(10).build())
          .children(
              HyperlinkBuilder
                  .create()
                  .text("mailto:francesco.kriegel@tu-dresden.de")
                  .onAction(new EventHandler<ActionEvent>() {

                    public final void handle(final ActionEvent event) {
                      try {
                        Desktop.getDesktop().mail(new URI("mailto:francesco.kriegel@tu-dresden.de"));
                      } catch (Exception e) {
                        new ErrorDialog(e).showAndWait();
                      }
                    }
                  })
                  .build(),
              HyperlinkBuilder
                  .create()
                  .text("http://lat.inf.tu-dresden.de/~francesco")
                  .onAction(new EventHandler<ActionEvent>() {

                    public final void handle(final ActionEvent event) {
                      try {
                        Desktop.getDesktop().browse(new URI("http://lat.inf.tu-dresden.de/~francesco"));
                      } catch (Exception e) {
                        new ErrorDialog(e).showAndWait();
                      }
                    }
                  })
                  .build())
          .build());
    }
  }

  private final class ErrorDialog extends FXDialog {

    private ErrorDialog(final Exception e) {
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
      rightBar.getMenus().add(fullScreenMenu);
    }

    private final void buildContextMenu() {
      final MenuItem newMenuItem =
          FXControls.newMenuItem("New", "image/document-new-3.png", new EventHandler<ActionEvent>() {

            public final void handle(final ActionEvent event) {
              showConstructAssistent();
            }
          });
      final MenuItem openMenuItem =
          FXControls.newMenuItem("Open", "image/document-open-2.png", new EventHandler<ActionEvent>() {

            public final void handle(final ActionEvent event) {
              showOpenFileDialog();
            }
          });
      final MenuItem saveMenuItem =
          FXControls.newMenuItem("Save", "image/document-save-5.png", true, new EventHandler<ActionEvent>() {

            public final void handle(final ActionEvent event) {
              selectedTab.get().save();
            }
          });
      final MenuItem saveAsMenuItem =
          FXControls.newMenuItem("Save As", "image/document-save-as-5.png", true, new EventHandler<ActionEvent>() {

            public final void handle(final ActionEvent event) {
              selectedTab.get().saveAs();
            }
          });
      final MenuItem exportMenuItem =
          FXControls.newMenuItem("Export", "image/document-export.png", true, new EventHandler<ActionEvent>() {

            public final void handle(final ActionEvent event) {
              selectedTab.get().export();
            }
          });
      final Menu historyMenu =
          MenuBuilder
              .create()
              .text("History")
              .graphic(FXControls.newImageView("image/document-open-recent-2.png"))
              .build();
      final MenuItem exitMenuItem =
          FXControls.newMenuItem("Exit", "image/dialog-close.png", new EventHandler<ActionEvent>() {

            public final void handle(final ActionEvent event) {
              stop();
            }
          });
      selectedTab.addListener(new ChangeListener<CFXTab<?, ?>>() {

        public final void changed(
            final ObservableValue<? extends CFXTab<?, ?>> observable,
            final CFXTab<?, ?> oldSelectedTab,
            final CFXTab<?, ?> newSelectedTab) {
          saveMenuItem.disableProperty().unbind();
          if (newSelectedTab == null) {
            saveMenuItem.setDisable(true);
            saveAsMenuItem.setDisable(true);
            exportMenuItem.setDisable(true);
          } else {
            saveMenuItem.disableProperty().bind(new BooleanBinding() {

              {
                bind(newSelectedTab.fca.unsavedChanges);
              }

              protected final boolean computeValue() {
                return !newSelectedTab.fca.unsavedChanges.get();
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
          exportMenuItem,
          new SeparatorMenuItem(),
          historyMenu,
          new SeparatorMenuItem(),
          exitMenuItem);
    }

    private final void buildHelpMenu() {
      if (Desktop.isDesktopSupported()) {
        final MenuItem helpMenuItem = FXControls.newMenuItem("Help", "image/help.png", new EventHandler<ActionEvent>() {

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
          FXControls.newMenuItem("Info", "image/help-contents.png", new EventHandler<ActionEvent>() {

            public final void handle(final ActionEvent event) {
              new InfoDialog().showAndWait();
            }
          });
      helpMenu.getItems().add(infoMenuItem);
    }
  }

  public Stage                                     primaryStage;
  private final BorderPane                         rootPane      = new BorderPane();
  public final TabPane                             tabPane       = new TabPane();
  public final ObjectBinding<CFXTab<?, ?>>         selectedTab   = new ObjectBinding<CFXTab<?, ?>>() {

                                                                   {
                                                                     bind(tabPane
                                                                         .getSelectionModel()
                                                                         .selectedItemProperty());
                                                                   }

                                                                   protected final CFXTab<?, ?> computeValue() {
                                                                     return (CFXTab<?, ?>) tabPane
                                                                         .getSelectionModel()
                                                                         .selectedItemProperty()
                                                                         .get();
                                                                   }
                                                                 };
  public final XMLFile                             configuration = initConfiguration();
  public File                                      lastDirectory;
  public final ListProperty<File>                  fileHistory   = new SimpleListProperty<File>(
                                                                     FXCollections.<File> observableArrayList());
  public final ObservableList<MatrixContext<?, ?>> contexts      = FXCollections.observableList(Lists.transform(
                                                                     tabPane.getTabs(),
                                                                     new Function<Tab, MatrixContext<?, ?>>() {

                                                                       public final MatrixContext<?, ?> apply(
                                                                           final Tab tab) {
                                                                         return ((CFXTab<?, ?>) tab).fca.context;
                                                                       }
                                                                     }));
  public final ObservableList<MatrixContext<?, ?>> orders        = FXCollections.observableList(Collections3.filter(
                                                                     contexts,
                                                                     new Predicate<MatrixContext<?, ?>>() {

                                                                       public final boolean apply(
                                                                           final MatrixContext<?, ?> context) {
                                                                         return context.isHomogen();
                                                                       }
                                                                     }));

  public final FCAService                          fcaService    = new FCAService();

  public final void start(final Stage primaryStage) {
    Platform.setImplicitExit(true);
    this.primaryStage = primaryStage;
    this.primaryStage.initStyle(StageStyle.DECORATED);
    this.primaryStage.setTitle("Concept Explorer FX");
    this.primaryStage.getIcons().add(new Image(GUI.class.getResourceAsStream("image/conexp-fx.png")));
    this.primaryStage.setScene(new Scene(rootPane, 1280, 800));
    this.primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

      public final void handle(final KeyEvent event) {
        if (event.getCode().equals(KeyCode.F11))
          GUI.this.primaryStage.setFullScreen(!GUI.this.primaryStage.isFullScreen());
      }
    });
    this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

      public final void handle(final WindowEvent event) {
        stop();
      }
    });
//    Screen screen = Screen.getPrimary();
//    Rectangle2D bounds = screen.getVisualBounds();
//    primaryStage.setX(bounds.getMinX());
//    primaryStage.setY(bounds.getMinY());
//    primaryStage.setWidth(bounds.getWidth());
//    primaryStage.setHeight(bounds.getHeight());
//    primaryStage.setFullScreen(true);
    this.rootPane.getStylesheets().add("conexp/fx/gui/style/style.css");
    new CFXMenuBar();
//    this.tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>()
//    {
//
//      
//      public final void
//          changed(final ObservableValue<? extends Tab> observable, final Tab oldValue, final Tab newValue)
//      {}
//    });
    this.tabPane.setSide(Side.BOTTOM);
    this.rootPane.setCenter(tabPane);
    this.primaryStage.show();
    Platform.runLater(new Runnable() {

      public final void run() {
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
            newTab(new Requests.Import.ImportCFX(new File(last_opened_file)));
          else if (last_opened_file.endsWith(".cxt"))
            newTab(new Requests.Import.ImportCXT(new File(last_opened_file)));
    if (configuration.containsKey("last_active_file"))
      for (final Tab tab : tabPane.getTabs()) {
        final CFXTab<?, ?> conExpTab = (CFXTab<?, ?>) tab;
        if (conExpTab.fca.file != null
            && conExpTab.fca.file.toString().equals(configuration.get("last_active_file").getStringValue()))
          tabPane.getSelectionModel().select(tab);
      }
  }

  private final void writeConfiguration() throws IOException {
    if (lastDirectory != null)
      configuration.put("last_directory", new StringData("last_directory", lastDirectory.toString()));
    configuration.put("last_opened_files", new StringListData("last_opened_files", "last_opened_file"));
    for (Tab tab : tabPane.getTabs()) {
      final CFXTab<?, ?> conExpTab = (CFXTab<?, ?>) tab;
      if (conExpTab.fca.file != null) {
        configuration.get("last_opened_files").getStringListValue().add(conExpTab.fca.file.toString());
        if (tabPane.getSelectionModel().getSelectedItem().equals(tab))
          configuration.put("last_active_file", new StringData("last_active_file", conExpTab.fca.file.toString()));
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

  public final <G, M> void newTab(final Request<G, M> request) {
    final CFXTab<G, M> tab = new CFXTab<G, M>(this, request);
    tabPane.getTabs().add(tab);
    tabPane.getSelectionModel().select(tab);
    tab.splitPane.getDividers().get(0).positionProperty().set(0.33d);
    Platform.runLater(new Runnable() {

      public final void run() {
        tab.splitPane.getDividers().get(0).positionProperty().set(0.33d);
        Platform.runLater(new Runnable() {

          public final void run() {
            tab.splitPane.getDividers().get(0).positionProperty().set(0.33d);
            Platform.runLater(new Runnable() {

              public final void run() {
                tab.splitPane.getDividers().get(0).positionProperty().set(0.33d);
              }
            });
          }
        });
      }
    });
  }

  private final void showConstructAssistent() {
    new ConstructAssistent(this).showAndWait();
  }

  private final void showOpenFileDialog() {
    final Pair<File, FileFormat> ffile = showOpenFileDialog("Open Formal Context File", FileFormat.CFX, FileFormat.CXT);
    if (ffile != null)
      openFFile(ffile);
  }

  @SuppressWarnings("incomplete-switch")
  private void openFFile(final Pair<File, FileFormat> ffile) {
    switch (ffile.second()) {
    case CFX:
      newTab(new Requests.Import.ImportCFX(ffile.first()));
      break;
    case CXT:
      newTab(new Requests.Import.ImportCXT(ffile.first()));
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
      e.printStackTrace();
      primaryStage.close();
      System.exit(1);
    }
  }

  private final void askForUnsavedChanges() {
    for (CFXTab<?, ?> tab : Collections3.elementsBySubClass(tabPane.getTabs(), CFXTab.class))
      if (tab.fca.unsavedChanges.get()
          && new FXDialog(primaryStage, Style.QUESTION, "Unsaved Changes", tab.fca.id.get()
              + " has unsaved changes. Do you want to save?", null).showAndWait().equals(Result.YES))
        tab.save();
  }
}
