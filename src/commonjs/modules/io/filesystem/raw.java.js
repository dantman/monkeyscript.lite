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

// java.io.File is our OpaqueLeaf
exports.pathToLeaf = function pathToLeaf(path) {
	if(path instanceof java.io.File)
		return path;
	return new java.io.File(String(path));
};

exports.leafToPath = function leafToPath(leaf) {
	return String(leaf.toString());
};

exports.leafToName = function leafToName(leaf) {
	return String(leaf.getName());
};

// java.io.File is also our OpaqueStat
exports.stat = function stat(leaf) {
	return leaf;
};

exports.canonical = function canonical(leaf) {
	return String(leaf.getCanonicalPath());
};

exports.exists = function exists(stat) {
	return !!stat.exists();
};

exports.size = function size(stat) {
	if(!exports.isFile(stat))
		return false;
	return Number(stat.length());
};

exports.isFile = function isFile(stat) {
	return !!stat.isFile();
};

exports.isDirectory = function isDirectory(stat) {
	return !!stat.isDirectory();
};

exports.lastModified = function lastModified(stat) {
	if(!exports.exists(stat))
		return false;
	return new Date(stat.lastModified());
};

exports.setLastModified = function setLastModified(leaf, time) {
	return !!leaf.setLastModified(time.getTime());
}

exports.isReadable = function isReadable(stat) {
	return !!stat.canRead();
};

exports.isWriteable = function isWriteable(stat) {
	return !!stat.canWrite(); // expand to parent dir?
};

exports.createDirectory = function createDirectory(leaf, recursive) {
	if(recursive)
		return !!leaf.mkdirs();
	else
		return !!leaf.mkdir()
};

exports.listPaths = function listPaths(leaf) {
	return Array.map(leaf.list(), function(fname) { return String(fname); });
};

exports.listPathsByPath = function listPathsByPath(leaf, pathCallback) {
	return Array.map(leaf.list(
		new JavaAdapter(java.lang.Object, java.io.FilenameFilter, {
			accept: function accept(dir, name) {
				return !!pathCallback.call(leaf, dir, String(name));
			}
		})
	), function(fname) { return String(fname); });
};

exports.listLeafs = function listLeafs(leaf) {
	return Array.slice(leaf.listFiles());
};

exports.listLeafsByPath = function listLeafsByPath(leaf, pathCallback) {
	return Array.slice(leaf.listFiles(
		new JavaAdapter(java.lang.Object, java.io.FilenameFilter, {
			accept: function accept(dir, name) {
				return !!pathCallback.call(leaf, dir, String(name));
			}
		})
	));
};

exports.listLeafsByLeaf = // Leaf and Stat are the same in Rhino
exports.listLeafsByStat = function listLeafsByLeaf(leaf, leafCallback) {
	return Array.slice(leaf.listFiles(
		new JavaAdapter(java.lang.Object, java.io.FileFilter, {
			accept: function accept(f) {
				return !!leafCallback.call(leaf, leaf, f);
			}
		})
	));
};

exports.open = function open(leaf, flags) {
	// @todo
	var level0 = {
		contentConstructor: flags.encoding ? String : Blob
	};
	
	var mode = [];
	if(flags.read) mode.push("r");
	if(flags.write) mode.push("w");
	if(!mode.length)
		throw new TypeError(".open called with neither a read or a write flag");
	if(mode.sync) mode.push("s");
	mode = mode.join('');
	
	if ( flags.write && flags.create && !leaf.exists() )
		throw new Error("File does not exist");
	
	var raf = new java.io.RandomAccessFile(leaf, mode);
	var channel = raf.getChannel();
	if(mode.write && mode.truncate)
		channel.truncate(0);
	
	level0.close = function() {
		raf.close();
	};
	
	/*var charset;
	if(flags.encoding) {
		try {
			charset = java.nio.charset.Charset.forName(flags.encoding);
		} catch( e ) {
			throw new Error("Charset ");
		}
		
	}*/
	
	level0.tell = function tell() {
		return raf.getFilePointer();
	};
	level0.seek = function seek(pos) {
		raf.seek(pos);
		return pos;
	};
	
	if ( flags.read ) {
		level0.readChunk = function readChunk(max) {
			if ( max == Infinity )
				max == 512;
			var bbuf = java.lang.reflect.Array.newInstance(java.lang.Byte.TYPE, max);
			var rlen = raf.read(bbuf, 0, max);
			if ( rlen == -1 )
				return Blob();
			return Blob(bbuf, 0, rlen); // MonkeyScript Lite extension
		};
		level0.skip = function skip(max) {
			if ( max == Infinity )
				max == 512;
			return raf.skipBytes(max);
		};
	}
	if ( flags.write ) {
		level0.write = function(data, off, len) {
			if ( flags.append ) {
				var oldPos = raf.getFilePointer();
				raf.seek(raf.length());
			}
			var bbuf = raf.toJavaByteArray(); // MonkeyScript Lite extension
			raf.write(bbuf, off, len);
			if ( flags.append )
				raf.seek(oldPos);
		};
		level0.flush = function() {
			raf.getChannel().force(true);
		};
	}
	
	return level0;
};

exports.remove = function remove(leaf) {
	return !!leaf['delete']();
};

exports.move = function move(from, to) {
	return !!from.renameTo(to);
};

