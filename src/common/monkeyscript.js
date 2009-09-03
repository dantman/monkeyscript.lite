
Object.defineConstant = function defineConstant(obj, prop, value, enumerable) {
	return Object.defineProperty(obj, prop, { value:value, enumerable:enumerable===true, writable:false, configurable:false });
};

var monkeyscript = {
	version: '0.0.0.0a',
	hooks: {},
	rc: [],
	included: []
};

(function(_native) {
	delete global._native;
	
	// Arguments
	var args = Array.slice(_native.arguments);
	if ( args[0] === '-h' ) {
		args.shift();
		monkeyscript.hookName = args.shift();
	} else {
		monkeyscript.scriptName = args.shift();
	}
	
	Object.seal(args);
	monkeyscript.arguments = args;
	global.arguments = Array.slice(args);
	
	global.Kernel = _native.Kernel;
	Object.seal(Kernel);
	
	monkeyscript.rc.push(
		Kernel.configDir + Kernel.os.fileSeparator + 'monkeyscriptrc.js',
		Kernel.configDir + Kernel.os.fileSeparator + 'monkeyscriptrc'
	);
	var baseNames = ['.monkeyscriptrc.js', '.monkeyscriptrc'];
	[
		Kernel.home,
		Kernel.currentWorkingDirectory
	].forEach(function(basePath) {
		baseNames.forEach(function(baseName) {
			monkeyscript.rc.push( basePath + Kernel.os.fileSeparator + baseName );
		});
	});
	Object.seal(monkeyscript.rc);
	
})(_native);

// Setup global Error objects
var IOError = Kernel.newError("IOError");

for each( let rc in monkeyscript.rc ) {
	try { include.ifExists(rc); }
	// Other errors will be printed, but still not affect running the script
	catch( e ) { print(e); }
}

if ( monkeyscript.hookName ) {
	let scriptName = monkeyscript.hooks[monkeyscript.hookName];
	if ( scriptName )
		include(scriptName);
	else
		throw new Error("The hook "+monkeyscript.hookName+" does not exist");
} else if ( monkeyscript.scriptName ) {
	if ( monkeyscript.scriptName.startsWith('javascript:') )
		eval(monkeyscript.scriptName.substr('javascript:'.length));
	else
		include(monkeyscript.scriptName);
} else
	throw new Error("No script specified");

