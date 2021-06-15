#!/bin/bash
shopt -s globstar

if [ $# -ne 1 ]; then
    echo "Incorrect arguments"
    echo "  rar/unrar the gpkg files in the btn100-master-geopackage folder"
    echo "  to circumvent git's file size restriction"
    echo "  gpkg-rar.sh [rar/unrar]"
    exit
fi

start="../btn100-master-geopackage/transformaciones-shape"

count_files() {
    echo "counting $1 files..."
    i=0
    for f in $start/**/*.$1 ; do
        i=$((i+1))
    done
    echo "$i $1 files to process..."
    sleep 2
}


if [ $1 == "rar" ]; then
    count_files gpkg
    for f in $start/**/*.gpkg ; do
        rar a -v90000k ${f%.*}.rar $f
    done
    exit
fi

if [ $1 == "unrar" ]; then
    count_files rar
    for f in $start/**/*.part1.rar ; do
        unrar x $f
    done
    exit
fi
