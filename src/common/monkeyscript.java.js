
// Add some _native stuff so that monkeyscript.js can work the same as if this was done by C
var _native = {
	arguments: _arguments,
	sleep: function sleep(msec) {
		// This isn't working?
		java.lang.Thread.sleep(msec);
	},
	getEnv: function(name) {
		return String(java.lang.System.getenv(name));
	}
};
delete _arguments;

