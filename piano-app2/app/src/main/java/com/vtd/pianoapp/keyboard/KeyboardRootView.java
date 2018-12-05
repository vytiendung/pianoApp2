package com.vtd.pianoapp.keyboard;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;
import com.vtd.pianoapp.MyApplication;
import com.vtd.pianoapp.R;
import com.vtd.pianoapp.game.HintHolder;
import com.vtd.pianoapp.songobject.RubyStep;

import java.util.ArrayList;

public class KeyboardRootView extends RelativeLayout implements KeyboardProxy {

	private AnimationContainerScene animationScene;
//	private AnimationContainerView animationContainerView;
	private KeyboardContainer keyboardContainer;

	public KeyboardRootView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void requestUI(Context context, SurfaceViewConfig builder) {
		keyboardContainer = findViewById(R.id.keyboard_container);
		AnimationContainerView noteAnimContainer = findViewById(R.id.note_anim_container);
		animationScene = new AnimationContainerScene(context, builder);
		animationScene.setKeyboardParams(keyboardContainer.pianoView.getKeyboardParams());
//		animationContainerView = new AnimationContainerView(context,keyboardContainer.pianoView.getKeyboardParams());
		noteAnimContainer.setKeyboardParams(keyboardContainer.pianoView.getKeyboardParams());
		float whiteKeyActualHeight = KeyboardHeight.currentHeightPixels();
		Log.d("ttt", "whiteKeyActualHeight = " + whiteKeyActualHeight);
		keyboardContainer.pianoView.setContentHeight((int) whiteKeyActualHeight);
		addOnScaleListener(noteAnimContainer);
		addOnScrollListener(noteAnimContainer);
		keyboardContainer.requestUI();
		noteAnimContainer.setBackgroundColor(MyApplication.getInstance().getResources().getColor(R.color.colorBlack));

		if (builder.getMode() == SurfaceViewConfig.CHALLENGE_MODE || builder.getMode() == SurfaceViewConfig.PRACTICE_MODE) {
			animationScene.drawGuideline();
		}
//		noteAnimContainer.addView(animationContainerView, 0);
	}

	public void addOnScaleListener(KeyboardScalingObserver observer) {
		keyboardContainer.addOnScaleListener(observer);
	}

	public void addOnScrollListener(KeyboardScrollingObserver observer) {
		keyboardContainer.addOnScrollListener(observer);
	}

	public void removeOnScaleListener(KeyboardScalingObserver scalingObserver) {
		keyboardContainer.removeOnScaleListener(scalingObserver);
	}

	public void removeOnScrollListener(KeyboardScrollingObserver scrollingObserver) {
		keyboardContainer.removeOnScrollListener(scrollingObserver);
	}

	public AnimationContainerScene getAnimationScene() {
		return animationScene;
	}

	public void cleanUp() {
		animationScene.removeAllChildren(true);
	}

	public KeyboardProxy getKeyboardProxy() {
		return this;
	}

	public AnimationLayerProxy getAnimLayerProxy() {
		return animationScene.getAnimationLayerProxy();
	}

	public void setOnHintListener(Runnable onHintListener) {
		keyboardContainer.pianoView.setOnHintListener(onHintListener);
	}

	@Override
	public void clearHint() {
		keyboardContainer.pianoView.clearHint();
	}

	@Override
	public void showHintForCurrentStep(ArrayList<HintHolder> hintHolders) {
		keyboardContainer.scrollToKeys(hintHolders);
		keyboardContainer.pianoView.showHintForCurrentStep(hintHolders);
	}

	@Override
	public void setSoundOnTouch(PerformActionListener keyActionListener) {
		keyboardContainer.pianoView.setPerformActionListener(keyActionListener);
	}

	@Override
	public void removeHint(int[] keyIndexes) {
		keyboardContainer.pianoView.removeHint(keyIndexes);
	}

	public SharedKeyboardParams getSharedKeyboardParams() {
		return keyboardContainer.pianoView.getKeyboardParams();
	}

	@Override
	public void notifyKeyboardHeightChanged() {
		keyboardContainer.notifyKeyboardHeightChanged();
	}

	@Override
	public void notifyKeyboardWidthChanged() {
		keyboardContainer.notifyKeyboardWidthChangedFromSetting();
	}

	@Override
	public void showHintForNextStep(ArrayList<HintHolder> hintHolders) {
		keyboardContainer.pianoView.showHintForNextStep(hintHolders);
	}

	@Override
	public void refreshView() {
		keyboardContainer.pianoView.refreshView();
	}

	@Override
	public void ioTouchDown(int keyIndex) {
		keyboardContainer.pianoView.ioTouchDown(keyIndex);
	}

	@Override
	public void ioTouchUp(int keyIndex) {
		keyboardContainer.pianoView.ioTouchUp(keyIndex);
	}

	@Override
	public void updateBlinks(RubyStep step) {
		keyboardContainer.pianoView.updateBlinks(step);
	}

	@Override
	public void removePressState() {
		keyboardContainer.pianoView.removePressState();
	}

	@Override
	public void showHintForFirstStep(ArrayList<HintHolder> hintHolders, ArrayList<Integer> keyIndexesToScroll) {
		scrollToCenter(keyIndexesToScroll);
		keyboardContainer.pianoView.showHintForCurrentStep(hintHolders);
	}

	public void scrollToCenter(ArrayList<Integer> keyIndexesToScroll) {
		keyboardContainer.scrollToCenter(keyIndexesToScroll);
	}

	public void scrollToKey(String keyname) {
		keyboardContainer.scrollToKey(keyname);
	}

	public void changeThemeKeyboard(Activity activity) {
		if (keyboardContainer != null)
			keyboardContainer.changeThemeKeyboard(activity);
	}

	public float calculateAbsPosX(int noteIndex) {
		SharedKeyboardParams keyboardParams = getSharedKeyboardParams();
		return keyboardParams.keyPosMapping.get(noteIndex).x - Math.abs(keyboardContainer.getScrollX());
	}
}
