// -*- coding: UTF-8 -*-
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

