// -*- coding: UTF-8 -*-

function File(path) {
	if ( path instanceof File )
		path = path.pathString;
	path = String(path);
	return Object.create(File.prototype, {
		_path: {
			value: path,
			enumerable: false,
			writable: false,
			configurable: false
		},
		_file: {
			value: new _native.File,
			enumerable: false,
			writable: false,
			configurable: false
		}
	});
}

File.prototype.open = function() {
	var block;
	if ( isObject(arguments[0]) ) {
		var [options, block] = arguments[1];
	} else {
		var options = {};
		var args = Array.slice(arguments);
		var flags = args.shift();
		
		if ( flags.has('r') ) options.read = true;
		if ( flags.has('w') ) options.write = true;
		if ( flags.has('x') ) { options.create = false; options.write = true; }
		if ( flags.has('a') ) options.append = true;
		if ( flags.has('+') ) options.truncate = false;
		if ( flags.has('s') ) options.sync = true;
		if ( flags.has('t') ) options.text = true;
		if ( flags.has('b') ) options.text = false;
		
		if ( isString(args[0]) && /^([-r][-w][-x]){3}$|^([augo]+[-+][rwxa]+)+$/.test(args[0]) )
			options.encoding = args.shift();
		
		if ( isNumber(args[0]) || isString(args[0]) )
			options.access = args.shift();
		
		if ( isFunction(args[0]) ) {
			var block = args.shift();
		}
	}
	options.append = !!options.append;
	if ( options.append ) options.write = true;
	if ( options.write && !options.hasOwnProperty('create') ) options.create = true;
	if ( !options.hasOwnProperty('truncate') ) options.truncate = true; // Defaults to true
	if ( !options.hasOwnProperty('text') ) options.text = true; // Defaults to true
	if ( options.encoding ) {
		if ( options.encoding.lc === 'binary' ) {
			options.text = false;
			options.encoding = 'binary';
		} else {
			options.text = true;
		}
	} else {
		if ( options.text !== false ) {
			options.encoding = Kernel.os.encoding;
			options.text = true;
		} else {
			options.encoding = 'binary';
			options.text = false;
		}
	}
	//options.encoding = options.encoding || Kernel.systemEncoding;
	
	function doOpen() {
		// Whoops, we have rhino specific stuff here we need to move out
		var rb, rt, wb, wt, o = {};
		o.contentConstructor = function() {
			return options.text ? String : Blob;
		};
		if ( options.read ) {
			var rb = new java.io.FileInputStream(this._file);
			if ( !options.text ) {
				o.read = function(len, bufNoSkip) {
					if ( len === Infinity )
						len = 512;
					if ( !bufNoSkip )
						return rb.skip(len);
					var bbuf = java.lang.reflect.Array.newInstance(java.lang.Byte.TYPE, len);
					var rLen = rb.read(bbuf, 0, len);
					return Blob(java.util.Arrays.copyOf(bbuf, rLen));
				};
			} else {
				var rt = new java.io.BufferedReader(new java.io.InputStreamReader(rb, options.encoding));
				o.read = function(len, bufNoSkip) {
					if ( len === Infinity )
						len = 512;
					if ( !bufNoSkip )
						return rb.skip(len);
					var cbuf = java.lang.reflect.Array.newInstance(java.lang.Character.TYPE, 512);
					var rLen = rb.read(cbuf, 0, len);
					return String(new java.lang.String(cbuf, 0, rLen));
				};
			}
		}
		if ( options.write ) {
			var wb = new java.io.FileOutputStream(this._file, options.append);
			if ( options.truncate ) {
				// Truncate when opening
				wb.getChannel().truncate(0);
			}
			function maybeSync() {
				if ( options.sync ) {
					o.flush();
					wb.getFD().sync();
				}
			}
			if ( !options.text ) {
				o.write = function(data) {
					data = data.valueOf();
					if(!(data instanceof Blob))
						throw new TypeError();
					var bbuf = data.toJavaByteArray(); // Extension for rhino environment
					wb.write(bbuf);
					maybeSync();
					return data.length;
				};
				o.flush = function() {
					wb.flush();
				};
			} else {
				var wt = new java.io.BufferedWriter(new java.io.OutputStreamWriter(wb, options.encoding));
				o.write = function(data) {
					data = data.valueOf();
					if(!isString(data))
						throw new TypeError();
					var cbuf = data.toJavaCharArray(); // Extension for rhino environment
					wt.write(cbuf);
					maybeSync();
					return data.length;
				};
				o.flush = function() {
					wt.flush();
				};
			}
		}
		o.close = function() {
			if ( rt ) rt.close();
			else if ( rb ) rb.close();
			if ( wt ) wt.close();
			else if ( wb ) wb.close();
		};
		return new Stream(o);
	}
	
	if ( block ) {
		var stream = doOpen();
		try {
			return block.call(this, stream);
		} finally {
			stream.close();
		}
	} else {
		return doOpen();
	}
};

File.open = function() {
	var args = Array.slice(arguments);
	var path = args.shift();
	var f = new File(f);
	return f.open.apply(f, args);
};

Object.defineProperties(File.prototype, {
	name: {
		get: function() {
			return String(this._file.getName());
		},
		set: function(name) {
			
		}
	},
	path: {
		get: function() {
			return new FilePath(this.pathString);
		},
		set: function(path) {
			
		}
	},
	pathString: {
		get: function() {
			return this._path;
		},
		set: function(path) {
			this.path = path;
		}
	},
	exists: {
		get: function() {
			return Boolean(this._file.exists());
		},
		set: function(exist) {
			if ( exist !== false )
				throw new TypeError("Cannot use file.exists = true; to create a file.");
			if ( this.exists ) {
				if ( this.isFile )
					if ( !this._file['delete']() )
						throw new IOError("Failed to delete file.");
				else
					throw new TypeError("Cannot delete a non-file.");
			}
		}
	},
	isReadable: {
		get: function() {
			return Boolean(this._file.canRead());
		}
	},
	isWritable: {
		get: function() {
			return Boolean(this._file.());
		}
	},
	isExecutable: {
		get: function() {
			return Boolean(this._file.canExecute());
		}
	isFile: {
		get: function() {
			return Boolean(this._file.isFile());
		}
	},
	isDirectory: {
		get: function() {
			return Boolean(this._file.isDirectory());
		}
	},
	isHidden: {
		get: function() {
			return Boolean(this._file.isHidden());
		}
	},
	lastModified: {
		get: function() {
			var longTime = this._file.lastModified();
			if ( !longTime )
				return false;
			return new Date(longTime);
		},
		set: function(val) {
			if ( val instanceof Date )
				val = val.getTime();
			if ( typeof val !== "number" )
				throw new TypeError("Given a invalid value to set last modified time to.");
			if ( !this._file.setLastModified(val) )
				throw new IOError("Failed to set last modified time");
		}
	},
	contents: {
		get: function() {
			return this.open('rt').read();
		},
		set: function(contents) {
			if ( contents === undefined )
				this.exists = false;
			else if ( contents instanceof Blob )
				this.open('wb').write(contents); // Ensure writing?
			else
				this.open('wt').write(contents); // Ensure writing?
		}
	},
	size: {
		get: function() {
			if ( this.isFile )
				return Number(this._file.length());
			return undefined;
		}
	}
});
File.prototype.remove = function remove() {
	try {
		this.exists = false;
		return true;
	} catch( e if e instanceof IOError ) {
		return false;
	}
};
for each( let name in ["unlink", "rm", "delete"] )
	Object.defineProperty(File.prototype, name, {value: File.prototype.remove, enumerable: false});
File.prototype.touch = function touch(time) {
	if ( !time )
		time = new Date;
	try {
		this.lastModified = time;
		return true;
	} catch ( e if e instanceof IOError ) {
		return false;
	} 
};
// ToDo: Change the _path and _file when renaming
File.prototype.rename =
File.prototype.renameTo = function renameTo(name) {
	try {
		this.name = name;
		return true;
	} catch ( e if e instanceof IOError ) {
		return false;
	}
};
File.prototype.move =
File.prototype.moveTo = function moveTo(path) {
	try {
		this.path = path;
		return true;
	} catch ( e if e instanceof IOError ) {
		return false;
	}
};
File.prototype.same = function same(file) {
	if (!(file instanceof File) || !(this instanceof File))
		return false;
	return this._file.equals(file._file);
};
File.prototype.copy =
File.prototype.copyTo = function(to) {
	if ( to instanceof Directory )
		to = new File(to.pathString + this.name); // pathString returned by Directory must have trailing / already
	if ( isString(to) )
		to = new File(to);
	if (!(to instanceof File))
		throw new TypeError("Bad data given as to value for copyTo");
	if ( to.isFile )
		throw new IOError("Cannot copy a non-existant or non-file.");
	var fromStream = this.open('rb');
	var toStream = this.open('wb');
	toStream.copy(fromStream);
};
File.prototype.copyFrom = function(from) {
	
};

exports.File = File;

