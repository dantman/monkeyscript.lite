// -*- coding: UTF-8 -*-

function Process() {
	
}

function ProcessFunction(executable) {
	// @todo Develop lang.javascript.xobject and use that to have full control over instanceof, etc...
	function exe() {
		// We're going to lazily be rhino dependent until we write the actual Process object
		var cmd = [executable].concat(Array.slice(arguments)).toJavaArray(java.lang.String);
		var p = java.lang.Runtime.getRuntime().exec(cmd, null, new java.io.File(Kernel.currentWorkingDirectory));
		var exit = p.waitFor();
		var o = {
			exit: exit,
			ok: exit === 0,
			get output() {
				var r = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream(), Kernel.os.encoding));
				var buf = new StringBuffer();
				var cbuf = java.lang.reflect.Array.newInstance(java.lang.Character.TYPE, 512);
				for(;;) {
					var len = r.read(cbuf);
					if ( len === -1 )
						break;
					buf.append(String(new java.lang.String(cbuf, 0, len)));
				}
				var out = buf.toString();
				delete o.output;
				o.output = out;
				return out;
			},
			get errors() {
				var r = new java.io.BufferedReader(new java.io.InputStreamReader(p.getErrorStream(), Kernel.os.encoding));
				var buf = new StringBuffer();
				var cbuf = java.lang.reflect.Array.newInstance(java.lang.Character.TYPE, 512);
				for(;;) {
					var len = r.read(cbuf);
					if ( len === -1 )
						break;
					buf.append(String(new java.lang.String(cbuf, 0, len)));
				}
				var out = buf.toString();
				delete o.errors;
				o.errors = out;
				return out;
			}
		};
		return o;
	}
	Object.defineConstant(exe, "executable", executable, true);
	return exe;
}

exports.Process = Process;
exports.ProcessFunction = ProcessFunction;

