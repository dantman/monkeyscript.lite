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

function ProcessFunction(executable) {
	// @todo Develop lang.javascript.xobject and use that to have full control over instanceof, etc...
	function exe() {
		// We're going to lazily be rhino dependent until we write the actual Process object
		var options = {};
		var args = Array.slice(arguments);
		if ( isObject(args[0]) ) {
			options = args.shift();
		}
		var cmd = [executable].concat(args).toJavaArray(java.lang.String);
		var p = java.lang.Runtime.getRuntime().exec(cmd, null, new java.io.File(options.directory||Kernel.currentWorkingDirectory));
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
			},
			orDie: function orDie() {
				if ( !o.ok )
					Kernel.die(o.errors);
				return o.output;
			}
		};
		return o;
	}
	exe.assert = function assert() {
		var o = exe.apply(this, arguments);
		if ( !o.ok )
			throw o.errors;
		return o.output;
	};
	Object.defineConstant(exe, "executable", executable, true);
	return exe;
}

exports.ProcessFunction = ProcessFunction;

