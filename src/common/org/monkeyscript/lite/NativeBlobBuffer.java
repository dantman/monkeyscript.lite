package org.monkeyscript.lite;

import java.util.Arrays;

import org.mozilla.javascript.*;

// org.mozilla.javascript.NativeString was used as a reference for implementation of this
final class NativeBlobBuffer extends AbstractBuffer {
	
	static final long serialVersionUID = 1251247849L;
	
	private static final Object BLOB_BUFFER_TAG = "BlobBuffer";
	
	protected String getTypeTag() { return "Blob"; }
	
	static void init(Scriptable proto, Scriptable scope, boolean sealed) {
		NativeBlobBuffer obj = NativeBlobBuffer.newEmpty();
		obj.exportAsJSClass(MAX_PROTOTYPE_ID, scope, sealed);
		obj.setPrototype(proto);
	}
	
	static private NativeBlobBuffer newEmpty() {
		return new NativeBlobBuffer(0);
	}
	
	private NativeBlobBuffer(int len) {
		this.length = len;
		this.bytes = new byte[0];
	}
	
	private NativeBlobBuffer(byte[] bytes) {
		this.bytes = bytes;
		this.length = bytes.length;
	}
	
	static private Object quickNewNativeBlobBuffer(Context cx, Scriptable scope, Object target) {
		return ScriptRuntime.newObject(cx, scope, "BlobBuffer", new Object[] { target });
	}
	
	@Override
	protected void initPrototypeId(int id) {
		String s;
		int arity;
		switch (id) {
			case Id_constructor:       arity=1; s="constructor";       break;
			case Id_toString:          arity=0; s="toString";          break;
			case Id_toSource:          arity=0; s="toSource";          break;
			case Id_valueOf:           arity=0; s="valueOf";           break;
//			case Id_slice:             arity=2; s="slice";             break;
//			case Id_equals:            arity=1; s="equals";            break;
			default: throw new IllegalArgumentException(String.valueOf(id));
		}
		initPrototypeMethod(BLOB_BUFFER_TAG, id, s, arity);
	}
	
	private static NativeBlobBuffer realThis(Object thisObj, IdFunctionObject f) {
		if (!(thisObj instanceof NativeBlobBuffer))
			throw incompatibleCallError(f);
		return (NativeBlobBuffer)thisObj;
	}
	
    public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (!f.hasTag(BLOB_BUFFER_TAG)) {
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
					
					case Id_valueOf:
						return realThis(thisObj, f);
					
					case Id_toSource: {
						byte[] b = realThis(thisObj, f).bytes;
						StringBuffer sb = new StringBuffer("(new BlobBuffer([]))");
						for (int i = 0; i < b.length; i++) {
							if (i > 0)
								sb.insert(sb.length()-3, ", ");
							sb.insert(sb.length()-3, Integer.toString(MonkeyScriptRuntime.byteToHighInt(b[i])));
						}
						return sb.toString();
					}
				}
				return super.execIdCall(f, cx, scope, thisObj, args);
			}
	}
	
	protected Object jsConstructor(Context cx, Scriptable scope, Object[] args) {
		if ( args.length > 0 ) {
			if ( args[0] instanceof Number )
				return new NativeBlobBuffer(ScriptRuntime.toInt32(args[0]));
			return new NativeBlobBuffer(MonkeyScriptRuntime.toByteArray(args[0]));
		} else {
			return NativeBlobBuffer.newEmpty();
		}
	}
	
	/* Make array-style property lookup work for buffers. */
	@Override
	public Object get(int index, Scriptable start) {
		if (0 <= index && index < length) {
			return MonkeyScriptRuntime.newBlob(bytes[index], start);
		}
		return super.get(index, start);
	}
	
	@Override
	public void put(int index, Scriptable start, Object value) {
		if (0 <= index && index < length) {
			
			
			// @todo
			return;
		}
		super.put(index, start, value);
	}
	
	protected boolean rawEquals(Object obj) {
		byte[] test = null;
		if ( obj.getClass() == byte[].class )
			test = (byte[])obj;
		else if ( obj instanceof NativeBlobBuffer )
			test = ((NativeBlobBuffer)obj).bytes;
		else if ( obj instanceof NativeBlob )
			test = ((NativeBlob)obj).toByteArray();
		
		if ( test != null )
			return Arrays.equals(bytes, test);
		return false;
	}
	
	protected void truncateRaw(int len) {
		bytes = java.util.Arrays.copyOf(bytes, len);
	}
	
	public byte[] toByteArray() {
		return Arrays.copyOf(bytes, (int)length);
	}
	
	private byte[] bytes;
}
