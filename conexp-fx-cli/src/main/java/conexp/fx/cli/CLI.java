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
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import conexp.fx.core.service.FCAService;

public class CLI {

  public static final FCAService fcaService = new FCAService();

  public static void main(final String[] args) throws RuntimeException {
    try {
      final CommandLine commandLine = parseArgs(args);
      printUnrecognizedOptions(commandLine);
      if (commandLine.hasOption(HELP.getLongOpt()))
        printHelp();
      else
        runCLI(commandLine);
    } catch (ParseException e) {
      throw new RuntimeException("Unable to parse command line arguments. Please check supplied arguments!", e);
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
                                                       .withDescription("computes all implications")
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
  }
}
