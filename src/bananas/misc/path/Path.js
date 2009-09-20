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

