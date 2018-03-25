# Development
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

### Requirements
* Java 8 (developed and tested with latest Oracle JDK)
* Scala 2.12
* Maven 3
* Git