package com.vtd.pianoapp.keyboard;

import android.graphics.Paint;
import android.util.SparseArray;
import com.vtd.pianoapp.object.Position;

public class SharedKeyboardParams {
	public SparseArray<Position> keyPosMapping;
	Paint keyPaint;
	public float whiteKeyWidth;
	public float blackKeyWidth;
	public float contentWidth;
	public int keyboardW;
}
