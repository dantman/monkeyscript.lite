ROOT = ${PWD}
SRC_DIR = ${ROOT}/src
LIB_DIR = ${ROOT}/lib
BUILD_DIR = ${ROOT}/build
BOOTSTRAP_DIR = ${ROOT}/bootstrap

BANANA_SRC = ${SRC_DIR}/banana
BANANA_BOOT = ${BOOTSTRAP_DIR}/lib/banana
BANANA_FILES = ${BANANA_BOOT}/banana.js \
	${BANANA_BOOT}/banana.help.js \
	${BANANA_BOOT}/banana.actions.js \
	${BANANA_BOOT}/banana.lib.js \
	${BANANA_BOOT}/README

JAKE_SRC = ${SRC_DIR}/jake
JAKE_BOOT = ${BOOTSTRAP_DIR}/lib/jake
JAKE_FILES = ${JAKE_BOOT}/jake.js \
	${JAKE_BOOT}/jake.lib.js \
	${JAKE_BOOT}/core-tasks.js

MODULE_SRC = ${SRC_DIR}/commonjs/modules
MODULE_BOOT = ${BOOTSTRAP_DIR}/lib/modules
MODULE_FILES = \
	${MODULE_BOOT}/system.js \
	${MODULE_BOOT}/io/filesystem/raw.js \
	${MODULE_BOOT}/io/process/function.js

#	${MODULE_BOOT}/io/stream.js \
#	${MODULE_BOOT}/io/filesystem.js \

CLASSPATH = ${BUILD_DIR}:${LIB_DIR}/js.jar:${LIB_DIR}/jline-0.9.94.jar
JAR = ${BOOTSTRAP_DIR}/lib/monkeyscript.jar

java = `if [ -n "${JAVA_HOME}" ]; then echo ${JAVA_HOME}/bin/java; else echo java; fi`
javac = `if [ -n "${JAVA_HOME}" ]; then echo ${JAVA_HOME}/bin/javac; else echo javac; fi`
jar = `if [ -n "${JAVA_HOME}" ]; then echo ${JAVA_HOME}/bin/jar; else echo jar; fi`
IDSWITCH = ${java} -cp lib/rhino/build/classes/ org.mozilla.javascript.tools.idswitch.Main

.PHONY: all
all: bootstrap
.PHONY: lite
lite: bootstrap

.PHONY: bootstrap
bootstrap: bootstrap-deps ${JAR} commonjs
	@@echo "MonkeyScript Lite Bootstrapped"
	@@echo

# Dirs
.PHONY: alldirs
alldirs: ${BUILD_DIR} ${BOOTSTRAP_DIR}/bin ${BOOTSTRAP_DIR}/lib ${BOOTSTRAP_DIR}/lib/banana ${BOOTSTRAP_DIR}/lib/jake
${BUILD_DIR}:
	@@mkdir -p ${BUILD_DIR}
${BOOTSTRAP_DIR}/bin:
	@@mkdir -p ${BOOTSTRAP_DIR}/bin
${BOOTSTRAP_DIR}/lib:
	@@mkdir -p ${BOOTSTRAP_DIR}/lib
${BOOTSTRAP_DIR}/lib/banana:
	@@mkdir -p ${BOOTSTRAP_DIR}/lib/banana
${BOOTSTRAP_DIR}/lib/jake:
	@@mkdir -p ${BOOTSTRAP_DIR}/lib/jake

# Copy things to dist dir
.PHONY: bootstrap-deps
bootstrap-deps: alldirs ${BOOTSTRAP_DIR}/bin/monkeyscript ${BOOTSTRAP_DIR}/lib/js.jar ${BOOTSTRAP_DIR}/lib/jline.jar dist-dep-banana dist-dep-jake ${BOOTSTRAP_DIR}/lib/monkeyscriptrc.js ${BOOTSTRAP_DIR}/lib/repl.js
${BOOTSTRAP_DIR}/bin/monkeyscript: ${BOOTSTRAP_DIR}/bin ${SRC_DIR}/common/monkeyscript
	cp ${SRC_DIR}/common/monkeyscript ${BOOTSTRAP_DIR}/bin/
	-chmod +x ${BOOTSTRAP_DIR}/bin/monkeyscript
${BOOTSTRAP_DIR}/lib/js.jar: ${BOOTSTRAP_DIR}/lib ${LIB_DIR}/js.jar
	cp ${LIB_DIR}/js.jar ${BOOTSTRAP_DIR}/lib/
${BOOTSTRAP_DIR}/lib/jline.jar: ${BOOTSTRAP_DIR}/lib ${LIB_DIR}/jline-0.9.94.jar
	cp ${LIB_DIR}/jline-0.9.94.jar ${BOOTSTRAP_DIR}/lib/jline.jar
${BOOTSTRAP_DIR}/lib/monkeyscriptrc.js: ${SRC_DIR}/common/monkeyscriptrc.js
	cp ${SRC_DIR}/common/monkeyscriptrc.js ${BOOTSTRAP_DIR}/lib/
${BOOTSTRAP_DIR}/lib/repl.js: ${SRC_DIR}/common/repl.js
	cp ${SRC_DIR}/common/repl.js ${BOOTSTRAP_DIR}/lib/
## Banana
.PHONY: dist-dep-banana
dist-dep-banana: $(BANANA_FILES) ${BOOTSTRAP_DIR}/bin/banana
$(BANANA_FILES): ${BANANA_BOOT}/% : ${BANANA_SRC}/% ${BANANA_BOOT}
	@@echo "Copying banana library file ${BANANA_SRC}/$(*F) to ${BANANA_BOOT}/"
	@@cp ${BANANA_SRC}/$(*F) ${BANANA_BOOT}/
${BOOTSTRAP_DIR}/bin/banana: ${BOOTSTRAP_DIR}/bin ${SRC_DIR}/banana/banana
	cp ${SRC_DIR}/banana/banana ${BOOTSTRAP_DIR}/bin/
	-chmod +x ${BOOTSTRAP_DIR}/bin/banana
## Jake
.PHONY: dist-dep-jake
dist-dep-jake: $(JAKE_FILES) ${BOOTSTRAP_DIR}/bin/jake
$(JAKE_FILES): ${JAKE_BOOT}/% : ${JAKE_SRC}/% ${JAKE_BOOT}
	@@echo "Copying jake library file ${JAKE_SRC}/$(*F) to ${JAKE_BOOT}/"
	@@cp ${JAKE_SRC}/$(*F) ${JAKE_BOOT}/
${BOOTSTRAP_DIR}/bin/jake: ${BOOTSTRAP_DIR}/bin ${SRC_DIR}/jake/jake
	cp ${SRC_DIR}/jake/jake ${BOOTSTRAP_DIR}/bin/
	-chmod +x ${BOOTSTRAP_DIR}/bin/jake

## CommonJS modules
.PHONY: commonjs
commonjs: ${MODULE_BOOT} $(MODULE_FILES) ${MODULE_BOOT}/io/buffer.jar ${MODULE_BOOT}/repl.jar
${MODULE_BOOT}:
	@@mkdir -p ${MODULE_BOOT}
$(MODULE_FILES): ${MODULE_BOOT}/% : ${MODULE_SRC}/% ${MODULE_BOOT}
	@@echo "Copying commonjs module file ${MODULE_SRC}/$(*D)/$(*F) to ${MODULE_BOOT}/"
	-mkdir -p ${MODULE_BOOT}/$(*D)
	@@cp ${MODULE_SRC}/$(*D)/$(*F) ${MODULE_BOOT}/$(*D)
${MODULE_BOOT}/io/buffer.jar: ${SRC_DIR}/commonjs/buffer/build/io/buffer.jar
	-mkdir -p ${MODULE_BOOT}/io/
	cp ${SRC_DIR}/commonjs/buffer/build/io/buffer.jar ${MODULE_BOOT}/io/
${SRC_DIR}/commonjs/buffer/build/io/buffer.jar: ${SRC_DIR}/commonjs/buffer/*.java ${SRC_DIR}/commonjs/buffer/Manifest
	@@echo "Building io/buffer.jar commonjs module using bootstrapped jake"
	cd ${SRC_DIR}/commonjs/buffer; ${BOOTSTRAP_DIR}/bin/jake
${MODULE_BOOT}/repl.jar: ${SRC_DIR}/commonjs/buffer/build/repl.jar
	-mkdir -p ${MODULE_BOOT}/
	cp ${SRC_DIR}/commonjs/repl/build/repl.jar ${MODULE_BOOT}/
${SRC_DIR}/commonjs/buffer/build/repl.jar: ${SRC_DIR}/commonjs/repl/*.java ${SRC_DIR}/commonjs/repl/Manifest
	@@echo "Building repl.jar commonjs module using bootstrapped jake"
	cd ${SRC_DIR}/commonjs/repl; ${BOOTSTRAP_DIR}/bin/jake

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
${JAR}: ${BUILD_DIR} ${BUILD_DIR}/org/monkeyscript/lite/*.class ${BUILD_DIR}/org/monkeyscript/lite/monkeyscript.js ${BUILD_DIR}/org/monkeyscript/lite/monkeyscript.java.js ${BUILD_DIR}/org/monkeyscript/lite/wrench17.js ${BUILD_DIR}/org/monkeyscript/lite/json2.js
	${jar} cmf ${SRC_DIR}/common/manifest ${JAR} -C ${BUILD_DIR} org/monkeyscript/lite/

# Cleanup
.PHONY: clean
clean:
	@@echo "Removing build directory:" ${BUILD_DIR}
	@@rm -rf ${BUILD_DIR}
	@@echo "Removing bootstrap directory:" ${BOOTSTRAP_DIR}
	@@rm -rf ${BOOTSTRAP_DIR}
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

