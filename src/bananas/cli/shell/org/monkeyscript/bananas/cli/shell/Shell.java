package org.monkeyscript.bananas.cli.shell;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;

import org.mozilla.javascript.*;
import org.monkeyscript.lite.*;

import org.mozilla.javascript.tools.ToolErrorReporter; // @todo Remove need for this code

public class Shell extends IdScriptableObject implements Function {
	
	public String getClassName() {
		return "Shell";
	}
	
	static void init(Scriptable scope, boolean sealed) {
		Shell obj = new Shell();
		obj.exportAsJSClass(0, scope, sealed);
	}
	
	private int oldOptimizationLevel;
	private NativeArray history;
	
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		return null; // @todo Implement actual script class
	}
	
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		// Based off org.mozilla.javascript.tools.shell.Main#processSource
		PrintStream ps = MonkeyScriptRuntime.getStdErr(cx, scope);
		InputStream is = MonkeyScriptRuntime.getStdIn(cx, scope);
		
		// Use the interpreter for interactive input
		oldOptimizationLevel = cx.getOptimizationLevel();
		cx.setOptimizationLevel(-1);
		
		String enc = System.getProperty("file.encoding");
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(is, enc));
		} catch(UnsupportedEncodingException e) {
			throw new UndeclaredThrowableException(e);
		}
		
		String codeStartPrompt    = "js> ";
		String codeContinuePrompt = "  > ";
		
		int lineno = 1;
		boolean hitEOF = false;
		while (!hitEOF) {
			ps.print(codeStartPrompt);
			ps.flush();
			String source = "";
			
			while (true) {
				String newline;
				try { newline = in.readLine(); }
				catch (IOException ioe) {
					ps.println(ioe.toString());
					break;
				}
				if (newline == null) {
					hitEOF = true;
					break;
				}
				source = source + newline + "\n";
				lineno++;
				if (cx.stringIsCompilableUnit(source))
					break;
				ps.print(codeContinuePrompt);
			}
			Script script = cx.compileString(source, "<stdin>", lineno, null);
			if (script != null) {
				try {
					Object result = script.exec(cx, scope); // @todo should we grab the top level scope?
					// Avoid printing out undefined or function definitions.
					if (result != Context.getUndefinedValue() && !(result instanceof Function && source.trim().startsWith("function"))) {
						try { ps.println(Context.toString(result));
						} catch (RhinoException rex) {
							ToolErrorReporter.reportException(cx.getErrorReporter(), rex);
						}
					}
					history.put((int)history.getLength(), history, source);
				} catch (RhinoException rex) {
					ToolErrorReporter.reportException(cx.getErrorReporter(), rex);
				} catch (VirtualMachineError ex) {
					// Treat StackOverflow and OutOfMemory as runtime errors
					ex.printStackTrace();
					String msg = ToolErrorReporter.getMessage("msg.uncaughtJSException", ex.toString());
					Context.reportError(msg);
				}
			}
		}
		ps.println();
		
		// Reset optimization level
		oldOptimizationLevel = cx.getOptimizationLevel();
		
		return Context.getUndefinedValue();
	}
	
}
