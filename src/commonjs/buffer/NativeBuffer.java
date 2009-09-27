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
package org.monkeyscript.lite.modules.io.buffer;
import org.monkeyscript.lite.*;
import org.mozilla.javascript.*;
import java.lang.reflect.InvocationTargetException;

class NativeBuffer extends IdScriptableObject {
	
	private static final String BUFFER_TAG = "Buffer";
	private static final String STRING_TAG = "String";
	private static final String BLOB_TAG = "Blob";
	private static final String SBUFFER_TAG = "StringBuffer";
	private static final String BBUFFER_TAG = "BlobBuffer";
	
	static void export( Context cx, Scriptable scope, ScriptableObject module, ScriptableObject exports )
			throws IllegalAccessException, InstantiationException, InvocationTargetException {
		NativeBuffer buf = new NativeBuffer(BUFFER_TAG, (String)null);
		IdFunctionObject ctor = buf.createConstructor(MAX_PROTOTYPE_ID, scope, false);
		ctor.addAsProperty(exports);
		Scriptable proto = (Scriptable)ScriptableObject.getProperty(ctor, "prototype");
		
		NativeBuffer sbuf = new NativeBuffer(SBUFFER_TAG, STRING_TAG);
		sbuf.createConstructor(MAX_PROTOTYPE_ID, scope, false).addAsProperty(exports);
		
		NativeBuffer bbuf = new NativeBuffer(BUFFER_TAG, BLOB_TAG);
		bbuf.createConstructor(MAX_PROTOTYPE_ID, scope, false).addAsProperty(exports);
	}
	
	private NativeBuffer(String tag, String type) {
		this.tag  = tag;
		this.type = type;
	}
	
	@Override
	public String getClassName() {
		return tag;
	}
	
	@Override
	public String toString() {
		return ("[" + getClassName() + " length=" + length + "]").intern();
	}
	
	protected static final int
		Id_length                    =  1,
		Id_contentConstructor        =  2,
		MAX_INSTANCE_ID              =  2;
	
	@Override
	protected int getMaxInstanceId() {
		if ( tag == BUFFER_TAG )
			return 0;
		return MAX_INSTANCE_ID;
	}
	
	@Override
	protected int findInstanceIdInfo(String s) {
		if ( tag != BUFFER_TAG ) {
			if (s.equals("length")) {
				return instanceIdInfo(DONTENUM, Id_length);
			}
			if (s.equals("contentConstructor")) {
				return instanceIdInfo(DONTENUM|READONLY|PERMANENT, Id_contentConstructor);
			}
		}
		return super.findInstanceIdInfo(s);
	}
	
	@Override
	protected String getInstanceIdName(int id) {
		if ( tag != BUFFER_TAG ) {
			if (id == Id_length) { return "length"; }
			if (id == Id_contentConstructor) { return "contentConstructor"; }
		}
		return super.getInstanceIdName(id);
	}
	
	@Override
	protected Object getInstanceIdValue(int id) {
		if ( tag != BUFFER_TAG ) {
			if (id == Id_length) {
				return ScriptRuntime.wrapInt((int)length);
			}
			if (id == Id_contentConstructor && type != null) {
				return ScriptRuntime.getTopLevelProp(this.getParentScope(), type);
			}
		}
		return super.getInstanceIdValue(id);
	}
	
	@Override
	protected void setInstanceIdValue(int id, Object value) {
		if (tag != BUFFER_TAG && id == Id_length) {
			// @todo
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
		initPrototypeMethod(tag, id, s, arity);
	}
	
    public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
    	
    	//System.out.println(f.tag);
    	
    	return super.execIdCall(f, cx, scope, thisObj, args);
	}
// #string_id_map#
	
	@Override
	protected int findPrototypeId(String s) {
		int id;
// #generated# Last update: 2009-09-21 18:59:59 PDT
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
	
	private String tag;
	private String type;
	private Object buffer;
	private long length;
}
