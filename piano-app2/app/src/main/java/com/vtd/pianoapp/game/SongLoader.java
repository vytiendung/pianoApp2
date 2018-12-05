package com.vtd.pianoapp.game;

import android.support.annotation.StringDef;
import com.vtd.pianoapp.MyApplication;
import com.vtd.pianoapp.common.Constant;
import com.vtd.pianoapp.songobject.GamePlayNote;
import com.vtd.pianoapp.songobject.RubyStep;
import com.vtd.pianoapp.util.NoteUtils;
import com.vtd.pianoapp.util.SongUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SongLoader {

	private static final String AUTO_DIR = "AUTO";
	private static final String ASSET_DIR = "ASSET";

	public static SongData loadBuildInSongData(String pathInAssets) {
		return decodeSongDataFromFileDir(pathInAssets, ASSET_DIR);
	}

	private static SongData decodeSongDataFromFileDir(String path, @BaseDir String baseDir) {
		SongData songData = null;
		try {
			InputStream rightInput = getInputStream(path, baseDir);
			String noteData = SongUtils.decodSongData(rightInput);

			String bgSongPath = SongUtils.getBackgroundSongPath(path);
			String bgNoteData = null;
			if (bgSongPath != null) {
				try {
					InputStream leftInput = getInputStream(bgSongPath, baseDir);
					bgNoteData = SongUtils.decodSongData(leftInput);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			songData = decodeSongDataToStepFormat(noteData, bgNoteData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return songData;
	}

	private static InputStream getInputStream(String path, String baseDir) throws IOException {
		InputStream rightInput;
		if (ASSET_DIR.equals(baseDir)) {
			rightInput = MyApplication.getInstance().getAssets().open(path);
		} else if (AUTO_DIR.equals(baseDir)) {
			File file = new File(path);
			if (file.exists()) {
				rightInput = new FileInputStream(path);
			} else {
				rightInput = MyApplication.getInstance().getAssets().open(path);
			}
		} else {
			rightInput = new FileInputStream(path);
		}
		return rightInput;
	}

	public static SongData decodeSongDataToStepFormat(String noteData, String bgNoteData) {
		SongData songData = new SongData();
		MainNoteData mainNoteData = decodeSongDataToListStep(noteData);
		songData.steps = mainNoteData.steps;
		songData.numNotes = mainNoteData.numNotes;
		if (bgNoteData == null) return songData;
		ArrayList<GamePlayNote> listBgNote = decodeSongDataToBackgroundNote(bgNoteData);
		songData.backgroundNotes = listBgNote;

		int maxRenderNotes = 2;
		for (RubyStep rubyStep : songData.steps) {
			if (rubyStep.notes.size() <= maxRenderNotes) continue;
			ArrayList<GamePlayNote> twoHighestNotes = new ArrayList<>();
			for (int i = 0; i < maxRenderNotes; i++) {
				GamePlayNote highest = findNoteWithMaxId(rubyStep.notes);
				twoHighestNotes.add(highest);
				rubyStep.notes.remove(highest);
			}
			songData.backgroundNotes.addAll(rubyStep.notes);
			rubyStep.notes.clear();
			rubyStep.notes.addAll(twoHighestNotes);
		}

		Collections.sort(listBgNote, new Comparator<GamePlayNote>() {
			@Override
			public int compare(GamePlayNote m1, GamePlayNote m2) {
				if (m1.startTime < m2.startTime) {
					return -1;
				} else {
					return 1;
				}
			}
		});
		return songData;
	}

	private static GamePlayNote findNoteWithMaxId(ArrayList<GamePlayNote> notes) {
		GamePlayNote highest = notes.get(0);
		for (GamePlayNote note : notes) {
			if (note.id > highest.id) {
				highest = note;
			}
		}
		return highest;
	}

	private static MainNoteData decodeSongDataToListStep(String noteData) {
		ArrayList<RubyStep> result = new ArrayList<>();
		int numNotes = 0;
		try {
			JSONArray arr = new JSONArray(noteData);

			float elapsedTime = 0f;
			int stepIndex = 0;
			for (int i = 0; i < arr.length(); ++i) {
				JSONArray arrayNotes = arr.getJSONArray(i);

				String name = arrayNotes.getString(0).toLowerCase();
				float length = (float) (arrayNotes.getDouble(1));
				if (name.equalsIgnoreCase("rest")) {
					elapsedTime += length;
					continue;
				}

				boolean isTie = arrayNotes.getBoolean(3);
				boolean isChord = arrayNotes.getBoolean(2);
				if (isTie && isChord) continue;

				if (isTie && stepIndex > 0) {
					elapsedTime += length;
					continue;
				}

				int fingerIndex = 0;
				if (arrayNotes.length() == 5) {
					fingerIndex = arrayNotes.getInt(4);
				}

				numNotes++;
				GamePlayNote gamePlayNote = new GamePlayNote();
				gamePlayNote.name = name;
				gamePlayNote.startTime = elapsedTime;
				gamePlayNote.duration = length;
				gamePlayNote.fingerIndex = fingerIndex;
				gamePlayNote.id = NoteUtils.noteIdOf(name);
				if (gamePlayNote.id == 20) continue;

				try {
					gamePlayNote.velocity = (float) arrayNotes.getDouble(4);
				} catch (Exception ex) {
					gamePlayNote.velocity = Constant.DEFAULT_VOLUME_VALUE;
				}

				if (isChord && stepIndex > 0) {
					RubyStep rubyStep = result.get(stepIndex - 1);
					gamePlayNote.startTime = rubyStep.startTime;

					float lastDuration = rubyStep.duration;
					rubyStep.duration = Math.max(lastDuration, length);
					rubyStep.notes.add(gamePlayNote);
					elapsedTime += (-lastDuration + rubyStep.duration);
				} else {
					RubyStep rubyStep = new RubyStep();
					rubyStep.notes = new ArrayList<>();
					rubyStep.notes.add(gamePlayNote);
					rubyStep.startTime = elapsedTime;
					rubyStep.duration = length;
					rubyStep.index = stepIndex++;
					result.add(rubyStep);

					elapsedTime += length;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return new MainNoteData(result, numNotes);
	}

	private static ArrayList<GamePlayNote> decodeSongDataToBackgroundNote(String jsonData) {
		ArrayList<GamePlayNote> result = new ArrayList<>();
		try {
			JSONArray arr = new JSONArray(jsonData);
			float elapsedTime = 0f;
			for (int i = 0; i < arr.length(); i++) {
				JSONArray arrayNotes = arr.getJSONArray(i);
				String name = arrayNotes.getString(0).toLowerCase();
				float length = (float) (arrayNotes.getDouble(1));
				if (name.equalsIgnoreCase("rest")) {
					elapsedTime += length;
					continue;
				}

				boolean isTie = arrayNotes.getBoolean(3);
				boolean isChord = arrayNotes.getBoolean(2);
				if (isTie && isChord) continue;

				if (isTie && result.size() > 0 && result.get(result.size() - 1).name.equals(name)) {

					elapsedTime += length;
					continue;
				}

				GamePlayNote gamePlayNote = new GamePlayNote();
				gamePlayNote.name = name;
				gamePlayNote.startTime = elapsedTime;
				gamePlayNote.duration = length;
				gamePlayNote.fingerIndex = 0;
				gamePlayNote.id = NoteUtils.noteIdOf(name);
				if (gamePlayNote.id == 20) continue;

				try {
					gamePlayNote.velocity = (float) arrayNotes.getDouble(4);
				} catch (Exception ex) {
					gamePlayNote.velocity = Constant.DEFAULT_VOLUME_VALUE;
				}
				result.add(gamePlayNote);
				if (isChord) {
					GamePlayNote prevNote = result.get(result.size() - 2);
					float lastDuration = prevNote.duration;
					gamePlayNote.startTime = prevNote.startTime;
					float maxDuration = Math.max(lastDuration, length);
					elapsedTime += (-lastDuration + maxDuration);
				} else {
					elapsedTime += length;
				}

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Retention(RetentionPolicy.SOURCE)
	@StringDef({AUTO_DIR, ASSET_DIR})
	private @interface BaseDir {
	}

	public static class MainNoteData {
		public ArrayList<RubyStep> steps;
		public int numNotes;

		public MainNoteData(ArrayList<RubyStep> steps, int numNotes) {
			this.steps = steps;
			this.numNotes = numNotes;
		}
	}
}
