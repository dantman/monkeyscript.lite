// -*- coding: UTF-8 -*-
{

let store;
let getInfo = function(q) {
	if ( global.banana && global.banana.init )
		banana.init();
	var ns = q;
	// ToDo: Version comparison
	var versions = Object.keys(store[ns]);
	versions.sort(); // ToDo: Use a proper version sort library
	var version = versions[versions.length-1];
	
	return store[ns][version];
};
let bananaMeta = function(info) {
	
	
};

function banana(q) {
	var info = getInfo(q);
	
	
	
}
banana.info = function(q) {
	return bananaMeta(getInfo(q));
};
banana.registries = {};
banana.registries.local = [];
banana.registries.global = [];
banana.init = function() {
	store = {};
	banana.registries.local.concat(banana.registries.global)
		.forEach(function(registryLocation) {
			var json = JSON.parse(Kernel.readFile(registryLocation));
			for ( var ns in json ) {
				for ( var version in json[ns] ) {
					store[ns] = store[ns] || {};
					if ( version in store[ns] )
						continue;
					store[ns][version] = json[ns][version];
				}
			}
		});
	delete banana.init;
};

}
