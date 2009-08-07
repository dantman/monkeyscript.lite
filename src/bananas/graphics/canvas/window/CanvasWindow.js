
function CanvasWindow() {
	Object.defineProperty(this, "_win", {value: _native.makeWindow(), enumerable: false, writable: false, configurable: false})
}
Object.defineProperties(CanvasWindow.prototype, {
	title: {
		get: function() { return this._win.getTitle(); },
		set: function(t) { this._win.setTitle(t); }
	},
	width: {
		get: function() { return this._win.getWidth(); },
		set: function(w) { this._win.setWidth(w); }
	},
	height: {
		get: function() { return this._win.getHeight(); },
		set: function(h) { this._win.setHeight(h); }
	}
});
CanvasWindow.prototype.open = function open() {
	this._win.open();
};
CanvasWindow.prototype.close = function close() {
	this._win.close();
};

exports.CanvasWindow = CanvasWindow;

