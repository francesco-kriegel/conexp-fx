package conexp.fx.core.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import conexp.fx.core.collections.Pair;
import javafx.stage.FileChooser.ExtensionFilter;

public enum FileFormat {
  ANY("All Files", "*"),
  CXT("Burmeister Format (Only Context)", "cxt"),
  CEX("ConExp Format (Only Context)", "cex"),
  CFX("ConExpFX Format (Context & Lattice)", "cfx"),
  CSVB("Comma Separated Values (pairs)", "csv"),
  CSVT("Comma Separated Values (triples)", "csv"),
  RDF("RDF Format (Graph Data)", "rdf", "rdfs", "owl", "xml"),
  NT("N-Triples", "nt"),
  N3("N3", "n3"),
  TTL("Turtle", "ttl"),
  TRIX("TriX", "xml", "trix"),
  TRIG("TriG", "trig"),
  BRDF("Binary RDF", "brf"),
  NQUADS("N-Quads", "nq"),
  JSONLD("JSON-LD", "jsonld"),
  RDFJSON("RDF-JSON", "rj"),
  RDFA("RDFa", "xhtml"),
  HTML("Hypertext Markup Language (Only Context)", "html"),
  PDF("Portable Document Format (Only Lattice)", "pdf"),
  PNG("Portable Network Graphics (Only Lattice)", "png"),
  SVG("Scalable Vector Graphics (Only Lattice)", "svg"),
  TEX("Ganter's fca.sty TeX Format (Context & Lattice)", "tex");

  public final String          title;
  public final String[]        suffix;
  public final ExtensionFilter extensionFilter;

  private FileFormat(final String title, final String... suffix) {
    this.title = title;
    this.suffix = suffix;
    String suffixes = "";
    for (String s : suffix)
      suffixes += ", *." + s;
    this.extensionFilter = new ExtensionFilter(
        title + suffixes,
        Arrays.asList(suffix).parallelStream().map(s -> "*." + s).collect(Collectors.toList()));
  }

  @Override
  public String toString() {
    String suffixes = "";
    final Iterator<String> it = Arrays.asList(suffix).iterator();
    if (it.hasNext())
      suffixes += "*." + it.next();
    while (it.hasNext())
      suffixes += ", *." + it.next();
    return title + " [" + suffixes + "]";
  }

  public static final Pair<File, FileFormat> of(final File file) {
    final String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
    if (suffix.toLowerCase().equals("csv")) {
      try {
        final Optional<String> firstNonEmptyLine = Files
            .lines(file.toPath())
            .map(String::trim)
            .filter(((Predicate<String>) String::isEmpty).negate())
            .findAny();
        if (firstNonEmptyLine.isPresent() && Strings.countOccurences(firstNonEmptyLine.get(), ";") < 2)
          return Pair.of(file, CSVB);
        else
          return Pair.of(file, CSVT);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    for (FileFormat ff : FileFormat.values())
      if (ff != ANY)
        for (String suf : ff.suffix)
          if (suf.equals(suffix))
            return Pair.of(file, ff);
    return Pair.of(file, ANY);
  }

  public static final Pair<File, FileFormat> of(final File file, final FileFormat... fileFormats) {
    final String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
    for (FileFormat ff : fileFormats)
      if (ff != ANY)
        for (String suf : ff.suffix)
          if (suf.equals(suffix))
            return Pair.of(file, ff);
    return Pair.of(file, ANY);
  }
}
