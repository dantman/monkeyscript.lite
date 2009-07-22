
var monkeyscript = {
	version: '0.0.0.0a',
	rc: []
};

(function(_native) {
	delete global._native;
	
	// Arguments
	var args = Array.slice(_native.arguments);
	monkeyscript.scriptName = args.shift();
	//Object.seal(args); // @ES5
	monkeyscript.arguments = args;
	global.arguments = Array.slice(args);
	
	global.Kernel = _native.Kernel;
	//Object.seal(Kernel); // @ES5
})(_native);

if ( monkeyscript.scriptName )
	if ( monkeyscript.scriptName.startsWith('javascript:') )
		eval(monkeyscript.scriptName.substr('javascript:'.length));
	else
		exec(monkeyscript.scriptName);
else
	throw new Error("No script specified");

