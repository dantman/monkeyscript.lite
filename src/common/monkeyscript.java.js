
include.once = includeOnce;
delete global.includeOnce;

include.ifExists = includeIfExists;
delete global.includeIfExists;

{
	let System = java.lang.System;
	let Runtime = java.lang.Runtime.getRuntime();	
	
	let sysprop = function sysprop(name) {
		return String(System.getProperty(name, ""));
	};
	let Kernel = {
		platform: {
			name: "Rhino",
			version: "1.7",
			toString: function() {
				return org.mozilla.javascript.Context.getCurrentContext().getImplementationVersion(); // this.name + ' ' + this.version;
			}
		},
		host: ["Java/", sysprop('java.specification.version'), ' "',
			sysprop("java.vendor"), '"/', sysprop("java.version"), ' "',
			sysprop("java.vm.name"), '"/', sysprop("java.vm.version")]
				.join(''),
		gc: function() {
			java.lang.System.gc();
		},
		sleep: function sleep(msec) {
			// This isn't working?
			java.lang.Thread.sleep(msec);
		},
		env: function env(name) {
			return String(System.getenv(name));
		},
		memory: {
			get max() {
				return Number(Runtime.maxMemory());
			},
			get free() {
				return Number(Runtime.freeMemory());
			},
			get total() {
				return Number(Runtime.totalMemory());
			},
			get used() {
				return Number(Runtime.totalMemory() - Runtime.freeMemory());
			}
		},
		get processors() {
			return Number(System.availableProcessors());
		},
		die: function(message) {
			print(message);
			Kernel.exit(1);
		},
		exit: function(status) {
			System.exit(status);
		},
		halt: function(status) {
			System.halt(status);
		},
		get user() {
			return sysprop("user.name", "");
		},
		get home() {
			return sysprop("user.home", "");
		},
		get currentWorkingDirectory() {
			return sysprop("user.dir", "");
		},
		os: {
			get name() {
				return sysprop("os.name");
			},
			get arch() {
				return sysprop("os.arch");
			},
			get version() {
				return sysprop("os.version");
			},
			get fileSeparator() {
				return sysprop("file.separator");
			},
			get pathSeparator() {
				return sysprop("path.separator");
			},
			get lineSeparator() {
				return sysprop("line.separator");
			},
			toString: function() {
				return this.name + ' ' + this.arch + ' (' + this.version + ')';
			},
			// @todo Temp Directory
			get temp() {
				// Windows TEMP and TMP
				// Unix-like /tmp (systems with fileSeparator=/ ?)
			}
		},
		get monkeyscriptHome() {
			// The shell always sets this
			return Kernel.env("MONKEYSCRIPT_HOME");
		},
		get configDir() {
			// Based on MonkeyScript Home on Windows
			// /etc/monkeyscript on Unix-like
			if( Kernel.env("PROGRAMFILES") )
				return Kernel.monkeyscriptHome;
			
			return "/etc/monkeyscript";
		},
		/**
		 * fs stores core filesystem access functionality.
		 * These is not intended as general use methods. Programmers should use
		 * the io banana for that instead.
		 * This is intended for use by internal and banana code which do not
		 * have access to the higher level filesystem access methods.
		 */
		fs: {
			exists: function(fileName) {
				var f = new java.io.File(fileName);
				return Boolean(f.exists());
			},
			canRead: function(fileName) {
				var f = new java.io.File(fileName);
				return Boolean(f.canRead());
			},
			canWrite: function(fileName) {
				var f = new java.io.File(fileName).getAbsoluteFile();
				return Boolean(f.exists() ? f.canWrite() : f.getParentFile().canWrite());
			},
			absoluteDirname: function(fileName) {
				return String(new java.io.File(fileName).getAbsoluteFile().getParent());
			},
			/**
			 * Read File reads in a UTF-8 encoded file from the filesystem and returns
			 * it as a string.
			 * 
			 * This is not intended as a general use method. Programmers should use
			 * the io banana for that instead.
			 * This is intended for use by internal and banana code which do not
			 * have access to the higher level filesystem access methods.
			 */
			readFile: function(fileName) {
				var f = new java.io.File(fileName);
				var is = new java.io.FileInputStream(f);
				var reader = new java.io.InputStreamReader(new java.io.BufferedInputStream(is), "UTF-8");
				try {
					var buf = new java.lang.StringBuffer();
					var offset = 0;
					while(true) {
						var charBuf = java.lang.reflect.Array.newInstance(java.lang.Character.TYPE, 512);
						var len = reader.read(charBuf, offset, 512);
						if ( len < 0 )
							break;
						buf.append(charBuf, 0, len);
					}
				} finally {
					reader.close();
				}
				return String(buf.toString());
			},
			/**
			 * Read File writes out a UTF-8 encoded file from the filesystem and
			 * creates it if necessary.
			 * 
			 * This is not intended as a general use method. Programmers should use
			 * the io banana for that instead.
			 * This is intended for use by internal and banana code which do not
			 * have access to the higher level filesystem access methods.
			 */
			writeFile: function(fileName, text) {
				var text = String(text);
				var f = new java.io.File(fileName);
				var os = new java.io.FileOutputStream(f)
				var writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(os, "UTF-8"));
				try {
					writer.write(text, 0, text.length);
				} finally {
					writer.close();
				}
				return true;
			}
		},
		/**
		 * Creates a new Error class and returns it.
		 * This is a hack primarily until the best way to create a new error class
		 * in Rhino can be found.
		 */
		newError: function(name) {
			// @ES5
			/*
			function XError(message) {
				var e = Object.create(Error.prototype);
				e.name = name;
				e.message = message || "";
				return e;
			}
			*/
			function CustomError(message) {
				if (!(this instanceof CustomError))
					return new CustomError(message);
				this.name = name;
				this.message = message || "";
			}
			CustomError.prototype.__proto__ = Error.prototype;
			CustomError.name = name;
			return CustomError;
		}
	};
	Kernel.gc.force = Kernel.gc; // In SpiderMonkey we have forced and non forced gc. In Rhino we just alias forced to normal.
	
	// Add some _native stuff so that monkeyscript.js can work the same as if this was done by C
	var _native = {
		arguments: _arguments,
		Kernel: Kernel
	};
}
delete _arguments;

