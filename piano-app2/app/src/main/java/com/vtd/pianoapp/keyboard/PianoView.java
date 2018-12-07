package com.vtd.pianoapp.keyboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import com.vtd.pianoapp.R;
import com.vtd.pianoapp.common.Config;
import com.vtd.pianoapp.common.Constant;
import com.vtd.pianoapp.common.NoteLabelManager;
import com.vtd.pianoapp.common.SettingsManager;
import com.vtd.pianoapp.game.GameplaySetting;
import com.vtd.pianoapp.game.HintHolder;
import com.vtd.pianoapp.gameUtils.PianoKeyHelper;
import com.vtd.pianoapp.object.Position;
import com.vtd.pianoapp.songobject.GamePlayNote;
import com.vtd.pianoapp.songobject.RubyStep;
import com.vtd.pianoapp.ui.BitmapFromPlist;
import com.vtd.pianoapp.ui.BitmapInfoProperty;
import com.vtd.pianoapp.util.BitmapUtils;
import com.vtd.pianoapp.util.NoteUtils;

import java.util.ArrayList;
import java.util.Iterator;


public class PianoView extends View implements  KeyboardScalingObserver {
	public String TAG = "PianoView";
	Config config;
	PerformActionListener performActionListener;
	int x = 1;
	private ArrayList<SingleKeyboardData> listKeyboardData;
	private SparseArray<Position> keyPosMapping;
	private SparseIntArray pointerKeyIndexMapping;
	private ArrayList<Integer> pointersIsBeingLocked;
	private BitmapFromPlist bitmapPlist;
	private Vibrator vibrator;
	private Paint keyPaint;
	private Paint pinkPaint;
	private Paint bluePaint;
	private Paint greenPaint;
	private Paint textPaint;
	private Paint circleHintPaint;
	private Paint textHintPaint;
	private int heightTextNumber;
	private int contentWidth;
	private float whiteKeyWidth;
	private float blackKeyWidth;
	private float whiteKeyHeight;
	private float blackKeyHeight;
	private float noteLabelWidth;
	private float blackKeyBlinkW;
	private float blackKeyBlinkH;
	private float whiteKeyBlinkW;
	private float whiteKeyBlinkH;
	private float screenH;
	private SharedKeyboardParams keyboardParams;
	private BitmapFromPlist bitmapPlist2;
	private RubyStep blinksKeyboardSteps;
	private int preWhiteKeyRight;
	private Thread threadUpdateBlinks;
	private Handler handlerUpdateBlinks;
	private Runnable runnableUpdateBlinks;
	private boolean isMoveEventLocked;
	private Runnable onHintListener;

	public PianoView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		if (isInEditMode()) return;
		config = Config.getInstance();
		listKeyboardData = new ArrayList<SingleKeyboardData>() {{
			for (int i = 0; i < config.noteList.size(); i++) {
				add(new SingleKeyboardData(i, config.noteList.get(i)));
			}
		}};
		keyPosMapping = new SparseArray<>();
		pointerKeyIndexMapping = new SparseIntArray();
		pointersIsBeingLocked = new ArrayList<>();
		buildBitmapPlist();
		buildDrawingPaint();
		noteLabelWidth = getLabelWidth(textPaint, whiteKeyWidth);
		keyboardParams = new SharedKeyboardParams();
		keyboardParams.keyPaint = keyPaint;
		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		screenH = Config.getInstance().winHeight;
		handlerUpdateBlinks = new Handler();
	}

	private void buildBitmapPlist() {
		String imgPath = config.imgPath + "keyboard/";
		bitmapPlist = BitmapUtils.getBitmapFromPlist(imgPath + "keyboard.plist");
		bitmapPlist2 = BitmapUtils.getBitmapFromPlist(config.imgPath + "guideNote.plist");
	}

	private void buildDrawingPaint() {
		keyPaint = new Paint();
		keyPaint.setDither(true);

		pinkPaint = new Paint();
		pinkPaint.setColor(0xffcfd4);
		pinkPaint.setAlpha(220);

		bluePaint = new Paint();
		bluePaint.setColor(0xbfe3ee);
		bluePaint.setAlpha(220);

		greenPaint = new Paint();
		greenPaint.setColor(0x9cedd6);
		greenPaint.setAlpha(220);

		textPaint = new Paint();
		textPaint.setColor(Color.BLACK);
		textPaint.setAntiAlias(true);
		if (config.deviceType == Constant.LARGE) {
			textPaint.setTextSize(12 * config.scaleY);
		} else {
			textPaint.setTextSize(18 * config.scaleY);
		}
		textPaint.setTextAlign(Paint.Align.LEFT);

		circleHintPaint = new Paint();
		circleHintPaint.setColor(Color.BLUE);
		circleHintPaint.setAntiAlias(true);

		textHintPaint = new Paint();
		textHintPaint.setDither(true);
		textHintPaint.setAntiAlias(true);
		textHintPaint.setTextAlign(Paint.Align.CENTER);
		textHintPaint.setAlpha(180);
		if (config.deviceType == Constant.LARGE) {
			textHintPaint.setTextSize(15 * config.scaleY);
		} else {
			textHintPaint.setTextSize(20 * config.scaleY);
		}
		final Paint.FontMetricsInt fm = textHintPaint.getFontMetricsInt();
		heightTextNumber = (int) Math.ceil(fm.bottom - fm.top);
	}

	public float getLabelWidth(Paint p, float max) {
		float w = 0;
		float t;
		String n;
		for (int i = 0; i < NoteLabelManager.getInstance().labelList.size(); i++) {
			n = NoteLabelManager.getInstance().labelList.get(i).trim();
			if (n.contains("m"))
				continue;
			t = p.measureText(n);
			t = t + t / 3 > max / 2 ? t + 4 : t + t / 3;
			if (t > w)
				w = t;
		}
		w = w < max * 0.45f ? max * 0.45f : w;
		w = w < max ? w : max;
		return w;
	}

	@Override
	public void onHorizScale() {
		calculateKeyboardPosition();
		getLayoutParams().width = contentWidth;
		requestLayout();
		refreshView();
	}

	@Override
	public void onVertScale() {
		whiteKeyHeight = KeyboardHeight.currentHeightPixels();
		calculateKeyboardPosition();
		requestLayout();
		refreshView();
	}

	private void calculateKeyboardPosition() {
		if (isInEditMode()) return;
		whiteKeyWidth = KeyboardWidth.whiteKeyWidthPixels();
		blackKeyWidth = whiteKeyWidth * Constant.WIDTH_BLACK_RATIO;// + (config.rows == Constant.PERFORM_MODE ? whiteKeyWidth / 9 : 0);
		calculateBlinkSize();
		blackKeyHeight = whiteKeyHeight * Constant.HEIGHT_BLACK_RATIO;
		noteLabelWidth = getLabelWidth(textPaint, whiteKeyWidth);
		noteLabelWidth = getLabelWidth(textPaint, whiteKeyWidth);

		float leftWhite = 0f;
		float leftBlack = -blackKeyWidth / 2f;
		int op = 1;
		for (int i = 0; i < config.noteList.size(); i++) {
			String noteName = config.noteList.get(i);
			if (noteName.contains("m")) {
				switch (op) {
					case 1:
						leftBlack += whiteKeyWidth + whiteKeyWidth * Constant.DELTA_BLACK_POS;
						break;
					case 2:
						leftBlack += whiteKeyWidth * 2 - whiteKeyWidth * Constant.DELTA_BLACK_POS * 2;
						break;
					case 3:
						leftBlack += whiteKeyWidth + whiteKeyWidth * Constant.DELTA_BLACK_POS * 2;
						break;
					case 4:
						leftBlack += whiteKeyWidth * 2 - whiteKeyWidth * Constant.DELTA_BLACK_POS * 2;
						break;
					case 5:
						leftBlack += whiteKeyWidth + whiteKeyWidth * Constant.DELTA_BLACK_POS;
						break;
					default:
						break;

				}
				keyPosMapping.put(i, new Position(leftBlack, screenH - whiteKeyHeight));
				op++;
				if (op > 5) op = 1;
			} else {
				keyPosMapping.put(i, new Position(leftWhite, screenH - whiteKeyHeight));
				leftWhite += whiteKeyWidth;
			}

		}
		contentWidth = (int) leftWhite;

		keyboardParams.whiteKeyWidth = whiteKeyWidth;
		keyboardParams.blackKeyWidth = blackKeyWidth;
		keyboardParams.keyPosMapping = keyPosMapping;
		keyboardParams.contentWidth = contentWidth;
	}

	private void calculateBlinkSize() {
		BitmapInfoProperty bitmapInfoProperty1 = bitmapPlist2.properties.get("trang_1.png");
		Rect srcRect = bitmapInfoProperty1.rect;
		float oldw, oldh;
		oldh = Math.abs(srcRect.bottom - srcRect.top);
		oldw = Math.abs(srcRect.right - srcRect.left);
		blackKeyBlinkW = blackKeyWidth * 2.5f;
		whiteKeyBlinkW = whiteKeyWidth * 2.5f;
		blackKeyBlinkH = (blackKeyBlinkW * oldh) / oldw;
		whiteKeyBlinkH = (whiteKeyBlinkW * oldh) / oldw;
	}

	void refreshView() {
		invalidate();
	}

	public void setContentHeight(int contentHeight) {
		whiteKeyHeight = contentHeight;
		requestLayout();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		try {
			switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
					return onTouchBegan(event);
				case MotionEvent.ACTION_POINTER_DOWN:
					return onTouchBegan(event);
				case MotionEvent.ACTION_POINTER_UP:
					return onTouchEnded(event);
				case MotionEvent.ACTION_UP:
					return onTouchEnded(event);
				case MotionEvent.ACTION_MOVE:
					return onTouchMoved(event);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return event.getY() > screenH - whiteKeyHeight;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//		Log.d(TAG, "onSizeChanged: xxxx");

		super.onSizeChanged(w, h, oldw, oldh);
		keyboardParams.keyboardW = w;
		calculateKeyboardPosition();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (isInEditMode()) return;
		try {
			render(canvas);
			drawBlink(canvas);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void render(Canvas canvas) {
		preWhiteKeyRight = 0;
		for (int i = 0; i < config.noteList.size(); i++) {
			SingleKeyboardData data = listKeyboardData.get(i);
			if (!data.isBlackKey()) {
				drawKey(data, canvas);
				drawRepeatingNoteIndicator(data, canvas);
			}
		}
		for (int i = 0; i < config.noteList.size(); i++) {
			SingleKeyboardData data = listKeyboardData.get(i);
			if (data.isBlackKey()) {
				drawKey(data, canvas);
				drawRepeatingNoteIndicator(data, canvas);
			}
		}
	}

	private void drawKey(SingleKeyboardData data, Canvas canvas) {
		String spriteName;
		float keyWidth, keyHeight;
		boolean hasLabel = !data.isBlackKey();
		if (data.isPressed() && config.enableKeyPressedEffect) {
			spriteName = data.getPressedSpriteName();
		} else if (data.isHinted()) {
			spriteName = data.getHintedSpriteName();
		} else {
			spriteName = data.getReleasedSpriteName();
		}
		if (data.isBlackKey()) {
			keyWidth = blackKeyWidth;
			keyHeight = blackKeyHeight;
		} else {
			keyWidth = whiteKeyWidth;
			keyHeight = whiteKeyHeight;
		}

		BitmapInfoProperty bitmapInfoProperty = bitmapPlist.properties.get(spriteName);
		Position point = getPoint(data);
		RectF desRect;
		if (data.isBlackKey()) {
			desRect = new RectF(point.x, point.y, point.x + keyWidth, point.y + keyHeight);
		} else {
			int right = (int) (point.x + keyWidth);
			desRect = new RectF(preWhiteKeyRight, point.y, right, point.y + keyHeight);
			preWhiteKeyRight = right;
		}
		canvas.drawBitmap(bitmapPlist.image, bitmapInfoProperty.rect, desRect, keyPaint);
		Log.d(TAG, "drawKey: " + preWhiteKeyRight + " " + point.y);
		if (hasLabel) {
			drawKeyLabel(data, canvas);
		}
	}

	private void drawKeyLabel(SingleKeyboardData data, Canvas canvas) {
		Position point = getPoint(data);
		float px = point.x + whiteKeyWidth / 12f;
		float py = point.y - whiteKeyWidth / 12f + whiteKeyHeight * 0.95f - noteLabelWidth;
		float h = noteLabelWidth;
		RectF rect = new RectF((int) px, (int) py, (int) px + h, (int) py + h);
		if (PianoKeyHelper.isPinkLabel(data.getName())) {
			canvas.drawRoundRect(rect, 4, 4, pinkPaint);
		} else if (PianoKeyHelper.isGreenLabel(data.getName())) {
			canvas.drawRoundRect(rect, 4, 4, greenPaint);
		} else {
			canvas.drawRoundRect(rect, 4, 4, bluePaint);
		}
		String realName = NoteLabelManager.getInstance().labelList.get(data.getIndex()).trim();
		Rect bounds = new Rect();
		textPaint.getTextBounds(realName, 0, realName.length(), bounds);
		float x = px + Math.abs(textPaint.measureText(realName) - h) / 2;
		float y = py + noteLabelWidth - Math.abs((bounds.top < 0 ? -bounds.top : bounds.top)
				+ (bounds.bottom < 0 ? -bounds.bottom : bounds.bottom) - h) / 2;

		int oldColor = textPaint.getColor();

		if (PianoKeyHelper.isPinkLabel(data.getName())) {
			textPaint.setColor(Color.rgb(247, 148, 29));
		} else if (PianoKeyHelper.isGreenLabel(data.getName())) {
			textPaint.setColor(Color.rgb(0, 114, 54));
		} else {
			textPaint.setColor(Color.rgb(0, 114, 188));
		}
//		if (config.noteLabelStyle == Constant.STYLE_TEXT_ONLY)
//
//		else {
//			textPaint.setColor(oldColor);
//		}
		canvas.drawText(realName, x, y, textPaint);
	}

	private Position getPoint(SingleKeyboardData data) {
		return keyPosMapping.get(data.getIndex());
	}

	private void drawRepeatingNoteIndicator(SingleKeyboardData data, Canvas canvas) {
		int repeatTimes = data.getRepeatTimes();
		if (!GameplaySetting.isGuideNotesEnabled() && repeatTimes >= 1) {
			float deltaX, deltaY;
			float radiusBg = blackKeyWidth / 12 * 5;
			float radiusAccent = blackKeyWidth / 3;
			if (data.isBlackKey()) {
				deltaX = blackKeyWidth / 2;
				deltaY = blackKeyHeight / 5 * 2;
			} else {
				deltaX = whiteKeyWidth / 2;
				deltaY = whiteKeyHeight / 6 + blackKeyHeight;
			}
			Position point = getPoint(data);

			circleHintPaint.setColor(Color.WHITE);
			canvas.drawCircle(point.x + deltaX, point.y + deltaY, radiusBg, circleHintPaint);

			int colorAccent = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null);
			circleHintPaint.setColor(colorAccent);
			canvas.drawCircle(point.x + deltaX, point.y + deltaY, radiusAccent, circleHintPaint);

			if (repeatTimes > 1) {
				textHintPaint.setColor(Color.WHITE);
				canvas.drawText(String.valueOf(repeatTimes), point.x + deltaX, point.y + deltaY + heightTextNumber * 0.25f,
						textHintPaint);
			}
		}
	}

	private void drawBlink(Canvas canvas) {
		if (blinksKeyboardSteps != null) {
			x++;
			x %= 5;
			x = x == 0 ? 5 : x;
			BitmapInfoProperty bitmapInfoProperty1 = bitmapPlist2.properties.get("trang_" + x + ".png");
			for (Iterator<GamePlayNote> iterator = blinksKeyboardSteps.notes.iterator(); iterator.hasNext(); ) {
				GamePlayNote note = iterator.next();
				try {
					drawBlink(canvas, NoteUtils.keyIndexOf(note.id), bitmapInfoProperty1);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		}
	}

	private void drawBlink(Canvas canvas, int keyboardIndex, BitmapInfoProperty bitmapInfoProperty1) {
		SingleKeyboardData data = listKeyboardData.get(keyboardIndex);
		Position point = getPoint(data);
		float neww = data.isBlackKey() ? blackKeyBlinkW : whiteKeyBlinkW;
		float newh = data.isBlackKey() ? blackKeyBlinkH : whiteKeyBlinkH;
		float detalx = (neww - (data.isBlackKey() ? blackKeyWidth : whiteKeyWidth)) / 2;
		RectF desRect = new RectF(point.x - detalx, point.y - newh / 2 + newh * 0.083f,
				point.x - detalx + neww, point.y + newh - newh / 2 + newh * 0.083f);

		canvas.drawBitmap(bitmapPlist2.image, bitmapInfoProperty1.rect, desRect, keyPaint);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		bitmapPlist.cleanUp();
		SettingsManager.getInstance()
				.putFloat(Constant.KEY_SCALE_X, config.keyScaleX)
				.putFloat(Constant.KEY_SCALE_Y, config.keyScaleY)
				.putInt(Constant.KEY_PER_SCREEN, config.keyPerScreen);
		Log.d("ttt", "onDetach: factor = " + config.keyScaleX + " - " + config.keyScaleY + " - " + config.keyPerScreen);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(contentWidth, heightMeasureSpec);
	}

	private boolean onTouchBegan(MotionEvent event) {
		int actionIndex = event.getActionIndex();
//		Log.d("ttt", "onTouchBegan: " + actionIndex);
		if (event.getY(actionIndex) < screenH - KeyboardHeight.currentHeightPixels()) return false;
		int touchedKeyIndex = getTouchedKey(event.getX(actionIndex), event.getY(actionIndex));
//		Log.d("ttt", "touchedKeyIndex = " + touchedKeyIndex + " - " + listKeyboardData.get(touchedKeyIndex).getName());

		onKeyPerformed(touchedKeyIndex, event.getPointerId(actionIndex), event.getY(actionIndex));
		refreshView();
		return true;
	}

	private void onKeyPerformed(int keyIndex, int pointerId, float posY) {
		savePointer(pointerId, keyIndex);
		listKeyboardData.get(keyIndex).setPressed();
		if (performActionListener == null) {
			performActionListener = KeySoundOnPerformAction.ref();
		}
		performActionListener.onKeyPerformed(keyIndex, pointerId);
	}

	private boolean onTouchMoved(MotionEvent event) {
		if (isMoveEventLocked) return true;
		boolean needRefresh = false;
		for (int i = 0; i < event.getPointerCount(); i++) {
			int pointerId = event.getPointerId(i);
			if (pointersIsBeingLocked.indexOf(pointerId) >= 0) {
				continue;
			}
			int lastTouchedKeyIndex = loadPointer(pointerId, -1);
//			Log.d("ttt", "eventY = " + event.getY(i) + ", limit = " + (config.winHeight - KeyboardHeight.currentHeightPixels()));
			if (event.getY(i) < screenH - KeyboardHeight.currentHeightPixels()) {
				if (lastTouchedKeyIndex != -1) {
					Log.d("ttt", "touch outside keyboard: last index = " + lastTouchedKeyIndex);
					clearPointer(pointerId);
					onKeyReleased(lastTouchedKeyIndex, pointerId);
					needRefresh = true;
				}
				continue;
			}
			int touchedKeyIndex = getTouchedKey(event.getX(i), event.getY(i));
			if (lastTouchedKeyIndex != -1 && lastTouchedKeyIndex != touchedKeyIndex) {
//				Log.d("ttt", "touch new key inside keyboard = " + touchedKeyIndex + ", old key = " + lastTouchedKeyIndex);
				onKeyReleased(lastTouchedKeyIndex, pointerId);
				onKeyPerformed(touchedKeyIndex, pointerId, event.getY(i));
				needRefresh = true;
			} else if (lastTouchedKeyIndex == -1) {
//				Log.d("ttt", "touch into keyboard from outside: key index = " + touchedKeyIndex);
				onKeyPerformed(touchedKeyIndex, pointerId, event.getY(i));
				needRefresh = true;
			}
		}
		if (needRefresh) {
			refreshView();
		}
		return true;
	}

	private boolean onTouchEnded(MotionEvent event) {
		int actionIndex = event.getActionIndex();
		int pointerId = event.getPointerId(actionIndex);
		int lockedPointerIdIndex = pointersIsBeingLocked.indexOf(pointerId);
		if (lockedPointerIdIndex >= 0) {
			pointersIsBeingLocked.remove(lockedPointerIdIndex);
		}
		int touchedKeyIndex = loadPointer(pointerId, -1);
		if (touchedKeyIndex != -1) {
			clearPointer(pointerId);
			onKeyReleased(touchedKeyIndex, pointerId);
			refreshView();
		}
		return true;
	}

	private void onKeyReleased(int keyIndex, int pointerId) {
		SingleKeyboardData target = listKeyboardData.get(keyIndex);
		if (target.isPressed()) {
			target.setReleased();
		}
		performActionListener.onKeyReleased(keyIndex, pointerId);
		clearBlinks(keyIndex);
	}

	private int getTouchedKey(float x, float y) {
		int numWhite = (int) (x / whiteKeyWidth);
		int numBlack;
		if (numWhite <= 1) {
			numBlack = numWhite == 1 ? 1 : 0;
		} else {
			numBlack = 1 + ((numWhite - 1) / 7) * 5;
			int rest = (numWhite - 1) % 7;
			if (rest == 2) {
				numBlack += 1;
			} else if (rest == 3 || rest == 4) {
				numBlack += 2;
			} else if (rest == 5) {
				numBlack += 3;
			} else if (rest == 6) {
				numBlack += 4;
			}
		}
		int whiteKeyIndex = numWhite + numBlack;
		if (y > blackKeyHeight + screenH - whiteKeyHeight) {
			return whiteKeyIndex;
		}
		float newBlackKeyW = blackKeyWidth;
		float newBlackKeyPos = whiteKeyIndex < listKeyboardData.size() - 1 ? keyPosMapping.get(whiteKeyIndex + 1).x : 0;

		if (Config.getInstance().isWideTouchArea && y < whiteKeyHeight / 2 + screenH - whiteKeyHeight) {
			try {
				if (listKeyboardData.get(whiteKeyIndex - 1).isBlackKey()) {
					newBlackKeyPos = keyPosMapping.get(whiteKeyIndex + 1).x - blackKeyWidth / 2;
				} else {
					newBlackKeyPos = keyPosMapping.get(whiteKeyIndex).x;
				}
			} catch (Exception ignored) {
				newBlackKeyPos = keyPosMapping.get(whiteKeyIndex).x;
			}
			try {
				if (listKeyboardData.get(whiteKeyIndex + 1).isBlackKey()) {
					newBlackKeyW = whiteKeyWidth / 2 + keyPosMapping.get(whiteKeyIndex).x - keyPosMapping.get(whiteKeyIndex -
							1).x;
				} else {
					newBlackKeyW = whiteKeyWidth + keyPosMapping.get(whiteKeyIndex).x - keyPosMapping.get(whiteKeyIndex - 1).x;
				}
			} catch (Exception ignored) {
			}
		}


		if (whiteKeyIndex > 0 && listKeyboardData.get(whiteKeyIndex - 1).isBlackKey()
				&& (keyPosMapping.get(whiteKeyIndex - 1).x + newBlackKeyW > x)) {
			return whiteKeyIndex - 1;
		}
		if (whiteKeyIndex < listKeyboardData.size() - 1 && listKeyboardData.get(whiteKeyIndex + 1).isBlackKey()
				&& x > newBlackKeyPos) {
			return whiteKeyIndex + 1;
		}
		return whiteKeyIndex;
	}

	private void savePointer(int pointerId, int keyIndex) {
		pointerKeyIndexMapping.put(pointerId, keyIndex);
	}

	private int loadPointer(int pointerId, int valueIfNotFound) {
		return pointerKeyIndexMapping.get(pointerId, valueIfNotFound);
	}

	private void clearPointer(int pointerId) {
		pointerKeyIndexMapping.delete(pointerId);
	}

	public void changeTheme(Activity activity) {
		buildBitmapPlist();
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				refreshView();
			}
		});
	}

	public void vibrate() {
		if (config.isVibrate)
			vibrator.vibrate(config.vibrateTime);
	}

	public int getContentWidth() {
		return contentWidth;
	}

	public SharedKeyboardParams getKeyboardParams() {
		return keyboardParams;
	}

	public void clearHint() {
		for (SingleKeyboardData data : listKeyboardData) {
			if (data.isHinted()) data.setReleased();
			data.resetRepeatTimes();
		}
		refreshView();
	}

	public void setOnHintListener(Runnable onHintListener) {
		this.onHintListener = onHintListener;
	}

	public void showHintForCurrentStep(ArrayList<HintHolder> hintHolders) {
		for (HintHolder hintHolder : hintHolders) {
			SingleKeyboardData singleKeyboardData = listKeyboardData.get(hintHolder.keyIndex);
			singleKeyboardData.setHinted();
			singleKeyboardData.setRepeatTimes(hintHolder.repeatTimes);
		}
		if (onHintListener != null) {
			onHintListener.run();
		}
	}

	public void setPerformActionListener(@Nullable PerformActionListener performActionListener) {
		this.performActionListener = performActionListener;
	}

	public void removeHint(int[] keyIndexes) {
		for (int index : keyIndexes) {
			SingleKeyboardData target = listKeyboardData.get(index);
			if (target.isHinted()) {
				target.setReleased();
			}
			target.resetRepeatTimes();
		}
	}

	public void showHintForNextStep(ArrayList<HintHolder> hintHolders) {
		for (HintHolder hintHolder : hintHolders) {
			SingleKeyboardData singleKeyboardData = listKeyboardData.get(hintHolder.keyIndex);
			if (singleKeyboardData.getRepeatTimes() < hintHolder.repeatTimes) {
				Log.d("ttt", "showHintForNextStep: index = " + hintHolder.keyIndex + ", repeatTimes = " + hintHolder
						.repeatTimes);
				singleKeyboardData.setRepeatTimes(hintHolder.repeatTimes);
			}
		}
	}

	public void ioTouchUp(int keyIndex) {
		if (listKeyboardData.get(keyIndex).isPressed()) {
			listKeyboardData.get(keyIndex).setReleased();
			refreshView();
			clearBlinks(keyIndex);
		}
	}

	public void clearBlinks(int keyIndex) {
		try {
			if (blinksKeyboardSteps != null) {
				for (GamePlayNote note : blinksKeyboardSteps.notes) {
					if (NoteUtils.keyIndexOf(note.id) == keyIndex) {
						blinksKeyboardSteps.notes.remove(note);
						break;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void ioTouchDown(int keyIndex) {
		listKeyboardData.get(keyIndex).setPressed();
		refreshView();
	}

	public void updateBlinks(RubyStep step) {
		blinksKeyboardSteps = new RubyStep(step);
		if (runnableUpdateBlinks == null) {
			runnableUpdateBlinks = getRunnableUpdateBlinks();
			threadUpdateBlinks = new Thread(runnableUpdateBlinks);
			threadUpdateBlinks.start();
		} else {
			if (!threadUpdateBlinks.isAlive()) {
				threadUpdateBlinks.interrupt();
				threadUpdateBlinks = new Thread(runnableUpdateBlinks);
				threadUpdateBlinks.start();
			}
		}
	}

	@NonNull
	private Runnable getRunnableUpdateBlinks() {
		return new Runnable() {
			@Override
			public void run() {
				try {
					while (blinksKeyboardSteps != null && blinksKeyboardSteps.duration > 0 && blinksKeyboardSteps.notes.size() >
							0) {
						handlerUpdateBlinks.post(new Runnable() {
							@Override
							public void run() {
								refreshView();
							}
						});
						blinksKeyboardSteps.duration -= 40;
						Thread.sleep(40);
					}
					if (blinksKeyboardSteps != null)
						blinksKeyboardSteps.notes.clear();
					handlerUpdateBlinks.post(new Runnable() {
						@Override
						public void run() {
							refreshView();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		};
	}

	public void removePressState() {
		try {
			boolean needRefreshView = false;
			for (SingleKeyboardData item : listKeyboardData) {
				if (item.isPressed()) {
					item.setReleased();
					needRefreshView = true;
				}
			}
			if (needRefreshView)
				refreshView();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void ignoreTouchMoveEvent() {
		for (int i = 0; i < pointerKeyIndexMapping.size(); i++) {
			int pointerId = pointerKeyIndexMapping.keyAt(i);
			if (pointersIsBeingLocked.indexOf(pointerId) < 0) {
				pointersIsBeingLocked.add(pointerId);
			}
		}
		isMoveEventLocked = true;
		postDelayed(new Runnable() {
			@Override
			public void run() {
				isMoveEventLocked = false;
			}
		}, 200);
	}
}
