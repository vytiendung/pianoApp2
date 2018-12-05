package com.vtd.pianoapp.keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.vtd.pianoapp.common.Config;
import com.vtd.pianoapp.common.Constant;
import com.vtd.pianoapp.object.Position;
import com.vtd.pianoapp.ui.BitmapFromPlist;
import com.vtd.pianoapp.ui.BitmapInfoProperty;
import com.vtd.pianoapp.util.BitmapUtils;

public class AnimationContainerView extends View implements KeyboardScalingObserver, KeyboardScrollingObserver {
	private BitmapFromPlist bitmapPlist;
	private Paint keyPaint;
	private SharedKeyboardParams keyboardParams;


	public AnimationContainerView(Context context, SharedKeyboardParams keyboardParams) {
		super(context);
		this.keyboardParams = keyboardParams;
		init();
	}

	public void setKeyboardParams(SharedKeyboardParams keyboardParams){
		this.keyboardParams = keyboardParams;
	}

	public AnimationContainerView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		this.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		bitmapPlist = BitmapUtils.getBitmapFromPlist(Config.getInstance().imgPath + "spriteCache.plist");
		keyPaint = new Paint();
		keyPaint.setDither(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		render(canvas);
	}

	private void render(Canvas canvas) {
		Log.d("abcxxx", "render: ");
		float key_guide_w = KeyboardWidth.whiteKeyWidthPixels() * Constant.WIDTH_BLACK_RATIO;
		float key_guide_h = Config.getInstance().winHeight - KeyboardHeight.currentHeightPixels();
		float key_guide_w_del =  key_guide_w/4;
		String spriteName = "key_guide.png";
		BitmapInfoProperty bitmapInfoProperty = bitmapPlist.properties.get(spriteName);
		for (int i = 0; i < Config.getInstance().noteList.size(); i++) {
			String name = Config.getInstance().noteList.get(i);
			if (name.contains("m")) {
				Position point = keyboardParams.keyPosMapping.get(i);
				RectF desRect = new RectF(point.x - key_guide_w_del , point.y - key_guide_h, point.x + key_guide_w_del , point.y);
				canvas.drawBitmap(bitmapPlist.image, bitmapInfoProperty.rect, desRect, keyPaint);
			}
		}

	}

	@Override
	public void onHorizScale() {
		requestLayout();
		invalidate();
	}

	@Override
	public void onVertScale() {
		requestLayout();
		invalidate();
	}

	@Override
	public void onScroll(int scrollX) {

	}
}
