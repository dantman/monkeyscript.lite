package org.monkeyscript.lite;

import java.util.Arrays;

import org.mozilla.javascript.*;

// org.mozilla.javascript.NativeString was used as a reference for implementation of this
abstract class AbstractBuffer extends IdScriptableObject {
	
	abstract protected String getTypeTag();
	
	protected static final int
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
	public String getClassName() {
		return getTypeTag() + "Buffer";
	}
	
	@Override
	public String toString() {
		return "[" + getTypeTag() + "Buffer length=" + length + "]";
	}
	
	@Override
	protected Object getInstanceIdValue(int id) {
		if (id == Id_length) {
			return ScriptRuntime.wrapInt((int)length);
		}
		if (id == Id_contentConstructor) {
			return ScriptRuntime.getTopLevelProp(this.getParentScope(), getTypeTag());
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
	
    public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		int id = f.methodId();
		again:
			for(;;) {
				switch (id) {
					case Id_toString:
						return thisObj.toString();
					
					case Id_equals:
						return js_equals(cx, scope, thisObj, args);
				}
				throw new IllegalArgumentException(String.valueOf(id));
			}
	}
	
	abstract protected Object jsConstructor(Context cx, Scriptable scope, Object[] args);
	
	protected static Object js_equals(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if ( thisObj instanceof AbstractBuffer )
			return ScriptRuntime.wrapBoolean(((AbstractBuffer)thisObj).rawEquals(args[0]));
		return false;
	}
	
	abstract protected boolean rawEquals(Object obj);
	
	protected void setLength(Object len) { setLength(ScriptRuntime.toInt32(len)); }
	protected void setLength(long len) { setLength((int)len); }
	protected void setLength(int len) {
		// @todo
	}
	
	protected void truncateRaw(long len) { truncateRaw((int)len); }
	abstract protected void truncateRaw(int len);
	
// #string_id_map#
	
	@Override
	protected int findPrototypeId(String s) {
		int id;
// #generated# Last update: 2009-08-25 18:31:58 PDT
        L0: { id = 0; String X = null; int c;
            L: switch (s.length()) {
            case 6: X="equals";id=Id_equals; break L;
            case 7: X="valueOf";id=Id_valueOf; break L;
            case 8: c=s.charAt(3);
                if (c=='o') { X="toSource";id=Id_toSource; }
                else if (c=='t') { X="toString";id=Id_toString; }
                break L;
            case 11: X="constructor";id=Id_constructor; break L;
            }
            if (X!=null && X!=s && !X.equals(s)) id = 0;
            break L0;
        }
// #/generated#
		return id;
	}
	
    protected static final int
		Id_constructor               = 1,
		Id_toString                  = 2,
		Id_toSource                  = 3,
		Id_valueOf                   = 4,
		Id_equals                    = 5,
		MAX_PROTOTYPE_ID             = 5;
	
// #/string_id_map#
	
	protected long length;
}
