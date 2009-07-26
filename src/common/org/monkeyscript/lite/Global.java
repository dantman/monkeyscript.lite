package org.monkeyscript.lite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.mozilla.javascript.*;
import org.mozilla.javascript.optimizer.*;

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
		boolean sealed = cx.isSealed();
		initStandardObjects( cx, sealed );
		defineProperty("global", this, ScriptableObject.DONTENUM | ScriptableObject.READONLY);
		NativeBlob.init( this, sealed );
		//NativeBuffer.init( this, sealed );
		String[] functions = {
			"exec",
			"print",
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
        PrintStream out = System.out;
        for (int i=0; i < args.length; i++) {
            if (i > 0)
                out.print(" ");

            // Convert the arbitrary JavaScript value into a string form.
            String s = Context.toString(args[i]);

            out.print(s);
        }
        out.println();
        return Context.getUndefinedValue();
    }
    
	/**
	 * MonkeyScript exec();
	 */
	public static Object exec( Context cx, Scriptable thisObj, Object[] args, Function funObj ) {
		if ( args.length == 0 )
			throw ScriptRuntime.typeError("exec() called without name of script to execute");
		try {
			String scriptFileName = (String) args[0];
			File scriptFile = new File(scriptFileName);
			String canonPath = scriptFile.getCanonicalPath();
			String dirName = scriptFile.getCanonicalFile().getParent();
			File dir = new File(dirName);
			String name = scriptFile.getName();
			if ( scriptFile.canRead() ) {
				/*File javaFile = new File( canonPath + ".class" ) );
				if ( dir.canWrite() ) {
				if ( javaFile.canRead() && javaFile.canWrite() ) {
					// Note, .class files we can't write to (ie: delete when invalid are ignored for now)
					if ( javaFile.lastModified() > scriptFile.lastModified() ) {
						// Ok to load java file
					} else {
						// Java file is old, delete it and continue
						javaFile.delete();
					}
				}*/
				
				FileInputStream is = new FileInputStream(scriptFile);
				ScriptReader in = new ScriptReader(is);
				
				if ( dir.canWrite() ) {
					boolean compileOk = true;
					// Compiled mode, eat the reader
					String source = in.eatSource();
					CompilerEnvirons compilerEnv = new CompilerEnvirons();
					compilerEnv.initFromContext(cx);
					ClassCompiler compiler = new ClassCompiler(compilerEnv);
					Object[] compiled = compiler.compileToClassFiles(source, canonPath, in.getFirstLine(), getJavaClassName(canonPath));
					if ( compiled != null && compiled.length > 0 ) {
						for (int j = 0; j != compiled.length; j += 2) {
							//String className = (String)compiled[j];
							byte[] bytes = (byte[])compiled[j + 1];
							String classFileName = name.concat(".class");
							if ( j > 0 )
								classFileName = classFileName.concat(Integer.toString(j));
							File outfile = new File(dir, classFileName);
							try {
								FileOutputStream os = new FileOutputStream(outfile);
								try { os.write(bytes); }
								finally { os.close(); }
							} catch (IOException ioe) {
								// If it fails, we ignore and use interpreted
								compileOk = false;
							}
						}
						
						if ( compileOk ) {
							// ToDo: Use what was just compiled
						}
					}
				}
				
				// Interpreted mode, use the reader
				return cx.evaluateReader( thisObj, in, scriptFile.getAbsolutePath(), in.getFirstLine(), null );
			} else {
				throw jsIOError("exec() called with name of a script that doesn't exist or cannot be read");
			}
		} catch( FileNotFoundException e ) {
			throw jsIOError("exec() called with name of a script that doesn't exist");
		} catch( UnsupportedEncodingException e ) {
			throw jsIOError("Unsupported character encodign for exec(): " + e.getMessage());
		} catch( IOException e ) {
			throw jsIOError(e.getMessage());
		}
	}
	
	// From org.mozilla.javascript.tools.jsc.Main.getClassName()
	private static String getJavaClassName(String name) {
		char[] s = new char[name.length()+1];
		char c;
		int j = 0;

		if (!Character.isJavaIdentifierStart(name.charAt(0))) {
			s[j++] = '_';
		}
		for (int i=0; i < name.length(); i++, j++) {
			c = name.charAt(i);
			if ( Character.isJavaIdentifierPart(c) ) {
				s[j] = c;
			} else {
				s[j] = '_';
			}
		}
		return (new String(s)).trim();
	}
	
	public static EcmaError jsIOError(String message) {
		return ScriptRuntime.constructError("IOError", message);
	}
	
	public static String js_FILE_(ScriptableObject obj) {
		//This is the correct way to do this, but doesn't work because it's protected and can't be proxied
		//int[] linep = new int[1];
		//String fileName = Context.getSourcePositionFromStackPublic(linep);
		//return fileName;
		return ScriptRuntime.constructError("Error", "").getSourceName();
	}
	
	public static String js_DIR_(ScriptableObject obj) {
		//This is the correct way to do this, but doesn't work because it's protected and can't be proxied
		//int[] linep = new int[1];
		//String fileName = Context.getSourcePositionFromStackPublic(linep);
		//return (new File(fileName)).getParent();
		return (new File(ScriptRuntime.constructError("Error", "").getSourceName())).getParent();
	}
	
	public static int js_LINE_(ScriptableObject obj) {
		//This is the correct way to do this, but doesn't work because it's protected and can't be proxied
		//int[] linep = new int[1];
		//String fileName = Context.getSourcePositionFromStackPublic(linep);
		//return linep[0];
		return ScriptRuntime.constructError("Error", "").getLineNumber();
	}
	
}
