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
// Implement some things like exec
package org.monkeyscript.lite;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.mozilla.javascript.*;

public class MonkeyScript {
	
	protected static Global global;
	
	public static Global getGlobal() { return global; }
	
	public static void main(String args[]) {
		global = new Global();
		
		try {
			Context cx = Context.enter();
			cx.setLanguageVersion(Context.VERSION_1_8);
			boolean sealed = cx.isSealed();
			
			global.init(cx);
			
			Object[] array = new Object[args.length];
			System.arraycopy(args, 0, array, 0, args.length);
			Scriptable argsObj = cx.newArray(global, array);
			global.defineProperty("_arguments", argsObj, ScriptableObject.DONTENUM);
			
			quickRunScript( cx, global, "json2.js" );
			quickRunScript( cx, global, "wrench17.js" );
			quickRunScript( cx, global, "monkeyscript.java.js" );
			try {
				quickRunScript( cx, global, "monkeyscript.js" );
				Global.runQueue( cx, global );
			} catch ( JavaScriptException e ) {
				StringBuffer buf = new StringBuffer();
				buf.append("Error in main thread");
				String sourceName = e.sourceName();
				int lineNumber = e.lineNumber();
				String lineSource = e.lineSource();
				int columnNumber = e.columnNumber();
				buf.append(" (");
				if ( sourceName != null )
					buf.append(sourceName);
				buf.append(":");
				buf.append(lineNumber);
				if ( columnNumber > 0 ) {
					buf.append("#");
					buf.append(columnNumber);
				}
				buf.append(")");
				System.err.println(buf.toString());
				if ( lineSource != null )
					System.err.println(lineSource);
				System.err.println(e.details());
				
				System.err.println(e.getScriptStackTrace());
			}
		} finally {
			Context.exit();
		}
		
	}
	
	protected static Object quickRunScript( Context cx, ScriptableObject scope, String fileName ) {
		try {
			InputStream is = MonkeyScript.class.getResourceAsStream( fileName );
			InputStreamReader in = new InputStreamReader(is, "UTF-8");
			return cx.evaluateReader( scope, in, fileName, 1, null );
		}
		catch( IOException e ) { reportWarning("Failed to exec script "+fileName, e); }
		catch( NullPointerException e ) { reportWarning("Failed to exec script "+fileName, e); }
		return null;
	}
	
	protected static void reportWarning(String message) {
		Context.reportWarning(message);
		System.out.println("[ WARNING: " + message + " ]");
	}
	
	protected static void reportWarning(String message, Throwable t) {
		Context.reportWarning(message, t);
		System.out.println("[ WARNING: " + message + " ]");
	}
}
