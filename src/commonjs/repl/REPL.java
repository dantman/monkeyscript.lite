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
package org.monkeyscript.lite.modules.repl;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.lang.reflect.InvocationTargetException;

import org.mozilla.javascript.*;
import org.monkeyscript.lite.*;

import org.mozilla.javascript.tools.ToolErrorReporter; // @todo Remove need for this code

public class REPL extends BaseFunction {
	
	static void export( Context cx, Scriptable scope, ScriptableObject module, ScriptableObject exports )
			throws IllegalAccessException, InstantiationException, InvocationTargetException {
		REPL repl = new REPL();
		ScriptRuntime.setFunctionProtoAndParent(repl, scope);
		exports.defineProperty("REPL", repl, 0);
		repl.defineFunctionProperties(new String[] { "doctest" }, REPL.class, ScriptableObject.DONTENUM);
	}
	
	@Override
	public String getClassName() {
		return "REPL";
	}
	
	@Override
	public String getFunctionName() {
		return "REPL";
	}
	
	private int oldOptimizationLevel;
	private NativeArray history;
	
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if ( history == null )
			history = (NativeArray)cx.newArray(scope, 0);
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
		
		int lineno = 0;
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
			try {
				Script script = cx.compileString(source, "<stdin>", lineno, null);
				if (script != null) {
					try {
						Object result = script.exec(cx, scope); // @todo should we grab the top level scope?
						// Avoid printing out undefined or function definitions.
						if (result != Context.getUndefinedValue() && !(result instanceof Function && source.trim().startsWith("function"))) {
							try { ps.println(Context.toString(result));
							} catch (RhinoException rex) {
								shellPrintError(cx, scope, rex);
							}
						}
						history.put((int)history.getLength(), history, source);
					} catch (RhinoException rex) {
						shellPrintError(cx, scope, rex);
					} catch (VirtualMachineError ex) {
						// Treat StackOverflow and OutOfMemory as runtime errors
						ex.printStackTrace();
						String msg = ToolErrorReporter.getMessage("msg.uncaughtJSException", ex.toString());
						Context.reportError(msg);
					}
				}
			} catch (RhinoException rex) {
				shellPrintError(cx, scope, rex);
			}
		}
		ps.println();
		
		// Reset optimization level
		cx.setOptimizationLevel(oldOptimizationLevel);
		
		return Context.getUndefinedValue();
	}
	
	private static void shellPrintError(Context cx, Scriptable scope, RhinoException rex) {
		MonkeyErrorPrinter.shellPrint(cx, ScriptableObject.getTopLevelScope(scope), rex,
			new StackTraceElementFilter() {
				public boolean accept(StackTraceElement e) {
					return !e.getClassName().equals("org.monkeyscript.lite.modules.repl.REPL");
				}
			});
	}
	
	public static Object doctest(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		Scriptable scope = ScriptableObject.getTopLevelScope(thisObj);
		// Based on org.mozilla.javascript.tools.shell.Global#runDoctest
		// Credit for doctest goes to authors of toolsrc/org/mozilla/javascript/tools/shell/Global.java#runDoctest
		String session = Context.toString(args[0]);
		String codeStartPrompt    = "js> ";
		String codeContinuePrompt = "  > ";
		if(args.length > 2) {
			codeStartPrompt    = Context.toString(args[1]);
			codeContinuePrompt = Context.toString(args[2]);
		}
		
		HashMap<String,String> doctestCanonicalizations = new HashMap<String,String>();
		
		String[] lines = session.split("[\n\r]+");
		int testCount = 0;
		int i = 0;
		while (i < lines.length && !lines[i].trim().startsWith(codeStartPrompt)) {
			i++; // skip lines that don't look like shell sessions
		}
		while (i < lines.length) {
			String inputString = lines[i].trim().substring(codeStartPrompt.length());
			inputString += "\n";
			i++;
			while (i < lines.length && lines[i].trim().startsWith(codeContinuePrompt)) {
				inputString += lines[i].trim().substring(codeContinuePrompt.length());
				inputString += "\n";
				i++;
			}
			String expectedString = "";
			while (i < lines.length && !lines[i].trim().startsWith(codeStartPrompt)) {
				expectedString += lines[i] + "\n";
				i++;
			}
			PrintStream savedOut = MonkeyScriptRuntime.getContextOut(cx);
			PrintStream savedErr = MonkeyScriptRuntime.getContextErr(cx);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ByteArrayOutputStream err = new ByteArrayOutputStream();
			MonkeyScriptRuntime.setContextOut(cx, new PrintStream(out));
			MonkeyScriptRuntime.setContextErr(cx, new PrintStream(err));
			String resultString = "";
			ErrorReporter savedErrorReporter = cx.getErrorReporter();
			cx.setErrorReporter(new ToolErrorReporter(false, MonkeyScriptRuntime.getContextErr(cx)));
			try {
				testCount++;
				Object result = cx.evaluateString(scope, inputString, "doctest input", 1, null);
				if (result != Context.getUndefinedValue() && !(result instanceof Function && inputString.trim().startsWith("function"))) {
					resultString = Context.toString(result);
				}
			} catch (RhinoException e) {
				ToolErrorReporter.reportException(cx.getErrorReporter(), e);
			} finally {
				MonkeyScriptRuntime.setContextOut(cx, savedOut);
				MonkeyScriptRuntime.setContextErr(cx, savedErr);
				cx.setErrorReporter(savedErrorReporter);
				resultString += err.toString() + out.toString();
			}
			if (!doctestOutputMatches(doctestCanonicalizations, expectedString, resultString)) {
				String message = "doctest failure running:\n" +
					inputString +
					"expected: " + expectedString +
					"actual: " + resultString + "\n";
				System.err.println(message);
			}
		}
		return testCount;
	}
	
	private static boolean doctestOutputMatches(HashMap<String,String> doctestCanonicalizations, String expected, String actual) {
		// Ported from org.mozilla.javascript.tools.shell.Global#doctestOutputMatches
		// Credit for doctest goes to authors of toolsrc/org/mozilla/javascript/tools/shell/Global.java#runDoctest
		expected = expected.trim();
		actual = actual.trim().replace("\r\n", "\n");
		if (expected.equals(actual))
			return true;
		for (Map.Entry<String,String> entry: doctestCanonicalizations.entrySet()) {
			expected = expected.replace(entry.getKey(), entry.getValue());
		}
		if (expected.equals(actual))
			return true;
		// java.lang.Object.toString() prints out a unique hex number associated
		// with each object. This number changes from run to run, so we want to
		// ignore differences between these numbers in the output. We search for a
		// regexp that matches the hex number preceded by '@', then enter mappings into
		// "doctestCanonicalizations" so that we ensure that the mappings are
		// consistent within a session.
		Pattern p = Pattern.compile("@[0-9a-fA-F]+");
		Matcher expectedMatcher = p.matcher(expected);
		Matcher actualMatcher = p.matcher(actual);
		for (;;) {
			if (!expectedMatcher.find())
				return false;
			if (!actualMatcher.find())
				return false;
			if (actualMatcher.start() != expectedMatcher.start())
				return false;
			int start = expectedMatcher.start();
			if (!expected.substring(0, start).equals(actual.substring(0, start)))
				return false;
			String expectedGroup = expectedMatcher.group();
			String actualGroup = actualMatcher.group();
			String mapping = doctestCanonicalizations.get(expectedGroup);
			if (mapping == null) {
				doctestCanonicalizations.put(expectedGroup, actualGroup);
				expected = expected.replace(expectedGroup, actualGroup);
			} else if (!actualGroup.equals(mapping)) {
				return false; // wrong object!
			}
			if (expected.equals(actual))
				return true;
		}
	}
	
}
