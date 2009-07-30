// -*- coding: UTF-8 -*-

function banana(q) {
	if ( global.banana && global.banana.init )
		banana.init();
	
}
banana.info = function(q) {
	if ( global.banana && global.banana.init )
		banana.init();
	
};
banana.registries = {};
banana.registries.local = [];
banana.registries.global = [];
banana.init = function() {
	var store = banana._store = {};
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

