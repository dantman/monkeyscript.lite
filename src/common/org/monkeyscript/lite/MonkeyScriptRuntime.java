/**!
 * @author Daniel Friesen
 * @copyright Copyright © 2009 Daniel Friesen
 * @license MIT License
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package org.monkeyscript.lite;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.File;

import java.net.URL;
import java.net.URLClassLoader;

import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.shell.ShellLine;

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
	
	public static char[] toCharArray(Object c) {
		if ( c.getClass() == char[].class )
			return (char[])c;
		if ( c instanceof Character )
			return new char[] { ((Character)c).charValue() };
		if ( c instanceof String )
			return ((String)c).toCharArray();
		if ( c instanceof NativeStringBuffer )
			return ((NativeStringBuffer)c).toCharArray();
		
		throw ScriptRuntime.typeError("Invalid data to convert to char");
	}
	
	public static char[] toCharArray(char c) {
		return new char[] { c };
	}
	
	/**
	 * Converts an object into a string if possible without using any magic
	 * toString methods on objects.
	 */
	public static String toRealString(Object o) {
		if ( o.getClass() == char[].class )
			return (new String((char[])o)).intern();
		if ( o instanceof Character )
			return ((Character)o).toString();
		if ( o instanceof String )
			return ((String)o).intern();
		if ( o instanceof NativeStringBuffer )
			return ((NativeStringBuffer)o).toString();
		
		throw ScriptRuntime.typeError("Invalid data to convert to string");
	}
	
	public static String toRealString(char c) {
		return Character.toString(c);
	}
	
	/**
	 * Get a InputStream that reads from stdin for the purpose of console interaction.
	 * This may return something using jline rather than a normal stream for this reason
	 */
	private static boolean attemptedJLineLoad = false;
	private static InputStream inStream = null;
	public static InputStream getStdIn(Context cx, Scriptable scope) {
		// See org.mozilla.javascript.tools.shell.Global#getIn
		if (inStream == null && !attemptedJLineLoad) {
			// Check if we can use JLine for better command line handling
			InputStream jlineStream = ShellLine.getStream(scope);
			if (jlineStream != null)
				inStream = jlineStream;
			attemptedJLineLoad = true;
		}
		return inStream == null ? System.in : inStream;
	}
	
	/**
	 * Get a PrintStream that prints to stdout for the purpose of console interaction.
	 * This may return something using jline rather than a normal stream for this reason
	 */
	private static PrintStream outStream = null;
	public static PrintStream getStdOut(Context cx, Scriptable scope) {
		return outStream == null ? System.out : outStream;
	}
	
	/**
	 * Get a PrintStream that prints to stderr for the purpose of console interaction.
	 * This may return something using jline rather than a normal stream for this reason
	 */
	private static PrintStream errStream = null;
	public static PrintStream getStdErr(Context cx, Scriptable scope) {
		return errStream == null ? System.err : errStream;
	}
	
	/**
	 * Get and set a stdout and stderr print streams for a context
	 */
	private static PrintStream genGetContextStream(Context cx, String type, PrintStream defaultStream) {
		Object ps = cx.getThreadLocal(("xMonkeyScriptStdStream"+type).intern());
		if(ps != null && ps instanceof PrintStream)
			return (PrintStream)ps;
		genSetContextStream(cx, type, defaultStream);
		return defaultStream;
	}
	private static void genSetContextStream(Context cx, String type, PrintStream ps) {
		cx.putThreadLocal(("xMonkeyScriptStdStream"+type).intern(), ps);
	}
	
	public static PrintStream getContextOut() { return getContextOut(Context.getCurrentContext()); }
	public static PrintStream getContextOut(Context cx) {
		return genGetContextStream(cx, "Out", System.out);
	}
	public static void setContextOut(PrintStream ps) { setContextOut(Context.getCurrentContext(), ps); }
	public static void setContextOut(Context cx, PrintStream ps) {
		genSetContextStream(cx, "Out", ps);
	}
	
	public static PrintStream getContextErr() { return getContextErr(Context.getCurrentContext()); }
	public static PrintStream getContextErr(Context cx) {
		return genGetContextStream(cx, "Err", System.err);
	}
	public static void setContextErr(PrintStream ps) { setContextOut(Context.getCurrentContext(), ps); }
	public static void setContextErr(Context cx, PrintStream ps) {
		genSetContextStream(cx, "Err", ps);
	}
	
	/*public static Object createJarLoader(Context cx, Scriptable scope, File[] jarFiles) {
		URL[] urls = new URL[jarFiles.length];
		for(int i=0; i<jarFiles.length; ++i) {
			urls[i] = jarFiles[i].toURI().toURL();
		}
		URLClassLoader cl = new URLClassLoader(urls);
		return NativeJavaTopPackage.construct(cx, scope, new Object[] { cl });
	}*/
	
}
