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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import conexp.fx.core.algorithm.nextclosure.NextClosures6;
import conexp.fx.core.algorithm.nextclosure.NextClosures6.Result;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter2;
import conexp.fx.test.algorithm.nextclosure.NextClosuresTest;

public class CLI {

  public static void main(final String[] args) throws RuntimeException {
    try {
      final CommandLine commandLine = parseArgs(args);
      printUnrecognizedOptions(commandLine);
      if (commandLine.hasOption(HELP.getLongOpt()))
        printHelp();
      else if (commandLine.hasOption(CALC_IMPLICATIONS.getLongOpt()))
        computeImplications(commandLine);
      else if (commandLine.hasOption(TEST.getLongOpt()))
        test(commandLine);
      else
        runCLI(commandLine);
    } catch (ParseException e) {
      throw new RuntimeException("Unable to parse command line arguments. Please check supplied arguments!", e);
    }
  }

  private static final void test(final CommandLine commandLine) {
    final String path = commandLine.getOptionValue(CLI.TEST.getLongOpt());
    NextClosuresTest.run(path);
  }

  private static final void computeImplications(final CommandLine commandLine) {
    if (!commandLine.hasOption(IMPORT_CXT.getLongOpt()))
      throw new IllegalArgumentException(
          "Unable to instanciate FCA service without formal context. Please specify a file in Burmeister formatting by adding a command line prefix\r\n\t--importContextFromCXT <path_to_file>");
    final File input = new File(commandLine.getOptionValue(CLI.IMPORT_CXT.getLongOpt()));
    final MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    CXTImporter2.read(cxt, input);
    final long start = System.currentTimeMillis();
    final Result<String, String> implicationalBase = NextClosures6.compute(cxt, true);
    final long duration = System.currentTimeMillis() - start;
    final File output =
        new File(input.getParentFile(), input.getName().substring(0, input.getName().lastIndexOf(".")) + ".nxc");
    exportImplications("Result from NextClosures for " + input.getName() + "\n" + "computation time: " + duration
        + "ms", implicationalBase, output);
  }

  private static final void exportImplications(
      final String description,
      final Result<String, String> result,
      final File file) {
    System.out.println("writing to " + file.getAbsolutePath());
    FileWriter fw = null;
    BufferedWriter bw = null;
    try {
      fw = new FileWriter(file);
      bw = new BufferedWriter(fw);
      bw.append(description + "\n\n");
      bw.append(result.implications.size() + " implications:\n");
      for (Entry<Set<String>, Set<String>> imp : result.implications.entrySet())
        bw.append(imp.getKey() + " --> " + imp.getValue() + "\n");
      bw.append("\n");
      bw.append(result.concepts.size() + " concepts:\n");
      for (Concept<String, String> con : result.concepts)
        bw.append(con.intent() + " <x> " + con.extent() + "\n");
      bw.append("\n");
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        bw.close();
        fw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static final void runCLI(final CommandLine commandLine) throws IllegalArgumentException {
    if (!commandLine.hasOption(IMPORT_CXT.getLongOpt()))
      throw new IllegalArgumentException(
          "Unable to instanciate FCA service without formal context. Please specify a file in Burmeister formatting by adding a command line prefix\r\n\t--importContextFromCXT <path_to_file>");
    new CLIInstance(commandLine).run();
  }

  private static final CommandLine parseArgs(final String[] args) throws ParseException {
    final CommandLineParser parser = new BasicParser();
    final CommandLine commandLine = parser.parse(OPTIONS, args);
    return commandLine;
  }

  private static final void printUnrecognizedOptions(final CommandLine commandLine) {
    @SuppressWarnings("rawtypes")
    final List unrecognizedArgs = commandLine.getArgList();
    if (!unrecognizedArgs.isEmpty()) {
      System.err.println("Unrecognized Arguments: " + unrecognizedArgs);
      System.err.println();
    }
  }

  private static final void printHelp() {
    HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.printHelp(
        120,
        "java -jar conexp-fx-VERSION.jar [OPTIONS]",
        "Available command line options for Concept Explorer FX",
        OPTIONS,
        "");
  }

  protected static final Options OPTIONS           = new Options();
  @SuppressWarnings("static-access")
  protected static final Option  HELP              = OptionBuilder
                                                       .isRequired(false)
                                                       .withLongOpt("showHelp")
                                                       .hasArg(false)
                                                       .withDescription(
                                                           "shows available command line options and their arguments.")
                                                       .create("help");
  @SuppressWarnings("static-access")
  protected static final Option  IMPORT_CXT        =
                                                       OptionBuilder
                                                           .isRequired(false)
                                                           .withLongOpt("importContextFromCXT")
                                                           .hasArg(true)
                                                           .withArgName("file")
                                                           .withDescription(
                                                               "imports formal context file in Burmeister formatting (*.cxt)")
                                                           .create("fromCXT");
  @SuppressWarnings("static-access")
  protected static final Option  CALC_CONCEPTS     = OptionBuilder
                                                       .isRequired(false)
                                                       .withLongOpt("calculateConcepts")
                                                       .hasArg(false)
                                                       .withDescription("computes all formal concepts")
                                                       .create("bk");
  @SuppressWarnings("static-access")
  protected static final Option  CALC_NEIGHBORHOOD = OptionBuilder
                                                       .isRequired(false)
                                                       .withLongOpt("calculateNeighborhood")
                                                       .hasArg(false)
                                                       .withDescription("computes the neighborhood relation")
                                                       .create("bvk");
  @SuppressWarnings("static-access")
  protected static final Option  CALC_LAYOUT       = OptionBuilder
                                                       .isRequired(false)
                                                       .withLongOpt("calculateLayout")
                                                       .hasArg(false)
                                                       .withDescription("computes a concept layout")
                                                       .create("blk");
  @SuppressWarnings("static-access")
  protected static final Option  CALC_IMPLICATIONS = OptionBuilder
                                                       .isRequired(false)
                                                       .withLongOpt("calculateImplications")
                                                       .hasArg(false)
                                                       .withDescription("computes implicational base")
                                                       .create("imp");
//  @SuppressWarnings("static-access")
//  protected static final Option  CALC_ASSOCIATIONS = OptionBuilder
//                                                       .isRequired(false)
//                                                       .withLongOpt("calculateAssociations")
//                                                       .hasArg(true)
//                                                       .withArgName("supp")
//                                                       .withArgName("conf")
//                                                       .withDescription("computes all association rules")
//                                                       .create("ass");
  @SuppressWarnings("static-access")
  protected static final Option  PRINT_TO_CONSOLE  = OptionBuilder
                                                       .isRequired(false)
                                                       .withLongOpt("printToConsole")
                                                       .hasArg(false)
                                                       .withDescription("prints all results to console")
                                                       .create("print");
  @SuppressWarnings("static-access")
  protected static final Option  EXPORT            =
                                                       OptionBuilder
                                                           .isRequired(false)
                                                           .withLongOpt("export")
                                                           .hasArgs(2)
                                                           .withValueSeparator(' ')
                                                           .withArgName("format")
                                                           .withArgName("file")
                                                           .withDescription(
                                                               "exports labeled and layouted concept lattice to SVG document (*.svg)")
                                                           .create("out");
  @SuppressWarnings("static-access")
  protected static final Option  TEST              = OptionBuilder
                                                       .isRequired(false)
                                                       .hasArg(true)
                                                       .withArgName("path")
                                                       .withLongOpt("runTestSuite")
                                                       .create("test");

  static {
    OPTIONS.addOption(HELP);
    OPTIONS.addOption(IMPORT_CXT);
    OPTIONS.addOption(CALC_CONCEPTS);
    OPTIONS.addOption(CALC_NEIGHBORHOOD);
    OPTIONS.addOption(CALC_LAYOUT);
    OPTIONS.addOption(CALC_IMPLICATIONS);
//    OPTIONS.addOption(CALC_ASSOCIATIONS);
    OPTIONS.addOption(PRINT_TO_CONSOLE);
    OPTIONS.addOption(EXPORT);
    OPTIONS.addOption(TEST);
  }
}
