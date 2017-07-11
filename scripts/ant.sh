#!/usr/bin/env bash

cd build
ant purge
ant -Dlibreoffice.home=/usr/lib/libreoffice -Declipse.home=~/Programme/eclipse/ -v

