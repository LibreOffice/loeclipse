## Introduction

This plugin helps with developing and debugging LibreOffice extensions/components from Eclipse.
It supports Java as well as Python extensions.

Requirements:
* LibreOffice 5.0 or greater
* Eclipse 4.5 "Mars" or greater

For historic releases supporting older versions of LibreOffice (and OpenOffice) visit [this site](http://bosdonnat.fr/pages/libreoffice-eclipse.html).

## Installation

### Prerequisites

* LibreOffice (5.0 or greater)
  * Ubuntu: Install `libreoffice`,`libreoffice-dev` and `libreoffice-java-common`.
  * Windows: Install LibreOffice and the LibreOffice SDK from https://www.libreoffice.org/download
  * macOS: No support for macOS currently (see bug [#54](https://github.com/LibreOffice/loeclipse/issues/54))
* Eclipse (4.5 "Mars" or greater)
  * Install "Eclipse IDE for Java Developers" from https://www.eclipse.org/downloads/packages/


Note that PyDev is required if you want to have (optional) Python extension support. You need to [install](https://marketplace.eclipse.org/content/pydev-python-ide-eclipse) that first manually.

### Installing LOEclipse

Install this plugin via the [Eclipse Marketplace](https://marketplace.eclipse.org/content/loeclipse). Drag the button below to your running Eclipse instance.

<a href="http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=2881446" class="drag" title="Drag to your running Eclipse* workspace. *Requires Eclipse Marketplace Client"><img typeof="foaf:Image" class="img-responsive" src="https://marketplace.eclipse.org/sites/all/themes/solstice/public/images/marketplace/btn-install.png" alt="Drag to your running Eclipse* workspace. *Requires Eclipse Marketplace Client" /></a>

You can choose to install Java or Python support or both.


## Usage

After installing the plugin, you can create new extensions/components by using *File->New->Project->LibreOffice*.

You can also have a look at the [LibreOffice Starter Extension](https://github.com/smehrbrodt/libreoffice-starter-extension). It contains a step-by-step guide to get started with Java extension development.

You find more resources for extension development on https://wiki.documentfoundation.org/Development/Extension_Development

## Features

* General
  * Wizards to create new projects, service or interface files
  * Projects can be deployed directly into your LibreOffice instance
  * Deployed extensions can be debugged right in Eclipse
* Java
  * Supported project types: Extensions, Components, URE Applications
  * Generate ant build script to build the extension without Eclipse
* Python
  * Supported project type: Extensions
  * Requires PyDev
