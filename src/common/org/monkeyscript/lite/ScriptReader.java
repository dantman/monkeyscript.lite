package org.monkeyscript.lite;

import java.util.Arrays;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.*;
import java.util.HashMap;

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
	
	private HashMap<Integer,Character> skipChars = new HashMap<Integer,Character>();
	
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
		String bomCharset = null;
		
		is.mark(3); // Mark the start of the file before we look for boms
		int fstart = 0;
		byte[] b = new byte[3];
		is.read(b, 0, 3);
		if ( b[0] == BOM_UTF16LE[0] && b[1] == BOM_UTF16LE[1] ) {
			// UTF-16 LE BOM
			bomCharset = "UTF-16LE";
			skip = 1; // BOM is one character
			skipChars.put(0, ' ');
			fstart = 2;
		} else if ( b[0] == BOM_UTF16BE[0] && b[1] == BOM_UTF16BE[1] ) {
			// UTF-16 BE BOM
			bomCharset = "UTF-16BE";
			skip = 1; // BOM is one character
			skipChars.put(0, ' ');
			fstart = 2;
		} else if ( Arrays.equals(b, BOM_UTF8) ) {
			// UTF-8 BOM
			bomCharset = "UTF-8";
			skip = 1; // Java treats the BOM as a single character, so just skip one
			skipChars.put(0, ' ');
			fstart = 3;
		}
		is.reset();
		
		if ( bomCharset != null ) {
			charset = bomCharset;
		}
		
		String charsetHint = bomCharset; // Try charset define by bom
		if ( charsetHint == null ) charsetHint = this.charset; // Try user specified charset
		if ( charsetHint == null ) charsetHint = "US-ASCII"; // Use US-ASCII as fallback
		/*byte[] HASH = "#".getBytes(suggestedCharset);
		byte[] SLASH = "/".getBytes(suggestedCharset);
		byte[] CR = "\r".getBytes(suggestedCharset);
		byte[] LF = "\n".getBytes(suggestedCharset);
		
		byte[] bb = new byte[HASH.length];
		is.read(bb, fstart, HASH.length);
		Arrays.equals(bb, HASH)
		*/
		
		is.mark(256); // Mark the start of the file before we look for charset info
		byte[] headBytes = new byte[256-fstart];
		int len = is.read(headBytes, fstart, headBytes.length);
		String head = new String(headBytes, charsetHint);
		
		String[] lines = new String[2];
		int cr = head.indexOf("\r");
		int lf = head.indexOf("\n");
		int off = cr > lf ? cr : lf;
		
		if ( off == -1 ) {
			lines[0] = head;
		} else {
			lines[0] = head.substring(0, off);
			
			if ( off == cr && lf == off+1 )
				off++;
			off++;
			
			cr = head.indexOf("\r", off);
			lf = head.indexOf("\n", off);
			int off2 = cr > lf ? cr : lf;
			
			if ( off2 == -1 ) {
				lines[1] = head.substring(off);
			} else {
				lines[1] = head.substring(off, off2);
			}
		}
		
		Pattern re = Pattern.compile("coding[=:]\\s*([-_.A-Z0-9]+)", Pattern.CASE_INSENSITIVE);
		for ( int l = 0; l != 2; l++ ) {
			// ToDo: Support /* and make sure it doesn't cause bugs
			if ( lines[l].startsWith("#") || lines[l].startsWith("//") ) {
				Matcher m = re.matcher(lines[l]);
				if ( m.find() ) {
					charset = m.group(1);
					break;
				}
			}
		}
		
		// ToDo: This will break on the second line in some cases won't it? (like a multi-byte character somewhere in the mix)
		if ( lines[0].startsWith("#") ) {
			if ( lines[0].length() == 1 ) {
				skipChars.put(skip, ' ');
			} else {
				skipChars.put(skip, '/');
				skipChars.put(skip+1, '/');
			}
		}
		if ( lines[1] != null && lines[1].startsWith("#") ) {
			if ( lines[1].length() == 1 ) {
				skipChars.put(skip+off, ' ');
			} else {
				skipChars.put(skip+off, '/');
				skipChars.put(skip+off+1, '/');
			}
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
		for (int i = 0; i < readLength; i++) {
			if ( skipChars.containsKey(i+off) )
				cbuf[i] = skipChars.get(i+off);
		}
		return readLength;
	}
	
	public void close() throws IOException {
		cread.close();
	}
	
	public int getFirstLine() {
		return firstLine;
	}
	
}

