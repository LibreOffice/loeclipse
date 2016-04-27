# LOEclipse

This plugin helps with developing and debugging LibreOffice extensions/components from Eclipse.

This release supports only LibreOffice 5.0 and upwards (due to some changed internal paths).
It was tested with Eclipse Mars (4.5). Older versions might still work, but are untested.

For historic releases supporting older versions of LibreOffice (and OpenOffice) visit [this site](http://bosdonnat.fr/pages/libreoffice-eclipse.html).

## Installation

Install this plugin via an update site in Eclipse pointing to one of:
* `http://eclipse-plugins.libreoffice.org` (to install a released version)
* the `site` directory (when building from source)

After installing the plugin, you can create new extensions/components by using *File->New->Project->LibreOffice*.

## Getting started

Have a look at the [LibreOffice Starter Extension](https://github.com/smehrbrodt/libreoffice-starter-extension). It contains a step-by-step guide to get started with extension development.

## Documentation

Developer and User Documentation is found in [core/doc/help/html](https://github.com/LibreOffice/loeclipse/tree/master/core/doc/help/html).

## Development

### Prerequisites
In order to build LOEclipseIntegration to produce an update site, you need to have LibreOffice, the LibreOffice SDK and Eclipse installed.

On Ubuntu for example you need `libreoffice-dev` and `libreoffice-java-common`.

Also you need to add some jar files to the build path in Eclipse. Select the *Window->Preferences* menu in Eclipse and open the *Java->Build path->User Library* configuration page. Then add a new library named `LO-Classes` and add the following jars from the LibreOffice installation (found in `program/classes`): `unoil.jar juh.jar jurt.jar ridl.jar unoloader.jar`. This is necessary in order to correctly build the sources.

### Working with Eclipse
You can run and debug this extension with Eclipse. Just import the projects using *File->Import* and run/debug the project as an *Eclipse installation*.

### Building an update site
To build an update site, run the following command in the `build` folder:

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

**Nice to have features:**
 * Adding C++ language support
 * Adding Python language support
 * New UNO wizard for other IDL types
 * Easy support of several services implementations in a component
 * Outline view for UNOIDL file
