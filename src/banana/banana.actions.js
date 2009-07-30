// -*- coding: UTF-8 -*-

var actions = {};

actions.collect = function(registryLoc, bananaLocs) {
	if ( !Kernel.fs.canWrite(registryLoc) )
		Kernel.die("Cannot write to the registry location: " + registryLoc);
	
	var bananas = {};
	
	const required = ['namespace', 'name', 'short'];
	pick:
		for ( var i = 0; i < bananaLocs.length; i++ ) {
			var bananaLoc = bananaLocs[i];
			if ( !Kernel.fs.exists(bananaLoc) ) {
				print("Warning: "+bananaLoc+" does not exist, skipping banana");
				continue;
			}
			if ( !Kernel.fs.canRead(bananaLoc) ) {
				print("Warning: Cannot read "+bananaLoc+", skipping banana");
				continue;
			}
			
			var jsonText = Kernel.fs.readFile(bananaLoc);
			var json = JSON.parse(jsonText);
			
			if ( !json ) {
				print("Warning: Invalid json in "+bananaLoc+", skipping banana");
				continue;
			}
			
			var ns = json.namespace;
			if ( ns in bananas ) {
				print("Warning: Cannot redeclare namespace "+ns+", skipping banana "+bananaLoc);
				continue;
			}
			for ( var k in required ) {
				if ( required[k] in json )
					continue;
				print("Warning: Banana metadata "+bananaLoc+" is missing required key "+required[k]+", skipping banana");
				continue pick;
			}
			
			var plantation = {};
			var components = plantation.components = { js: [] };
			var version = json.version || "";
			
			plantation.name = json.name;
			plantation['short'] = json['short'];
			plantation.path = Kernel.fs.absoluteDirname(bananaLoc);
			if ( json.components )
				json.components.forEach(function(component) {
					if ( isString(component) ) {
						component.endsWith('.js');
						component = {
							type: "js",
							file: component
						};
					}
					
					if ( component.type === 'js' )
						components.js.push(component.file);
				});
			bananas[ns] = bananas[ns] || {};
			bananas[ns][version] = plantation;
			
		}
	
	Kernel.fs.writeFile(registryLoc, JSON.stringify(bananas));
	
};
