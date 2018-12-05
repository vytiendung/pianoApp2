package com.vtd.pianoapp.keyboard;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import com.vtd.pianoapp.R;
import com.vtd.pianoapp.common.Config;
import com.vtd.pianoapp.game.HintHolder;
import com.vtd.pianoapp.object.Position;
import com.vtd.pianoapp.util.NoteUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class KeyboardContainer extends HorizontalScrollView {
	public PianoView pianoView;
	float xScaleFactor;
	float yScaleFactor;
	boolean lookScroll = false;
	int scrollXOnScale;
	private ScaleGestureDetector horizScaleDetector;
	private ThreeFingerGestureDetector vertScaleDetector;
	private KeyboardScalingListener scalingListener;
	private KeyboardScrollingListener scrollingListener;
	private float horizScaleCenterX = 0;

	public KeyboardContainer(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		setup();
	}

	private void setup() {
		print("new KeyboardScrollView()");
		this.setHorizontalScrollBarEnabled(false);
		setOverScrollMode(HorizontalScrollView.OVER_SCROLL_NEVER);
		xScaleFactor = KeyboardWidth.currentScaleFactor();
		yScaleFactor = KeyboardHeight.currentScaleFactor();
		print("setup factor: " + xScaleFactor + " - " + yScaleFactor + " - " + Config.getInstance().keyPerScreen);
		horizScaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				try {
					float detectorScaleFactor = detector.getScaleFactor();
					if ((!KeyboardWidth.equalMaxWidth() || detectorScaleFactor < 1)
							&& (!KeyboardWidth.equalMinWidth() || detectorScaleFactor > 1)) {
						KeyboardWidth.saveByScaleFactor(xScaleFactor *= detectorScaleFactor);

						float focusX = detector.getFocusX();
						float currentSpanX;
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
							currentSpanX = detector.getCurrentSpanX();
						} else {
							currentSpanX = detector.getCurrentSpan();
						}
						float direction = detectorScaleFactor > 1 ? -1 : 1;
						horizScaleCenterX = focusX + direction * (horizScaleCenterX * currentSpanX / 2f);
//						print("onScale: horizScaleCenterX = " + horizScaleCenterX);

						float scrollX = (float) getScrollX() + horizScaleCenterX;
						float pos = scrollX / pianoView.getContentWidth();
						notifyKeyboardWidthChanged();
						scrollX = pianoView.getContentWidth() * pos - horizScaleCenterX;
						final int roundedValue = Math.round(scrollX);
						lookScroll = false;
						scrollTo(roundedValue, getScrollY());
						lookScroll = true;
						post(new Runnable() {
							@Override
							public void run() {
								lookScroll = false;
							}
						});
//						scrollingListener.notifyOnScroll(roundedValue);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return true;
			}
		});
		vertScaleDetector = new ThreeFingerGestureDetector(new ThreeFingerGestureDetector.ThreeFingerGestureListener() {
			@Override
			public void onScaleVertically(float scaleFactor) {
				if ((!KeyboardHeight.equalMaxCurrentHeight() || scaleFactor < 1)
						&& (!KeyboardHeight.equalMinCurrentHeight() || scaleFactor > 1)) {
					KeyboardHeight.saveByScaleFactor(yScaleFactor *= scaleFactor);
					notifyKeyboardHeightChanged();
				}
			}
		});

		scalingListener = new KeyboardScalingListener();
		scrollingListener = new KeyboardScrollingListener();
	}

	private void print(String s) {
		Log.d("ttt", s);
	}

	public void notifyKeyboardHeightChanged() {
		scalingListener.notifyOnVertScale();
		yScaleFactor = KeyboardHeight.currentScaleFactor();
	}

	public void notifyKeyboardWidthChanged() {
		scalingListener.notifyOnHorizScale();
		xScaleFactor = KeyboardWidth.currentScaleFactor();
	}

	@Override
	public void scrollTo(int x, int y) {
		if (!lookScroll) {
			scrollXOnScale = x;
			super.scrollTo(x, y);
			scrollingListener.notifyOnScroll(x);

		} else {
			super.scrollTo(scrollXOnScale, y);
			scrollingListener.notifyOnScroll(y);
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		pianoView = ((PianoView) findViewById(R.id.piano_view));
		addOnScaleListener(pianoView);
		getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
			@Override
			public void onScrollChanged() {
				scrollingListener.notifyOnScroll(getScrollX());
			}
		});
	}

	public void addOnScaleListener(KeyboardScalingObserver scalingObserver) {
		scalingListener.register(scalingObserver);
	}

	public void removeOnScaleListener(KeyboardScalingObserver scalingObserver) {
		scalingListener.unregister(scalingObserver);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		try {
			boolean b = event.getY() < (Config.getInstance().winHeight - KeyboardHeight.currentHeightPixels());
			return b && super.onInterceptTouchEvent(event);
		} catch (Exception ignored) {
			return false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean b = event.getY() < (Config.getInstance().winHeight - KeyboardHeight.currentHeightPixels());
		if (b) {
			vertScaleDetector.onTouchEvent(event);
			horizScaleDetector.onTouchEvent(event);
			findHorizScaleCenterX(event);
		}
		try {
			return b && super.onTouchEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void findHorizScaleCenterX(MotionEvent event) {
		horizScaleCenterX = 0;
		float deviationSumX = 0;
		float deviation = 0;
		int pointerCount = event.getPointerCount();
		int historySize = event.getHistorySize();
		for (int i = 0; i < pointerCount; i++) {
			float currentX = event.getX(i);
			float historicalX = currentX;
			if (historySize > 0) {
				historicalX = event.getHistoricalX(i, 0);
			}
			deviation += currentX - historicalX;
			deviationSumX += Math.abs(currentX - historicalX);
		}
		if (deviation != 0) {
			horizScaleCenterX = deviation / deviationSumX;
		}
	}

	public void requestUI() {
		notifyKeyboardWidthChanged();
	}

	public void addOnScrollListener(KeyboardScrollingObserver scrollingObserver) {
		scrollingListener.register(scrollingObserver);
	}

	public void removeOnScrollListener(KeyboardScrollingObserver scrollingObserver) {
		scrollingListener.unregister(scrollingObserver);
	}

	public void scrollToKeys(ArrayList<HintHolder> hintHolders) {
		SharedKeyboardParams renderParams = pianoView.getKeyboardParams();
		if (hintHolders.size() > 1) {
			Collections.sort(hintHolders, new Comparator<HintHolder>() {
				@Override
				public int compare(HintHolder o1, HintHolder o2) {
					return (o1.keyIndex < o2.keyIndex) ? -1 : ((o1.keyIndex == o2.keyIndex) ? 0 : 1);
				}
			});
		}

		int minCurrent = getScrollX();
		int maxCurrent = minCurrent + Config.getInstance().winWidth;
		int minTarget = minCurrent;
		int maxTarget = maxCurrent;
		for (int i = 0; i < hintHolders.size(); i++) {
			int keyIndex = hintHolders.get(i).keyIndex;
			int left = (int) renderParams.keyPosMapping.get(keyIndex).x;
			int right = left + (int) (NoteUtils.isBlackKey(keyIndex) ? renderParams.blackKeyWidth : renderParams.whiteKeyWidth);
			if (left >= minCurrent && right <= maxCurrent) return;
			if (left < minCurrent) {
				minTarget = left;
			} else if (right > maxCurrent && maxTarget == maxCurrent) {
				maxTarget = right;
			}
		}

		boolean requestLeft = false;
		boolean requestRight = false;
		if (minTarget < minCurrent) {
			requestLeft = true;
		}
		if (maxTarget > maxCurrent) {
			requestRight = true;
		}

		if (requestLeft && requestRight) {
			if (minCurrent - minTarget < maxTarget - maxCurrent) {
				scrollTo(minTarget, getScrollY());
			} else {
				scrollTo(maxTarget - Config.getInstance().winWidth, getScrollY());
			}
			pianoView.ignoreTouchMoveEvent();
		} else if (requestLeft) {
			scrollTo(minTarget, getScrollY());
			pianoView.ignoreTouchMoveEvent();
		} else if (requestRight) {
			scrollTo(maxTarget - Config.getInstance().winWidth, getScrollY());
			pianoView.ignoreTouchMoveEvent();
		}
	}

	public void scrollToCenter(ArrayList<Integer> keyIndexesToScroll) {
		if (keyIndexesToScroll.size() == 0) return;
		int numKeysPerScreen = KeyboardWidth.getActualNumKeysPerScreen();
		float delta = (numKeysPerScreen - 2) / 2;
		int firstKey = keyIndexesToScroll.get(0);
		float averageIndex = 0;
		for (int i = 0; i < keyIndexesToScroll.size(); i++) {
			int keyIndex = keyIndexesToScroll.get(i);
			float nextAverageIndex = (averageIndex + keyIndex) / (i + 1);
			if (Math.abs(nextAverageIndex - firstKey) <= delta) {
				averageIndex = nextAverageIndex;
			} else {
				break;
			}
		}

		SharedKeyboardParams renderParams = pianoView.getKeyboardParams();
		Position centerKeyPosition = renderParams.keyPosMapping.get(Math.round(averageIndex));
		int targetScrollX = (int) (centerKeyPosition.x - Config.getInstance().winWidth / 2);
		scrollTo(targetScrollX, getScrollY());
	}

	public void scrollToKey(String keyname) {
		try {
			SharedKeyboardParams renderParams = pianoView.getKeyboardParams();
			int noteId = NoteUtils.noteIdOf(keyname);
			if (noteId == 20) return;
			int noteIndex = NoteUtils.keyIndexOf(noteId);
			scrollTo((int) renderParams.keyPosMapping.get(noteIndex).x, getScrollY());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void notifyKeyboardWidthChangedFromSetting() {
//		if (preWhiteKeyW != 0){
//			float halfScreenW = Config.getInstance().winWidth/2;
//			float newScrollX = (int) ((getScrollX() /preWhiteKeyW)*KeyboardWidth.whiteKeyWidthPixels());
//			newScrollX = newScrollX - (halfScreenW/KeyboardWidth.whiteKeyWidthPixels() - halfScreenW/preWhiteKeyW)*KeyboardWidth
// .whiteKeyWidthPixels();
//			lookScroll = false;
//			scrollTo((int) newScrollX,getScrollY());
//			lookScroll = true;
//			post(new Runnable() {
//				@Override
//				public void run() {
//					lookScroll = false;
//				}
//			});
//			scrollingListener.notifyOnScroll((int) -newScrollX);
//		}
//		preWhiteKeyW = KeyboardWidth.whiteKeyWidthPixels();
//		notifyKeyboardWidthChanged();

		horizScaleCenterX = Config.getInstance().winWidth / 2;
		float scrollX = (float) getScrollX() + horizScaleCenterX; //Config.getInstance().winWidth / 2;
		float pos = scrollX / pianoView.getContentWidth();
		notifyKeyboardWidthChanged();
		scrollX = pianoView.getContentWidth() * pos - horizScaleCenterX; //- Config.getInstance().winWidth / 2;
		int roundedValue = Math.round(scrollX);
		lookScroll = false;
		scrollTo(roundedValue, getScrollY());
		lookScroll = true;
		post(new Runnable() {
			@Override
			public void run() {
				lookScroll = false;
			}
		});
//		scrollingListener.notifyOnScroll(roundedValue);
	}

	public void changeThemeKeyboard(Activity activity) {
		if (pianoView != null)
			pianoView.changeTheme(activity);
	}
}
