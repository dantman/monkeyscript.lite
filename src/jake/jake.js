// -*- coding: UTF-8 -*-

// @todo Mature the io banana and migrate Jake to use it completely

include(__DIR__+"/jake.lib.js");

var jakefile = Kernel.currentWorkingDirectory + Kernel.os.fileSeparator + "Jake.js";
if ( !Kernel.fs.canRead(jakefile) )
	Kernel.die("Jake.js cannot be found in or read from the current directory.");

include(jakefile);

_runTasks();
