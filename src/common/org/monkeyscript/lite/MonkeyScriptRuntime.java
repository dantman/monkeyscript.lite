package org.monkeyscript.lite;

import org.mozilla.javascript.*;

/**
 * This is the class that implements extensions to the runtime
 */
public class MonkeyScriptRuntime {
	
    /**
	 * No instances should be created.
	 */
	protected MonkeyScriptRuntime() {}
	
    private static final String[] lazilyNames = {
        "Blob",         "org.monkeyscript.lite.NativeBlob",
        "Buffer",       "org.monkeyscript.lite.NativeBuffer",
        "BlobBuffer",   "org.monkeyscript.lite.NativeBlobBuffer",
        "StringBuffer", "org.monkeyscript.lite.NativeStringBuffer",
	};
	
    public static Scriptable newBlob(byte b, Scriptable scope) {
		return newBlob(new byte[] { b }, scope);
	}
	
    public static Scriptable newBlob(byte[] b, Scriptable scope) {
		return ScriptRuntime.newObject(Context.getCurrentContext(), scope, "Blob", new Object[] { b });
	}
	
	public static int byteToHighInt(byte b) {
		return (b >= 0) ? b : -1 * ((b ^ 0xFF) + 1);
	}
	
	public static byte toByte(Object b) {
		byte[] bytes = toByteArray(b);
		if ( bytes.length > 0 )
			return bytes[0];
		throw ScriptRuntime.typeError("Invalid data to convert to byte");
	}
	
	public static byte toByte(Number b) {
		return toByte(b.intValue());
	}
	
	public static byte toByte(int n) {
		if ( n < 0 || n > 255 )
			throw ScriptRuntime.typeError("Invalid data to convert to byte");
		return (byte)n;
	}
	
	public static byte[] toByteArray(Object b) {
		if ( b.getClass() == byte[].class )
			return (byte[])b;
		if ( b instanceof Byte )
			return new byte[] { ((Byte)b).byteValue() };
		if ( b instanceof NativeBlob )
			return ((NativeBlob)b).toByteArray();
		if ( b instanceof NativeBlobBuffer )
			return ((NativeBlob)b).toByteArray();
		if ( b instanceof Number )
			return new byte[] { toByte((Number)b) };
		if ( b instanceof NativeArray ) {
			NativeArray na = (NativeArray)b;
			Object[] oa = ScriptRuntime.getArrayElements(na);
			return toByteArray(oa);
		}
		if ( b.getClass() == Object[].class ) {
			Object[] oa = (Object[])b;
			byte[] ba = new byte[oa.length];
			for (int i=0; i != oa.length; i++) {
				ba[i] = toByte(oa[i]);
			}
			return ba;
		}
		
		throw ScriptRuntime.typeError("Invalid data to convert to byte");
	}
	
	public static byte[] toByteArray(byte b) {
		return new byte[] { b };
	}
	
	/**
	 * Converts an object into a string if possible without using any magic
	 * toString methods on objects.
	 */
	public static String toRealString(Object o) {
		if ( o.getClass() == char[].class )
			return new String((char[])o).intern();
		if ( o instanceof String )
			return ((String)o).intern();
		if ( o instanceof NativeStringBuffer )
			return ((NativeStringBuffer)o).toString();
		
		throw ScriptRuntime.typeError("Invalid data to convert to string");
	}
	
}
