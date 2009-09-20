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

import java.util.Arrays;

/**
 * An ArrayUtils class which helps manipulate char[] and byte[] arrays
 * abstractly without knowing which is being used.
 */
public class ArrayUtils {
    
    /**
	 * No instances should be created.
	 */
	protected ArrayUtils() {}
	
	public static void transfer(Object src, int srcPos, Object dest, int destPos, int length) {
		if ( src.getClass() != byte[].class & src.getClass() != char[].class )
			throw illegal();
		System.arraycopy(src, srcPos, dest, destPos, length);
	}
	
	public static Object clone(Object src) {
		if ( src.getClass() == byte[].class )
			return Arrays.copyOf((byte[])src, ((byte[])src).length);
		if ( src.getClass() == char[].class )
			return Arrays.copyOf((char[])src, ((char[])src).length);
		throw illegal();
	}
	
	public static Object copyOf(Object src, int newLength) {
		Object copy = newArray(src, newLength);
		transfer(src, 0, copy, 0, Math.min(length(src), newLength));
		return copy;
	}
	
	/**
	 * Return a new empty array of the same type as the one passed
	 */
	public static Object newEmpty(Object src) {
		return newArray(src, 0);
	}
	
	/**
	 * Return a new array of the same type as the one passed
	 */
	public static Object newArray(Object src, int len) {
		if ( src.getClass() == byte[].class )
			return new byte[len];
		if ( src.getClass() == char[].class )
			return new char[len];
		throw illegal();
	}
	
	public static int length(Object src) {
		if ( src.getClass() == byte[].class )
			return ((byte[])src).length;
		if ( src.getClass() == char[].class )
			return ((char[])src).length;
		throw illegal();
	}
	
	private static IllegalArgumentException illegal() {
		return new IllegalArgumentException("Only char[] and byte[] arrays are handled by ArrayUtils");
	}
	
}
