
function Stream() {
	
}

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


