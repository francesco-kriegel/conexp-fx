# Concept Explorer FX (conexp-fx) 
<!--- ![Continuous Integration Status](https://travis-ci.org/francesco-kriegel/conexp-fx.svg "Continuous Integration Status") --->
Copyright ⓒ 2010-2018 Francesco Kriegel

## Requirements
* Java 8 (developed and tested with latest Oracle JDK)
* Scala 2.12
* Maven 3
* Git

## Supported Platforms
* Linux
* Mac OS
* Windows

## Features
Most time-consuming algorithms use parallel threads to shorten computation time.  
Please note that currently not all features are available from the graphical user interface and from the command line interface.

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
To run Concept Explorer FX with little effort, you can use the Java WebStart bundles which are installed on my homepage at
(http://lat.inf.tu-dresden.de/~francesco/conexp-fx/conexp-fx.html). Please make sure that your browser has the latest Java WebStart plugin installed. The security warning can be deactivated by importing the certificate `conexp-fx.cer` to the Trusted CAs.

### Import Certificate to Trusted CAs
* Use keytool to import it to trusted.cacerts.
	command: `keytool -import -alias conexp-fx -keystore PATH_TO_TRUSTED_CACERTS`
  On Mac OS X, trusted.cacerts is located in `~/Library/Application Support/Oracle/Java/Deployment/security/trusted.cacerts`.
  On Windows, trusted.cacerts is located in `%USERPROFILE%\AppData\LocalLow\Sun\Java\Deploymenr\security\trusted.cacerts`.
* Use Java Control Panel.
	Security -> Manage Certificates... -> Certificate Type: Signer CA -> Import

In case you do not want to run the software within your browser, there is also the option of downloading the latest release for your platform,
either from this GitHub repository, or from the download site at (http://lat.inf.tu-dresden.de/~francesco/conexp-fx/download.html)

The source code of Concept Explorer FX can be used in at least three ways.
1. Add it as a Maven dependency to your Java project.  
2. Start the graphical user interface (class `conexp.fx.gui.ConExpFX`).  
3. Use the command line interface (class `conexp.fx.cli.CLI`).  

## Documentation
* Short Documentation: (http://lat.inf.tu-dresden.de/~francesco/conexp-fx/conexp-fx.html)
* JavaDoc: (http://lat.inf.tu-dresden.de/~francesco/conexp-fx/apidocs/index.html)

## Development
### Installation of Non-Maven-Dependencies and JavaFX-JavaDoc and JavaFX-Source to Local Maven Repository
1. Set `javafx.version` property to your installed Java version.
2. Download JavaFX-JavaDoc package to `lib` folder.
3. Run command `mvn validate -Pinstall-dependencies` in project's root directory.

### Project Lifecycle
The project lifecycle sticks to the default maven lifecycle.
* validate
* compile test-compile
* package
* install
* deploy
* release:prepare release:perform (release:rollback)

## License
Concept Explorer FX (conexp-fx)

Copyright ⓒ 2010-2018 Francesco Kriegel

You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
