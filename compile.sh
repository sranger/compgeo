#!/bin/sh

#set ANT_HOME="/path/to/ant/bin"
#set JAVA_HOME="/path/to/java/bin"
#set PATH="$PATH:$ANT_HOME:$JAVA_HOME"
#export PATH

ant dist

cp dist/com.stephenwranger.compgeo.jar ../com.stephenwranger.thesis/lib
