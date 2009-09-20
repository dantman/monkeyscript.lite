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
			value: new _native.File(path),
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
		
		if ( !isString(flags) )
			throw new TypeError("Mode is not a string");
		
		if ( flags.contains('r') ) options.read = true;
		if ( flags.contains('w') ) options.write = true;
		if ( flags.contains('x') ) { options.create = false; options.write = true; }
		if ( flags.contains('a') ) options.append = true;
		if ( flags.contains('+') ) options.truncate = false;
		if ( flags.contains('s') ) options.sync = true;
		if ( flags.contains('t') ) options.text = true;
		if ( flags.contains('b') ) options.text = false;
		
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
		var o = _native.getStream.call(this, options);
		o.contentConstructor = function() {
			return options.text ? String : Blob;
		};
		return new exports.Stream(o)
	}
	
	if ( block ) {
		var stream = doOpen.call(this);
		try {
			return block.call(this, stream);
		} finally {
			stream.close();
		}
	} else {
		return doOpen.call(this);
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
			if ( this._file.exists() )
				return Boolean(this._file.canWrite());
			var p = this._file.getAbsoluteFile();
			if ( p ) p = p.getParent();
			if ( p )
				return Boolean(p.canWrite());
			return false;
		}
	},
	isExecutable: {
		get: function() {
			return Boolean(this._file.canExecute());
		}
	},
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

