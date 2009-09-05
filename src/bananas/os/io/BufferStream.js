// -*- coding: UTF-8 -*-

function BufferStream(buf) {
	return new exports.Stream({
		contentConstructor: buf.contentConstructor,
		read: function(len, bufNoSkip) {
			if ( this.position >= buf.length )
				return Stream.EOF;
			if ( !bufNoSkip )
				return { length: len };
			return buf.slice(this.position, len);
		},
		write: function(data) {
			// @todo
		}
	});
}

exports.BufferStream = BufferStream;

