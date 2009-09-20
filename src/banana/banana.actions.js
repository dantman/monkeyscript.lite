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

global.actions = {};

actions.collect = function(registryLoc, bananaLocs) {
	if ( !Kernel.fs.canWrite(registryLoc) )
		Kernel.die("Cannot write to the registry location: " + registryLoc);
	
	var bananas = {};
	
	const required = ['namespace', 'name', 'shortname'];
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
			
			var jsonText = Kernel.fs.readFile(bananaLoc).trim();
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
			var components = plantation.components = { js: [], jars: [] };
			var version = json.version || "";
			
			plantation.name = json.name;
			plantation.shortname = json.shortname;
			plantation.path = Kernel.fs.absoluteDirname(bananaLoc);
			if ( json.components )
				json.components.forEach(function(component) {
					if ( isString(component) ) {
						if ( component.endsWith('.js') ) {
							component = {
								type: "js",
								file: component
							};
						} else if ( component.endsWith('.jar') ) {
							component = {
								type: "jar",
								file: component
							};
						}
					}
					
					if ( component.type === 'js' )
						components.js.push(component.file);
					else if ( component.type === 'jar' )
						components.jars.push(component.file);
				});
			bananas[ns] = bananas[ns] || {};
			bananas[ns][version] = plantation;
			
		}
	
	Kernel.fs.writeFile(registryLoc, JSON.stringify(bananas));
	
};
