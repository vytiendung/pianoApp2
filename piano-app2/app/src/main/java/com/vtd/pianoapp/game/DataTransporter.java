package com.vtd.pianoapp.game;

import com.vtd.pianoapp.songobject.Song;

public class DataTransporter {
	private static DataTransporter store;
	private Song song;
	private boolean isDataAvailable;

	public static DataTransporter load() {
		if (store == null) {
			store = new DataTransporter();
		}
		return store;
	}

	public static void free() {
		store = null;
	}

	private DataTransporter() {
	}

	private void invalidate() {
		if (isDataAvailable) {
			isDataAvailable = false;
		}
	}

	public DataTransporter putSong(Song song) {
		invalidate();
		this.song = song;
		return this;
	}

	public Song getSong() {
		return isDataAvailable ? song : null;
	}

	public void sent() {
		isDataAvailable = true;
	}
}
