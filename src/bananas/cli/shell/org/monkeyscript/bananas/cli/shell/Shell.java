package org.monkeyscript.bananas.cli.shell;

import org.mozilla.javascript.*;
import org.monkeyscript.lite.*;

public class Shell extends IdScriptableObject implements Function {
	
	private int oldOptimizationLevel;
	private NativeArray history;
	
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
			Script script = loadScriptFromSource(cx, source, "<stdin>", lineno, null);
			if (script != null) {
				Object result = evaluateScript(script, cx, global);
				// Avoid printing out undefined or function definitions.
				if (result != Context.getUndefinedValue() && !(result instanceof Function && source.trim().startsWith("function"))) {
					try { ps.println(Context.toString(result));
					} catch (RhinoException rex) {
						ToolErrorReporter.reportException(cx.getErrorReporter(), rex);
					}
				}
				history.put((int)history.getLength(), history, source);
			}
		}
		ps.println();
		
		// Reset optimization level
		oldOptimizationLevel = cx.getOptimizationLevel();
		
	}
	
}
