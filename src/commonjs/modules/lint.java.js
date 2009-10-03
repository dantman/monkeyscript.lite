// -*- coding: UTF-8 -*-

exports.lint = function lint(code) {
	try {
		org.mozilla.javascript.Context.getCurrentContext().compileString(code, "<lint>", 1, null);
		return true;
	} catch ( e ) {
		throw new SyntaxError(e.message, e.fileName, e.lineNumber);
	}
};

exports.lintFile = function lintFile(path, encoding) {
	try {
		var f = new java.io.File(path);
		var fis = new java.io.FileInputStream(f);
		var reader = encoding ?
			org.monkeyscript.lite.ScriptReader(fis, encoding) :
			org.monkeyscript.lite.ScriptReader(fis);
		org.mozilla.javascript.Context.getCurrentContext().compileReader(reader, path, reader.getFirstLine(), null);
		return true;
	} catch ( e ) {
		throw new SyntaxError(e.message, e.fileName, e.lineNumber);
	}
};

