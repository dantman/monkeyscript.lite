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
import org.mozilla.javascript.*;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * This class implements various cleaned up error printing mechanisms
 */
public class MonkeyErrorPrinter {
	
    /**
	 * No instances should be created.
	 */
	protected MonkeyErrorPrinter() {}
	
	/**
	 * Special printer for shells. Accepts a context, scope, and error.
	 * Prints a short easy to read error to the context err and saves a
	 * __ERROR__ to the scope object which can get more info
	 * @todo Handlers for other types of errors, and more generic ondes
	 */
	public static void shellPrint(Context cx, Scriptable scope, RhinoException rex) {
		shellPrint(cx, scope, rex, null);
	}
	public static void shellPrint(Context cx, Scriptable scope, RhinoException rex, StackTraceElementFilter filter) {
		PrintStream err = MonkeyScriptRuntime.getContextErr(cx);
		
		err.print("js: \"");
		err.print(rex.sourceName());
		err.print("\"");
		if ( rex.lineNumber() > 0 ) {
			err.print(", line ");
			err.print(rex.lineNumber());
		}
		if ( rex.columnNumber() > 0 ) {
			err.print(", column ");
			err.print(rex.columnNumber());
		}
		err.println(": uncaught JavaScript runtime exception:");
		err.print("    ");
		if ( rex instanceof EvaluatorException )
			err.print("SyntaxError: ");
		err.println(rex.details());
		// Stack code
		StackTraceElement[] stack = rex.getStackTrace();
		for (int i = 0; i < stack.length; i++) {
			StackTraceElement e = stack[i];
			String className = e.getClassName();
			if ( className.startsWith("org.mozilla.javascript.") && !className.startsWith("org.mozilla.javascript.gen.") )
				continue;
			if ( className.startsWith("java.lang.reflect.") || className.startsWith("sun.reflect.") )
				continue;
			if ( className.equals("org.monkeyscript.lite.MonkeyScript") )
				continue;
			if ( className.equals("org.monkeyscript.lite.Global") )
				continue;
			String name = e.getFileName();
			if ( name.equals("monkeyscript.js") )
				continue;
			if ( filter != null && !filter.accept(e) )
				continue;
			if ( e.getLineNumber() > -1 && name != null ) {
				err.print("    at ");
				if ( name.startsWith("/") ) {
					err.print(name);
					err.print(':');
					err.println(e.getLineNumber());
				} else {
					err.print(e.getClassName());
					err.print("#");
					err.print(e.getMethodName());
					err.print("(");
					err.print(name);
					err.print(':');
					err.print(e.getLineNumber());
					err.println(")");
				}
			}
		}
		
		StringWriter sw = new StringWriter();
		rex.printStackTrace(new PrintWriter(sw));
		scope.put("__ERROR__", scope, sw.toString());
		err.println("js: Complete trace info saved to __ERROR__.");
	}
	
}
