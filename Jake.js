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

group('test', function(options) {
	task('commonjs', function(options) {
		var {ProcessFunction} = banana('os.process');
		var run = new ProcessFunction("monkeyscript");
		var dir = new java.io.File('commonjs-tests/compliance/');
		Array.forEach(dir.listFiles(), function(testDir) {
			if(testDir.isDirectory()) {
				var t = String(testDir.getAbsolutePath());
				print('//\n// Running compliance test "'+testDir.getName()+'"\n//');
				print(run("--rc", "javascript:require.paths.push('"+t+"');", t+"/program.js").output);
			}
		});
		var dir = new java.io.File('commonjs-tests/extensions/');
		Array.forEach(dir.listFiles(), function(testDir) {
			if(testDir.isDirectory()) {
				var t = String(testDir.getAbsolutePath());
				print('//\n// Running extension test "'+testDir.getName()+'"\n//');
				print(run("--rc", "javascript:require.paths.push('"+t+"');", t+"/program.js").output);
			}
		});
	});
});

function copy(a, b) {
	var File = java.io.File;
	if (!(a instanceof File))
		a = new java.io.File(a);
	if (!(b instanceof File))
		b = new java.io.File(b);
	var from = new java.io.FileInputStream(a),
	      to = new java.io.FileOutputStream(b);
	var buf = java.lang.reflect.Array.newInstance(java.lang.Byte.TYPE, 512);
	do {
		var rlen = from.read(buf);
		if ( rlen != -1 )
			to.write(buf, 0, rlen);
	} while( rlen != -1 );
	print("File " + a.toString() + " copied to " + b.toString());
}

task('bootstrap', function(options) {
	var File = java.io.File;
	options.BOOTSTRAP_DIR = options.BOOTSTRAP_DIR || "bootstrap";
	options.SRC_DIR = options.SRC_DIR || "src";
	for each ( var fname in ["lint.js", "repl.js"] )
		copy(new File(options.SRC_DIR, "common/"+fname), new File(options.BOOTSTRAP_DIR, "lib/"+fname));
	
	var manifest = JSON.parse(Kernel.fs.readFile('./src/commonjs/modules/Manifest.json'));
	for each ( let modname in manifest ) {
		var f = new File(options.BOOTSTRAP_DIR, "lib/modules/"+modname);
		f.getParentFile().mkdirs();
		copy(new File(options.SRC_DIR, "commonjs/modules/"+modname), f);
	}
});

function defaultInstallData() {
	// @todo, Special case on Windows to use a different base and not include /monkeyscript in libdir
	return {
		base: "/usr/local",
		bindir: "bin",
		libdir: "lib/monkeyscript",
		coremod: "core-modules",
		jakedir: "jake",
		mode: "lite",
		scriptover: true
	};
}

function installData() {
	var install = {};
	if ( Kernel.fs.canRead('./install.json') )
		install = JSON.parse(Kernel.fs.readFile('./install.json'));
	return Object.merge({}, defaultInstallData(), install);
}

task("install", function() {
	var {ProcessFunction} = require('io/process/function');
	function jbin(exe) {
		var JAVA_HOME = Kernel.env("JAVA_HOME");
		if ( JAVA_HOME ) {
			var exepath = [JAVA_HOME,'bin',exe].join(Kernel.os.fileSeparator);
			if ( Kernel.fs.exists(exepath) )
				return new ProcessFunction(exepath);
		}
		var PATH = Kernel.env("PATH").split(Kernel.os.pathSeparator);
		for each( var path in PATH ) {
			var exepath = path+Kernel.os.fileSeparator+exe;
			if ( Kernel.fs.exists(exepath) )
				return new ProcessFunction(exepath);
		}
		return false;
	}
	var jake = new ProcessFunction(Kernel.currentWorkingDirectory+"/bootstrap/bin/jake");
	var javac = jbin("javac");
	var jar = jbin("jar");
	if ( !javac )
		Kernel.die("Could not find javac, please set JAVA_HOME to a location where javac can be found within a /bin/ subdirectory");
	if ( !jar )
		Kernel.die("Could not find jar, please set JAVA_HOME to a location where javac can be found within a /bin/ subdirectory");
	
	var install = installData();
	install.base = install.base.trimRight('/') 
	if ( !/^\/|^[A-Z]:[\\\/]/i.test(install.bindir) )
		install.bindir = install.base + '/' + install.bindir;
	if ( !/^\/|^[A-Z]:[\\\/]/i.test(install.libdir) )
		install.libdir = install.base + '/' + install.libdir;
	if ( !/^\/|^[A-Z]:[\\\/]/i.test(install.coremod) )
		install.coremod = install.libdir + '/' + install.coremod;
	if ( !/^\/|^[A-Z]:[\\\/]/i.test(install.jakedir) )
		install.jakedir = install.libdir + '/' + install.jakedir;
	
	if ( install.mode !== "lite" )
		Kernel.die("Unknown install mode, only 'lite' may currently be installed");
	
	print("Installing MonkeyScript Lite");
	print("Libs being installed to: "+install.libdir);
	print("Executables being installed to: "+install.bindir);
	print("Core modules being installed to: "+install.coremod);
	print("Jake being installed to: "+install.jakedir);
	
	var File = java.io.File;
	
	var fd = {}
	
	for each ( let dir in ["bindir", "libdir", "coremod", "jakedir"] ) {
		var fdir = new File(install[dir]);
		fd[dir] = fdir;
		if ( !fdir.exists() ) {
			if ( !fdir.mkdirs() )
				Kernel.die("ERROR: "+install[dir]+" directory could not be created");
			print(install[dir]+" directory created");
		}
	}
	
	for each ( let dir in ["bindir", "libdir", "coremod", "jakedir"] ) {
		if ( !fd[dir].canWrite() )
			Kernel.die("ERROR: "+install[dir]+" is not writeable");
	}
	
	copy("src/common/repl.js", new File(fd.libdir, "repl.js"));
	// Jake
	for each ( let fname in ["core-tasks.js", "jake.js", "jake.lib.js"] )
		copy("src/jake/"+fname, new File(fd.jakedir, fname));
	// Modules
	var manifest = JSON.parse(Kernel.fs.readFile('./src/commonjs/modules/Manifest.json'));
	for each ( let modname in manifest ) {
		(new File(fd.coremod, modname)).getParentFile().mkdirs();
		copy(new File("src/commonjs/modules/",modname), new File(fd.coremod, modname));
	}
	// JARs
	copy("lib/jline-0.9.94.jar", new File(fd.libdir, "jline.jar"));
	copy("lib/js.jar", new File(fd.libdir, "js.jar"));
	// @todo Run make?
	var CLASSPATH = (new File(fd.libdir, "js.jar")).toString();
	
	print("Compiling java source files for MonkeyScript Lite");
	var files = Array.slice((new File("src/common/org/monkeyscript/lite/")).list())
		.filter(function(path) { return path.endsWith(".java"); })
		.map(function(path) { return "src/common/org/monkeyscript/lite/"+path });
	var args = ["-cp", CLASSPATH, "-d", "build"];
	(new File("build/")).mkdirs();
	javac.apply(undefined, args.concat(files)).orDie();
	print("Finished compiling");
	copy("src/common/monkeyscript.js", "build/org/monkeyscript/lite/monkeyscript.js");
	copy("src/common/monkeyscript.java.js", "build/org/monkeyscript/lite/monkeyscript.java.js");
	copy("lib/wrench17.js", "build/org/monkeyscript/lite/wrench17.js");
	copy("lib/json2.js", "build/org/monkeyscript/lite/json2.js");

	print("Building monkeyscript.jar");
	jar("cmf", "src/common/manifest", "build/monkeyscript.jar", "-C", "build/", "org/monkeyscript/lite/").orDie();
	copy("build/monkeyscript.jar", new File(fd.libdir, "monkeyscript.jar"));
	
	//print("Building buffer via jake");
	//print(jake({directory:Kernel.currentWorkingDirectory+"/src/commonjs/buffer/"}, "all").orDie());
	
	// Executables
	copy("src/jake/jake", new File(fd.bindir, "jake")); // @todo Windows .bat
	var sh = Kernel.fs.readFile("src/common/monkeyscript.sh");
	sh = sh.replace(/<<<MONKEYSCRIPT_HOME>>>/g, fd.libdir);
	var shfile = new File(fd.bindir, "monkeyscript.lite");
	Kernel.fs.writeFile(shfile.toString(), sh);
	print("Wrote bash file to "+shfile.toString());
	shfile.setExecutable(true, false);
	
	var msfile = new File(install.bindir, 'monkeyscript');
	if ( !msfile.exists() || install.scriptover ) {
		Kernel.fs.writeFile(msfile.toString(), '#!/bin/bash\n$(dirname -- "$0")/monkeyscript.lite "$@"\n');
		print("Wrote bash alias to "+msfile.toString());
		msfile.setExecutable(true, false);
	} else {
		print("Ignoring already existant "+msfile.toString());
	}
	
});

task("configure", function() {
	var install = installData();
	var config = {};
	if ( Kernel.fs.canRead('./install.json') )
		config = JSON.parse(Kernel.fs.readFile('./install.json'));
		
	var oldInstall = installData(), installConfig = {};
	print("Please configure the install parameters. Leave blank to use defaults.");
	var out = java.lang.System.out, input = java.lang.System["in"], console = java.lang.System.console();
	var input = {};
	input.base = String(console.readLine("%s [%s]: ", "Base for the install paths", install.base));
	input.bindir = String(console.readLine("%s [%s]: ", "Executable install path (relative to the base)", install.bindir));
	input.libdir = String(console.readLine("%s [%s]: ", "Lib install path (relative to the base)", install.libdir));
	input.coremod = String(console.readLine("%s [%s]: ", "Core modules install path (relative to the lib install path)", install.coremod));
	input.jakedir = String(console.readLine("%s [%s]: ", "Jake install path (relative to the lib install path)", install.jakedir));
	input.scriptover = String(console.readLine("%s [%s]: ", "Install executable `monkeyscript` over existing ones", install.scriptover));
	if ( input.scriptover != "" )
		input.scriptover = !!input.scriptover.match(/^T(rue)?|^Y(es)?|Ok/i);
	
	for ( var k in input ) {
		if ( k in input && input[k] !== "" )
			config[k] = input[k];
	}
	Kernel.fs.writeFile('./install.json', JSON.stringify(config));
});

task("clean", function() {
	(new java.io.File('./install.json'))["delete"]();
});

