package org.monkeyscript.lite;

import org.mozilla.javascript.*;

public class Kernel {
	
	public static class ExtensibleError implements IdFunctionCall {
		
		protected static final int Id_new_CommonError = 1;
		
		public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (f.hasTag("Global")) {
				int methodId = f.methodId();
				if ( methodId == Id_new_CommonError ) {
					return NativeError.make(cx, scope, f, args);
				}
			}
			throw f.unknown();
		}
		
	}
	
	public static void makeError(Context cx, Scriptable scope, boolean sealed, String name) {
		Scriptable errorProto = ScriptRuntime.newObject(cx, scope, "Error", ScriptRuntime.emptyArgs);
		errorProto.put("name", errorProto, name);
		if (sealed) {
			if (errorProto instanceof ScriptableObject) {
				((ScriptableObject)errorProto).sealObject();
			}
		}
		
		IdFunctionCall xerr = new ExtensibleError();
		IdFunctionObject ctor = new IdFunctionObject(xerr, "Global", ExtensibleError.Id_new_CommonError, name, 1, scope);
		NativeError.make(cx, scope, f, args);
		ctor.markAsConstructor(errorProto);
		if (sealed) {
			ctor.sealObject();
		}
		ctor.exportAsScopeProperty();
	}
	
}
