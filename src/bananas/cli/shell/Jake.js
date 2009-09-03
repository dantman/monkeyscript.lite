// -*- coding: UTF-8 -*-

task("default", ["all"]);
task("all", ["build"]);
task("build", function(options) {
	var {ProcessFunction} = banana('os.process');
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
	
	// Till we get io working, why not do this the fun way.
	var mkdir = new ProcessFunction("mkdir");
	mkdir("classes");
	var CLASSPATH = options.CLASSPATH || "../../../../dist/lib/js.jar:../../../../dist/lib/monkeyscript.jar";
	javac("-cp", CLASSPATH, "-d", "classes", "org/monkeyscript/bananas/cli/shell/Shell.java").orDie();
});

task("clean", function(options) {
	print("@todo Clean");
});
