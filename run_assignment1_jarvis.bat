@ECHO OFF
set CLASSPATH=%CLASSPATH%;./lib/com.stephenwranger.graphics.jar;./dist/com.stephenwranger.compgeo.jar

REM set ANT_HOME="/path/to/ant/bin"
REM set JAVA_HOME="/path/to/java/bin"
REM set PATH=%PATH%;%ANT_HOME%;%JAVA_HOME%

java -Djava.net.useSystemProxies=true -Xmx2048m com.stephenwranger.compgeo.assignment1.Assignment1 JarvisMarch %1 %2