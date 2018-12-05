package com.vtd.pianoapp.util;


import com.vtd.pianoapp.songobject.GamePlayNote;

import java.util.ArrayList;

public class NoteUtils {
	private static final boolean hasIndex = true;
	private static String[] names = new String[]{"c", "d", "e", "f", "g", "a", "b"};
	private static int[] sharpNoteIndexes = new int[]{1, 3, 6, 8, 10};
	private static ArrayList<String> notes;

	public static int noteIdOf(String noteName) {
		if (notes == null) {
			notes = new ArrayList<>();
			getNoteData();
		}
		return notes.indexOf(noteName) + 1 + 20;
	}

	private static void getNoteData() {
		ArrayList<String> temp = new ArrayList<>();
		for (int i = 0; i < names.length; i++) {
			temp.add(names[i]);
			if (i != 2 && i != 6) {
				temp.add(names[i]);
			}
		}

		for (int i = 1; i <= 7; i++) {
			for (int j = 0; j < temp.size(); j++) {
				String key = temp.get(j);
				if (hasIndex) {
					key += i;
				}
				if (isSharpNote(j)) {
					key += "m";
				}
				notes.add(key);
			}
		}
		String note1 = names[5];
		String note2 = names[5];
		String note3 = names[6];
		String note108 = names[0];
		if (hasIndex) {
			note1 += "0";
			note2 += "0";
			note3 += "0";
			note108 += "8";
		}
		note2 += "m";
		notes.add(0, note1);
		notes.add(1, note2);
		notes.add(2, note3);
		notes.add(note108);
	}

	private static boolean isSharpNote(int index) {
		for (int sharpNote : sharpNoteIndexes) {
			if (index == sharpNote)
				return true;
		}
		return false;
	}

	public static boolean isSharpNote(GamePlayNote gamePlayNote) {
		return gamePlayNote.name.contains("m");
	}

	public static int keyIndexOf(GamePlayNote note) {
		return keyIndexOf(note.id);
	}

	public static int keyIndexOf(int noteId) {
		return noteId - 21;
	}

	public static int noteIdOf(int keyIndex) {
		return keyIndex + 21;
	}

	public static boolean isBlackKey(int keyIndex) {
		if (notes == null) {
			notes = new ArrayList<>();
			getNoteData();
		}
		return notes.get(keyIndex).contains("m");
	}
}
