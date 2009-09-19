// -*- coding: UTF-8 -*-

task('commonjs-test', function(options) {
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
