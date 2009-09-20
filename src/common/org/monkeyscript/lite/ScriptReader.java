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
import java.io.Reader;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Special ScriptReader class
 * This class allows for special handling of charset options in script files
 */
public class ScriptReader extends Reader {
	
	private BufferedInputStream buf;
	private BufferedReader cread;
	private String charset;
	private int skip = 0;
	private int firstLine = 1;
	private char[] prefix, suffix;
	private static final byte[] BOM_UTF8 = new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF };
	private static final byte[] BOM_UTF16LE = new byte[] { (byte)0xFF, (byte)0xFE };
	private static final byte[] BOM_UTF16BE = new byte[] { (byte)0xFE, (byte)0xFF };
	
	public ScriptReader(InputStream is) throws IOException, UnsupportedEncodingException {
		this.buf = new BufferedInputStream(is);
		init();
	}
	
	public ScriptReader(InputStream is, String charset) throws IOException, UnsupportedEncodingException {
		this.buf = new BufferedInputStream(is);
		this.charset = charset;
		init();
	}
	
	public ScriptReader(InputStream is, String prefix, String suffix) throws IOException, UnsupportedEncodingException {
		this.buf = new BufferedInputStream(is);
		this.prefix = (prefix+"\n").toCharArray();
		this.suffix = ("\n"+suffix).toCharArray();
		init();
	}
	
	public ScriptReader(InputStream is, String prefix, String suffix, String charset) throws IOException, UnsupportedEncodingException {
		this.buf = new BufferedInputStream(is);
		this.prefix = (prefix+"\n").toCharArray();
		this.suffix = ("\n"+suffix).toCharArray();
		this.charset = charset;
		init();
	}
	
	private void init() throws IOException, UnsupportedEncodingException {
		if ( prefix != null ) {
			firstLine += (new String(prefix)).split("\n").length-2;
		}
		
		BufferedInputStream is = this.buf;
		String charset = "UTF-8"; // Default charsets (ToDo: Use global.monkeyscript.defaultEncoding instead)
		is.mark(255); // Mark the start of the file before we look for charset info
		
		byte[] b = new byte[3];
		is.read(b, 0, 3);
		if ( b[0] == BOM_UTF16LE[0] && b[1] == BOM_UTF16LE[1] ) {
			// UTF-16 LE BOM
			charset = "UTF-16LE";
			skip = 1; // BOM is one character
		} else if ( b[0] == BOM_UTF16BE[0] && b[1] == BOM_UTF16BE[1] ) {
			// UTF-16 BE BOM
			charset = "UTF-16BE";
			skip = 1; // BOM is one character
		} else if ( Arrays.equals(b, BOM_UTF8) ) {
			// UTF-8 BOM
			charset = "UTF-8";
			skip = 1; // Java treats the BOM as a single character, so just skip one
		}
		
		is.reset(); // Reset to the start of the file so the script may be read
		
		if ( this.charset != null // User specified a charset
		 && !this.charset.equalsIgnoreCase(charset) // Charset is not a case insensitive match to what we found
		 && ( // either
		      !this.charset.equalsIgnoreCase("UTF-16") // User didn't specify UTF-16
		      // We didn't find a UTF-16 subtype
		   || !( charset.equalsIgnoreCase("UTF-16BE") || charset.equalsIgnoreCase("UTF-16LE") )
		    ) ) {
			// A user specified charset was given, however a BOM was found that gave a separate charset indication
			throw new EncodingMismatchException("The user specified encoding "+this.charset+" was found, however the file appears to be using "+charset);
		}
		
		this.charset = charset;
		cread = new BufferedReader(new InputStreamReader(this.buf, this.charset));
	}
	
	private boolean hitPrefixEnd = false;
	private boolean hitReaderEnd = false;
	private boolean hitSuffixEnd = false;
	private int mOff = 0;
	/**
	 * Basic read method implemented for the Reader
	 * 
	 * @param char[] cbuf Character buffer to read into
	 * @param int    off  Offset from the start of the cbuf to read into
	 * @param len    len  Lenght from the offset to limit reading into the cbuf
	 * @return int The number of characters that was read, or -1 if EOF
	 */
	public int read(char[] cbuf, int off, int len) throws IOException {
		// Read from prefix
		if ( !hitPrefixEnd && prefix != null && prefix.length > 0 ) {
			int i = 0;
			for (; i < len && i+mOff < prefix.length; i++) {
				cbuf[off+i] = prefix[i+mOff];
			}
			mOff += i;
			if ( mOff >= prefix.length ) {
				mOff = 0;
				hitPrefixEnd = true;
			}
			return i;
		}
		// Read from reader
		if ( !hitReaderEnd ) {
			int readLength = cread.read(cbuf, off, len);
			if ( readLength == -1 ) {
				mOff = 0;
				hitReaderEnd = true;
			} else {
				for (int i = 0; mOff+i < skip && i < readLength; i++) {
					// This section of code replaces characters in the read buffer with spaces
					// if they are below the skip threshold
					cbuf[off+i] = ' ';
				}
				mOff += readLength;
				return readLength;
			}
		}
		// Read from suffix
		if ( !hitSuffixEnd && suffix != null && suffix.length > 0 ) {
			int i = 0;
			for (; i < len && i+mOff < suffix.length; i++) {
				cbuf[off+i] = suffix[i+mOff];
			}
			mOff += i;
			if ( mOff >= suffix.length ) {
				mOff = 0;
				hitSuffixEnd = true;
			}
			return i;
		}
		return -1;
	}
	
	public void close() throws IOException {
		cread.close();
	}
	
	public int getFirstLine() {
		return firstLine;
	}
	
}

