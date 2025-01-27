# LOEclipse

## Introduction

This plugin helps with developing and debugging LibreOffice extensions/components from Eclipse.

Please visit the https://libreoffice.github.io/loeclipse/ for installation instructions and user documentation.

Only read further if you want to work on LOEclipse itself.

## Development

### Prerequisites

*LibreOffice 7.0 or newer is required.*

* Ubuntu: Install `libreoffice`,`libreoffice-dev` and `libreoffice-java-common`.
* Windows: Install LibreOffice and the LibreOffice SDK from https://www.libreoffice.org/download
* macOS: No support for macOS currently (see [bug #54](https://github.com/LibreOffice/loeclipse/issues/54))

### Setup Eclipse for development

1. Install *[Eclipse IDE for Eclipse Committers](https://www.eclipse.org/downloads/packages/)*. The *Eclipse IDE for Java Developers* will **not** work.
2. Install [PyDev](https://marketplace.eclipse.org/content/pydev-python-ide-eclipse) (from Help->Eclipse Marketplace)
3. Clone this repository
4. Import the three projects `build`, `core` and `java` (using *File->Import->General->Existing projects into workspace*)
5. Add the LibreOffice jars to the build path
   * Go to *Window->Preferences* in Eclipse and open the *Java->Build path->User Libraries* configuration page.
   * Add a new library named `LO-Classes`
   * Select the Library, click *Add External Jars*
   * Add the LibreOffice jars `libreoffice.jar unoil.jar`. You find them here:
     * macOS: `/Applications/LibreOffice.app/Contents/Resources/java`
     * Ubuntu: `/usr/lib/libreoffice/program/classes`
     * Windows: `C:\Program Files[ (x86)]\LibreOffice 5\program\classes\`
   * Now there should be no more project errors.
6. Go to *Run->Run Configurations*, and create a new run configuration of the type *Eclipse Application*. Now you can run or debug the LOEclipse plugin using this run configuration.

### Getting help

Please open an issue if you experience any problems while working on this project.

## Release Management

### Release new version

* Update `CHANGELOG.md`
* Bump version in `core/META-INF/MANIFEST.MF` `java/META-INF/MANIFEST.MF` `python/META-INF/MANIFEST.MF`
  * Use [Semantic Versioning](https://semver.org/) for version number bumps
* Commit changes with message "Release x.y.z"
* Create a tag: `git tag vX.Y.Z` (e.g. `git tag v4.0.2`)
* Push changes: `git push && git push --tags`
* Create a new release on https://github.com/LibreOffice/loeclipse/releases
  * Choose previously created tag
  * Release title is `vx.y.z` (e.g. v4.0.2)
  * Copy the changelog as release notes

### Publish new version

After releasing a new version, we need to build the update site. To do that, run the following command in the `build` folder:

`ant -Dlibreoffice.home=... -Declipse.home=...`

where `libreoffice.home` is the path to the LibreOffice installation and `eclipse.home` the path to the eclipse installation (needs to contain a `plugins` and a `features` directory).

You can also persist the options by setting the `ANT_ARGS` variable to

`-Dlibreoffice.home=... -Declipse.home=...`.

### Available build targets

Run `ant help` to see the available build targets.
