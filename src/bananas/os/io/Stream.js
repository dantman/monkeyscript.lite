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

function read(fnRead, len, bufNoSkip) {
	if ( len === 0 )
		throw new TypeError("Cannot read nothing from a stream");
	if ( !len ) {
		if ( bufNoSkip ) {
			var chunk, buf = new Buffer(this.contentConstructor);
			for(;;) {
				chunk = read.call(this, fnRead, Infinity, buf);
				// @todo, handle stuf read into buffer instead of returned
				if ( !chunk || !chunk.length )
					break;
				buf.append(chunk);
			}
			var data = buf.valueOf();
		} else {
			// Skip, buffering creates unwelcome behavior when skipping
			var chunk, len = 0;
			for(;;) {
				chunk = read.call(this, fnRead, Infinity, false);
				if ( !chunk || !chunk.length )
					break;
				len += isNumber(chunk) ? chunk : chunk.length;
			}
			var data = { length: len };
		}
		// Do we need to return and short circut position addition?
	} else {
		var data = fnRead.call(this, len, bufNoSkip);
		if ( data === Stream.EOF )
			data = this.contentConstructor();
	}
	this.position += data.length;
	return data;
}

function wrapRead(fnRead) {
	return function(len) {
		return read.call(this, fnRead, len, true);
	};
}

function wrapSkip(fnRead) {
	return function(len) {
		var n = read.call(this, fnRead, len, false);
		if ( !isNumber(n) )
			// If there is no special handling of .skip data is likely
			// returned instead of a length, pull length from that
			n = n.length;
		return n;
	};
}

function wrapWrite(fnWrite) {
	// @todo
}

function Stream(obj) {
	if (!(this instanceof Stream))
		return new Stream(obj);
	
	if ( isFunction(obj.contentConstructor) )
		this.__defineGetter__('contentConstructor', obj.contentConstructor);
	else if ( obj.contentConstructor === String || obj.contentConstructor === Blob )
		this.__defineGetter__('contentConstructor', function() { return obj.contentConstructor });
	else
		throw new TypeError("Object did not contain contentConstructor function");
	if ( isFunction(obj.read) ) {
		this.read = wrapRead(obj.read);
		this.skip = wrapSkip(obj.read);
	}
	if ( isFunction(obj.write) )
		this.write = wrapWrite(obj.write);
	
	this.position = 0;
}

Stream.EOF = {eof:true};

Stream.prototype.rewind = function() {
	return this.position = 0;
};

Stream.prototype.yank = function(len) {
	if ( !this.read ) // Error when read not implemented
		throw IOError("Stream is not opened for reading");
	if ( typeof len !== 'number' || len === Infinity ) // Without a len .yank is the same as .read
		return this.read();
	
	// Open a buffer of the same type as the stream
	var buf = new Buffer(this.contentConstructor);
	
	var max = len;
	do {
		var max = len - buf.length;
		var chunk = this.read(max);
		if ( !chunk.length )
			break; // EOF can't read anymore
		buf.append(chunk);
	} while( buf.length < max );
	
	return buf.valueOf(); // valueOf returns String for text mode, and Blob for binary mode.
};

Stream.prototype.copy = function(from) {
	if(from instanceof Stream) {
		for(;;) {
			var chunk = from.read(Infinity);
			if ( !chunk.length )
				break;
			while( chunk.length )
				chunk = this.write(chunk);
		}
	} else {
		while( from.length )
			from = this.write(from);
	}
};

exports.Stream = Stream;

