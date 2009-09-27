// -*- coding: UTF-8 -*-
/**!
 * @author Daniel Friesen
 * @copyright Copyright © 2009 Daniel Friesen
 * @license MIT License
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

task("default", ["all"]);
task("all", ["build"]);
task("build", function(options) {
	var {ProcessFunction} = require('io/process/function');
	function jbin(exe) {
		var JAVA_HOME = Kernel.env("JAVA_HOME");
		if ( JAVA_HOME ) {
			var exepath = [JAVA_HOME,'bin',exe].join(Kernel.os.fileSeparator);
			if ( Kernel.fs.exists(exepath) )
				return new ProcessFunction(exepath);
		}
		var PATH = Kernel.env("JAVA_HOME").split(Kernel.os.pathSeparator);
		for each( var path in PATH ) {
			var exepath = JAVA_HOME+Kernel.os.fileSeparator+exe;
			if ( Kernel.fs.exists(exepath) )
				return new ProcessFunction(exepath);
		}
		return false;
	}

	var javac = jbin("javac");
	var jar = jbin("jar");
	if ( !javac )
		Kernel.die("Could not find javac, please set JAVA_HOME to a location where javac can be found within a /bin/ subdirectory");
	if ( !jar )
		Kernel.die("Could not find jar, please set JAVA_HOME to a location where javac can be found within a /bin/ subdirectory");
	
	var mkdir = new ProcessFunction("mkdir");
	mkdir("-p", "org/monkeyscript/lite/modules/repl", "build");
	
	var CLASSPATH = [
		[Kernel.monkeyscriptHome, "js.jar"].join(Kernel.os.fileSeparator),
		[Kernel.monkeyscriptHome, "monkeyscript.jar"].join(Kernel.os.fileSeparator)
	];
	if ( options.CLASSPATH )
		CLASSPATH.unshift(options.CLASSPATH);
	CLASSPATH = CLASSPATH.join(Kernel.os.pathSeparator);
	javac("-cp", CLASSPATH, "-d", ".", "REPL.java", "Exports.java").orDie();
	print("java classes compiled");
	jar("cmf", "Manifest", "build/repl.jar", "org/monkeyscript/lite/modules/repl/").orDie();
	print("jar built");
});

task("clean", function(options) {
	var {ProcessFunction} = banana('os.process');
	var rm = new ProcessFunction("rm");
	rm("-r", "org", "build");
	print("org and build folders cleaned up");
});

