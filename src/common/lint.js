// -*- coding: UTF-8 -*-

var l = require('lint');
var location = arguments[0];
if(!location)
	Kernel.die("No script or code specified");

try {
	if ( location.startsWith("javascript:") )
		l.lint(location.substr('javascript:'.length));
	else
		l.lintFile(location);
	print("No syntax errors detected in "+location);
} catch ( e if e instanceof SyntaxError ) {
	print("SyntaxError: "+e.message+" on line "+e.lineNumber+" of "+e.fileName);
}
