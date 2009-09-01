// -*- coding: UTF-8 -*-

function newBase(w, h) {
	delete this._image;
	delete this._graphics;
	this._image = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_4BYTE_ABGR );;
	this._graphics = this._image.createGraphics();
	/*Object.defineProperties(this, {
		_base: {
			value: base,
			enumerable: false,
			writable: false
		},
		_graphics: {
			value: base.createGraphics ? base.createGraphics() : base.getGraphics()/*,
			enumerable: false,
			writable: false
		}
	});*/
}

function Canvas() {
	newBase.call(this, 300, 150);
}
Object.defineProperties(Canvas.prototype, {
	width: {
		get: function() {
			return this._image.getWidth();
		},
		set: function(value) {
			newBase.call(this, value, this._image.getHeight());
		}
	},
	height: {
		get: function() {
			return this._image.getHeight();
		},
		set: function(value) {
			newBase.call(this, this._image.getWidth(), value);
		}
	}
});
Canvas.prototype.getContext = function(contextId) {
	if ( contextId === "2d" )
		return new CanvasRenderingContext2d(this);
	return null;
};

function CanvasRenderingContext2D(canvas) {
	Object.defineProperty(this, "canvas", canvas, { writable: false, configurable: false });
	this.globalAlpha = 1.0;
	this.globalCompositeOperation = "source-over";
	this.strokeStyle = "black";
	this.fillStyle = "black";
	this.lineWidth = 1;
	this.lineCap = "butt";
	this.lineJoin = "miter";
	this.miterLimit = 10;
	this.shadowOffsetX = 0;
	this.shadowOffsetY = 0;
	this.shadowBlur = 0;
	this.shadowColor = "transparent black";
	this.font = "10px sans-serif";
	this.textAlign = "start";
	this.textBaseline = "alphabetic";
}
var Color = java.awt.Color;
var colorMap = {
	BLACK: Color.BLACK,
	BLUE: Color.BLUE,
	CYAN: Color.CYAN,
	DARKGRAY: Color.DARK_GRAY,
	GRAY: Color.GRAY,
	GREEN: Color.GREEN,
	LIGHTGRAY: Color.LIGHT_GRAY,
	MAGENTA: Color.MAGENTA,
	ORANGE: Color.ORANGE,
	PINK: Color.PINK,
	RED: Color.RED,
	WHITE: Color.WHITE,
	YELLOW: Color.YELLOW
};

Object.defineProperties(Canvas.prototype, {
	color: {
		set: function(value) {
			var color;
			var colorName = value.toUpperCase().replace(/_/g, '');
			if ( colorName in colorMap ) {
				color = colorMap[colorName];
			} else {
				throw new TypeError("Could not understand color value");
			}
			this._graphics.setColor(color);
		}
	}
});
CanvasRenderingContext2D.prototype.fillRect = function fillRect(x, y, w, h) {
	this._graphics.drawRect(x, y, w, h);
};

exports.Canvas = Canvas;
exports.CanvasRenderingContext2D = CanvasRenderingContext2D;

