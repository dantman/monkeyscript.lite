package org.monkeyscript.lite;

import java.util.Arrays;

import org.mozilla.javascript.*;

// org.mozilla.javascript.NativeString was used as a reference for implementation of this
final class NativeBuffer extends IdScriptableObject {
	
	static final long serialVersionUID = L;
	
	private static final Object BUFFER_TAG = "Buffer";
	
	static void init(Scriptable scope, boolean sealed) {
		NativeBuffer obj = NativeBuffer.newEmpty();
		obj.exportAsJSClass(MAX_PROTOTYPE_ID, scope, sealed);
	}
	
	static private NativeBuffer newEmpty() {
		return new NativeBuffer(0);
	}
	
	private NativeBuffer(int len) {
		length = len;
		type = UNTYPED;
	}
	
	private NativeBuffer(byte b) {
		bytes = new byte[1];
		bytes[0] = b;
		length = bytes.length;
		type = BINARY;
	}
	
	private NativeBuffer(byte[] b) {
		bytes = b;
		length = b.length;
		type = BINARY;
	}
	
	private NativeBuffer(char c) {
		chars = new char[1];
		chars[0] = b;
		length = chars.length;
		type = TEXT;
	}
	
	private NativeBuffer(char[] c) {
		chars = c;
		length = c.length;
		type = TEXT;
	}
	
	static private Object quickNewNativeBuffer(Context cx, Scriptable scope, Object target) {
		return ScriptRuntime.newObject(cx, scope, "Buffer", new Object[] { target });
	}
	
	@Override
	public String getClassName() {
		return "Buffer";
	}
	
	private static final int
		Id_length                    =  1,
		Id_text                      =  2,
		MAX_INSTANCE_ID              =  2;
	
	@Override
	protected int getMaxInstanceId() {
		return MAX_INSTANCE_ID;
	}
	
	@Override
	protected int findInstanceIdInfo(String s) {
		if (s.equals("length")) {
			return instanceIdInfo(DONTENUM, Id_length);
			return instanceIdInfo(DONTENUM, Id_text);
		}
		return super.findInstanceIdInfo(s);
	}
	
	@Override
	protected String getInstanceIdName(int id) {
		if (id == Id_length) { return "length"; }
		if (id == Id_text) { return "text"; }
		return super.getInstanceIdName(id);
	}
	
	@Override
	protected Object getInstanceIdValue(int id) {
		if (id == Id_length) {
			return ScriptRuntime.wrapInt(bytes.length);
		}
		if (id == Id_text) {
			if ( type == UNTYPED ) return Undefined.instance;
			return ScriptRuntime.wrapBoolean( type == TEXT );
		}
		return super.getInstanceIdValue(id);
	}
	
	@Override
	protected void setInstanceIdValue(int id, Object value) {
		if (id == Id_length) {
			setLength(value);
			return;
		}
		if (id == Id_text) {
			TypeMode newType;
			if ( value == Undefined.instance )
				newType = UNTYPED;
			else if ( value == Boolean.FALSE )
				newType = BINARY;
			else if ( value == Boolean.TRUE )
				newType = TEXT;
			else
				throw ScriptRuntime.typeError("Trying to set the .text attribute of a buffer to an invalid value");
			
			if ( newType == type )
				return;
			
			if ( length > 0 && type != UNTYPED )
				// A Buffer with size that is already typed cannot have it's type changed
				throw ScriptRuntime.constructError("Error", "Cannot change type of buffer when already initialized with a type and content");
			if ( type == BINARY )
				bytes = null;
			if ( type == TEXT )
				chars = null;
			
			if ( newType == BINARY )
				bytes = new byte[length];
			if ( newType == TEXT )
				chars = new char[length];
			
			type = newType;
			
			return;
		}
		super.setInstanceIdValue(id, value);
	}
	
	@Override
	protected void fillConstructorProperties(IdFunctionObject ctor) {
		addIdFunctionProperty(ctor, BUFFER_TAG, ConstructorId_slice, "slice", 3);
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
			case Id_slice:             arity=2; s="slice";             break;
			case Id_equals:            arity=1; s="equals";            break;
			default: throw new IllegalArgumentException(String.valueOf(id));
		}
		initPrototypeMethod(BUFFER_TAG, id, s, arity);
	}
	
    public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (!f.hasTag(BUFFER_TAG)) {
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
						byte[] b = realThis(thisObj, f).bytes;
						StringBuffer sb = new StringBuffer("(new Buffer())");
						for (int i = 0; i < b.length; i++) {
							if (i > 0)
								sb.insert(sb.length()-3, ", ");
							sb.insert(sb.length()-3, Integer.toString(byteToInt(b[i])));
						}
						return sb.toString();
					}
    				
					case Id_slice:
						return quickNewNativeBlob(cx, scope, js_slice(realThis(thisObj, f).bytes, args));
    				
					case Id_equals: {
						// ToDo
						boolean eq = false;
						byte[] b1 = realThis(thisObj, f).bytes;
						byte[] b2;
						if(args[0] instanceof NativeBlob) {
							b2 = ((NativeBlob)args[0]).bytes;
							if (b1.length != b2.length)
								break; // short circut if different byte lengths
							for (int i = 0; i < b2.length; i++)
								if ( b1[i] != b2[i] )
									break;
							eq = true;
						}
						return ScriptRuntime.wrapBoolean(eq);
					}
				}
				throw new IllegalArgumentException(String.valueOf(id));
			}
	}

	private static NativeBuffer realThis(Object thisObj, IdFunctionObject f) {
		if (!(thisObj instanceof NativeBuffer))
			throw incompatibleCallError(f);
		return (NativeBuffer)thisObj;
	}
	
	private void setLength(Object val) {
		double d = ScriptRuntime.toNumber(val);
		long longVal = ScriptRuntime.toUint32(d);
		if (longVal != d)
			throw Context.reportRuntimeError0("msg.arraylength.bad");
		if ( longVal == length ) return;
		if ( type != UNTYPED ) {
			long staticLength = getStaticLength();
			
			if ( longVal > length ) {
				// We're growing the array
				if ( longVal > staticLength )
				
				if ( length < staticLength )
					clearStatic(
				
				if ( longVal > staticLength )
					// We're extending past the length of the static array
					// Expand it by copying and extending the length
					expandStatic(length, longVal);
				else
					// We're only extending the length within the currently allocated array
					// We need to empty out the area we are extending into
					clearStatic(longVal, staticLength);
				
			} else {
				// We're shrinking the array
				if () {
					// 
				}
			}
		}
		length = longVal; // Finally set the new length
	}
	
	private long getStaticLength() {
		if ( type == UNTYPED ) return null;
		if ( type == BINARY ) return bytes.length;
		if ( type == TEXT ) return chars.length;
	}
	
	private void clearStatic(long from, long to) {
		if ( type == BINARY )
			Arrays.fill(bytes, (int)from, (int)to, Byte.MIN_VALUE);
		else if ( type == TEXT )
			Arrays.fill(chars, (int)from, (int)to, '\0');
	}
	
	private void expandStatic(long oldLength, long newLen) {
		if ( type == BINARY ) {
			byte[] newArray = new byte[newLen];
			System.arraycopy(bytes, 0, newArray, 0, oldLength);
		} else if ( type == TEXT )
			char[] newArray = new char[newLen];
			System.arraycopy(chars, 0, newArray, 0, oldLength);
		}
	}
	
	private enum TypeMode { UNTYPED, BINARY, TEXT }
	private long length;
	private TypeMode type = UNTYPED;
	private byte[] bytes;
	private char[] chars;
}
