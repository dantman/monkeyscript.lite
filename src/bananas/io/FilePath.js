// -*- coding: UTF-8 -*-
var {Path} = banana('misc.path');

var FilePath = Path.newPathSystem({
	listSeparator: Kernel.os.pathSeparator,
	pathSeparator: Kernel.os.fileSeparator,
	pathSplitter: /[^\/\\]/,
	base: function() {
		return Kernel.currentWorkingDirectory;
	},
	// ~/
	home: function() {
		return Kernel.home;
	},
	// ~user/
	user: function(user) {
		
	}
});

FilePath.prototype.cwd = FilePath.prototype.base;


exports.FilePath = FilePath;

