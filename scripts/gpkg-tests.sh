#!/bin/bash
shopt -s globstar

if [ $# -ne 2 ]; then
    echo "Incorrect arguments"
    echo "  gpkg-tests.sh - run ktr transformations and/or check ttl results"
    echo "  gpkg-tests.sh [run/check] [DIRECTORY]"
    exit
fi

#start="../btn100-master-geopackage/transformaciones-shape/1-UnidadesAdministrativas/2-ZonaProtegida"
start="$2"

if [ $1 == "run" ]; then
    rm gpkg-tests-pan.log
    echo "counting transformations..."
    i=0
    for f in $start/**/*.ktr ; do
        i=$((i+1))
    done
    echo "$i ktr files to process"

    j=0
    echo "running gpkg transformations..."
    for f in $start/**/*.ktr ; do
        pan -file=$f >> gpkg-tests-pan.log 2>&1 
        j=$((j+1))
        echo "$j/$i $f"
    done
    echo "done"
fi

echo "finding differences in gpkg transformation results..."
for f in $start/**/*.ttl ; do
    cmp $f $(sed 's/btn100-master-geopackage/btn100-master/g' <<< $f)
done
echo "done"
