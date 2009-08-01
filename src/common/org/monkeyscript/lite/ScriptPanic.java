package org.monkeyscript.lite;

import java.io.IOException;

/**
 * An error thrown when something unexpected that shouldn't happen occurs
 * ie: global.monkeyscript does not exist.
 * 
 * Why is this a runtime exception?
 * - This is never supposed to happen
 * - It can't be checked for
 * - These errors can't be recovered from
 * - These errors only happen at runtime
 */
public class ScriptPanic extends RuntimeException {
	public ScriptPanic() { super(); }
	public ScriptPanic(String message) { super(message); }
	public ScriptPanic(String message, Throwable cause) { super(message, cause); }
	public ScriptPanic(Throwable cause) { super(cause); }
}
