#!/bin/sh


##
# Java VM flags
JAVA_FLAGS='-Djava.net.useSystemProxies=true -Xmx2048m'

##
# The main class
MAIN='com.stephenwranger.compgeo.assignment1.Assignment1'

EXTRA_CLASSPATH='';

for JAR in $( find ./lib -name '*.jar' )
do
  EXTRA_CLASSPATH=$EXTRA_CLASSPATH:$JAR
done

for JAR in $( find ./dist -name '*.jar' )
do
  EXTRA_CLASSPATH=$EXTRA_CLASSPATH:$JAR
done

if [ -n "$EXTRA_CLASSPATH" ]; then
   if [ -n "$CLASSPATH" ]; then
     export CLASSPATH="$CLASSPATH:$EXTRA_CLASSPATH"
   else
     export CLASSPATH="$EXTRA_CLASSPATH"
   fi
fi

export CLASSPATH

java ${JAVA_FLAGS} ${MAIN} JarvisMarch $@

