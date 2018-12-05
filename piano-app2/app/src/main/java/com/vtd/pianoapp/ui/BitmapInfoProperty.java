package com.vtd.pianoapp.ui;

import android.graphics.Rect;

public class BitmapInfoProperty {
	public Rect rect;
	private boolean isRotate;

	public BitmapInfoProperty(Rect rect, boolean isRotate) {
		super();
		this.isRotate = isRotate;
		if (isRotate) {
			//noinspection SuspiciousNameCombination
			this.rect = new Rect(rect.left, rect.top, rect.bottom, rect.right);
		} else {
			this.rect = rect;
		}
		this.rect = rect;
	}

	@Override
	public String toString() {
		return "x=" + rect.left + "  y=" + rect.top + "  w=" + rect.width() + " h=" + rect.height() + "  rotate=" + isRotate;
	}
}
