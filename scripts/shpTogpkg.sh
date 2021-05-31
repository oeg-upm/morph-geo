#!/bin/bash
shopt -s globstar
start="../btn100-master/transformaciones-shape/1-UnidadesAdministrativas"
for f in $start/**/*.shp ; do
    echo "f"
    ogr2ogr -f "GPKG" "${f%.*}.gpkg" "$f" -nln Table
done
