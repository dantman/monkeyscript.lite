SRC_DIR = src
LIB_DIR = lib

PREFIX = .
DIST_DIR = ${PREFIX}/dist

CLASSPATH = ${DIST_DIR}/build:${DIST_DIR}/js.jar
JAR = ${DIST_DIR}/monkeyscript.jar

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

${JAR}: ${DIST_DIR}/js.jar ${DIST_DIR}/build/org/monkeyscript/lite/*.class ${DIST_DIR}/build/org/monkeyscript/lite/monkeyscript.js ${DIST_DIR}/build/org/monkeyscript/lite/monkeyscript.java.js ${DIST_DIR}/build/org/monkeyscript/lite/wrench17.js
	jar cmf ${SRC_DIR}/common/manifest ${JAR} -C ${DIST_DIR}/build org/monkeyscript/lite/

${DIST_DIR}/build/org/monkeyscript/lite/*.class: ${SRC_DIR}/common/org/monkeyscript/lite/*.java
	javac -cp ${CLASSPATH} -d ${DIST_DIR}/build ${SRC_DIR}/common/org/monkeyscript/lite/*.java

clean:
	@@echo "Removing Distribution directory:" ${DIST_DIR}
	@@rm -rf ${DIST_DIR}
	@@echo

wrench:
	@@echo "Updating lib/wrench17.js"
	cd lib/wrench/; make wrench17 DIST_DIR=..

