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

group('test', function(options) {
	task('commonjs', function(options) {
		var {ProcessFunction} = banana('os.process');
		var run = new ProcessFunction("monkeyscript");
		var dir = new java.io.File('commonjs-tests/compliance/');
		Array.forEach(dir.listFiles(), function(testDir) {
			if(testDir.isDirectory()) {
				var t = String(testDir.getAbsolutePath());
				print('//\n// Running compliance test "'+testDir.getName()+'"\n//');
				print(run("--rc", "javascript:require.paths.push('"+t+"');", t+"/program.js").output);
			}
		});
		var dir = new java.io.File('commonjs-tests/extensions/');
		Array.forEach(dir.listFiles(), function(testDir) {
			if(testDir.isDirectory()) {
				var t = String(testDir.getAbsolutePath());
				print('//\n// Running extension test "'+testDir.getName()+'"\n//');
				print(run("--rc", "javascript:require.paths.push('"+t+"');", t+"/program.js").output);
			}
		});
	});
});
