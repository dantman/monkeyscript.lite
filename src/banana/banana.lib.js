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

{

var LoadError = Kernel.newError("LoadError");

let store;
let getInfo = function(q) {
	if ( banana.init )
		banana.init();
	var ns = q;
	if (!(ns in store)) return false;
	// ToDo: Version comparison
	var versions = Object.keys(store[ns]);
	versions.sort(); // ToDo: Use a proper version sort library
	var version = versions[versions.length-1];
	
	return store[ns][version];
};

function banana(q) {
	var info = getInfo(q);
	if ( !info )
		throw new LoadError("Failed to load "+q);
	var self = new banana.Meta(info);
	
	var _native = {}, exports = {};
	if ( Kernel.platform.name === "Rhino" ) {
		var _jar;
		if ( self.jars.length ) {
			let urls = java.lang.reflect.Array.newInstance(java.net.URL, self.jars.length);
			self.jars.forEach(function(path, i) {
				var file = new java.io.File(path);
				var url = file.toURI().toURL();
				urls[i] = url;
			});
			var cl = new java.net.URLClassLoader(urls);
			_jar = new JavaTopPackage(cl);
		}
		
		// We're using Rhino, for native we use a _native.js in the same dir as the banana
		if ( Kernel.fs.canRead(self.path+'/_native.js') ) {
			var fn = Kernel.globalExecWrapped(self.path+'/_native.js', '(function(_native, _jar, banana, self, exports) {', '//*/\n;return _native;\n})');
			_native = fn.call(undefined, _native, _jar, banana, self, exports);
		}
	}
	for each ( script in self.scripts ) {
		// var arguments = global.arguments;?
		var fn = Kernel.globalExecWrapped(script, '(function(_native, banana, self, exports) {', '//*/\n;return exports;\n})');
		if ( !isFunction(fn) )
			throw Error("Something went wrong when loading "+q+" script "+script);
		//exports = fn.call(self, _native, banana, self, exports);
		exports = fn.call(undefined, _native, banana, self, exports);
	}
	if ( !isObject(exports) )
		throw Error("Something went wrong when loading "+q+" one of the scripts screwed up the exports object");
	
	return exports;
}
banana.meta = function(q) {
	return new banana.Meta(getInfo(q));
};
banana.registries = {};
banana.registries.local = [];
banana.registries.global = [];
banana.init = function() {
	store = {};
	banana.registries.local.concat(banana.registries.global)
		.forEach(function(registryLocation) {
			var json = JSON.parse(Kernel.fs.readFile(registryLocation));
			for ( var ns in json ) {
				for ( var version in json[ns] ) {
					store[ns] = store[ns] || {};
					if ( version in store[ns] )
						continue;
					json[ns][version].ns = ns;
					store[ns][version] = json[ns][version];
				}
			}
		});
	delete banana.init;
};
banana.Meta = function(info) {
	if (!(this instanceof banana.Meta)) throw new Error();
	if ( !info ) return false;
	this.namespace = info.ns;
	this.name = info.name;
	this.shortname = info.shortname;
	this.path = info.path;
	this.jars = info.components.jars.map(function(f) { return info.path + Kernel.os.fileSeparator + f });
	this.scripts = info.components.js.map(function(f) { return info.path + Kernel.os.fileSeparator + f });
};

}
