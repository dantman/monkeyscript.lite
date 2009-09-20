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

import org.mozilla.javascript.*;

// org.mozilla.javascript.NativeString was used as a reference for implementation of this
final class NativeBuffer extends IdScriptableObject {
	
	static final long serialVersionUID = 1251247849L;
	
	private static final Object BUFFER_TAG = "Buffer";
	
	static void init(Scriptable scope, boolean sealed) {
		NativeBuffer obj = new NativeBuffer();
		IdFunctionObject ctor = obj.exportAsJSClass(MAX_PROTOTYPE_ID, scope, sealed);
		Scriptable proto = (Scriptable)ScriptableObject.getProperty(ctor, "prototype");
		NativeBlobBuffer.init(proto, scope, sealed);
		NativeStringBuffer.init(proto, scope, sealed);
	}
	
	private NativeBuffer() {}
	
	static private Object quickNewNativeBuffer(Context cx, Scriptable scope, Object target) {
		return ScriptRuntime.newObject(cx, scope, "Buffer", new Object[] { target });
	}
	
	@Override
	public String getClassName() {
		return "Buffer";
	}
	
	private static final int
		MAX_INSTANCE_ID              =  0;
	
	@Override
	protected int getMaxInstanceId() {
		return MAX_INSTANCE_ID;
	}
	
	@Override
	protected void initPrototypeId(int id) {
		String s;
		int arity;
		switch (id) {
			case Id_constructor:       arity=2; s="constructor";       break;
			case Id_toString:          arity=0; s="toString";          break;
			case Id_toSource:          arity=0; s="toSource";          break;
			default: throw new IllegalArgumentException(String.valueOf(id));
		}
		initPrototypeMethod(BUFFER_TAG, id, s, arity);
	}
	
    public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (!f.hasTag(BUFFER_TAG)) {
			return super.execIdCall(f, cx, scope, thisObj, args);
		}
		int id = f.methodId();
		again:
			for(;;) {
				switch (id) {
					case Id_constructor: {
						boolean inNewExpr = (thisObj == null);
						if (!inNewExpr && args.length == 0)
							// IdFunctionObject.construct will set up parent, proto
							return f.construct(cx, scope, args);
						if (inNewExpr && args.length == 0)
							jsConstructor(cx, scope, args);
						if (args.length > 0) {
							if ( args[0] instanceof String ) {
								return ScriptRuntime.newObject(cx, scope, "StringBuffer", args);
							} else if ( args[0] instanceof ScriptableObject ) {
								ScriptableObject o = (ScriptableObject)args[0];
								String c = o.getClassName();
								if ( c.equals("String") || c.equals("Blob") ) {
									String constructorName = c + "Buffer";
									return ScriptRuntime.newObject(cx, scope, constructorName, args);
								} else if ( o instanceof IdFunctionObject ) {
									if ( ((IdFunctionObject)o).hasTag("String") ) {
										Object[] ctorArgs = new Object[0];
										if ( args.length > 1 ) {
											ctorArgs = new Object[] { args[1] };
										}
										return ScriptRuntime.newObject(cx, scope, "StringBuffer", ctorArgs);
									} else if ( ((IdFunctionObject)o).hasTag("Blob") ) {
										Object[] ctorArgs = new Object[0];
										if ( args.length > 1 ) {
											ctorArgs = new Object[] { args[1] };
										}
										return ScriptRuntime.newObject(cx, scope, "BlobBuffer", ctorArgs);
									}
								}
							}
						}
						
						throw ScriptRuntime.typeError("Bad arguments to Buffer");
					}
					case Id_toString:
						return thisObj.toString();
					
					case Id_valueOf:
						return realThis(thisObj, f);
					
					case Id_toSource: {
						return "(new Buffer())";
					}
				}
				throw new IllegalArgumentException(String.valueOf(id));
			}
	}

	private static NativeBuffer realThis(Object thisObj, IdFunctionObject f) {
		if (!(thisObj instanceof NativeBuffer))
			throw incompatibleCallError(f);
		return (NativeBuffer)thisObj;
	}
	
	private static Object jsConstructor(Context cx, Scriptable scope, Object[] args) {
		return new NativeBuffer();
	}
	
// #string_id_map#
	
	@Override
	protected int findPrototypeId(String s) {
		int id;
// #generated# Last update: 2009-08-26 20:26:20 PDT
        L0: { id = 0; String X = null; int c;
            int s_length = s.length();
            if (s_length==7) { X="valueOf";id=Id_valueOf; }
            else if (s_length==8) {
                c=s.charAt(3);
                if (c=='o') { X="toSource";id=Id_toSource; }
                else if (c=='t') { X="toString";id=Id_toString; }
            }
            else if (s_length==11) { X="constructor";id=Id_constructor; }
            if (X!=null && X!=s && !X.equals(s)) id = 0;
            break L0;
        }
// #/generated#
		return id;
	}
	
    private static final int
		Id_constructor               = 1,
		Id_toString                  = 2,
		Id_toSource                  = 3,
		Id_valueOf                   = 4,
		MAX_PROTOTYPE_ID             = 4;
	
// #/string_id_map#
	
}
