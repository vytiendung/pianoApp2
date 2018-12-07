package com.vtd.pianoapp.keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;
import com.vtd.pianoapp.practice.Cell;

import java.util.ArrayList;

public class AnimationLayerCanvas extends RelativeLayout implements AnimationLayerProxy {

	private Paint paint;

	public AnimationLayerCanvas(Context context) {
		super(context);
	}

	public AnimationLayerCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		paint.setDither(true);
		paint.setARGB(255,255,255,255);
		Log.d("abcxxx", "AnimationLayerCanvas: ");
	}


	@Override
	protected void onDraw(Canvas canvas) {
		Log.d("abcxxx", "onDraw: ");
		render(canvas);
	}

	private void render(Canvas canvas) {
		Log.d("abcxxx", "render:");
		RectF desRect = new RectF(10, 10, 30, 30);
		canvas.drawRect(desRect, paint);
	}

	@Override
	public void clearChildren() {

	}

	@Override
	public float getY() {
		return 0;
	}

	@Override
	public void insert(Cell cell) {

	}

	@Override
	public void setY(float y) {

	}

	@Override
	public void destroyChildren(ArrayList<Cell> children) {

	}

	@Override
	public void setVisible(boolean isVisible) {

	}

	@Override
	public boolean getVisible() {
		return false;
	}
}
