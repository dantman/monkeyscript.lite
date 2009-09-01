SRC_DIR = src
LIB_DIR = lib
BUILD_DIR = build
DIST_DIR = dist

BANANA_SRC = ${SRC_DIR}/banana
BANANA_DIST = ${DIST_DIR}/lib/banana
BANANA_FILES = ${BANANA_DIST}/banana.js \
	${BANANA_DIST}/banana.help.js \
	${BANANA_DIST}/banana.actions.js \
	${BANANA_DIST}/banana.lib.js \
	${BANANA_DIST}/README

CLASSPATH = ${BUILD_DIR}:${LIB_DIR}/js.jar:${LIB_DIR}/jline-0.9.94.jar
JAR = ${DIST_DIR}/lib/monkeyscript.jar

IDSWITCH = java -cp lib/rhino/build/classes/ org.mozilla.javascript.tools.idswitch.Main

.PHONY: all
all: lite

.PHONY: lite
lite: dist-deps ${JAR}
	@@echo "MonkeyScript Lite Built"
	@@echo

# Dirs
.PHONY: alldirs
alldirs: ${BUILD_DIR} ${DIST_DIR} ${DIST_DIR}/bin ${DIST_DIR}/lib ${DIST_DIR}/lib/banana
${BUILD_DIR}:
	@@mkdir -p ${BUILD_DIR}
${DIST_DIR}:
	@@mkdir -p ${DIST_DIR}
${DIST_DIR}/bin:
	@@mkdir -p ${DIST_DIR}/bin
${DIST_DIR}/lib:
	@@mkdir -p ${DIST_DIR}/lib
${DIST_DIR}/lib/banana:
	@@mkdir -p ${DIST_DIR}/lib/banana

# Copy things to dist dir
.PHONY: dist-deps
dist-deps: ${DIST_DIR}/bin/monkeyscript ${DIST_DIR}/lib/js.jar ${DIST_DIR}/lib/jline.jar dist-dep-banana
${DIST_DIR}/bin/monkeyscript: alldirs
	cp ${SRC_DIR}/common/monkeyscript ${DIST_DIR}/bin/
	-chmod +x ${DIST_DIR}/bin/monkeyscript
${DIST_DIR}/lib/js.jar: alldirs ${LIB_DIR}/js.jar
	cp ${LIB_DIR}/js.jar ${DIST_DIR}/lib/
${DIST_DIR}/lib/jline.jar: alldirs ${LIB_DIR}/jline-0.9.94.jar
	cp ${LIB_DIR}/jline-0.9.94.jar ${DIST_DIR}/lib/jline.jar
## Banana
.PHONY: dist-dep-banana
dist-dep-banana: $(BANANA_FILES) ${DIST_DIR}/bin/banana
$(BANANA_FILES): ${BANANA_DIST}/% : ${BANANA_SRC}/% alldirs
	@@echo "Copying banana library file ${BANANA_SRC}/$(*F) to ${BANANA_DIST}/"
	cp ${BANANA_SRC}/$(*F) ${BANANA_DIST}/
${DIST_DIR}/bin/banana: alldirs
	cp ${SRC_DIR}/banana/banana ${DIST_DIR}/bin/
	-chmod +x ${DIST_DIR}/bin/banana

# JAR + Deps
## Class files for JAR
.PHONY: compile
compile: ${BUILD_DIR} ${BUILD_DIR}/org/monkeyscript/lite/*.class
${BUILD_DIR}/org/monkeyscript/lite/*.class: ${SRC_DIR}/common/org/monkeyscript/lite/*.java
	javac -cp ${CLASSPATH} -d ${BUILD_DIR} ${SRC_DIR}/common/org/monkeyscript/lite/*.java
## .js inside jar
.PHONY: jardeps
jardeps: compile ${BUILD_DIR}/org/monkeyscript/lite/monkeyscript.js ${BUILD_DIR}/org/monkeyscript/lite/monkeyscript.java.js ${BUILD_DIR}/org/monkeyscript/lite/wrench17.js ${BUILD_DIR}/org/monkeyscript/lite/json2.js
${BUILD_DIR}/org/monkeyscript/lite/monkeyscript.js: ${BUILD_DIR} ${SRC_DIR}/common/monkeyscript.js
	cp ${SRC_DIR}/common/monkeyscript.js ${BUILD_DIR}/org/monkeyscript/lite
${BUILD_DIR}/org/monkeyscript/lite/monkeyscript.java.js: ${BUILD_DIR} ${SRC_DIR}/common/monkeyscript.java.js
	cp ${SRC_DIR}/common/monkeyscript.java.js ${BUILD_DIR}/org/monkeyscript/lite
${BUILD_DIR}/org/monkeyscript/lite/wrench17.js: ${BUILD_DIR} ${LIB_DIR}/wrench17.js
	cp ${LIB_DIR}/wrench17.js ${BUILD_DIR}/org/monkeyscript/lite
${BUILD_DIR}/org/monkeyscript/lite/json2.js: ${BUILD_DIR} ${LIB_DIR}/json2.js
	cp ${LIB_DIR}/json2.js ${BUILD_DIR}/org/monkeyscript/lite
## Build the JAR
${JAR}: alldirs jardeps
	jar cmf ${SRC_DIR}/common/manifest ${JAR} -C ${BUILD_DIR} org/monkeyscript/lite/

# Cleanup
.PHONY: clean
clean:
	@@echo "Removing build directory:" ${BUILD_DIR}
	@@rm -rf ${BUILD_DIR}
	@@echo "Removing distribution directory:" ${DIST_DIR}
	@@rm -rf ${DIST_DIR}
	@@echo

# Debug
.PHONY: debug-compile
debug-compile: ${BUILD_DIR}
	javac -Xlint:deprecation -Xlint:unchecked -cp ${CLASSPATH} -d ${BUILD_DIR} ${SRC_DIR}/common/org/monkeyscript/lite/*.java

# Lib build
.PHONY: wrench
wrench:
	@@if [ ! -e lib/wrench/Makefile ]; then echo "Wrench.js repo does not appear to be checked out.\nYou probably did not initialize the submodule.\nPlease run \`git submodule init\` followed by \`git submodule update\`"; exit 1; fi;
	@@echo "Updating lib/wrench17.js"
	cd lib/wrench/; make wrench17 DIST_DIR=..

.PHONY: rhino
rhino:
	@@if [ ! -e lib/rhino/build.xml ]; then echo "Rhino repo does not appear to be checked out.\nYou probably did not initialize the submodule.\nPlease run \`git submodule init\` followed by \`git submodule update\`"; exit 1; fi;
	@@echo "Updating lib/js.jar"
	cd lib/rhino/; ant jar -Ddist.dir=..

# Tools
.PHONY: idswitch
idswitch:
	@@if [ ! -e lib/rhino/build/classes/org/mozilla/javascript/tools/idswitch/Main.class ]; then echo "To use idswitch the rhino submodule must be checked out and built"; exit 1; fi
	@@echo "Running idswitch"
	${IDSWITCH} ${SRC_DIR}/common/org/monkeyscript/lite/NativeBlob.java
	${IDSWITCH} ${SRC_DIR}/common/org/monkeyscript/lite/AbstractBuffer.java
	${IDSWITCH} ${SRC_DIR}/common/org/monkeyscript/lite/NativeBuffer.java
	${IDSWITCH} ${SRC_DIR}/common/org/monkeyscript/lite/NativeStringBuffer.java
	${IDSWITCH} ${SRC_DIR}/common/org/monkeyscript/lite/NativeBlobBuffer.java
	@@echo "Done"

