package com.vtd.pianoapp.keyboard;

import com.vtd.pianoapp.game.HintHolder;
import com.vtd.pianoapp.songobject.RubyStep;

import java.util.ArrayList;

public interface KeyboardProxy {
	void clearHint();

	void showHintForCurrentStep(ArrayList<HintHolder> hintHolders);

	void setSoundOnTouch(PerformActionListener keyActionListener);

	void removeHint(int[] keyIndexes);

	SharedKeyboardParams getSharedKeyboardParams();

	void notifyKeyboardHeightChanged();

	void notifyKeyboardWidthChanged();

	void showHintForNextStep(ArrayList<HintHolder> hintHolders);

	void refreshView();

	void ioTouchDown(int keyIndex);

	void ioTouchUp(int keyIndex);

	void updateBlinks(RubyStep step);

	void removePressState();

	void showHintForFirstStep(ArrayList<HintHolder> hintHolders, ArrayList<Integer> keyIndexesToScroll);
}
