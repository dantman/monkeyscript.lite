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

_native.makeWindow = function(canvas) {
	var frame = new javax.swing.JFrame();
	var jcanvas = new JavaAdapter(javax.swing.JComponent, {
		paint: function(g) {
			g.drawImage(canvas._image,
				java.awt.image.AffineTransformOp(new java.awt.geom.AffineTransform(), java.awt.image.AffineTransformOp.TYPE_BICUBIC),
				0, 0);
		}
	});
	frame.add(jcanvas);
	frame.pack();
	
	var win = {
		_frame: frame,
		getGraphics: function() {
			return this._frame.getGraphics();
		},
		getTitle: function() {
			return String(this._frame.getTitle());
		},
		setTitle: function(title) {
			return this._frame.setTitle(title);
		},
		getWidth: function() {
			return Number(this._frame.getWidth());
		},
		getHeight: function() {
			return Number(this._frame.getHeight());
		},
		setWidth: function(w) {
			this._frame.setSize(w, this.getHeight());
		},
		setHeight: function(h) {
			this._frame.setSize(this.getWidth(), h);
		},
		open: function() {
			this._frame.setVisible(true);
		},
		close: function() {
			this._frame.dispose();
		}
	};
	win._frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
	win._frame.setVisible(false);
	return win;
};
