package org.monkeyscript.lite;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.mozilla.javascript.*;

public class Kernel extends ScriptableObject {
	
	static void init(Scriptable scope, boolean sealed) {
		ScriptableObject obj = (ScriptableObject)scope;
		Kernel kernel = new Kernel();
		kernel.setParentScope(scope);
		obj.defineProperty("Kernel", kernel, READONLY|PERMANENT);
		if (sealed)
			kernel.sealObject();
	}
	
	public String getClassName() {
		return "Kernel";
	}
	
	public Scriptable getPrototype() {
		return null;
	}
	
	public Kernel() {
		String[] functions = {
			"valueOf",
			"globalExecWrapped"
		};
		defineFunctionProperties(functions, Kernel.class, ScriptableObject.DONTENUM);
	}
	
	public static Object valueOf(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		return thisObj;
	}
	
	public static Object globalExecWrapped( Context cx, Scriptable thisObj, Object[] args, Function funObj ) {
		Scriptable scope = ScriptableObject.getTopLevelScope((ScriptableObject)funObj);
		if ( args.length == 0 )
			throw ScriptRuntime.typeError("No script name");
		String scriptFileName = (String)args[0];
		String prefix = args.length > 1 ? (String)args[1] : "";
		String suffix = args.length > 2 ? (String)args[2] : "";
		File scriptFile = new File(scriptFileName);
		try {
			if ( scriptFile.canRead() ) {
				FileInputStream is = new FileInputStream(scriptFile);
				ScriptReader in = new ScriptReader(is, prefix, suffix);
				return cx.evaluateReader( scope, in, scriptFile.getAbsolutePath(), in.getFirstLine(), null );
			} else {
				throw Global.jsIOError("Script "+scriptFileName+" does not exist or cannot be read");
			}
		} catch( FileNotFoundException e ) {
			throw Global.jsIOError("Script "+scriptFileName+" does not exist");
		} catch( UnsupportedEncodingException e ) {
			throw Global.jsIOError("Unsupported character encoding: " + e.getMessage());
		} catch( IOException e ) {
			throw Global.jsIOError(e.getMessage());
		}
	}
	
}
