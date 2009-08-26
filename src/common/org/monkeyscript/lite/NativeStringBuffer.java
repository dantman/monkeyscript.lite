package org.monkeyscript.lite;

import java.util.Arrays;

import org.mozilla.javascript.*;

// org.mozilla.javascript.NativeString was used as a reference for implementation of this
final class NativeStringBuffer extends AbstractBuffer {
	
	static final long serialVersionUID = 1251247849L;
	
	private static final Object STRING_BUFFER_TAG = "StringBuffer";
	
	static void init(Scriptable proto, Scriptable scope, boolean sealed) {
		NativeStringBuffer obj = NativeStringBuffer.newEmpty();
		obj.exportAsJSClass(MAX_PROTOTYPE_ID, scope, sealed);
		obj.setPrototype(proto);
	}
	
	static private NativeStringBuffer newEmpty() {
		return new NativeStringBuffer(0);
	}
	
	private NativeStringBuffer(int len) {
		this.length = len;
		this.chars = new char[0];
	}
	
	private NativeStringBuffer(String str) {
		this.length = str.length();
		this.chars = str.toCharArray();
	}
	
	private NativeStringBuffer(char[] chars) {
		this.chars = chars;
		this.length = chars.length;
	}
	
	static private Object quickNewNativeStringBuffer(Context cx, Scriptable scope, Object target) {
		return ScriptRuntime.newObject(cx, scope, "StringBuffer", new Object[] { target });
	}
	
	@Override
	public String getClassName() {
		return "StringBuffer";
	}
	
	private static final int
		Id_length                    =  1,
		Id_contentConstructor        =  2,
		MAX_INSTANCE_ID              =  2;
	
	@Override
	protected int getMaxInstanceId() {
		return MAX_INSTANCE_ID;
	}
	
	@Override
	protected int findInstanceIdInfo(String s) {
		if (s.equals("length")) {
			return instanceIdInfo(DONTENUM, Id_length);
		}
		if (s.equals("contentConstructor")) {
			return instanceIdInfo(DONTENUM|READONLY|PERMANENT, Id_contentConstructor);
		}
		return super.findInstanceIdInfo(s);
	}
	
	@Override
	protected String getInstanceIdName(int id) {
		if (id == Id_length) { return "length"; }
		if (id == Id_contentConstructor) { return "contentConstructor"; }
		return super.getInstanceIdName(id);
	}
	
	@Override
	protected Object getInstanceIdValue(int id) {
		if (id == Id_length) {
			return ScriptRuntime.wrapInt(chars.length);
		}
		if (id == Id_contentConstructor) {
			return ScriptRuntime.getTopLevelProp(this.getParentScope(), "String");
		}
		return super.getInstanceIdValue(id);
	}
	
	@Override
	protected void setInstanceIdValue(int id, Object value) {
		if (id == Id_length) {
			setLength(value);
			return;
		}
		super.setInstanceIdValue(id, value);
	}
	
	@Override
	protected void fillConstructorProperties(IdFunctionObject ctor) {
		super.fillConstructorProperties(ctor);
	}
	
	@Override
	protected void initPrototypeId(int id) {
		String s;
		int arity;
		switch (id) {
			case Id_constructor:       arity=1; s="constructor";       break;
			case Id_toString:          arity=0; s="toString";          break;
			case Id_toSource:          arity=0; s="toSource";          break;
//			case Id_slice:             arity=2; s="slice";             break;
//			case Id_equals:            arity=1; s="equals";            break;
			default: throw new IllegalArgumentException(String.valueOf(id));
		}
		initPrototypeMethod(STRING_BUFFER_TAG, id, s, arity);
	}
	
	private static NativeStringBuffer realThis(Object thisObj, IdFunctionObject f) {
		if (!(thisObj instanceof NativeStringBuffer))
			throw incompatibleCallError(f);
		return (NativeStringBuffer)thisObj;
	}
	
    public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (!f.hasTag(STRING_BUFFER_TAG)) {
			return super.execIdCall(f, cx, scope, thisObj, args);
		}
		int id = f.methodId();
		again:
			for(;;) {
				switch (id) {
					case Id_constructor: {
						boolean inNewExpr = (thisObj == null);
						if (!inNewExpr) {
							// IdFunctionObject.construct will set up parent, proto
							return f.construct(cx, scope, args);
						}
						return jsConstructor(cx, scope, args);
					}
					case Id_toString:
						return thisObj.toString();
					
					case Id_valueOf:
						return realThis(thisObj, f);
					
					case Id_toSource: {
						return "(new StringBuffer(\""+ScriptRuntime.escapeString(realThis(thisObj, f).toString())+"\"))";
					}
				}
				throw new IllegalArgumentException(String.valueOf(id));
			}
	}
	
	@Override
	public String toString() {
		return new String(chars, length).intern();
	}
	
	/* Make array-style property lookup work for strings. */
	@Override
	public Object get(int index, Scriptable start) {
		if (0 <= index && index < string.length()) {
			return toString().substring(index, index + 1);
		}
		return super.get(index, start);
	}
	
	@Override
	public void put(int index, Scriptable start, Object value) {
		if (0 <= index && index < string.length()) {
			// @todo
			return;
		}
		super.put(index, start, value);
	}
	
	
	
	private long length;
	private char[] chars;
}
