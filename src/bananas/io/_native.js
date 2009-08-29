({
	File: java.io.File, // We'll just use java.io.File and C side mimic the API
	getStream: function(options) {
		var rb, rt, wb, wt, o = {};
		if ( options.read ) {
			var rb = new java.io.FileInputStream(this._file);
			if ( !options.text ) {
				o.read = function(len, bufNoSkip) {
					if ( len === Infinity )
						len = 512;
					if ( !bufNoSkip )
						return rb.skip(len);
					var bbuf = java.lang.reflect.Array.newInstance(java.lang.Byte.TYPE, len);
					var rLen = rb.read(bbuf, 0, len);
					if ( rLen < 0 )
						return Blob();
					return Blob(java.util.Arrays.copyOf(bbuf, rLen));
				};
			} else {
				var rt = new java.io.BufferedReader(new java.io.InputStreamReader(rb, options.encoding));
				o.read = function(len, bufNoSkip) {
					if ( len === Infinity )
						len = 512;
					if ( !bufNoSkip )
						return rb.skip(len);
					var cbuf = java.lang.reflect.Array.newInstance(java.lang.Character.TYPE, len);
					var rLen = rt.read(cbuf, 0, len);
					if ( rLen < 0 )
						return "";
					return String(new java.lang.String(cbuf, 0, rLen));
				};
			}
		}
		if ( options.write ) {
			var wb = new java.io.FileOutputStream(this._file, options.append);
			if ( options.truncate ) {
				// Truncate when opening
				wb.getChannel().truncate(0);
			}
			function maybeSync() {
				if ( options.sync ) {
					o.flush();
					wb.getFD().sync();
				}
			}
			if ( !options.text ) {
				o.write = function(data) {
					data = data.valueOf();
					if(!(data instanceof Blob))
						throw new TypeError();
					var bbuf = data.toJavaByteArray(); // Extension for rhino environment
					wb.write(bbuf);
					maybeSync();
					return data.length;
				};
				o.flush = function() {
					wb.flush();
				};
			} else {
				var wt = new java.io.BufferedWriter(new java.io.OutputStreamWriter(wb, options.encoding));
				o.write = function(data) {
					data = data.valueOf();
					if(!isString(data))
						throw new TypeError();
					var cbuf = data.toJavaCharArray(); // Extension for rhino environment
					wt.write(cbuf);
					maybeSync();
					return data.length;
				};
				o.flush = function() {
					wt.flush();
				};
			}
		}
		o.close = function() {
			o.flush();
			if ( rt ) rt.close();
			else if ( rb ) rb.close();
			if ( wt ) wt.close();
			else if ( wb ) wb.close();
		};
		return o;
	}
});
