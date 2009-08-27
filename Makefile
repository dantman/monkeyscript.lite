SRC_DIR = src
LIB_DIR = lib

PREFIX = .
DIST_DIR = ${PREFIX}/dist

CLASSPATH = ${DIST_DIR}/build:${DIST_DIR}/js.jar
JAR = ${DIST_DIR}/monkeyscript.jar

IDSWITCH = java -cp lib/rhino/build/classes/ org.mozilla.javascript.tools.idswitch.Main

all: lite

${DIST_DIR}:
	@@mkdir -p ${DIST_DIR}
	@@mkdir -p ${DIST_DIR}/build

lite: ${DIST_DIR} ${DIST_DIR}/monkeyscript ${JAR}
	@@echo "MonkeyScript Lite Built"
	@@echo

${DIST_DIR}/monkeyscript:
	cp ${SRC_DIR}/common/monkeyscript ${DIST_DIR}
	-chmod +x ${DIST_DIR}/monkeyscript

${DIST_DIR}/js.jar: ${LIB_DIR}/js.jar
	cp ${LIB_DIR}/js.jar ${DIST_DIR}

${DIST_DIR}/build/org/monkeyscript/lite/monkeyscript.js: ${SRC_DIR}/common/monkeyscript.js
	cp ${SRC_DIR}/common/monkeyscript.js ${DIST_DIR}/build/org/monkeyscript/lite
${DIST_DIR}/build/org/monkeyscript/lite/monkeyscript.java.js: ${SRC_DIR}/common/monkeyscript.java.js
	cp ${SRC_DIR}/common/monkeyscript.java.js ${DIST_DIR}/build/org/monkeyscript/lite
${DIST_DIR}/build/org/monkeyscript/lite/wrench17.js: ${LIB_DIR}/wrench17.js
	cp ${LIB_DIR}/wrench17.js ${DIST_DIR}/build/org/monkeyscript/lite
${DIST_DIR}/build/org/monkeyscript/lite/json2.js: ${LIB_DIR}/json2.js
	cp ${LIB_DIR}/json2.js ${DIST_DIR}/build/org/monkeyscript/lite

${JAR}: ${DIST_DIR}/js.jar ${DIST_DIR}/build/org/monkeyscript/lite/*.class ${DIST_DIR}/build/org/monkeyscript/lite/monkeyscript.js ${DIST_DIR}/build/org/monkeyscript/lite/monkeyscript.java.js ${DIST_DIR}/build/org/monkeyscript/lite/wrench17.js ${DIST_DIR}/build/org/monkeyscript/lite/json2.js
	jar cmf ${SRC_DIR}/common/manifest ${JAR} -C ${DIST_DIR}/build org/monkeyscript/lite/

debug-compile:
	javac -Xlint:deprecation -cp ${CLASSPATH} -d ${DIST_DIR}/build ${SRC_DIR}/common/org/monkeyscript/lite/*.java

${DIST_DIR}/build/org/monkeyscript/lite/*.class: ${SRC_DIR}/common/org/monkeyscript/lite/*.java
	javac -cp ${CLASSPATH} -d ${DIST_DIR}/build ${SRC_DIR}/common/org/monkeyscript/lite/*.java

clean:
	@@echo "Removing Distribution directory:" ${DIST_DIR}
	@@rm -rf ${DIST_DIR}
	@@echo

wrench:
	@@if [ ! -e lib/wrench/Makefile ]; then echo "Wrench.js repo does not appear to be checked out.\nYou probably did not initialize the submodule.\nPlease run \`git submodule init\` followed by \`git submodule update\`"; exit 1; fi;
	@@echo "Updating lib/wrench17.js"
	cd lib/wrench/; make wrench17 DIST_DIR=..

rhino:
	@@if [ ! -e lib/rhino/build.xml ]; then echo "Rhino repo does not appear to be checked out.\nYou probably did not initialize the submodule.\nPlease run \`git submodule init\` followed by \`git submodule update\`"; exit 1; fi;
	@@echo "Updating lib/js.jar"
	cd lib/rhino/; ant jar -Ddist.dir=..

idswitch:
	@@if [ ! -e lib/rhino/build/classes/org/mozilla/javascript/tools/idswitch/Main.class ]; then echo "To use idswitch the rhino submodule must be checked out and built"; exit 1; fi
	@@echo "Running idswitch"
	${IDSWITCH} ${SRC_DIR}/common/org/monkeyscript/lite/NativeBlob.java
	${IDSWITCH} ${SRC_DIR}/common/org/monkeyscript/lite/AbstractBuffer.java
	${IDSWITCH} ${SRC_DIR}/common/org/monkeyscript/lite/NativeBuffer.java
	${IDSWITCH} ${SRC_DIR}/common/org/monkeyscript/lite/NativeStringBuffer.java
	${IDSWITCH} ${SRC_DIR}/common/org/monkeyscript/lite/NativeBlobBuffer.java
	@@echo "Done"

