// -*- coding: UTF-8 -*-

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
};

exports.remove = function remove(leaf) {
	return !!leaf['delete']();
};

exports.move = function move(from, to) {
	return !!from.renameTo(to);
};

