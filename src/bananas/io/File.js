// -*- coding: UTF-8 -*-

function File(path) {
	
}

File.prototype.open = function() {
	var block;
	if ( isObject(arguments[0]) ) {
		var [options, block] = arguments[1];
	} else {
		var options = {};
		var args = Array.slice(arguments);
		var flags = args.shift();
		
		if ( flags.has('r') ) options.read = true;
		if ( flags.has('w') ) options.write = true;
		if ( flags.has('x') ) { options.create = false; options.write = true; }
		if ( flags.has('a') ) options.append = true;
		if ( flags.has('+') ) options.truncate = false;
		if ( flags.has('s') ) options.sync = true;
		if ( flags.has('t') ) options.text = true;
		if ( flags.has('b') ) options.text = false;
		
		if ( isString(args[0]) && /^([-r][-w][-x]){3}$|^([augo]+[-+][rwxa]+)+$/.test(args[0]) )
			options.encoding = args.shift();
		
		if ( isNumber(args[0]) || isString(args[0]) )
			options.access = args.shift();
		
		if ( isFunction(args[0]) ) {
			var block = args.shift();
		}
	}
	if ( options.append ) options.write = true;
	if ( options.write && !options.hasOwnProperty('create') ) options.create = true;
	if ( !options.hasOwnProperty('truncate') ) options.truncate = true; // Defaults to true
	if ( !options.hasOwnProperty('text') ) options.text = true; // Defaults to true
	if ( options.encoding && options.encoding.lc !== 'binary' ) {
		options.text = true;
	} else {
		//options.text = false;
		//options.encoding = 'binary';
	}
	options.encoding = options.encoding || Kernel.systemEncoding;
	
	function doOpen() {
		
	}
	
	if ( block ) {
		var stream = doOpen();
		try {
			return block.call(this, stream);
		} finally {
			stream.close();
		}
	} else {
		return doOpen();
	}
};

File.open = function(path) {
	
};

