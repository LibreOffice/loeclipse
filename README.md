# LOEclipse

## Introduction

This plugin helps with developing and debugging LibreOffice extensions/components from Eclipse.

Please visit the http://libreoffice.github.io/loeclipse/ for installation instructions and user documentation.

Only read further if you want to work on LOEclipse itself.

## Development

### Prerequisites

* Ubuntu: Install `libreoffice`,`libreoffice-dev` and `libreoffice-java-common`.
* Windows: Install LibreOffice and the LibreOffice SDK from http://www.libreoffice.org/download
* macOS: No support for macOS currently (see bug #54)

### Setup Eclipse for development

1. Install *[Eclipse IDE for Eclipse Committers](http://www.eclipse.org/downloads/packages/)*. The *Eclipse IDE for Java Developers* will **not** work.
2. Clone this repository
3. Import the three projects `build`, `core` and `java` (using *File->Import->General->Existing projects into workspace*)
4. Add the LibreOffice jars to the build path
   * Go to *Window->Preferences* in Eclipse and open the *Java->Build path->User Libraries* configuration page.
   * Add a new library named `LO-Classes`
   * Select the Library, click *Add External Jars*
   * Add the LibreOffice jars `unoil.jar juh.jar jurt.jar ridl.jar unoloader.jar`. You find them here:
     * macOS: `/Applications/LibreOffice.app/Contents/Resources/java`
     * Ubuntu: `/usr/lib/libreoffice/program/classes`
     * Windows: `C:\Program Files[ (x86)]\LibreOffice 5\program\classes\`
   * Now there should be no more project errors.
5. Go to *Run->Run Configurations*, and create a new run configuration of the type *Eclipse Application*. Now you can run or debug the LOEclipse plugin using this run configuration.

### Getting help

Join [#libreoffice-dev on freenode](https://irc.documentfoundation.org/?settings=#libreoffice-dev) to get in touch with LibreOffice and LOEclipse developers.

You can also write to the [LibreOffice development mailing list](https://lists.freedesktop.org/mailman/listinfo/libreoffice) if you have trouble working on this project.

## Release Management

After releasing a new version, we need to build the update site. To do that, run the following command in the `build` folder:

`ant -Dlibreoffice.home=... -Declipse.home=...`

where `libreoffice.home` is the path to the LibreOffice installation and `eclipse.home` the path to the eclipse installation (needs to contain a `plugins` and a `features` directory).

You can also persist the options by setting the `ANT_ARGS` variable to

`-Dlibreoffice.home=... -Declipse.home=...`.

### Available build targets
Run `ant help` to see the available build targets.
