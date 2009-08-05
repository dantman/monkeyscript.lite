// -*- coding: UTF-8 -*-
{

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
	this.scripts = info.components.js.map(function(f) { return info.path + Kernel.os.fileSeparator + f });
};

}
