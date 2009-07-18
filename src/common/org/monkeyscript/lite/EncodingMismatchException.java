package org.monkeyscript.lite;

import java.io.IOException;

public class EncodingMismatchException extends IOException {
	public EncodingMismatchException() { super(); }
	public EncodingMismatchException(String message) { super(message); }
	public EncodingMismatchException(String message, Throwable cause) { super(message, cause); }
	public EncodingMismatchException(Throwable cause) { super(cause); }
}
