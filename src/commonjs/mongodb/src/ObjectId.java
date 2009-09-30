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
package org.monkeyscript.lite.modules.mongodb;
import org.monkeyscript.lite.*;
import org.mozilla.javascript.*;
import org.mozilla.javascript.annotations.*;

public class ObjectId extends ScriptableObject {
	
	private final static String CLASSNAME = "ObjectId";
	
	@Override
	public String getClassName() {
		return CLASSNAME;
	}
	
	private com.mongodb.ObjectId objectid;
	
	public ObjectId() {}
	
	@JSConstructor
	public ObjectId(Object id) {
		if ( id instanceof String ) {
			if ( !com.mongodb.ObjectId.isValid((String)id) )
				throw ScriptRuntime.typeError("Invalid format for objectid string");
			objectid = new com.mongodb.ObjectId((String)id);
		} else {
			objectid = new com.mongodb.ObjectId();
		}
	}
	
	@Override
	@JSFunction
	public String toString() {
		return objectid.toString();
	}
	
	@JSFunction
	public Object valueOf() {
		return toString();
	}
	
	@JSFunction
	public boolean equals(Object b) {
		if(b instanceof ObjectId)
			return equals((ObjectId)b);
		if(b instanceof com.mongodb.ObjectId)
			return equals((com.mongodb.ObjectId)b);
		return false;
	}
	public boolean equals(ObjectId b) {
		return equals(b.objectid);
	}
	public boolean equals(com.mongodb.ObjectId b) {
		return objectid.equals(b);
	}
	/*
	@JSFunction("equals")
	public Object jsequals(Object b) {
		return ScriptRuntime.wrapBoolean(equals(b));
	}
	*/
}
