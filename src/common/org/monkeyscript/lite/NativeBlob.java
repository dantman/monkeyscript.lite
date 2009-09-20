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
import java.io.UnsupportedEncodingException;

import org.mozilla.javascript.*;

// org.mozilla.javascript.NativeString was used as a reference for implementation of this
final class NativeBlob extends IdScriptableObject {
	
	static final long serialVersionUID = 1248149631L;
	
	private static final Object BLOB_TAG = "Blob";
	
	static void init(Scriptable scope, boolean sealed) {
		NativeBlob obj = NativeBlob.newEmpty();
		obj.exportAsJSClass(MAX_PROTOTYPE_ID, scope, sealed);
	}
	
	static private NativeBlob newEmpty() {
		byte[] b = new byte[0];
		return new NativeBlob(b);
	}
	
	private NativeBlob(byte b) {
		bytes = new byte[1];
		bytes[0] = b;
	}
	
	private NativeBlob(byte[] b) {
		bytes = b;
	}
	
	@Override
	public String getClassName() {
		return "Blob";
	}
	
	private static final int
		Id_length                    =  1,
		Id_contentConstructor        =  2,
		MAX_INSTANCE_ID              =  2;
	
	@Override
	protected int getMaxInstanceId() {
		return MAX_INSTANCE_ID;
	}
	
	@Override
	protected int findInstanceIdInfo(String s) {
		if (s.equals("length")) {
			return instanceIdInfo(DONTENUM | READONLY | PERMANENT, Id_length);
		}
		if (s.equals("contentConstructor")) {
			return instanceIdInfo(DONTENUM|READONLY|PERMANENT, Id_contentConstructor);
		}
		return super.findInstanceIdInfo(s);
	}
	
	@Override
	protected String getInstanceIdName(int id) {
		if (id == Id_length) { return "length"; }
		if (id == Id_contentConstructor) { return "contentConstructor"; }
		return super.getInstanceIdName(id);
	}
	
	@Override
	protected Object getInstanceIdValue(int id) {
		if (id == Id_length) {
			return ScriptRuntime.wrapInt(bytes.length);
		}
		if (id == Id_contentConstructor) {
			return ScriptRuntime.getTopLevelProp(this.getParentScope(), "Blob");
		}
		return super.getInstanceIdValue(id);
	}
	
	@Override
	protected void fillConstructorProperties(IdFunctionObject ctor) {
		addIdFunctionProperty(ctor, BLOB_TAG, ConstructorId_byteAt, "byteAt", 2);
		addIdFunctionProperty(ctor, BLOB_TAG, ConstructorId_valueAt, "valueAt", 2);
		addIdFunctionProperty(ctor, BLOB_TAG, ConstructorId_indexOf, "indexOf", 2);
		addIdFunctionProperty(ctor, BLOB_TAG, ConstructorId_lastIndexOf, "lastIndexOf", 2);
		addIdFunctionProperty(ctor, BLOB_TAG, ConstructorId_split, "split", 3);
		addIdFunctionProperty(ctor, BLOB_TAG, ConstructorId_concat, "concat", 2);
		addIdFunctionProperty(ctor, BLOB_TAG, ConstructorId_slice, "slice", 3);
		super.fillConstructorProperties(ctor);
	}
	
	@Override
	protected void initPrototypeId(int id) {
		String s;
		int arity;
		switch (id) {
			case Id_constructor:       arity=1; s="constructor";       break;
			case Id_toString:          arity=1; s="toString";          break;
			case Id_toBlob:            arity=2; s="toBlob";            break;
			case Id_toArray:           arity=1; s="toArray";           break;
			case Id_toSource:          arity=0; s="toSource";          break;
			case Id_valueOf:           arity=0; s="valueOf";           break;
			case Id_byteAt:            arity=1; s="byteAt";            break;
			case Id_valueAt:           arity=1; s="valueAt";           break;
			case Id_indexOf:           arity=1; s="indexOf";           break;
			case Id_lastIndexOf:       arity=1; s="lastIndexOf";       break;
			case Id_split:             arity=2; s="split";             break;
			case Id_slice:             arity=2; s="slice";             break;
			case Id_concat:            arity=1; s="concat";            break;
			case Id_equals:            arity=1; s="equals";            break;
			default: throw new IllegalArgumentException(String.valueOf(id));
		}
		initPrototypeMethod(BLOB_TAG, id, s, arity);
	}
	
    @Override
    public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (!f.hasTag(BLOB_TAG)) {
			return super.execIdCall(f, cx, scope, thisObj, args);
		}
		int id = f.methodId();
		again:
			for(;;) {
				switch (id) {
					case ConstructorId_byteAt:
					case ConstructorId_valueAt:
					case ConstructorId_indexOf:
					case ConstructorId_lastIndexOf:
					case ConstructorId_split:
					case ConstructorId_concat:
					case ConstructorId_slice: {
						thisObj = realThis(args[0], f);
						Object[] newArgs = new Object[args.length-1];
						for (int i=0; i < newArgs.length; i++)
							newArgs[i] = args[i+1];
						args = newArgs;
						id = -id;
						continue again;
					}
					
					case Id_constructor: {
						boolean inNewExpr = (thisObj == null);
						if (!inNewExpr) {
							// IdFunctionObject.construct will set up parent, proto
							return f.construct(cx, scope, args);
						}
						return jsConstructor(cx, scope, args);
					}
/*					case Id_constructor: {
						if ( args.length > 0 ) {
							/*if ( args[0] instanceof NativeNumber ) {
								byte b = (byte) ScriptRuntime.toInt32( args[0] )-Byte.MIN_VALUE;
								return new NativeString(b);
							} else if ( args[0] instanceof NativeArray ) {
								byte[] ba = new byte[#];
								for (long i = 0; i < ba.length; i++) {
									Object bo = NativeArray.getElm(cx, args[0], i);
									if (!(bo instanceof NativeNumber))
										break;
									byte b = (byte) ScriptRuntime.toInt32( bo )-Byte.MIN_VALUE;
									return new NativeBlob(b);
								}
								
							}*/
							//byte[] b = nativeConvert( args[0] );
							//return new NativeBlob(b);
							/*
						} else {
							return NativeBlob.newEmpty();
						}
					}*/
					
					case Id_toString: {
						if ( args.length > 0 ) {
							String enc = ScriptRuntime.toString(args[0]);
							try {
								return new String(realThis(thisObj, f).bytes, enc).intern();
							} catch ( UnsupportedEncodingException e ) {
								throw ScriptRuntime.typeError("Unknown encoding "+enc+" passed to blob.toString");
							}
						}
						return realThis(thisObj, f).toString();
					}
					
					case Id_toBlob: {
						if ( args.length >= 2 ) {
							String fromEnc = ScriptRuntime.toString(args[0]);
							String toEnc = ScriptRuntime.toString(args[1]);
							try {
								return MonkeyScriptRuntime.newBlob((new String(realThis(thisObj, f).bytes, fromEnc)).getBytes(toEnc), scope);
							} catch ( UnsupportedEncodingException e ) {
								throw ScriptRuntime.typeError("Unknown encoding passed to blob.toBlob");
							}
						} else if ( args.length == 1 ) {
							throw ScriptRuntime.typeError("toBlob called on blob with one argument, caller likely assumed this blob was a string");
						} else {
							return realThis(thisObj, f);
						}
					}
					
					case Id_valueOf:
						return realThis(thisObj, f);
					
					case Id_toArray: {
						byte[] b = realThis(thisObj, f).bytes;
						String enc = null;
						if ( args.length > 0 )
							enc = ScriptRuntime.toString(args[0]);
						Scriptable array = cx.newArray(scope, b.length);
						try {
							for(int i=0; i<b.length; ++i) {
								ScriptableObject.putProperty(array, i,
									enc != null ?
									(new String(b, i, 1, enc)).intern() :
									MonkeyScriptRuntime.byteToHighInt(b[i]));
							}
						} catch ( UnsupportedEncodingException e ) {
							throw ScriptRuntime.typeError("Unknown encoding "+enc+" passed to blob.toArray");
						}
						return array;
					}
    				
					case Id_toSource: {
						byte[] b = realThis(thisObj, f).bytes;
						StringBuffer sb = new StringBuffer("(new Blob([]))");
						for (int i = 0; i < b.length; i++) {
							if (i > 0)
								sb.insert(sb.length()-3, ", ");
							sb.insert(sb.length()-3, Integer.toString(MonkeyScriptRuntime.byteToHighInt(b[i])));
						}
						return sb.toString();
					}
					
					case Id_byteAt:
					case Id_valueAt:
					case Id_byteCodeAt:
					case Id_codeAt: {
						byte[] target = realThis(thisObj, f).bytes;
						double pos = ScriptRuntime.toInteger(args, 0);
						if (pos < 0 || pos >= target.length) {
							if (id == Id_byteAt) return NativeBlob.newEmpty();
							else return ScriptRuntime.NaNobj;
						}
						byte b = target[(int)pos];
						if (id == Id_byteAt || id == Id_valueAt) return MonkeyScriptRuntime.newBlob(b, scope);
						else return ScriptRuntime.wrapInt(MonkeyScriptRuntime.byteToHighInt(b));
					}
    				
					case Id_indexOf:
					case Id_lastIndexOf:
						if ( args.length == 0 )
							break;
						byte[] needle = MonkeyScriptRuntime.toByteArray(args[0]);
						int offset = 0;
						if ( args.length > 1 )
							offset = ScriptRuntime.toInt32( args[1] );
						
						if ( id != Id_lastIndexOf )
							return ScriptRuntime.wrapInt(js_indexOf(realThis(thisObj, f).bytes, needle, offset));
						return ScriptRuntime.wrapInt(js_lastIndexOf(realThis(thisObj, f).bytes, needle, offset));
    				
					case Id_split:
						return js_split(cx, scope, realThis(thisObj, f).bytes, args);
    				
					case Id_slice:
						return MonkeyScriptRuntime.newBlob(js_slice(realThis(thisObj, f).bytes, args), scope);
    				
					case Id_concat:
						return MonkeyScriptRuntime.newBlob(js_concat(realThis(thisObj, f).bytes, args), scope);
    				
					case Id_equals: {
						boolean eq = false;
						byte[] b1 = realThis(thisObj, f).bytes;
						byte[] b2;
						if(args[0] instanceof NativeBlob) {
							b2 = ((NativeBlob)args[0]).bytes;
							if (b1.length != b2.length)
								break; // short circut if different byte lengths
							for (int i = 0; i < b2.length; i++)
								if ( b1[i] != b2[i] )
									break;
							eq = true;
						}
						return ScriptRuntime.wrapBoolean(eq);
					}
				}
				throw new IllegalArgumentException(String.valueOf(id));
			}
	}

	private static NativeBlob realThis(Object thisObj, IdFunctionObject f) {
		if (!(thisObj instanceof NativeBlob))
			throw incompatibleCallError(f);
		return (NativeBlob)thisObj;
	}
	
	@Override
	public String toString() {
		return "[Blob length=" + bytes.length + "]";
	}
	
	@Override
	public Object get(int index, Scriptable start) {
		if (0 <= index && index < bytes.length) {
			Context cx = Context.getCurrentContext();
			Scriptable scope = start.getParentScope();
			return MonkeyScriptRuntime.newBlob(bytes[index], scope);
		}
		return super.get(index, start);
	}
	
	@Override
	public void put(int index, Scriptable start, Object value) {
		if (0 <= index && index < bytes.length) {
			return;
		}
		super.put(index, start, value);
	}
	
	private static Object jsConstructor(Context cx, Scriptable scope, Object[] args) {
		if ( args.length > 0 ) {
			return new NativeBlob(MonkeyScriptRuntime.toByteArray(args[0]));
		} else {
			return NativeBlob.newEmpty();
		}
	}
	
	private static int js_indexOf(byte[] target, byte[] search, int begin2) {
		double begin = (double) begin2;
		
		if (begin > target.length - search.length) {
			return -1;
		} else {
			if (begin < 0)
				begin = 0;
			// byte arrays have no indexOf like Strings to
			// Enter long byte searching code
			look:
				for (int a = (int)begin; a < (int)target.length - search.length; a++) {
					for (int b = 0; b < (int)search.length; b++ ) {
						if ( target[a+b] != search[b] )
							continue look; // Not a match, move on
					}
					// This code is only reached if the loop above finds a complete match
					return a;
				}
			return -1;
		}
	}
	
	private static int js_lastIndexOf(byte[] target, byte[] search, int end2) {
		double end = (double) end2;
		
		if (end != end || end > target.length)
			end = target.length;
		else if (end < 0)
			end = 0;
		
		look:
			for (int a = (int)(end-search.length); a >= 0; a--) {
				for (int b = 0; b < (int)search.length; b++ ) {
					if ( target[a+b] != search[b] )
						continue look; // Not a match, move on
				}
				// This code is only reached if the loop above finds a complete match
				return a;
			}
		return -1;
	}
	
	private static Object js_split(Context cx, Scriptable scope, byte[] target, Object[] args) {
		// Mostly based on NativeString#js_split
		
		// create an empty Array to return;
		Scriptable top = getTopLevelScope(scope);
		//Scriptable result = ScriptRuntime.newObject(cx, top, "Array", null);
		Scriptable result = cx.newArray(scope, 0);
		
		// return an array consisting of the target if no separator given
		// don't check against undefined, because we want
		// 'fooundefinedbar'.split(void 0) to split to ['foo', 'bar']
		if (args.length < 1) {
			result.put(0, result, MonkeyScriptRuntime.newBlob(target, scope));
			return result;
		}
		
		// Use the second argument as the split limit, if given.
		boolean limited = (args.length > 1) && (args[1] != Undefined.instance);
		long limit = 0;  // Initialize to avoid warning.
		if (limited) {
			/* Clamp limit between 0 and 1 + string length. */
			limit = ScriptRuntime.toUint32(args[1]);
			if (limit > target.length)
				limit = 1 + target.length;
		}

        byte[] separator = MonkeyScriptRuntime.toByteArray(args[0]);
        int[] matchlen = new int[1];
        matchlen[0] = separator.length;
		
		// split target with separator
		int[] ip = { 0 };
		int match;
		int len = 0;
		boolean[] matched = { false };
		byte[][][] parens = { null };
		
		// ToDo: split isn't finished, this portion is majorly different than string split code
		/*
        while ((match = find_split(cx, scope, target, separator, version,
                                   reProxy, re, ip, matchlen, matched, parens))
               >= 0)
        {
            if ((limited && len >= limit) || (match > target.length()))
                break;

            String substr;
            if (target.length() == 0)
                substr = target;
            else
                substr = target.substring(ip[0], match);

            result.put(len, result, substr);
            len++;
        /*
         * Deviate from ECMA to imitate Perl, which omits a final
         * split unless a limit argument is given and big enough.
         *//*
                if (!limited && ip[0] == target.length)
                    break;
            }
        }*/
        return result;
	}
	
	private static byte[] js_slice(byte[] target, Object[] args) {
		// Based on NativeString#js_slice
		if (args.length != 0) {
			double begin = ScriptRuntime.toInteger(args[0]);
			double end;
			int length = target.length;
			if (begin < 0) {
				begin += length;
				if (begin < 0)
					begin = 0;
			} else if (begin > length) {
				begin = length;
			}

			if (args.length == 1) {
				end = length;
			} else {
				end = ScriptRuntime.toInteger(args[1]);
				if (end < 0) {
					end += length;
					if (end < 0)
						end = 0;
				} else if (end > length) {
					end = length;
				}
				if (end < begin)
					end = begin;
			}
			
			return Arrays.copyOfRange(target, (int)begin, (int)(end-begin));
		}
		return target;
	}
	
	private static byte[] js_concat(byte[] target, Object[] args) {
		int N = args.length;
		if (N == 0) { return target; }
		else if (N == 1) {
			byte[] arg = MonkeyScriptRuntime.toByteArray(args[0]);
			byte[] newblob = Arrays.copyOf(target, target.length+arg.length);
			
			for (int i = 0; i != arg.length; ++i)
				newblob[target.length+i] = arg[i];
			
			return newblob;
		}

		// Find total capacity for the final blob
		int size = target.length;
		byte[][] argsAsBytes = new byte[N][];
		for (int i = 0; i != N; ++i) {
			byte[] b = MonkeyScriptRuntime.toByteArray(args[i]);
			argsAsBytes[i] = b;
			size += b.length;
		}

		byte[] result = Arrays.copyOf(target, size);
		int index = target.length;
		for (int byteArrayN = 0; byteArrayN != N; ++byteArrayN) {
			byte[] b = argsAsBytes[byteArrayN];
			for (int i = 0; i != b.length; ++i, ++index)
				result[index] = b[i];
		}
		return result;
	}
	
// #string_id_map#
	
	@Override
	protected int findPrototypeId(String s) {
		int id;
// #generated# Last update: 2009-09-05 03:49:58 PDT
        L0: { id = 0; String X = null; int c;
            L: switch (s.length()) {
            case 5: c=s.charAt(1);
                if (c=='l') { X="slice";id=Id_slice; }
                else if (c=='p') { X="split";id=Id_split; }
                break L;
            case 6: switch (s.charAt(2)) {
                case 'B': X="toBlob";id=Id_toBlob; break L;
                case 'd': X="codeAt";id=Id_codeAt; break L;
                case 'n': X="concat";id=Id_concat; break L;
                case 't': X="byteAt";id=Id_byteAt; break L;
                case 'u': X="equals";id=Id_equals; break L;
                } break L;
            case 7: c=s.charAt(0);
                if (c=='i') { X="indexOf";id=Id_indexOf; }
                else if (c=='t') { X="toArray";id=Id_toArray; }
                else if (c=='v') {
                    c=s.charAt(6);
                    if (c=='f') { X="valueOf";id=Id_valueOf; }
                    else if (c=='t') { X="valueAt";id=Id_valueAt; }
                }
                break L;
            case 8: c=s.charAt(3);
                if (c=='o') { X="toSource";id=Id_toSource; }
                else if (c=='t') { X="toString";id=Id_toString; }
                break L;
            case 10: X="byteCodeAt";id=Id_byteCodeAt; break L;
            case 11: c=s.charAt(0);
                if (c=='c') { X="constructor";id=Id_constructor; }
                else if (c=='l') { X="lastIndexOf";id=Id_lastIndexOf; }
                break L;
            }
            if (X!=null && X!=s && !X.equals(s)) id = 0;
            break L0;
        }
// #/generated#
		return id;
	}
	
    private static final int
		Id_constructor               = 1,
		Id_toString                  = 2,
		Id_toBlob                    = 4,
		Id_toArray                   = 5,
		Id_toSource                  = 6,
		Id_valueOf                   = 7,
		Id_byteAt                    = 8,
		Id_valueAt                   = 9,
		Id_byteCodeAt                = 10,
		Id_codeAt                    = 11,
		Id_indexOf                   = 12,
		Id_lastIndexOf               = 13,
		Id_concat                    = 14,
		Id_split                     = 15,
		Id_slice                     = 16,
		Id_equals                    = 17,
		MAX_PROTOTYPE_ID             = 17;
	
// #/string_id_map#
	
	private static final int 
		ConstructorId_byteAt         = -Id_byteAt,
		ConstructorId_valueAt        = -Id_valueAt,
		ConstructorId_indexOf        = -Id_indexOf,
		ConstructorId_lastIndexOf    = -Id_lastIndexOf,
		ConstructorId_split          = -Id_split,
		ConstructorId_concat         = -Id_concat,
		ConstructorId_slice          = -Id_slice;
	
	public byte[] toByteArray() {
		return Arrays.copyOf(bytes, bytes.length);
	}
	
	private byte[] bytes;
}
