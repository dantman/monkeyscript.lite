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
abstract class AbstractBuffer extends IdScriptableObject {
	
	abstract protected String getTypeTag();
	
	protected String getTag() {
		return (getTypeTag() + "Buffer").intern();
	}
	
	@Override
	public String getClassName() {
		return getTag();
	}
	
	protected static final int
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
			return instanceIdInfo(DONTENUM, Id_length);
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
			return ScriptRuntime.wrapInt((int)length);
		}
		if (id == Id_contentConstructor) {
			return ScriptRuntime.getTopLevelProp(this.getParentScope(), getTypeTag());
		}
		return super.getInstanceIdValue(id);
	}
	
	@Override
	protected void setInstanceIdValue(int id, Object value) {
		if (id == Id_length) {
			setLength(value);
			return;
		}
		super.setInstanceIdValue(id, value);
	}
	
	@Override
	protected void fillConstructorProperties(IdFunctionObject ctor) {
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
			case Id_equals:            arity=1; s="equals";            break;
			case Id_slice:             arity=2; s="slice";             break;
			case Id_append:            arity=1; s="append";            break;
			default: throw new IllegalArgumentException(String.valueOf(id));
		}
		initPrototypeMethod(getTag(), id, s, arity);
	}
	
    public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (!f.hasTag(getTag())) {
			return super.execIdCall(f, cx, scope, thisObj, args);
		}
		int id = f.methodId();
		again:
			for(;;) {
				switch (id) {
					case Id_toString:
						return thisObj.toString();
					
					case Id_equals:
						if ( !safeThis(thisObj) )
							return Boolean.FALSE;
						return js_equals(cx, scope, (AbstractBuffer)thisObj, args);
					
					case Id_slice:
						return null;
						
					case Id_append:
						if ( !safeThis(thisObj) )
							throw ScriptRuntime.typeError("Cannot do append on invalid buffer type");
						js_append(cx, scope, (AbstractBuffer)thisObj, args);
						return null;
				}
				throw new IllegalArgumentException(String.valueOf(id));
			}
	}
	
	@Override
	public String toString() {
		return ("[" + getClassName() + " length=" + length + "]").intern();
	}
	
	abstract protected Object jsConstructor(Context cx, Scriptable scope, Object[] args);
	
	protected static Object js_equals(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		return ScriptRuntime.wrapBoolean(((AbstractBuffer)thisObj).rawEquals(args[0]));
	}
	
	protected void js_append(Context cx, Scriptable scope, AbstractBuffer thisObj, Object[] args) {
		if ( args.length == 0 )
			return;
		Object data = toWildArray(args[0]);
		
		thisObj.rawSlice((int)thisObj.length, 0, data);
	}
	
	/**
	 * Test that must return true ONLY if the object passed to it is the same
	 * type of buffer, including data type.
	 */
	abstract protected boolean safeThis(Scriptable thisObj);
	
	/**
	 * Returns the char[] or byte[] array for the buffer as an Object so it
	 * can be worked on abstractly
	 */
	abstract protected Object getArray();
	
	/**
	 * Sets the char[] or byte[] array for the buffer abstractly
	 */
	abstract protected void setArray(Object arr);
	
	abstract protected Object toWildArray(Object arg);
	abstract protected int getWildArrayLength(Object data);
	
	abstract protected boolean rawEquals(Object obj);
	
	protected Object rawSlice(int offset, int chop, Object data) {
		if ( offset > length )
			throw ScriptRuntime.constructError("RangeError", "Slice offset to high");
		if ( offset < 0 )
			throw ScriptRuntime.constructError("RangeError", "Slice offset to low");
		
		if ( offset == length && chop == 0 ) {
			// Short-circut optimized form of append
			int len = ArrayUtils.length(data);
			truncateRaw(length+len);
			ArrayUtils.transfer(data, 0, getArray(), (int)length, len);
			length += len;
			return ArrayUtils.newEmpty(getArray());
		}
		
		throw ScriptRuntime.constructError("Error", "not yet implemented");
	}
	
	protected void setLength(Object len) { setLength(ScriptRuntime.toInt32(len)); }
	protected void setLength(long len) { setLength((int)len); }
	protected void setLength(int len) {
		// @todo
	}
	
	protected void truncateRaw(long len) { truncateRaw((int)len); }
	protected void truncateRaw(int len) {
		setArray(ArrayUtils.copyOf(getArray(), len));
	}
	
// #string_id_map#
	
	@Override
	protected int findPrototypeId(String s) {
		int id;
// #generated# Last update: 2009-08-26 20:26:20 PDT
        L0: { id = 0; String X = null; int c;
            L: switch (s.length()) {
            case 5: X="slice";id=Id_slice; break L;
            case 6: c=s.charAt(0);
                if (c=='a') { X="append";id=Id_append; }
                else if (c=='e') { X="equals";id=Id_equals; }
                break L;
            case 7: X="valueOf";id=Id_valueOf; break L;
            case 8: c=s.charAt(3);
                if (c=='o') { X="toSource";id=Id_toSource; }
                else if (c=='t') { X="toString";id=Id_toString; }
                break L;
            case 11: X="constructor";id=Id_constructor; break L;
            }
            if (X!=null && X!=s && !X.equals(s)) id = 0;
            break L0;
        }
// #/generated#
		return id;
	}
	
    protected static final int
		Id_constructor               = 1,
		Id_toString                  = 2,
		Id_toSource                  = 3,
		Id_valueOf                   = 4,
		Id_equals                    = 5,
		Id_slice                     = 6,
		Id_append                    = 7,
		MAX_PROTOTYPE_ID             = 7;
	
// #/string_id_map#
	
	protected long length;
}
