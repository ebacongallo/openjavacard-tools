#!/bin/sh
home=`dirname $0`
java -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO -jar $home/tool/build/jar/tool-0.1-SNAPSHOT-fat.jar $@
