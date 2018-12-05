package com.vtd.pianoapp.keyboard;


import android.view.MotionEvent;
import com.vtd.pianoapp.common.Config;

public class ThreeFingerGestureDetector {
	private static final float SCALE_LIMIT = Config.getInstance().winHeight*0.45f;
	private static final float MIN_MOVEMENT_TO_SCALE = 10;
	private final ThreeFingerGestureListener listener;
	private float lastAverageYCoord;
	private boolean isScaleAllowed;

    public ThreeFingerGestureDetector(ThreeFingerGestureListener listener) {
        this.listener = listener;
    }

    public void onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() != 3) return;
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_POINTER_UP) {
            lastAverageYCoord = getAveragePointerCoords(event);
	        isScaleAllowed = false;
        } else if (action == MotionEvent.ACTION_MOVE) {
	        float averageYCoords = getAveragePointerCoords(event);
	        if (Math.abs(averageYCoords - lastAverageYCoord) > MIN_MOVEMENT_TO_SCALE) {
		        isScaleAllowed = true;
	        }
	        if (isScaleAllowed) {
		        float deltaPixels = averageYCoords - lastAverageYCoord;
		        lastAverageYCoord = averageYCoords;
		        listener.onScaleVertically(1 - deltaPixels / SCALE_LIMIT);
	        }
        }
    }

	private float getAveragePointerCoords(MotionEvent event) {
	    float sumYCoords = 0;
	    int pointerCount = event.getPointerCount();
	    for (int i = 0; i < pointerCount; i++) {
		    sumYCoords += event.getY(i);
	    }
	    return sumYCoords / (float) pointerCount;
    }

    public static class ThreeFingerGestureListener {
        public void onScaleVertically(float scaleFactor) {
        }
    }
}
