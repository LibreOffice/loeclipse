# LOEclipse

This plugin helps with developing and debugging LibreOffice extensions from Eclipse.

## Installation

Installation instructions can be found on [this site](http://bosdonnat.fr/pages/libreoffice-eclipse.html).

## Development

After checkout, run the following command in the `build` folder:

`ant -Dlibreoffice.home=... -Declipse.home=...`

where `libreoffice.home` is the path to the LibreOffice installation and `eclipse.home` the path to the eclipse installation (needs to contain a `plugins` and a `features` directory).

Run `ant help` for more options.
