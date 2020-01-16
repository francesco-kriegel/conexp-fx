package conexp.fx.cli;

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

import java.io.File;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import conexp.fx.core.algorithm.nextclosures.NextClosures2Bit;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.Pair;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.core.context.Implication;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.exporter.CXTExporter;
import conexp.fx.core.importer.CXTImporter;
import conexp.fx.gui.ConExpFX;

public final class CLI {

  public static void main(final String[] args) throws Exception {
    new CLI(args);
  }

  private final CommandLine                commandLine;
  private MatrixContext<String, String>    cxt;
  private Set<Concept<String, String>>     concepts;
  private Set<Implication<String, String>> implications;
  private ConceptLattice<String, String>   lattice;

  private CLI(final String[] args) throws Exception {
    super();
    try {
      commandLine = new DefaultParser().parse(OPTIONS, args);
    } catch (ParseException e) {
      printHelp();
      throw new RuntimeException("Unable to parse command line arguments. Please check supplied arguments!", e);
    }
    if (!commandLine.getArgList().isEmpty()) {
      printHelp();
      throw new RuntimeException("Unrecognized Arguments: " + commandLine.getArgList());
    }
    execute();
  }

  private final void printHelp() {
    new HelpFormatter().printHelp(
        120,
        "java -jar conexp-fx-VERSION-jar-with_dependencies.jar [OPTIONS]",
        "Available command line options for Concept Explorer FX",
        OPTIONS,
        "");
  }

  private final void execute() throws Exception {
    if (commandLine.getOptions().length == 0 || commandLine.hasOption(GUI.getLongOpt()))
      ConExpFX.main(new String[] {});
    else if (commandLine.hasOption(HELP.getLongOpt()))
      printHelp();
    else {
      final Consumer<Concept<String, String>> c1;
      final Consumer<Implication<String, String>> c2;
      final Consumer<String> c3;
      final Consumer<Double> c4;
      if (commandLine.hasOption(PRINT_TO_CONSOLE.getLongOpt())) {
        c1 = System.out::println;
        c2 = System.out::println;
        c3 = System.out::println;
        c4 = System.out::println;
      } else {
        c1 = __ -> {};
        c2 = __ -> {};
        c3 = __ -> {};
        c4 = __ -> {};
      }
      if (!commandLine.hasOption(IMPORT_CXT.getLongOpt()))
        throw new IllegalArgumentException(
            "Unable to instanciate FCA service without formal context. Please specify a file in Burmeister formatting by adding a command line prefix\r\n\t--importContextFromCXT <path_to_file>");
      final File input = new File(commandLine.getOptionValue(IMPORT_CXT.getLongOpt()));
      cxt = CXTImporter.read(input);
      if (commandLine.hasOption(CALC_CONCEPTS.getLongOpt()) || commandLine.hasOption(CALC_IMPLICATIONS.getLongOpt())
          || commandLine.hasOption(CALC_NEIGHBORHOOD.getLongOpt())) {
        final Pair<Set<Concept<String, String>>, Set<Implication<String, String>>> result =
            NextClosures2Bit.bitCompute(cxt, Executors.newWorkStealingPool(), c1, c2, c3, c4, () -> false);
        concepts = result.first();
        implications = result.second();
      }
      if (commandLine.hasOption(CALC_NEIGHBORHOOD.getLongOpt())) {
//        IPred.
      }
      if (commandLine.hasOption(WRITE_TO_FILE.getLongOpt())) {
        final String filename = input.getName().substring(0, input.getName().lastIndexOf("."));
        if (concepts != null)
          Collections3.writeToFile(
              new File(input.getParentFile(), filename + ".concepts"),
              concepts,
              "Formal Concepts of " + input.getAbsolutePath());
        if (implications != null)
          Collections3.writeToFile(
              new File(input.getParentFile(), filename + ".implications"),
              implications,
              "Implications of " + input.getAbsolutePath());
        if (lattice != null)
          CXTExporter.export(lattice, new File(input.getParentFile(), filename + ".lattice"));
      }
    }
  }

//  private final void test(final CommandLine commandLine) {
//    final String path = commandLine.getOptionValue(CLI.TEST.getLongOpt());
//    NextClosuresTest.run(path);
//    TestSuite.main(new String[] {});
////    -Xms256m
////    -Xmx2048m
////    -Djub.customkey=oracle-1.7.0_45
////    -Djub.consumers=CONSOLE,H2,XML
////    -Djub.db.file=benchmarks/.benchmarks
////    -Djub.xml.file=benchmarks/latest.xml    
//  }

  private final Options OPTIONS           = new Options();
  @SuppressWarnings("static-access")
  private final Option  HELP              = OptionBuilder
      .isRequired(false)
      .withLongOpt("showHelp")
      .hasArg(false)
      .withDescription("shows available command line options and their arguments.")
      .create("help");
  @SuppressWarnings("static-access")
  private final Option  IMPORT_CXT        = OptionBuilder
      .isRequired(false)
      .withLongOpt("importContextFromCXT")
      .hasArg(true)
      .withArgName("file")
      .withDescription("imports formal context file in Burmeister formatting (*.cxt)")
      .create("import");
  @SuppressWarnings("static-access")
  private final Option  CALC_CONCEPTS     = OptionBuilder
      .isRequired(false)
      .withLongOpt("calculateConcepts")
      .hasArg(false)
      .withDescription("computes all formal concepts")
      .create("concepts");
  @SuppressWarnings("static-access")
  private final Option  CALC_NEIGHBORHOOD = OptionBuilder
      .isRequired(false)
      .withLongOpt("calculateNeighborhood")
      .hasArg(false)
      .withDescription("computes the neighborhood relation")
      .create("lattice");
//  @SuppressWarnings("static-access")
//  private final Option  CALC_LAYOUT       = OptionBuilder
//                                              .isRequired(false)
//                                              .withLongOpt("calculateLayout")
//                                              .hasArg(false)
//                                              .withDescription("computes a concept layout")
//                                              .create("layout");
  @SuppressWarnings("static-access")
  private final Option  CALC_IMPLICATIONS = OptionBuilder
      .isRequired(false)
      .withLongOpt("calculateImplications")
      .hasArg(false)
      .withDescription("computes implicational base")
      .create("implications");
//  @SuppressWarnings("static-access")
//  private final Option  CALC_ASSOCIATIONS = OptionBuilder
//                                              .isRequired(false)
//                                              .withLongOpt("calculateAssociations")
//                                              .hasArg(true)
//                                              .withArgName("supp")
//                                              .withArgName("conf")
//                                              .withDescription("computes all association rules")
//                                              .create("associationrules");
  @SuppressWarnings("static-access")
  private final Option  PRINT_TO_CONSOLE  = OptionBuilder
      .isRequired(false)
      .withLongOpt("printToConsole")
      .hasArg(false)
      .withDescription("prints all results to console")
      .create("print");
  @SuppressWarnings("static-access")
  private final Option  WRITE_TO_FILE     = OptionBuilder
      .isRequired(false)
      .withLongOpt("writeToFile")
      .hasArg(false)
      // .hasArgs(2)
      // .withValueSeparator(' ')
      // .withArgName("format")
      // .withArgName("file")
      .withDescription("writes all results to files")
      .create("write");
  @SuppressWarnings("static-access")
  private final Option  GUI               = OptionBuilder
      .isRequired(false)
      .hasArg(false)
      .withDescription("starts the JavaFX gui")
      .withLongOpt("startGUI")
      .create("gui");
//  @SuppressWarnings("static-access")
//  private final Option  TEST              = OptionBuilder
//                                                       .isRequired(false)
//                                                       .hasArg(true)
//                                                       .withArgName("path")
//                                                       .withDescription("runs the test suite")
//                                                       .withLongOpt("runTestSuite")
//                                                       .create("test");
//  @SuppressWarnings("static-access")
//  private final Option  BENCHMARK         = OptionBuilder
//                                                       .isRequired(false)
//                                                       .hasArg(true)
//                                                       .withArgName("path")
//                                                       .withDescription("runs the benchmark suite")
//                                                       .withLongOpt("runBenchmarkSuite")
//                                                       .create("bench");

  {
    OPTIONS.addOption(GUI);
    OPTIONS.addOption(HELP);
    OPTIONS.addOption(IMPORT_CXT);
    OPTIONS.addOption(CALC_CONCEPTS);
    OPTIONS.addOption(CALC_NEIGHBORHOOD);
//    OPTIONS.addOption(CALC_LAYOUT);
    OPTIONS.addOption(CALC_IMPLICATIONS);
//    OPTIONS.addOption(CALC_ASSOCIATIONS);
    OPTIONS.addOption(PRINT_TO_CONSOLE);
    OPTIONS.addOption(WRITE_TO_FILE);
//    OPTIONS.addOption(TEST);
//    OPTIONS.addOption(BENCHMARK);
  }
}
