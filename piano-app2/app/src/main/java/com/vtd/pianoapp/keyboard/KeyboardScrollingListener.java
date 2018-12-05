package com.vtd.pianoapp.keyboard;

import java.util.ArrayList;

public class KeyboardScrollingListener {
	private final ArrayList<KeyboardScrollingObserver> observers = new ArrayList<>();

	public void register(KeyboardScrollingObserver observer) {
		if (!observers.contains(observer)) {
			observers.add(observer);
		}
	}

	public void unregister(KeyboardScrollingObserver observer) {
		if (observers.contains(observer)) {
			observers.remove(observer);
		}
	}

	public void notifyOnScroll(int scrollX) {
		for (KeyboardScrollingObserver observer : observers) {
			observer.onScroll(scrollX);
		}
	}
}
