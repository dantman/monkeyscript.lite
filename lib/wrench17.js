// -*- coding: UTF-8 -*-
/*!
 * Wrench JavaScript 1.7 Environment v0.1a
 * http://wrench.monkeyscript.org/
 *
 * Copyright © 2009 Daniel Friesen
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

Object.invert = function invert(obj) {
	var o = {};
	for( var k in obj )
		if( obj.hasOwnProperty(k) )
			o[obj[k]] = k;
	return o;
};

Object.merge = function merge() {
	
};

(function(useCount) {
	Object.count = function count(obj) {
		if ( useCount ) return obj.__count__;
		return Object.keys(obj).length;
	};
})(({}).__count__ === 0);

Object.keys = function keys(obj) {
	var keys = [];
	for ( var k in obj )
		if ( obj.hasOwnProperty( k ) )
			keys.push( k );
	return keys;
};

Object.values = function values(obj) {
	var values = [];
	for ( var k in obj )
		if ( obj.hasOwnProperty( k ) )
			values.push( obj[k] );
	return values;
};


/**
 * Return a padded string for the number. By defeault this function zero fills.
 * 
 * @param len The minimum lenght of the string to return
 * @param chars Characters to use when padding the number, defaulting to zero padding
 * @param radix An optional radix to use when turning the number into a string
 */
Number.prototype.pad = function pad(len, chars, radix) {
	return this.toString(radix || 10).padLeft(len || 0, chars || "0");
};

/**
 * Extension to Math.random which returns an integer between 2 values instead of
 * a random float.
 * Partly borrowed from https://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/Math/random#Example.3a_Using_Math.random
 */
Math.rand = function rand(min, max) {
	return Math.floor( Math.random() * ( max - min + 1 ) ) + min;
};

/**
 * Calculate the sum of a list of numbers. Is also useful as an argument to .reduceNative
 */
if(!Math.sum)
	Math.sum = function sum(/*...*/) {
		if( !arguments.length )
			throw new TypeError();
		var args = Array.slice(arguments);
		var s = args.shift();
		while( args.length )
			s += args.shift();
		return s;
	};

/**
 * Calculate the average value from a list of numbers.
 */
Math.avg = function avg(/*...*/) {
	return Math.sum.apply(Math, arguments) / arguments.length;
};

/**
 * Repeat a string a number of times.
 * 
 * @param int num The number of times to repeat
 * @param String separator An optional separator to insert in between the strings
 * @return String the repeated string
 */
String.prototype.repeat = function repeat(num, separator) {
	return Array.fill(num||1, this).join(separator||'');
};

/**
 * Expand a string repeating it up to a certain length.
 * 
 * @param int len The length of the string to expand to
 * @return String the expanded string
 */
String.prototype.expand = function expand(len) {
	return this.repeat(Math.ceil(len / this.length)).substr(0, len);
};

/**
 * Return a version of this string with the first character in upper case
 * 
 * @return String The string with the modified case
 */
String.prototype.toFirstUpperCase = function toFirstUpperCase() {
	return this.charAt(0).toUpperCase() + this.substr(1);
};

/**
 * Return a version of this string with the first character in lower case
 * 
 * @return String The string with the modified case
 */
String.prototype.toFirstLowerCase = function toFirstLowerCase() {
	return this.charAt(0).toLowerCase() + this.substr(1);
};

/**
 * Return a version fo this string with all words matched by \w given an upper
 * case first character.
 * 
 * @return String The string with the modified case
 */
String.prototype.toTitleCase = function toTitleCase() {
	return this.replace(/\w+/g, function(m) { return m.toFirstUpperCase(); });
};

/**
 * Check and see if this string starts with another
 * 
 * @return Boolean A boolean indicating if this string starts with another
 */
String.prototype.startsWith = function startsWith(other) {
	return this.substr(0, other.length) === other;
};

/**
 * Check and see if this string ends with another
 * 
 * @return Boolean A boolean indicating if this string ends with another
 */
String.prototype.endsWith = function endsWith(other) {
	return this.substr(-other.length) === other;
};

/**
 * Check and see if this string contains another
 * 
 * @return Boolean A boolean indicating if this string contains another
 */
String.prototype.contains = function contains(other) {
	return this.indexOf(other) > -1;
};

/**
 * Count the number of times a substring is found within this string
 * 
 * @param other The substring to search for
 * @param offset An optional offset from the start of the string for the search
 * @return Number An integer indicating how many times the substring is found
 */
String.prototype.numberOf = function numberOf(other, offset) {
	offset = offset || 0;
	var i, c = 0;
	while( (i = this.indexOf(other, offset)) && i >= 0 ) {
		c++;
		offset = i + other.length;
	}
	return c;
};

/**
 * Reverse the order of characters in this string
 * 
 * @return String A new string with characters in the reverse order
 */
String.prototype.reverse = function reverse() {
	return this.split('').reverse().join('');
};

if ( !String.prototype.trimLeft )
	String.prototype.trimLeft = function trimLeft() {
		return this.replace(/^\s\s*/, '');
	};

if ( !String.prototype.trimRight )
	String.prototype.trimRight = function trimRight() {
		return this.replace(/\s*\s*$/, '');
	};

if ( !String.prototype.trim )
	String.prototype.trim = function trim() {
		return this.trimLeft().trimRight();
	};

String.prototype.strip = function strip(chars, internal) {
	if(!chars) throw new TypeError("Stripping requires a list of characters to strip");
	internal = internal || 3;
	// This creates a table where chars[char] will be truthy/falsey for inclusion
	var chars = Object.invert(typeof chars === 'string' ? chars.split('') : chars);
	
	var start = 0, end = this.length;
	
	if ( internal & 1 ) { // Left
		while( this.charAt(start) in chars )
			start++;
	}
	if ( internal & 2 ) { // Right
		while( this.charAt(end-1) in chars && end > start )
			end--;
	}
	
	return this.substring(start, end);
};

String.prototype.stripLeft = function stripLeft(chars) {
	return this.strip(chars, 1);
};

String.prototype.stripRight = function stripRight(chars) {
	return this.strip(chars, 2);
};

String.prototype.pad = function pad(len, chars) {
	return this.padLeft(Math.floor(len/2), chars).padRight(Math.ceil(len/2), chars);
};

String.prototype.padLeft = function padLeft(len, chars) {
	chars = chars || ' ';
	return chars.expand(Math.max(0, len - this.length)) + this;
};

String.prototype.padRight = function padRight() {
	chars = chars || ' ';
	return this + chars.expand(Math.max(0, len - this.length));
};

String.prototype.partition = function partition(sep) {
	if ( sep instanceof RegExp ) {
		var m = this.match(m);
		var i = m ? m.index : -1;
		sep = m[0];
	} else {
		var i = this.indexOf(sep);
	}
	var l = sep.length;
	
	return i > -1 ?
		[ this.substr(0, i), sep, this.substr(i+l) ] :
		[ this, '', '' ];
};

String.prototype.partitionRight = function partitionRight(sep) {
	var i = this.lastIndexOf(sep);
	return i > -1 ?
		[ this.substr(0, i), sep, this.substr(i+sep.length) ] :
		[ '', '', this ];
};

String.prototype.explode = function explode(sep, limit) {
	
};

String.prototype.scan = function scan(regex, offset) {
	var m, list = [];
	offset = offset || 0;
	if ( regex.global ) {
		var str = this.substr(offset);
		while( m = regex.exec(str) )
			list.push( m.length > 1 ? m.slice(1) : m[0] );
	} else {
		while( m = this.substr(offset).match(regex) ) {
			offset = m.index + m[0].length;
			list.push( m.length > 1 ? m.slice(1) : m[0] );
		}
	}
	
	return list;
};

/**
 * Converts a dash (foo-bar) or underscore (foo_bar) style name into a
 * cammel case (fooBar) name.
 */
String.prototype.toCamelCase = function toCamelCase() {
	return this.replace(/[-_][a-z]/g, function(i) { return i[1].toUpperCase(); });
};

/**
 * Converts a cammel case (fooBar) name into an underscore (foo_bar) style name.
 */
String.prototype.toUnderscore = function toUnderscore() {
	return this.replace(/[A-Z]/, function(i) { return '_' + i.toLowerCase(); });
};

/**
 * Converts a cammel case (fooBar) name into a dash (foo-bar) style name.
 */
String.prototype.toDash = function toDash() {
	return this.replace(/[A-Z]/, function(i) { return '-' + i.toLowerCase(); });
};

(function fn(method) {
	if(method in String) return fn;
	String[method] = function(arr) {
		return String.prototype[method].apply(arr, Array.slice(arguments, 1));
	};
	return fn;
})('split')('explode')
('repeat')('expand');

// https://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Objects/Array/reduce
if (!Array.prototype.reduce) {
	Array.prototype.reduce = function(fun /*, initial*/) {
		var len = this.length >>> 0;
		if (typeof fun != "function")
			throw new TypeError();
		
		// no value to return if no initial value and an empty array
		if (len == 0 && arguments.length == 1)
			throw new TypeError();
		
		var i = 0;
		if (arguments.length >= 2)
		{
			var rv = arguments[1];
		}
		else
		{
			do {
				if (i in this) {
					rv = this[i++];
					break;
				}
				
				// if array contains no values, no initial value to return
				if (++i >= len)
					throw new TypeError();
			}
			while (true);
		}
		
		for (; i < len; i++) {
			if (i in this)
				rv = fun.call(null, rv, this[i], i, this);
		}
		
		return rv;
	};
}

// https://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Objects/Array/reduceRight
if (!Array.prototype.reduceRight) {
	Array.prototype.reduceRight = function(fun /*, initial*/) {
		var len = this.length >>> 0;
		if (typeof fun != "function")
			throw new TypeError();
		
		// no value to return if no initial value, empty array
		if (len == 0 && arguments.length == 1)
			throw new TypeError();
		
		var i = len - 1;
		if (arguments.length >= 2) {
			var rv = arguments[1];
		} else {
			do {
				if (i in this) {
					rv = this[i--];
					break;
				}
				
				// if array contains no values, no initial value to return
				if (--i < 0)
					throw new TypeError();
			}
			while (true);
		}
		
		for (; i >= 0; i--) {
			if (i in this)
				rv = fun.call(null, rv, this[i], i, this);
		}
		
		return rv;
	};
}

/**
 * Create a new array of a specified length filled with a certain value
 * 
 * @param size The size of the array to create
 * @param value The value to populate all the items in the array with
 */
Array.fill = function fill(size, value) {
	var arr = new Array(Number(size||0));
	for ( var i = arr.length-1; i >= 0; --i )
		arr[i] = value;
	return arr;
};

/**
 * Return a new array with the contents of this array repeated over
 * `num` amount of times.
 * 
 * @param num Integer The number of times to repeat the array
 */
Array.prototype.repeat = function repeat(num) {
	return Array.prototype.concat.apply([], Array.fill(num, this));
};

/**
 * Check if the array contains an item
 */
Array.prototype.has = function has(item) {
	return this.indexOf(item) > -1;
};

/**
 * Return an array where all duplicate items have been removed
 * 
 * @return Array A new array with a unique list of all items in this array
 */
Array.prototype.unique = function unique() {
	return this.filter(function(item, i, arr) { return arr.indexOf(item) >= i; });
};

/**
 * Shuffle the array
 * 
 * @return Array The same array for convenience
 */
Array.prototype.shuffle = function shuffle() {
	return this.sort(function() { return Math.random() > 0.5 ? 1 : -1; });
};

/**
 * Return an item from an index in the array
 * This is provided for client-side convenience so you have the same technique
 * for getting an item on both an array and a list of html nodes.
 */
Array.prototype.item = function item(i) {
	return this[i];
};

/**
 * Remove the first (or more) occurrence(s) of an item from the array
 * 
 * @param item The item to remove
 * @param max The max number of items to remove, use Infinity to remove them all
 */
Array.prototype.remove = function remove(item, max) {
	max = max || 1;
	while(max) {
		var i = this.indexOf(item);
		if( i < 0 ) break;
		this.splice(i, 1);
		max--;
	}
	// ToDo: Determine a reasonable return value
};

/**
 * Append a list of items from an array onto the end of this array
 * 
 * @param items Array The array of items to append to this array
 */
Array.prototype.append = function append(items) {
	return Array.prototype.push.apply( this, items );
};

/**
 * Clear an array of all items
 */
Array.prototype.clear = function clear() {
	return this.splice(0, this.length);
};

/**
 * Clean out all undefined and null values inside of an array
 * if false is passed to empty then only undefined items are cleaned
 * if true is passed to empty then empty strings will also be cleaned
 */
Array.prototype.clean = function clean(empty) {
	return this.filter(function(item) {
		if ( item === undefined ) return false;
		if ( empty !== false && item === null ) return false;
		if ( empty === true && item === "" ) return false;
		return true;
	});
};

/**
 * Return a random item from this array.
 */
Array.prototype.rand = function() {
	return this[Math.rand(0, this.length-1)];
};

/**
 * A version of array.reduce() which only passes the two values to reduce on
 * to the callback so you can use native methods like Math.max inside a reduce.
 * 
 * @param Function fn The callback function
 */
Array.prototype.reduceNative = function reduceNative(fn) {
	return this.reduce(function(a, b) { return fn(a, b); });
};

/**
 * Returns a new version of this array which has been flattened
 * Flattening turns an array like [[1,2,3], [4,5,6], [7,8,9]];
 * into one like [1,2,3,4,5,6,7,8,9];
 */
Array.prototype.flat = function flat() {
	var arr = [];
	function reduce(item) {
		if( item instanceof Array )
			item.forEach(reduce);
		else arr.push(item);
	};
	this.forEach(reduce);
	return arr;
};

/**
 * Compare this array with another one and return an array with all the items
 * in this one that are not in the other.
 * 
 * @param Array otherArray The array to compare to
 * @return Array An array with all items in this array not in otherArray
 */
Array.prototype.diff = function diff(otherArray) {
	return this.filter(function(item) {
		return !otherArray.has(item);
	});
};

/**
 * Compare this array with another one and return an array with all the items
 * that are in both arrays.
 * 
 * @param Array otherArray The array to compare to
 * @return Array An array with all items in this array and otherArray
 */
Array.prototype.intersect = function intersect(otherArray) {
	return this.filter(function(item) {
		return otherArray.has(item);
	});
};

/**
 * Define an array iterator.
 * Using this we never need to hide properties from loops and both for and
 * for each loop syntax become available for looping over arrays while
 * guaranteeing that they will also always iterate in order.
 */
Array.prototype.__iterator__ = function(isKeys) {
	for( let i = 0, l = this.length; i<l; ++i )
		yield isKeys ? i : this[i];
};

String.prototype.__defineGetter__( 'uc', String.prototype.toUpperCase );
String.prototype.__defineGetter__( 'lc', String.prototype.toLowerCase );
String.prototype.__defineGetter__( 'ucfirst', String.prototype.toFirstUpperCase );
String.prototype.__defineGetter__( 'lcfirst', String.prototype.toFirstLowerCase );

