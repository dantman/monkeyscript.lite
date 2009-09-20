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

var {Canvas} = banana('graphics.canvas');

function CanvasWindow() {
	this.canvas = new Canvas();
	Object.defineProperty(this, "_win", {value: _native.makeWindow(this.canvas), enumerable: false, writable: false, configurable: false});
	this.width = this.canvas.width;
	this.height = this.canvas.height;
}
CanvasWindow.prototype = Object.create(Canvas.prototype, {
	title: {
		get: function() { return this._win.getTitle(); },
		set: function(t) { this._win.setTitle(t); }
	},
	width: {
		get: function() { return this._win.getWidth(); },
		set: function(w) { this._win.setWidth(w); this.canvas.width = w; }
	},
	height: {
		get: function() { return this._win.getHeight(); },
		set: function(h) { this._win.setHeight(h); this.canvas.height = h; }
	}
});
CanvasWindow.prototype.open = function open() {
	this._win.open();
};
CanvasWindow.prototype.close = function close() {
	this._win.close();
};

exports.CanvasWindow = CanvasWindow;

