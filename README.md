# LOEclipse

This plugin helps with developing and debugging LibreOffice extensions/components from Eclipse.

This release supports only LibreOffice 5.0 and upwards (due to some changed internal paths).
It was tested with Eclipse Mars (4.5). Older versions might still work, but are untested.

For historic releases supporting older versions of LibreOffice (and OpenOffice) visit [this site](http://bosdonnat.fr/pages/libreoffice-eclipse.html).

## Installation

Install this plugin via the [Eclipse Marketplace](https://marketplace.eclipse.org/content/loeclipse). Drag the button below to your running Eclipse instance.

<a href="http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=2881446" class="drag" title="Drag to your running Eclipse workspace to install LOEclipse"><img class="img-responsive" src="https://marketplace.eclipse.org/sites/all/themes/solstice/public/images/marketplace/btn-install.png" alt="Drag to your running Eclipse workspace to install LOEclipse" /></a>

After installing the plugin, you can create new extensions/components by using *File->New->Project->LibreOffice*.

You can also have a look at the [LibreOffice Starter Extension](https://github.com/smehrbrodt/libreoffice-starter-extension). It contains a step-by-step guide to get started with extension development.

## Documentation

Developer and User Documentation is found in [core/doc/help/html](https://github.com/LibreOffice/loeclipse/tree/master/core/doc/help/html).

## Development

### Prerequisites
In order to build LOEclipseIntegration to produce an update site, you need to have LibreOffice, the LibreOffice SDK and Eclipse installed.

* Ubuntu: Install `libreoffice`,`libreoffice-dev` and `libreoffice-java-common`.
* Windows/macOS: Install LibreOffice and the LibreOffice SDK from http://www.libreoffice.org/download

### Setup Eclipse for development

1. Install *Eclipse IDE for Eclipse Committers*. The *Eclipse IDE for Java Developers* will **not** work.
2. Clone this repository
3. Import the three projects `build`, `core` and `java` (using *File->Import->General->Existing projects into workspace*)
4. Go to *Window->Preferences* in Eclipse and open the *Java->Build path->User Library* configuration page. Then add a new library named `LO-Classes` and add the following jars (via *Add External Jars*) from the LibreOffice installation (found in `program/classes`): `unoil.jar juh.jar jurt.jar ridl.jar unoloader.jar`. This is necessary in order to correctly build the sources.
5. Go to *Run->Run Configurations*, and create a new run configuration of the type *Eclipse Application*. Now you can run or debug the LOEclipse plugin using this run configuration.

## Release Management

After releasing a new version, we need to build the update site. To do that, run the following command in the `build` folder:

`ant -Dlibreoffice.home=... -Declipse.home=...`

where `libreoffice.home` is the path to the LibreOffice installation and `eclipse.home` the path to the eclipse installation (needs to contain a `plugins` and a `features` directory).

You can also persist the options by setting the `ANT_ARGS` variable to

`-Dlibreoffice.home=... -Declipse.home=...`.

### Available build targets
Run `ant help` to see the available build targets.

## Features
* UNO-IDL syntax highlighting
* SDK and LibreOffice Configuration
* Java code generation
* New UNO project wizard
* New UNO file wizard
* New UNO service wizard
* New UNO interface wizard
* Skeleton-maker integration for basic components creation
* URE configuration
* New URE application wizard
* Automatic component build and packaging
* Automatic deployment to an existing LibreOffice installation
* Remote debugging using the Eclipse debugger
* Generate ant build script to build the extension without Eclipse
