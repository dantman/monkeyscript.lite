// -*- coding: UTF-8 -*-

function jbin(exe) {
	var JAVA_HOME = Kernel.env("JAVA_HOME");
	if ( JAVA_HOME ) {
		var exepath = [JAVA_HOME,'bin',exe].join(Kernel.os.fileSeparator);
		if ( Kernel.fs.exists(exepath) )
			return exepath;
	}
	var PATH = Kernel.env("JAVA_HOME").split(Kernel.os.pathSeparator);
	for each( var path in PATH ) {
		var exepath = JAVA_HOME+Kernel.os.fileSeparator+exe;
		if ( Kernel.fs.exists(exepath) )
			return exepath;
	}
	return false;
}

task("default", ["all"]);
task("all", ["build"]);
task("build", function(options) {
	var javac = jbin("javac");
	var jar = jbin("jar");
	if ( !javac )
		Kernel.die("Could not find javac, please set JAVA_HOME to a location where javac can be found within a /bin/ subdirectory");
	if ( !jar )
		Kernel.die("Could not find jar, please set JAVA_HOME to a location where javac can be found within a /bin/ subdirectory");
	
});

task("clean", function(options) {
	print("@todo Clean");
});
