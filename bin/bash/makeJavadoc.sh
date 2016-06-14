#!/bin/bash

prefix="~"

if [ ! -z "$1" ]
then
    prefix="$1"
fi

javadoc -d ${prefix}/jtracer/docs/javadocs                \
        -sourcepath ${prefix}/jtracer/src/main/java       \
        -subpackages net.libcsdbg.jtracer

if [ "$?" -eq 0 ]
then
    echo "jTracer javadoc generation was successful"
    exit 0
else
    echo "jTracer javadoc generation failed"
    exit 1
fi
