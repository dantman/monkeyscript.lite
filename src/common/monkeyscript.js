
var monkeyscript = {
	version: '0.0.0.0a',
	rc: []
};

(function(_native) {
	delete global._native;
	
	// Arguments
	var args = Array.slice(_native.arguments);
	monkeyscript.scriptName = args.shift();
	//Object.seal(args); // ToDo: Support ES5 seal
	monkeyscript.arguments = args;
	global.arguments = Array.slice(args);
	
	global.sleep = _native.sleep;
	
})(_native);

if ( monkeyscript.scriptName )
	if ( monkeyscript.scriptName.startsWith('javascript:') )
		eval(monkeyscript.scriptName.substr('javascript:'.length));
	else
		exec(monkeyscript.scriptName);
else
	throw new Error("No script specified");

