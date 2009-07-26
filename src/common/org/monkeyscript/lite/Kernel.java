package org.monkeyscript.lite;

import org.mozilla.javascript.*;

public class Kernel {
	
	public static void makeError(Context cx, Scriptable scope, boolean sealed, String name) {
		/*Scriptable errorProto = ScriptRuntime.newObject(cx, scope, "Error", ScriptRuntime.emptyArgs);
		errorProto.put("name", errorProto, name);
		if (sealed) {
			if (errorProto instanceof ScriptableObject) {
				((ScriptableObject)errorProto).sealObject();
			}
		}
		IdFunctionObject ctor = new IdFunctionObject(obj, "Global", Id_new_CommonError, name, 1, scope);
		NativeError.make(cx, scope, f, args);
		ctor.markAsConstructor(errorProto);
		if (sealed) {
			ctor.sealObject();
		}
		ctor.exportAsScopeProperty();*/
	}
	
}
