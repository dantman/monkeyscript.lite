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

include.once(__DIR__+'/banana.actions.js');
include.once(__DIR__+'/banana.help.js');

var args = Array.slice(arguments);

var action = args.shift();

if(!action) {
	print("No action was specified.");
	print("Valid actions: "+Object.keys(actions).join(', '));
	Kernel.exit(1);
}

switch(action) {
case 'collect':
	if ( !args.length )
		Kernel.exit(help('collect'));
	var registryFile = args.shift();
	var bananaFiles = args;
	if ( !registryFile )
		Kernel.die('Registry file not specified.');
	if ( !bananaFiles.length )
		Kernel.die("Can't build a registry of no bananas.");
	actions.collect(registryFile, bananaFiles);
	break;
default:
	Kernel.die("Invalid action specified.");
}

