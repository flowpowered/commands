# Flow Commands [![License](http://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat)][License] [![Flattr this](http://img.shields.io/badge/flattr-donate-lightgrey.svg?style=flat)][Donate] [![Build Status](http://img.shields.io/travis/flow/flow-commands/develop.svg?style=flat)](https://travis-ci.org/flow/flow-commands) [![Coverage Status](http://img.shields.io/coveralls/flow/flow-commands/develop.svg?style=flat)](https://coveralls.io/r/flow/flow-commands)

Command-line parsing library for building a tree of commands, in which each command can have positional arguments, flags, subcommands, and tab-completion. Support for central management of the commands' location in the tree is also available, which is useful if the commands' implementations are provided by third-party code at runtime - you can use it to better organize your command namespace and avoid name conflicts. It also supports interface-based, as well as annotated method-based command implementations.

## Getting Started
* [Examples and code snippets](https://github.com/flow/examples/tree/master/commands)
* [Official documentation](#documentation)
* [IRC support chat](http://kiwiirc.com/client/irc.esper.net/flow)
* [Issues tracker](https://github.com/flow/commands/issues)

## Source Code
The latest and greatest source can be found here on [GitHub](https://github.com/flow/commands). If you are using Git, use this command to clone the project:

    git clone git://github.com/flow/commands.git

Or download the latest [development archive](https://github.com/flow/commands/archive/develop.zip) or the latest [stable archive](https://github.com/flow/commands/archive/master.zip).

## Dependencies
We love open-source libraries! This project uses are few of them to make things easier. If you aren't using Maven or Gradle, you'll need these!
* [com.flowpowered:flow-persistence](https://oss.sonatype.org/#nexus-search;gav~com.flowpowered~flow-persistence~~~)
* [com.flowpowered:flow-chat](https://oss.sonatype.org/#nexus-search;gav~com.flowpowered~flow-chat~~~)
* [com.flowpowered:flow-commons](https://oss.sonatype.org/#nexus-search;gav~com.flowpowered~flow-commons~~~)
* [com.google.guava:guava](https://oss.sonatype.org/#nexus-search;gav~com.google.guava~guava~~~)

## Building from Source
This project can be built with the _latest_ [Java Development Kit](http://oracle.com/technetwork/java/javase/downloads) and [Maven](http://maven.apache.org/) or [Gradle](http://www.gradle.org/). Maven and Gradle are used to simplify dependency management, but using either of them is optional.

For Maven, the command `mvn clean package` will build the project and will put the compiled JAR in `target`, and `mvn clean install` will copy it to your local Maven repository.

For Gradle, the command `gradlew` will build the project and will put the compiled JAR in `~/build/distributions`, and `gradlew install` will copy it to your local Maven repository.

## Contributing
Are you a talented programmer looking to contribute some code? We'd love the help!

* Open a pull request with your changes, following our [guidelines and coding standards](CONTRIBUTING.md).
* Please follow the above guidelines for your pull request(s) accepted.
* For help setting up the project, keep reading!

Love the project? Feel free to [donate] to help continue development! Flow projects are open-source and powered by community members, like yourself. Without you, we wouldn't be here today!

Don't forget to watch and star our repo to keep up-to-date with the latest Flow development!

## Usage
If you're using [Maven](http://maven.apache.org/download.html) to manage project dependencies, simply include the following in your `pom.xml` file:

    <dependency>
        <groupId>com.flowpowered</groupId>
        <artifactId>flow-commands</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </dependency>

If you do not already have the required repo in your repository list, you will need to add this as well:

    <repository>
        <id>sonatype-nexus</id>
        <url>https://oss.sonatype.org/content/groups/public</url>
    </repository>

If you're using [Gradle](https://www.gradle.org/) to manage project dependencies, simply include the following in your `build.gradle` file:

    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            name = 'sonatype-nexus'
            url = 'https://oss.sonatype.org/content/groups/public/'
        }
    }
    dependencies {
        compile 'com.flowpowered:flow-commands:0.1.0-SNAPSHOT'
    }

If you'd prefer to manually import the latest .jar file, you can get it [here](https://github.com/flow/commands/releases).

## Documentation
Want to get friendly with the project and put it to good use? Check out the latest [Javadocs](https://flowpowered.com/commands).

To generate Javadocs with Maven, use the `mvn javadoc:javadoc` command. To view the Javadocs simply go to `target/site/apidocs/` and open `index.html` in a web browser.

To generate Javadocs with Gradle, use the `gradlew javadoc` command. To view the Javadocs simply go to `build/docs/javadoc/` and open `index.html` in a web browser.

## Version Control
We've adopted the [git flow branching model](http://nvie.com/posts/a-successful-git-branching-model/) in our projects. The creators of git flow released a [short intro video](http://vimeo.com/16018419) to explain the model.

The `master` branch is production-ready and contains the latest tagged releases. Before a release is made, it is stagged in `release/x` branches before being pushed and tagged in the `master` branch. Small patches from `hotfix/x` branches are also pushed to `master`, and will always have a release version. The `develop` branch is pre-production, and is where we push `feature/x` branches for testing.

## Legal Stuff
Flow Commands is licensed under the [MIT License][License]. Basically, you can do whatever you want as long as you include the original copyright. Please see the `LICENSE.txt` file for details.

## Credits
* [Spout](https://spout.org/) and contributors - *where we all began, and for much of the re-licensed code.*
* All the people behind [Java](http://www.oracle.com/technetwork/java/index.html), [Maven](https://maven.apache.org/), and [Gradle](https://www.gradle.org/).

[Donate]: https://flattr.com/submit/auto?user_id=spout&url=https://github.com/flow/commands&title=Flow+Commands&language=Java&tags=github&category=software
[License]: https://tldrlegal.com/l/mit
