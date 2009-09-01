// -*- coding: UTF-8 -*-

function path2obj(Path, path) {
	if ( isObject(path) ) {
		return path;
	} else if ( isString(path) ) {
		return Object.seal({
			path: Object.seal(path.split(Path.pathSplitter))
		});
	} else {
		throw new TypeError("Bad input to path");
	}
}

function Path(path) {
	return Object.create(Path.prototype, {
		_path: {
			value: path2obj(Path, path),
			enumerable: false,
			writable: false,
			configurable: false
		}
	});
}
Path.newPathSystem = function newPathSystem(options) {
	function XPathSystem() {
	
	}
	XPathSystem.prototype = Object.create(this.prototype);
	return XPathSystem;
};
Object.defineProperties(Path, {
	listSeparator: {
		value: ':',
		enumerable: false,
		writable: false,
		configurable: false
	},
	pathSeparator: {
		value: '/',
		enumerable: false,
		writable: false,
		configurable: false
	},
	pathSplitter: {
		value: '/',
		enumerable: false,
		writable: false,
		configurable: false
	}
});
Object.defineProperties(Path.prototype, {
	parent: {
		get: function() {
			var p = Object.seal({
				path: this._path.path.slice()
			});
			p.path.pop();
			Object.seal(p.path);
			return this.constructor(p);
		}
	},
	name: {
		get: function() {
			var p = this._path.path;
			var l = p.length;
			return p[l-1] || p[l-2];
		}
	},
	extension: {
		get: function() {
			var ext = this.name.partitionRight('.')[2];
			if ( ext )
				return ext;
			return false;
		}
	}
});
Path.prototype.toString = function toString() {
	return this._path.path.join(this.constructor.pathSeparator);
};
Path.prototype.basename = function basename(ext) {
	var name = this.name;
	var n = name.partitionRight('.');
	return n[2] === ext ? n[1] : name;
};

exports.Path = Path;

