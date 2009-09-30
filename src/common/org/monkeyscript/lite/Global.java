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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.DelayQueue;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.*;
import java.net.URL;
import java.net.URLClassLoader;

import java.lang.InterruptedException;
import java.lang.reflect.InvocationTargetException;

import org.mozilla.javascript.*;

public class Global extends ImporterTopLevel {
	
	public Global() {}
	public Global(Context cx) { init(cx); }
	public void init(ContextFactory factory) {
		factory.call(new ContextAction() {
			public Object run(Context cx) {
				init(cx);
				return null;
			}
		});
	}
	
	public void init(Context cx) {
		cx.putThreadLocal("xMonkeyScriptAsyncQueue", new DelayQueue<DelayedFunction>());
		cx.putThreadLocal("xMonkeyScriptRequireMap", new HashMap<String,ScriptableObject>());
		
		boolean sealed = cx.isSealed();
		initStandardObjects( cx, sealed );
		defineProperty("global", this, ScriptableObject.DONTENUM | ScriptableObject.READONLY);
		Kernel.init( this, sealed );
		NativeBlob.init( this, sealed );
		NativeBuffer.init( this, sealed );
		String[] functions = {
			"exec",
			"globalExec",
			"include",
			"includeOnce",
			"includeIfExists",
			"print",
			"setTimeout",
			"clearTimeout",
			"require", // CommonJS
		};
		defineFunctionProperties(functions, Global.class, ScriptableObject.DONTENUM);
		try {
			defineProperty("__FILE__", this, Global.class.getMethod("js_FILE_", ScriptRuntime.ScriptableObjectClass), null, ScriptableObject.DONTENUM | ScriptableObject.READONLY);
			defineProperty("__DIR__", this, Global.class.getMethod("js_DIR_", ScriptRuntime.ScriptableObjectClass), null, ScriptableObject.DONTENUM | ScriptableObject.READONLY);
			defineProperty("__LINE__", this, Global.class.getMethod("js_LINE_", ScriptRuntime.ScriptableObjectClass), null, ScriptableObject.DONTENUM | ScriptableObject.READONLY);
		} catch ( NoSuchMethodException e ) {
			System.out.println(e);
			System.exit(1);
		}
	}
	
	public static Object print(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		PrintStream out = MonkeyScriptRuntime.getContextOut();
		for (int i=0; i < args.length; i++) {
			// Convert the arbitrary JavaScript value into a string form.
			out.print(Context.toString(args[i]));
		}
		out.println();
		return Context.getUndefinedValue();
	}
	
	protected static final int SCRIPT_RESULT = 1;
	protected static final int IF_EXISTS     = 2;
	protected static final int ADD_INCLUDED  = 4;
	protected static final int NOT_INCLUDED  = 8;
	
	private static Object runScript( Context cx, Scriptable thisObj, Object[] args, Function funObj, int flags ) {
		Scriptable scope = ScriptableObject.getTopLevelScope((ScriptableObject)funObj);
		if ( args.length == 0 )
			throw ScriptRuntime.typeError("No script name");
		String scriptFileName = (String)args[0];
		File scriptFile = new File(scriptFileName);
		try {
			if ( scriptFile.canRead() ) {
				if ( (flags & NOT_INCLUDED ) != 0 ) {
					Scriptable included = getIncluded(scope);
					if ( ScriptableObject.callMethod(included, "has", new Object[] { scriptFile.getCanonicalPath() }) == Boolean.TRUE )
						return Boolean.TRUE;
				}
				FileInputStream is = new FileInputStream(scriptFile);
				ScriptReader in = new ScriptReader(is);
				if ( (flags & ADD_INCLUDED) != 0 ) {
					Scriptable included = getIncluded(scope);
					ScriptableObject.callMethod(included, "push", new Object[] { scriptFile.getCanonicalPath() });
				}
				Object res = cx.evaluateReader( scope, in, scriptFile.getAbsolutePath(), in.getFirstLine(), null );
				return (flags & SCRIPT_RESULT) != 0 ? res : Boolean.TRUE;
			} else {
				if ( (flags & IF_EXISTS) != 0 )
					return false;
				throw jsIOError("Script "+scriptFileName+" does not exist or cannot be read");
			}
		} catch( FileNotFoundException e ) {
			if ( (flags & IF_EXISTS) != 0 )
				return false;
			throw jsIOError("Script "+scriptFileName+" does not exist");
		} catch( UnsupportedEncodingException e ) {
			throw jsIOError("Unsupported character encoding: " + e.getMessage());
		} catch( IOException e ) {
			throw jsIOError(e.getMessage());
		}
		//return Boolean.FALSE;
	}
	
	public static Object exec( Context cx, Scriptable thisObj, Object[] args, Function funObj ) {
		// @todo
		throw ScriptRuntime.constructError("Error", "Unimplemented");
	}
	
	public static Object globalExec( Context cx, Scriptable thisObj, Object[] args, Function funObj ) {
		return runScript(cx, thisObj, args, funObj, SCRIPT_RESULT);
	}
	
	public static Object include( Context cx, Scriptable thisObj, Object[] args, Function funObj ) {
		return runScript(cx, thisObj, args, funObj, ADD_INCLUDED);
	}
	
	public static Object includeOnce( Context cx, Scriptable thisObj, Object[] args, Function funObj ) {
		return runScript(cx, thisObj, args, funObj, ADD_INCLUDED | NOT_INCLUDED);
	}
	
	public static Object includeIfExists( Context cx, Scriptable thisObj, Object[] args, Function funObj ) {
		return runScript(cx, thisObj, args, funObj, IF_EXISTS);
	}
	
	public static Scriptable getIncluded( Scriptable scope ) {
		Object monkeyscript = ScriptRuntime.getTopLevelProp(scope, "monkeyscript");
		if (!(monkeyscript instanceof Scriptable))
			throw new ScriptPanic("monkeyscript global does not exist");
		Object included = ScriptableObject.getProperty((Scriptable)monkeyscript, "included");
		if (!(included instanceof Scriptable))
			throw new ScriptPanic("monkeyscript.included does not exist");
		if (!ScriptRuntime.isArrayObject(included))
			throw new ScriptPanic("monkeyscript.included is not an array");
		return (Scriptable)included;
	}
	
	public static EcmaError jsIOError(String message) {
		return ScriptRuntime.constructError("IOError", message);
	}
	
	public static String js_FILE_(ScriptableObject obj) { return js_FILE_((Scriptable)obj); }
	public static String js_FILE_(Scriptable obj) {
		//This is the correct way to do this, but doesn't work because it's protected and can't be proxied
		//int[] linep = new int[1];
		//String fileName = Context.getSourcePositionFromStackPublic(linep);
		//return fileName;
		return ScriptRuntime.constructError("Error", "").sourceName();
	}
	
	public static String js_DIR_(ScriptableObject obj) { return js_DIR_((Scriptable)obj); }
	public static String js_DIR_(Scriptable obj) {
		//This is the correct way to do this, but doesn't work because it's protected and can't be proxied
		//int[] linep = new int[1];
		//String fileName = Context.getSourcePositionFromStackPublic(linep);
		//return (new File(fileName)).getParent();
		return (new File(ScriptRuntime.constructError("Error", "").sourceName())).getParent();
	}
	
	public static int js_LINE_(ScriptableObject obj) { return js_LINE_((Scriptable)obj); }
	public static int js_LINE_(Scriptable obj) {
		//This is the correct way to do this, but doesn't work because it's protected and can't be proxied
		//int[] linep = new int[1];
		//String fileName = Context.getSourcePositionFromStackPublic(linep);
		//return linep[0];
		return ScriptRuntime.constructError("Error", "").lineNumber();
	}
	
	/** ASYNC **/
	@SuppressWarnings("unchecked")
	private static DelayQueue<DelayedFunction> getAsyncQueue(Context cx) {
		Object asyncQueue = cx.getThreadLocal("xMonkeyScriptAsyncQueue");
		if (!(asyncQueue instanceof DelayQueue))
			throw new NullPointerException();
		return (DelayQueue<DelayedFunction>)asyncQueue;
	}
	public static Object setTimeout( Context cx, Scriptable thisObj, Object[] args, Function funObj ) {
		if ( args.length < 2 || !(args[0] instanceof Function) || !(args[1] instanceof Number) )
			throw ScriptRuntime.typeError("Invalid arguments to setTimeout");
		Function fn = (Function)args[0];
		long ms = ((Number)args[1]).longValue();
		DelayedFunction dfn = new DelayedFunction(fn, ms);
		getAsyncQueue(cx).add(dfn);
		return dfn;
	}
	
	public static Object clearTimeout( Context cx, Scriptable thisObj, Object[] args, Function funObj ) {
		if ( args.length < 1 || !(args[0] instanceof DelayedFunction) )
			throw ScriptRuntime.typeError("Invalid arguments to clearTimeout");
		DelayQueue<DelayedFunction> asyncQueue = getAsyncQueue(cx);
		DelayedFunction dfn = (DelayedFunction)args[0];
		return ScriptRuntime.wrapBoolean(asyncQueue.remove(dfn));
	}
	
	public static void runQueue( Context cx, Scriptable global ) {
		DelayQueue<DelayedFunction> que = getAsyncQueue(cx);
		while( que.size() > 0 ) {
			try {
				DelayedFunction dfn = que.take();
				dfn.call( cx, global, global, new Object[0] );
			} catch ( InterruptedException e ) {}
		}
	}
	
	/** CommonJS */
	@SuppressWarnings("unchecked")
	public static HashMap<String,ScriptableObject> getRequireMap(Context cx) {
		Object map = cx.getThreadLocal("xMonkeyScriptRequireMap");
		if (!(map instanceof HashMap))
			throw new NullPointerException();
		return (HashMap<String,ScriptableObject>)map;
	}
	public static Object require( Context cx, Scriptable thisObj, Object[] args, Function funObj ) {
		if ( args.length == 0 || !(args[0] instanceof String) )
			throw ScriptRuntime.typeError("Please pass a module identifier to require()");
		Scriptable scope = ScriptableObject.getTopLevelScope(thisObj);
		String identifier = (String)args[0];
		String[] pieces = identifier.split("/");
		
		boolean isJar = false;
		File modFile = null;
		if ( pieces[0].length() == 0 ) {
			// Absolute identifier, started with / (non-standard)
			modFile = new File(identifier+".jar");
			isJar = true;
			if ( !modFile.exists() ) {
				isJar = false;
				modFile = new File(identifier+".java.js");
				if ( !modFile.exists() ) {
					modFile = new File(identifier+".js");
				}
			}
		} else if ( pieces[0].equals(".") || pieces[0].equals("..") ) {
			// Relative identifier
			File base = new File(js_DIR_(thisObj));
			modFile = new File(base, identifier+".jar");
			isJar = true;
			if ( !modFile.exists() ) {
				isJar = false;
				modFile = new File(base, identifier+".java.js");
				if ( !modFile.exists() ) {
					modFile = new File(base, identifier+".js");
				}
			}
		} else {
			// Top-level identifier
			Object[] objs = ScriptRuntime.getArrayElements((Scriptable)funObj.get("paths", scope));
			for(int i=0; i<objs.length; i++) {
				String path = ScriptRuntime.toString(objs[i]);
				File base = new File(path);
				File f = new File(base, identifier+".jar");
				if ( f.exists() ) {
					isJar = true;
					modFile = f;
					break;
				}
				f = new File(base, identifier+".java.js");
				if ( f.exists() ) {
					isJar = false;
					modFile = f;
					break;
				}
				f = new File(base, identifier+".js");
				if ( f.exists() ) {
					isJar = false;
					modFile = f;
					break;
				}
			}
		}
		
		if ( modFile == null || !modFile.canRead() ) // ToDo setup LoadError
			throw ScriptRuntime.constructError("Error", "Could not find CommonJS module from identifier "+identifier);
		
		try {
			String absolutePath = modFile.getAbsolutePath();
			String canonicalPath = modFile.getCanonicalPath();
			
			HashMap<String,ScriptableObject> map = getRequireMap(cx);
			ScriptableObject exports = map.get(canonicalPath);
			if ( exports != null )
				return exports;
			
			exports = (ScriptableObject)cx.newObject(scope);
			map.put(canonicalPath, exports);
			
			ScriptableObject module = (ScriptableObject)cx.newObject(scope);
			module.defineProperty("path", canonicalPath, READONLY|PERMANENT);
			module.defineProperty("id", identifier, READONLY|PERMANENT); // @todo Expand relative paths to top-level?
			
			if(isJar) {
				try {
					JarFile jarFile = new JarFile(modFile);
					Manifest manifest = jarFile.getManifest();
					String exportsClassName = (String)manifest.getMainAttributes().get(new Attributes.Name("X-CommonJS-Exports"));
					if ( exportsClassName == null )
						throw ScriptRuntime.constructError("SyntaxError", "Jar based module does not contain a X-CommonJS-Exports manifest line");
					
					URLClassLoader classLoader = new URLClassLoader(new URL[] { modFile.toURI().toURL() });
					Class<?> exporterClass = Class.forName(exportsClassName, true, classLoader);
					
					
					if ( !Exports.class.isAssignableFrom(exporterClass) )
						throw ScriptRuntime.constructError("SyntaxError", "Jar based module's exporter is not a subclass of Exports");
					
					Exports exporter = (Exports)exporterClass.newInstance();
					
					exporter.export(cx, scope, module, exports);
					
				} catch( ClassNotFoundException e ) {
					throw ScriptRuntime.constructError("SyntaxError", "Jar based module's exporter could not be found: "+e.getMessage());
				} catch( InstantiationException e ) {
					throw ScriptRuntime.constructError("SyntaxError", "Jar based module's exporter could not be instanced");
				} catch( IllegalAccessException e ) {
					throw ScriptRuntime.constructError("Error", "Jar based module's exporter caused an illegal access exception");
				} catch( InvocationTargetException e ) {
					throw ScriptRuntime.constructError("Error", "Attempting to export Jar based module's caused an InvocationTargetException");
				}
			} else {
				FileInputStream is = new FileInputStream(modFile);
				ScriptReader in = new ScriptReader(is, "(function(module, exports) {", "//*/\n;return exports;\n})");
				Object moduleReturn = cx.evaluateReader( scope, in, absolutePath, in.getFirstLine(), null );
				if (!(moduleReturn instanceof Function))
					throw ScriptRuntime.constructError("SyntaxError", "Bad module syntax for "+absolutePath);
				Function fn = (Function)moduleReturn;
				Object returnedExports = fn.call(cx, scope, thisObj/*(Scriptable)cx.getUndefinedValue()*/, new Object[] { module, exports });
				if ( exports != returnedExports )
					// Warn the programmer they may have done something bad
					System.err.println("WARNING: CommonJS module "+identifier+" appears to have replaced the exports object which cannot be replaced. This could be a indication of a programming mistake in that module.");
			}
			
			return exports;
		} catch( FileNotFoundException e ) {
			throw ScriptRuntime.constructError("Error", "Could not find CommonJS module from identifier "+identifier);
		} catch( UnsupportedEncodingException e ) {
			throw Global.jsIOError("Unsupported character encoding: " + e.getMessage());
		}/* catch( IllegalAccessException e ) {
			throw Global.jsIOError(e.getMessage());
		}*/ catch( IOException e ) {
			throw Global.jsIOError(e.getMessage());
		}
	}
	
}
