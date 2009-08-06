package org.monkeyscript.lite;

import org.mozilla.javascript.*;

public class Kernel extends ScriptableObject {
	
	static void init(Scriptable scope, boolean sealed) {
		ScriptableObject obj = (ScriptableObject)scope;
		Kernel kernel = new Kernel();
		obj.defineProperty("Kernel", kernel, READONLY|PERMANENT);
		if (sealed)
			obj.sealObject();
	}
	
	public String getClassName() {
		return "Kernel";
	}
	
	public Scriptable getPrototype() {
		return null;
	}
	
	public Kernel() {
		String[] functions = {
			"valueOf"
		};
		defineFunctionProperties(functions, Kernel.class, ScriptableObject.DONTENUM);
	}
	
	public String toString() {
		return "[object Kernel]";
	}
	
	public static Object valueOf(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		return thisObj;
	}
	
}
