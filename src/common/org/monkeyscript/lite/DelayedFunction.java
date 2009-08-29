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

