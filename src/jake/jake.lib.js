// -*- coding: UTF-8 -*-

{
	
	let runActions = [];
	let options = {};
	var args = arguments.slice();
	for(;args.length;) {
		var action = args.shift();
		if ( action.startsWith('-') ) {
			if ( action == "-C" ) {
				Kernel.currentWorkingDirectory = args.shift();
			}
		} else if ( action.contains('=') ) {
			var [option, ,value] = action.partition('=');
			options[option] = value;
		} else {
			runActions.push(action);
		}
	}
	
	
	let gstack = [];
	let tasks = {};
	let tasknames = {};
	let groups = {}
	
	function task(name) {
		if ( !isString(name) )
			throw new TypeError("Invalid Jake task name");
		if ( gstack.length )
			name = gstack.join('.').concat('.').concat(name);
		var cname = name.toLowerCase();
		var deps = Array.slice(arguments, 1).flat();
		tasknames[cname] = name;
		if ( cname in tasks )
			print("WARNING: Redefining task "+cname);
		tasks[cname] = deps;
	}

	function group(name, fn) {
		if ( !isString(name) )
			throw new TypeError("Invalid Jake group name");
		if ( gstack.length )
			name = gstack.join('.').concat('.').concat(name);
		var cname = name.toLowerCase();
		if ( cname in groups )
			print("WARNING: Redefining group "+cname);
		groups[cname] = fn;
	}
	
	function runGroup(name, options) {
		name = name.toLowerCase();
		gstack.push(name);
		groups[name](options);
		gstack.pop();
	}
	
	function alltasks() {
		//return Object.values(tasknames);
		var run = {}, newGroups;
		for(;;) {
			newGroups = false;
			for ( var group in groups ) {
				if ( group in run )
					continue;
				newGroups = true;
				runGroup(group, false);
				run[group] = true;
			}
			if ( !newGroups )
				break;
		}
	}
	
	let tasksRun = {};
	function runTask(task, force) {
		var task = task.toLowerCase();
		var t = tasks[task];
		if ( !t ) {
			Kernel.die("No task by the name "+task+" could be found");
		}
		if ( !force && task in tasksRun )
			return;
		t.forEach(function(dep) {
			if ( isFunction(dep) ) {
				dep(options);
			} else if ( isString(dep) ) {
				runTask(dep, force);
			}
		});
	}
	
	function _runTasks() {
		if ( runActions.length )
			runActions.forEach(function(task) {
				runTask(task);
			});
		else {
			runTask("default");
		}
	}
	
}
