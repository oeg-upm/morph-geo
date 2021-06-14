#!/bin/bash
shopt -s globstar
start="../btn100-master-geopackage/transformaciones-shape"
echo "converting shp files to gpkg..."
for f in $start/**/*.shp ; do
    echo "$f"
    ogr2ogr -f "GPKG" "${f%.*}.gpkg" "$f" -nln Table -nlt MULTIPOLYGON
done
echo "done"

echo "converting ktr to gpkg..."
for f in $start/**/*.ktr ; do
    echo "$f"
    sed -e 's/<from>shp/<from>gpkg/g' \
        -e 's/<name>shp/<name>gpkg/g' \
        -e 's/<inputFormat>ESRI_SHP/<inputFormat>GEOPACKAGE/g' \
        -e 's:<key>FORCE_TO_2D:<key>DB_TABLE_NAME</key> <value>Table</value> </param> <param> <key>FORCE_TO_2D:g' \
        -e 's:shp</inputFileName>:gpkg</inputFileName>:g' \
        -e 's/<encoding>ISO-8859-1/<encoding>UTF-8/g' \
        -i $f
done
echo "done"
