#!/bin/bash
shopt -s globstar

start="../btn100-master-geopackage/transformaciones-shape"

for f in $start/**/*.gpkg ; do
    if [ $(stat -c%s "$f") -ge 100000000 ]; then
        git add ${f%.*}*.rar
    else
        git add $f
    fi
done
