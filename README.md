# LOEclipse

This plugin helps with developing and debugging LibreOffice extensions from Eclipse.

More information can be found on [this site](http://bosdonnat.fr/pages/libreoffice-eclipse.html).

## Installation

Using update sites
* Launch eclipse
* Help->Software updates->Find & install
* Select the "Add new features" option
* Add a new update site pointing to one of:
  * http://cedric.bosdonnat.free.fr/ooeclipseintegration/ (to install a released version)
  * the `site` directory (when building from source)
* Now click Next and follow the wizard's instructions

## Development

In order to build LOEclipseIntegration to produce an update site, you need to have LibreOffice and Eclipse installed. Note that on Linux, you might need to install extra dependencies (`libreoffice-java-common` on Ubuntu).

After checkout, run the following command in the `build` folder:

`ant -Dlibreoffice.home=... -Declipse.home=...`

where `libreoffice.home` is the path to the LibreOffice installation and `eclipse.home` the path to the eclipse installation (needs to contain a `plugins` and a `features` directory).

You can also persist the options by setting the `ANT_ARGS` variable to

`-Dlibreoffice.home=... -Declipse.home=...`.

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

**Planned features:**
 * Adding C++ language support
 * New UNO wizard for other IDL types
 * Easy support of several services implementations in a component
 * Outline view for UNOIDL file
 * Automatic deployment to an existing LibreOffice installation
 * Remote debugging using the Eclipse debugger
 * Adding Python language support
