# Concept Explorer FX (conexp-fx) ![Continuous Integration Status](https://travis-ci.org/francesco-kriegel/conexp-fx.svg "Continuous Integration Status")
Copyright ⓒ 2010-2015 Francesco Kriegel

## Requirements
* Java 8 (developed and tested with latest Oracle JDK)
* Maven 3
* Git

## Supported Platforms
* Linux
* Mac OS
* Windows

## Features
Most time-consuming algorithms use parallel threads to shorten computation time.  
Please note, that currently not all features are available from the graphical user interface and from the command line interface.

### Formal Concept Analysis
* Data model for handling formal contexts
* Algorithms for computing concept lattices:
    * set of all formal concepts (NextClosure and NextClosures),
    * and its neighborhood relation (iPred)
* Algorithm for computing attribute-incremental changes in concept lattices (iFox)
* A genetic algorithm for computing additive diagrams of concept lattices (using conflict distance and chain decompositions)
* Algorithms for computing (constrained) implicational bases (NextClosure, NextClosures and ConstrainedNextClosures)
* GUI components for editing formal contexts and their concept lattices
* Importer for various formats (CEX, CFX, CSV, CXT, SPARQL)
* Exporter for various formats (CFX, CXT, HTML, PDF, PNG, SVG, TeX)
* GUI supports LaTeX expressions in formal context objects and attributes

### Description Logics
* Data model for handling interpretations
* Algorithms for computing model-based most-specific concept descriptions (in EL and ALQ)
* Algorithms for computing bases of general concept inclusions for finite interpretations (in EL and ALQ), also relative to a background ontology
* Interacts with Sesame, OWL API, ELK and HermiT
* Import of RDF data
* Export of OWL ontologies
* Basic support for exporting LaTeX

## Usage
To run Concept Explorer FX with little effort, you can use the Java WebStart bundles, that are installed on my homepage at
(http://lat.inf.tu-dresden.de/~francesco/conexp-fx/conexp-fx.html). Please make sure that your browser has the latest Java WebStart plugin installed.

In case you do not want to run the software within your browser, there is also the option of downloading the latest release for your platform,
either from this GitHub repository, or from the download site at (http://lat.inf.tu-dresden.de/~francesco/conexp-fx/download.html)

The source code Concept Explorer FX can be used in at least three ways.  
1. Add it as a Maven dependency to your Java project.  
2. Start the graphical user interface (class `conexp.fx.gui.ConExpFX`).  
3. Use the command line interface (class `conexp.fx.cli.CLI`).  

## Documentation
* Short Documentation: (http://lat.inf.tu-dresden.de/~francesco/conexp-fx/conexp-fx.html)
* JavaDoc: (http://lat.inf.tu-dresden.de/~francesco/conexp-fx/apidocs/index.html)

## License
Copyright ⓒ 2010-2015 Francesco Kriegel

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
