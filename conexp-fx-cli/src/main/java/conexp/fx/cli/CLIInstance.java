package conexp.fx.cli;

/*
 * #%L
 * Concept Explorer FX - Command Line Interface
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
import java.io.File;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;

import org.apache.commons.cli.CommandLine;

import conexp.fx.core.builder.Requests;
import conexp.fx.core.collections.relation.RelationEvent;
import conexp.fx.core.collections.relation.RelationEventHandler;
import conexp.fx.core.concurrent.BlockingTask;
import conexp.fx.core.context.Concept;
import conexp.fx.core.service.FCAInstance;
import conexp.fx.core.service.FCAInstance.InitLevel;
import conexp.fx.core.service.FCAListener;
import conexp.fx.core.util.FileFormat;
import de.tudresden.inf.tcs.fcalib.Implication;

public class CLIInstance extends FCAListener<String, String> {

  private FCAInstance<String, String> fcaInstance;
  private final CommandLine           commandLine;

  protected CLIInstance(CommandLine commandLine) {
    super();
    this.commandLine = commandLine;
  }

  protected void run() {
    fcaInitialize();
    fcaProcess();
    fcaExport();
  }

  private void fcaInitialize() {
    // final boolean print = commandLine.hasOption(CLI.PRINT_TO_CONSOLE.getLongOpt());
    final File file = new File(commandLine.getOptionValue(CLI.IMPORT_CXT.getLongOpt()));
    fcaInstance = CLI.fcaService.<String, String> add(new Requests.Import.ImportCXT(file));
    initializeInstance(fcaInstance);
    // check whether could be left out
    // fcaInstance.layout.observe();
  }

  private void fcaProcess() {
    InitLevel initLevel = null;
    boolean calcConcepts =
        commandLine.hasOption(CLI.CALC_CONCEPTS.getLongOpt())
            || commandLine.hasOption(CLI.CALC_NEIGHBORHOOD.getLongOpt())
            || commandLine.hasOption(CLI.CALC_LAYOUT.getLongOpt());
    boolean calcNeighborhood =
        commandLine.hasOption(CLI.CALC_NEIGHBORHOOD.getLongOpt())
            || commandLine.hasOption(CLI.CALC_LAYOUT.getLongOpt());
    boolean calcLayout = commandLine.hasOption(CLI.CALC_LAYOUT.getLongOpt());
    boolean calcImplications = commandLine.hasOption(CLI.CALC_IMPLICATIONS.getLongOpt());
    if (calcLayout)
      initLevel = InitLevel.LAYOUT;
    else if (calcNeighborhood)
      initLevel = InitLevel.LATTICE;
    else if (calcConcepts)
      initLevel = InitLevel.CONCEPTS;
    if (initLevel != null)
      fcaInstance.simpleInit(initLevel);
    if (calcImplications)
      fcaInstance.calcImplications();
  }

  private void fcaExport() {
    if (commandLine.hasOption(CLI.EXPORT.getLongOpt())) {
      fcaInstance.executor.submit(new BlockingTask("export") {

        @Override
        protected void _call() {
          final String[] value = commandLine.getOptionValues(CLI.EXPORT.getLongOpt());
          fcaInstance.export(FileFormat.valueOf(value[0].toUpperCase()), new File(value[1]));
        }
      });
    }
  }

  @Override
  protected void initializeListeners() {
    fcaInstance.lattice.addEventHandler(new RelationEventHandler<Concept<String, String>, Concept<String, String>>() {

      @Override
      public void handle(RelationEvent<Concept<String, String>, Concept<String, String>> event) {
        for (Concept<String, String> c : event.getRows())
          print(">>> concept >>> " + c);
      }
    }, RelationEvent.ROWS_ADDED);
//    fca.lattice.addEventHandler(new RelationEventHandler<Concept<String, String>, Concept<String, String>>() {
//
//      @Override
//      public void handle(RelationEvent<Concept<String, String>, Concept<String, String>> event) {
//        print(">>> all concepts changed >>>");
//        for (Concept<String, String> c : fca.lattice.rowHeads())
//          print(">>> concept >>> " + c);
//      }
//    }, RelationEvent.ALL_CHANGED);
    fcaInstance.implications.addListener(new ListChangeListener<Implication<String>>() {

      @Override
      public void onChanged(javafx.collections.ListChangeListener.Change<? extends Implication<String>> c) {
        if (c.wasAdded())
          for (Implication<String> i : c.getAddedSubList())
            print(">>> implication >>> " + i);
      }
    });
    fcaInstance.executor.overallProgressBinding.addListener(new ChangeListener<Number>() {

      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        print("current overall progress: " + (int) (newValue.doubleValue() * 100d));
      }
    });
    fcaInstance.executor.currentTaskProperty.addListener(new ChangeListener<BlockingTask>() {

      private final ChangeListener<String> messageListener = new ChangeListener<String>() {

                                                             @Override
                                                             public void changed(
                                                                 ObservableValue<? extends String> observable,
                                                                 String oldValue,
                                                                 String newValue) {
                                                               print(newValue);
                                                             }
                                                           };

      @Override
      public void changed(
          ObservableValue<? extends BlockingTask> observable,
          BlockingTask oldValue,
          BlockingTask newValue) {
        if (oldValue != null)
          oldValue.messageProperty().removeListener(messageListener);
        if (newValue != null)
          newValue.messageProperty().addListener(messageListener);
      }
    });
  }

  private final void print(final Object line) {
    System.out.println(line.toString());
  }
}
