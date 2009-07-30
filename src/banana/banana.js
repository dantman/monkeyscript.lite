// -*- coding: UTF-8 -*-

exec(__DIR__+'/banana.actions.js');
exec(__DIR__+'/banana.help.js');

var args = Array.slice(arguments);

var action = args.shift();

if(!action) {
	print("No action was specified.");
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
	print("Invalid action specified.");
	Kernel.exit(1);
}

