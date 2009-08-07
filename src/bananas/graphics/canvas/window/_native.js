({
	
	makeWindow: function() {
		var win = {
			_frame: new javax.swing.JFrame(),
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
	}
	
})
