
Object.defineConstant = function defineConstant(obj, prop, value, enumerable) {
	return Object.defineProperty(obj, prop, { value:value, enumerable:enumerable===true, writable:false, configurable:false });
};

var monkeyscript = {
	version: '0.0.0.0a',
	hooks: {},
	rc: [],
	included: [],
	platform: ["MonkeyScript", ( Kernel.platform.name === "Rhino" ? " Lite" : "" ), " using engine ", Kernel.platform, " on host ", Kernel.host].join('') 
};

(function(_native) {
	delete global._native;
	
	// RC Scripts
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
	
	// Arguments
	var args = Array.slice(_native.arguments);
	
	function handleArg(name) {
		switch(name) {
		case 'l':
		case 'lint':
			// Lint a file
			Kernel.die("MonkeyScript lint support has not been implemented yet");
			break;
		case 'i':
		case 'a': // php uses -a sortof abnormally, support it for the hell of it
		case 'interactive':
		case 'repl':
			// Run a hook
			monkeyscript.hookName = "interactive";
			handleArg.needScript = false;
			break;
		case 'h':
		case 'hook':
			// Run a hook
			monkeyscript.hookName = args.shift();
			handleArg.needScript = false;
			break;
		case 'no-rc':
			// Empty out the list of rc files
			monkeyscript.rc.clear();
			break;
		case 'rc':
			// Add a new rc file to run
			monkeyscript.rc.push(args.shift());
			break;
		default:
			Kernel.die('Unknown argument "'+name+'".');
		}
	}
	handleArg.needScript = true;
	argsloop: for(var arg;args.length;) {
		arg = args.shift();
		if ( arg === "--" ) {
			monkeyscript.scriptName = args.shift();
			break;
		} else if ( arg.length > 2 && arg.startsWith("--") ) {
			handleArg(arg.substr(2));
		} else if ( arg.length > 1 && arg.startsWith("-") ) {
			arg.substr(1).split('').forEach(handleArg);
		} else {
			if ( handleArg.needScript )
				monkeyscript.scriptName = arg;
			else
				args.unshift(arg);
			break;
		}
	}
	
	Object.defineProperty(monkeyscript, "arguments", { value: args, writable: false, configurable: false });
	global.arguments = Array.slice(args);
	
	global.Kernel = _native.Kernel;
	
	// Seal things
	Object.seal(args);
	Object.seal(Kernel);
	Object.seal(monkeyscript.rc);
	
})(_native);

// Setup global Error objects
var IOError = Kernel.newError("IOError");

for each( let rc in monkeyscript.rc ) {
	try {
		if ( rc.startsWith('javascript:') )
			eval(rc.substr('javascript:'.length));
		else
			include.ifExists(rc);
	}
	// Other errors will be printed, but still not affect running the script
	catch( e ) { print(e); }
}

if ( monkeyscript.hookName ) {
	monkeyscript.scriptName = monkeyscript.hooks[monkeyscript.hookName];
	if ( !monkeyscript.scriptName )
		Kernel.die("The hook "+monkeyscript.hookName+" does not exist");
}
if ( monkeyscript.scriptName ) {
	if ( isFunction(monkeyscript.scriptName) )
		monkeyscript.scriptName.apply(undefined, arguments);
	else if ( monkeyscript.scriptName.startsWith('javascript:') )
		eval(monkeyscript.scriptName.substr('javascript:'.length));
	else {
		if ( !Kernel.fs.exists(monkeyscript.scriptName) )
			Kernel.die("The script "+monkeyscript.scriptName+" does not exist");
		include(monkeyscript.scriptName);
	}
} else
	Kernel.die("No script specified");

