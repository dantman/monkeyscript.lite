#!/bin/bash

JAVA=java
if [ -n "$JAVA_HOME" ]; then
	JAVA=$JAVA_HOME/bin/java
fi

if [ ! -n "$MONKEYSCRIPT_HOME" ]; then
	export MONKEYSCRIPT_HOME="<<<MONKEYSCRIPT_HOME>>>"
	if [ ! -n "$MONKEYSCRIPT_HOME" ]; then
		echo "MONKEYSCRIPT_HOME could not be determined, please set the environment variable"
		exit 1
	fi
fi

$JAVA -Xbootclasspath/p:"$MONKEYSCRIPT_HOME/js.jar:$MONKEYSCRIPT_HOME/jline.jar" -jar "$MONKEYSCRIPT_HOME/monkeyscript.jar" "$@"

