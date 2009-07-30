// -*- coding: UTF-8 -*-

function read(fnRead, len) {
	if ( len === 0 )
		throw new TypeError("Cannot read nothing from a stream");
	
	if ( !len ) {
		var chunk, buf = new Buffer();
		buf.encoding = this.encoding;
		while( chunk = read.call(this, fnRead, Infinity) )
			buf.append(chunk);
		var data = buf.valueOf();
	} else {
		var data = fnRead(this.position, len);
	}
	this.position += data.length;
	return data;
}

function wrapRead(fnRead) {
	return function(len) {
		try {
			return read.call(this, fnRead, len);
		} catch ( e if e === Stream.EOF ) {
			return this.text ? "" : new Blob();
		}
	};
}

function Stream(obj) {
	if (!(this instanceof Stream))
		return new Stream(obj);
	
	if ( isFunction(obj.read) )
		this.read = wrapRead(obj.read);
	if ( isFunction(obj.write) )
		this.write = wrapWrite(obj.write);
	
	this.__defineGetter__('encoding', function() {
		return obj.encoding();
	});
	this.position = 0;
}

Stream.EOF = {eof:true};

Stream.prototype.rewind = function() {
	return this.position = 0;
};
Stream.prototype.__defineGetter__('text', function() {
	this.encoding.lc !== 'binary';
});

Stream.prototype.yank = function(len) {
	if ( !this.read ) // Error when read not implemented
		throw IOError("Stream is not opened for reading");
	if ( typeof len !== 'number' || len === Infinity ) // Without a len .yank is the same as .read
		return this.read();
	
	// Open a buffer of the same type as the stream
	var buf = new Buffer();
	buf.text = this.text;
	
	var max = len;
	do {
		var max = len - buf.length;
		var chunk = this.read(max);
		if ( !chunk )
			break; // EOF can't read anymore
		buf.append(chunk);
	} while( buf.length < max );
	
	return buf.valueOf(); // valueOf returns String for text mode, and Blob for binary mode.
};

exports.Stream = Stream;

