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
	
	private void init() throws IOException, UnsupportedEncodingException {
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
	
	public int read(char[] cbuf, int off, int len) throws IOException {
		int readLength = cread.read(cbuf, off, len);
		// This section of code replaces characters in the read buffer with spaces
		// if they are below the skip threshold
		for (int i = off; i < skip; i++) {
			cbuf[i] = ' ';
		}
		return readLength;
	}
	
	public void close() throws IOException {
		cread.close();
	}
	
	public int getFirstLine() {
		return firstLine;
	}
	
	public String eatSource() throws IOException {
		StringBuffer buf = new StringBuffer();
		int offset = 0;
		while(true) {
			char[] c = new char[512];
			int len = read(c, offset, 512);
			if ( len < 0 )
				break;
			buf.append(c, 0, len);
		}
		return buf.toString();
	}
	
}

