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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.mozilla.javascript.*;

public class DelayedFunction extends BaseFunction implements Delayed {
	
	private Function fn;
	private long end;
	
	private long now() { return TimeUnit.NANOSECONDS.toMillis(System.nanoTime()); }
	
	public DelayedFunction(Function fn, long ms) {
		this.fn = fn;
		this.end = now() + ms;
	}
	
	public long getDelay(TimeUnit unit) {
		long remaining = end-now();
		return unit.convert(remaining, TimeUnit.MILLISECONDS);
	}
	
	public int compareTo(Delayed d) {
		return (int)(this.getDelay(TimeUnit.MILLISECONDS)-d.getDelay(TimeUnit.MILLISECONDS));
	}
	
	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if ( fn == null )
			throw ScriptRuntime.constructError("Error", "A delayed function (timeout) can only be called once");
		fn.call(cx, scope, thisObj, args);
		fn = null; // Function may only be called once, save memory by unsetting it allowing it to be collected
		return Context.getUndefinedValue();
	}  
	
}

