
var monkeyscript = {
	version: '0.0.0.0a',
	hooks: {},
	rc: []
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
	
	//Object.seal(args); // @ES5
	monkeyscript.arguments = args;
	global.arguments = Array.slice(args);
	
	global.Kernel = _native.Kernel;
	//Object.seal(Kernel); // @ES5
	
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
	//Object.seal(monkeyscript.rc); // @ES5
	
})(_native);

// Setup global Error objects
var IOError = Kernel.newError("IOError");

//for each( let rc in monkeyscript.rc ) // Rhino's __iterator__ is broken atm
for ( let k in monkeyscript.rc ) {
	var rc = monkeyscript.rc[k];
	try { exec(rc); }
	// We're just going to ignore IOErrors made by rc scripts
	catch( e if e instanceof IOError ) {}
	// Other errors will be printed, but still not affect running the script
	catch( e ) { print(e); }
}

if ( monkeyscript.hookName ) {
	let scriptName = monkeyscript.hooks[monkeyscript.hookName];
	if ( scriptName )
		exec(scriptName);
	else
		throw new Error("The hook "+monkeyscript.hookName+" does not exist");
} else if ( monkeyscript.scriptName ) {
	if ( monkeyscript.scriptName.startsWith('javascript:') )
		eval(monkeyscript.scriptName.substr('javascript:'.length));
	else
		exec(monkeyscript.scriptName);
} else
	throw new Error("No script specified");

