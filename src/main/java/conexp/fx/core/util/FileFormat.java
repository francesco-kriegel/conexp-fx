package conexp.fx.core.util;

/*
 * #%L
 * Concept Explorer FX - Core
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
import java.util.Arrays;
import java.util.stream.Collectors;

import javafx.stage.FileChooser.ExtensionFilter;
import conexp.fx.core.collections.pair.Pair;

public enum FileFormat {
  ANY("All Files", "*"),
  CXT("Burmeister Format (Only Context)", "cxt"),
  CEX("ConExp Format (Only Context)", "cex"),
  CFX("ConExpFX Format (Context & Lattice)", "cfx"),
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
    this.extensionFilter =
        new ExtensionFilter(title + suffixes, Arrays
            .asList(suffix)
            .parallelStream()
            .map(s -> "*." + s)
            .collect(Collectors.toList()));
  }

  public static final Pair<File, FileFormat> of(final File file) {
    final String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
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
