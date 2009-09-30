// -*- coding: UTF-8 -*-
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

function Transcoder(from, to) {
	if ( arguments.length < 2 || !isString(from) || !isString(to) )
		throw new TypeError("Transcoder requires two string arguments, a source and destination charset");
	from = from.toUpperCase();
	to = to.toUpperCase();
	Object.defineProperties(this, {
		sourceCharset: {
			value: from,
			writable: false, configurable: false
		},
		destinationCharset: {
			value: to,
			writable: false, configurable: false
		},
		_decoder: {
			value: Charset.forName(from).newDecoder(),
			enumerable: false, writable: false, configurable: false
		},
		_encoder: {
			value: Charset.forName(to).newEncoder(),
			enumerable: false, writable: false, configurable: false
		}
	});
}
Transcoder.prototype.push
Transcoder.prototype.pushAccumulate
Transcoder.prototype.close

exports.Transcoder = Transcoder;
exports.supports = function supports(charset) {
	return !!java.nio.charset.Charset.isSupported(charset);
};

