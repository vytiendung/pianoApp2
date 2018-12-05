package com.vtd.pianoapp.keyboard;

import java.util.ArrayList;

public class KeyboardScalingListener {
	private final ArrayList<KeyboardScalingObserver> observers = new ArrayList<>();

	public void register(KeyboardScalingObserver observer) {
		if (!observers.contains(observer)) {
			observers.add(observer);
		}
	}

	public void unregister(KeyboardScalingObserver observer) {
		if (observers.contains(observer)) {
			observers.remove(observer);
		}
	}

	public void notifyOnHorizScale() {
		for (int i = 0; i < observers.size(); i++) {
			observers.get(i).onHorizScale();
		}
	}

	public void notifyOnVertScale() {
		for (int i = 0; i < observers.size(); i++) {
			observers.get(i).onVertScale();
		}
	}
}
