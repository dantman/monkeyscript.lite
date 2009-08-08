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
	if ( options.append ) options.write = true;
	if ( options.write && !options.hasOwnProperty('create') ) options.create = true;
	if ( !options.hasOwnProperty('truncate') ) options.truncate = true; // Defaults to true
	if ( !options.hasOwnProperty('text') ) options.text = true; // Defaults to true
	if ( options.encoding && options.encoding.lc !== 'binary' ) {
		options.text = true;
	} else {
		//options.text = false;
		//options.encoding = 'binary';
	}
	options.encoding = options.encoding || Kernel.systemEncoding;
	
	function doOpen() {
		
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

