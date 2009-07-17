package org.monkeyscript.lite;

import java.util.Arrays;

import org.mozilla.javascript.*;

// org.mozilla.javascript.NativeString was used as a reference for implementation of this
final class NativeBlob extends IdScriptableObject {
	
	private static final Object BLOB_TAG = "Blob";
	
	static void init(Scriptable scope, boolean sealed) {
		NativeBlob obj = NativeBlob.newEmpty();
		obj.exportAsJSClass(MAX_PROTOTYPE_ID, scope, sealed);
	}
	
	static NativeBlob newEmpty() {
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
		MAX_INSTANCE_ID              =  1;
	
	@Override
	protected int getMaxInstanceId() {
		return MAX_INSTANCE_ID;
	}
	
	@Override
	protected int findInstanceIdInfo(String s) {
		if (s.equals("length")) {
			return instanceIdInfo(DONTENUM | READONLY | PERMANENT, Id_length);
		}
		return super.findInstanceIdInfo(s);
	}
	
	@Override
	protected String getInstanceIdName(int id) {
		if (id == Id_length) { return "length"; }
		return super.getInstanceIdName(id);
	}
	
	@Override
	protected Object getInstanceIdValue(int id) {
		if (id == Id_length) {
			return ScriptRuntime.wrapInt(bytes.length);
		}
		return super.getInstanceIdValue(id);
	}
	
	@Override
	protected void fillConstructorProperties(IdFunctionObject ctor) {
		addIdFunctionProperty(ctor, BLOB_TAG, ConstructorId_byteAt, "byteAt", 2);
		addIdFunctionProperty(ctor, BLOB_TAG, ConstructorId_intAt, "intAt", 2);
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
			case Id_toString:          arity=0; s="toString";          break;
			case Id_toSource:          arity=0; s="toSource";          break;
			case Id_valueOf:           arity=0; s="valueOf";           break;
			case Id_byteAt:            arity=1; s="byteAt";            break;
			case Id_intAt:             arity=1; s="intAt";             break;
			case Id_indexOf:           arity=1; s="indexOf";           break;
			case Id_lastIndexOf:       arity=1; s="lastIndexOf";       break;
			case Id_split:             arity=2; s="split";             break;
			case Id_concat:            arity=1; s="concat";            break;
			case Id_slice:             arity=2; s="slice";             break;
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
					case ConstructorId_intAt:
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
							byte[] b = nativeConvert( cx, args[0] );
							return new NativeBlob(b);
						} else {
							return NativeBlob.newEmpty();
						}
					}
					
					case Id_toString:
						return thisObj.toString();
					
					case Id_valueOf:
						return thisObj;
					
					case Id_toSource: {
						byte[] b = realThis(thisObj, f).bytes;
						StringBuffer sb = new StringBuffer("(new Blob([]))");
						for (int i = 0; i < bytes.length; i++) {
							if (i > 0)
								sb.insert(sb.length()-3, ", ");
							sb.insert(sb.length()-3, Integer.toString(byteToInt(bytes[i])));
						}
						return sb.toString();
					}
    
					case Id_byteAt:
					case Id_intAt: {
						byte[] target = realThis(thisObj, f).bytes;
						double pos = ScriptRuntime.toInteger(args, 0);
						if (pos < 0 || pos >= target.length) {
							if (id == Id_byteAt) return NativeBlob.newEmpty();
							else return ScriptRuntime.NaNobj;
						}
						byte b = target[(int)pos];
						if (id == Id_byteAt) return new NativeBlob(b);
						else return ScriptRuntime.wrapInt(byteToInt(b));
					}
    
					case Id_indexOf:
						if ( args.length == 0 )
							break;
						byte[] needle = nativeConvert(cx, args[0]);
						int offset = 0;
						if ( args.length > 1 )
							offset = ScriptRuntime.toInt32( args[1] );
						return ScriptRuntime.wrapInt(js_indexOf(realThis(thisObj, f).bytes, needle, offset));
					
					case Id_lastIndexOf:
						return ScriptRuntime.wrapInt(js_lastIndexOf(realThis(thisObj, f).bytes, args));
    
					case Id_split:
						return js_split(cx, scope, realThis(thisObj, f).bytes, args);
    
					case Id_concat:
						return js_concat(realThis(thisObj, f).bytes, args);
    
					case Id_slice:
						return js_slice(realThis(thisObj, f).bytes, args);
    
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
	
	private static byte[] nativeConvert(Context cx, Object o) {
		if(o instanceof NativeBlob)
			return ((NativeBlob)o).bytes;
		if(o instanceof NativeNumber) {
			int ii = ScriptRuntime.toInt32( o );
			byte[] b = new byte[1];
			b[0] = intToByte(ii);
			return b;
		}
		if(o instanceof NativeArray) {
			NativeArray a = (NativeArray) o;
			byte[] ba = new byte[(int)a.getLength()];
			for (int i = 0; i < ba.length; i++) {
				Object bo = NativeArray.getElm(cx, (Scriptable)a, (long)i);
				if (!(bo instanceof NativeNumber))
					throw ScriptRuntime.typeError("Contents of data array used as argument to blob method was not entirely numbers");
				int ii = ScriptRuntime.toInt32( bo );
				byte b = intToByte(ii);
				ba[i] = b;
			}
			return ba;
		}
		throw ScriptRuntime.typeError("Invalid data type used as argument to a blob method");
	}
	
	private static int byteToInt(byte b) {
		Byte bb = new Byte(b);
		int bi = bb.intValue();
		Byte byteMin = new Byte(Byte.MIN_VALUE);
		int bmin = byteMin.intValue();
		return bi-bmin;
	}
	
	private static byte intToByte(int i) {
		int bmax = Byte.MAX_VALUE-Byte.MIN_VALUE;//(new Byte(Byte.MAX_VALUE-Byte.MIN_VALUE)).intValue();
		int bmin = (new Byte(Byte.MIN_VALUE)).intValue();
		if ( i > bmax )
			throw ScriptRuntime.typeError("Integer representation of byte to high.");
		byte b = (byte)(i+bmin);
		return b;
	}
	
	@Override
	public String toString() {
		return "[Blob length=" + bytes.length + "]";
	}
	
	@Override
	public Object get(int index, Scriptable start) {
		if (0 <= index && index < bytes.length) {
			return new NativeBlob(bytes[index]);
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
	
	
	private static byte[] js_slice(byte[] target, Object[] args) {
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
	
// #string_id_map#
	
	@Override
	protected int findPrototypeId(String s) {
		int id;
// #generated#
// #/generated#
		return id;
	}
	
    private static final int
		Id_constructor               = 1,
		Id_toString                  = 2,
		Id_toSource                  = 3,
		Id_valueOf                   = 4,
		Id_byteAt                    = 5,
		Id_intAt                     = 6,
		Id_indexOf                   = 7,
		Id_lastIndexOf               = 8,
		Id_split                     = 9,
		Id_concat                    = 10,
		Id_slice                     = 11,
		Id_equals                    = 12,
		MAX_PROTOTYPE_ID             = 12;
	
// #/string_id_map#
	
	private static final int 
		ConstructorId_byteAt         = -Id_byteAt,
		ConstructorId_intAt          = -Id_intAt,
		ConstructorId_indexOf        = -Id_indexOf,
		ConstructorId_lastIndexOf    = -Id_lastIndexOf,
		ConstructorId_split          = -Id_split,
		ConstructorId_concat         = -Id_concat,
		ConstructorId_slice          = -Id_slice;
	
	private byte[] bytes;
}
