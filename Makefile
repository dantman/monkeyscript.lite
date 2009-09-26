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

JAKE_SRC = ${SRC_DIR}/jake
JAKE_DIST = ${DIST_DIR}/lib/jake
JAKE_FILES = ${JAKE_DIST}/jake.js \
	${JAKE_DIST}/jake.lib.js \
	${JAKE_DIST}/core-tasks.js

CLASSPATH = ${BUILD_DIR}:${LIB_DIR}/js.jar:${LIB_DIR}/jline-0.9.94.jar
JAR = ${DIST_DIR}/lib/monkeyscript.jar

java = `if [ -n "${JAVA_HOME}" ]; then echo ${JAVA_HOME}/bin/java; else echo java; fi`
javac = `if [ -n "${JAVA_HOME}" ]; then echo ${JAVA_HOME}/bin/javac; else echo javac; fi`
jar = `if [ -n "${JAVA_HOME}" ]; then echo ${JAVA_HOME}/bin/jar; else echo jar; fi`
IDSWITCH = ${java} -cp lib/rhino/build/classes/ org.mozilla.javascript.tools.idswitch.Main

.PHONY: all
all: lite

.PHONY: lite
lite: dist-deps ${JAR}
	@@echo "MonkeyScript Lite Built"
	@@echo

# Dirs
.PHONY: alldirs
alldirs: ${BUILD_DIR} ${DIST_DIR} ${DIST_DIR}/bin ${DIST_DIR}/lib ${DIST_DIR}/lib/banana ${DIST_DIR}/lib/jake
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
${DIST_DIR}/lib/jake:
	@@mkdir -p ${DIST_DIR}/lib/jake

# Copy things to dist dir
.PHONY: dist-deps
dist-deps: alldirs ${DIST_DIR}/bin/monkeyscript ${DIST_DIR}/lib/js.jar ${DIST_DIR}/lib/jline.jar dist-dep-banana dist-dep-jake
${DIST_DIR}/bin/monkeyscript: ${DIST_DIR}/bin ${SRC_DIR}/common/monkeyscript
	cp ${SRC_DIR}/common/monkeyscript ${DIST_DIR}/bin/
	-chmod +x ${DIST_DIR}/bin/monkeyscript
${DIST_DIR}/lib/js.jar: ${DIST_DIR}/lib ${LIB_DIR}/js.jar
	cp ${LIB_DIR}/js.jar ${DIST_DIR}/lib/
${DIST_DIR}/lib/jline.jar: ${DIST_DIR}/lib ${LIB_DIR}/jline-0.9.94.jar
	cp ${LIB_DIR}/jline-0.9.94.jar ${DIST_DIR}/lib/jline.jar
## Banana
.PHONY: dist-dep-banana
dist-dep-banana: $(BANANA_FILES) ${DIST_DIR}/bin/banana
$(BANANA_FILES): ${BANANA_DIST}/% : ${BANANA_SRC}/% ${BANANA_DIST}
	@@echo "Copying banana library file ${BANANA_SRC}/$(*F) to ${BANANA_DIST}/"
	@@cp ${BANANA_SRC}/$(*F) ${BANANA_DIST}/
${DIST_DIR}/bin/banana: ${DIST_DIR}/bin ${SRC_DIR}/banana/banana
	cp ${SRC_DIR}/banana/banana ${DIST_DIR}/bin/
	-chmod +x ${DIST_DIR}/bin/banana
## Jake
.PHONY: dist-dep-jake
dist-dep-jake: $(JAKE_FILES) ${DIST_DIR}/bin/jake
$(JAKE_FILES): ${JAKE_DIST}/% : ${JAKE_SRC}/% ${JAKE_DIST}
	@@echo "Copying jake library file ${JAKE_SRC}/$(*F) to ${JAKE_DIST}/"
	@@cp ${JAKE_SRC}/$(*F) ${JAKE_DIST}/
${DIST_DIR}/bin/jake: ${DIST_DIR}/bin ${SRC_DIR}/jake/jake
	cp ${SRC_DIR}/jake/jake ${DIST_DIR}/bin/
	-chmod +x ${DIST_DIR}/bin/jake

# JAR + Deps
## Class files for JAR
.PHONY: compile
compile: ${BUILD_DIR} ${BUILD_DIR}/org/monkeyscript/lite/*.class
${BUILD_DIR}/org/monkeyscript/lite/*.class: ${SRC_DIR}/common/org/monkeyscript/lite/*.java
	${javac} -cp ${CLASSPATH} -d ${BUILD_DIR} ${SRC_DIR}/common/org/monkeyscript/lite/*.java
## .js inside jar
.PHONY: jardeps
jardeps: ${BUILD_DIR} ${BUILD_DIR}/org/monkeyscript/lite/*.class ${BUILD_DIR}/org/monkeyscript/lite/monkeyscript.js ${BUILD_DIR}/org/monkeyscript/lite/monkeyscript.java.js ${BUILD_DIR}/org/monkeyscript/lite/wrench17.js ${BUILD_DIR}/org/monkeyscript/lite/json2.js
${BUILD_DIR}/org/monkeyscript/lite/monkeyscript.js: ${BUILD_DIR} ${SRC_DIR}/common/monkeyscript.js
	cp ${SRC_DIR}/common/monkeyscript.js ${BUILD_DIR}/org/monkeyscript/lite
${BUILD_DIR}/org/monkeyscript/lite/monkeyscript.java.js: ${BUILD_DIR} ${SRC_DIR}/common/monkeyscript.java.js
	cp ${SRC_DIR}/common/monkeyscript.java.js ${BUILD_DIR}/org/monkeyscript/lite
${BUILD_DIR}/org/monkeyscript/lite/wrench17.js: ${BUILD_DIR} ${LIB_DIR}/wrench17.js
	cp ${LIB_DIR}/wrench17.js ${BUILD_DIR}/org/monkeyscript/lite
${BUILD_DIR}/org/monkeyscript/lite/json2.js: ${BUILD_DIR} ${LIB_DIR}/json2.js
	cp ${LIB_DIR}/json2.js ${BUILD_DIR}/org/monkeyscript/lite
## Build the JAR
${JAR}: ${DIST_DIR} ${BUILD_DIR} ${BUILD_DIR}/org/monkeyscript/lite/*.class ${BUILD_DIR}/org/monkeyscript/lite/monkeyscript.js ${BUILD_DIR}/org/monkeyscript/lite/monkeyscript.java.js ${BUILD_DIR}/org/monkeyscript/lite/wrench17.js ${BUILD_DIR}/org/monkeyscript/lite/json2.js
	${jar} cmf ${SRC_DIR}/common/manifest ${JAR} -C ${BUILD_DIR} org/monkeyscript/lite/

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
	${javac} -Xlint:deprecation -Xlint:unchecked -cp ${CLASSPATH} -d ${BUILD_DIR} ${SRC_DIR}/common/org/monkeyscript/lite/*.java

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

