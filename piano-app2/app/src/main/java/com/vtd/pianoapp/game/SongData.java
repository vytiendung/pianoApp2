package com.vtd.pianoapp.game;

import com.vtd.pianoapp.songobject.GamePlayNote;
import com.vtd.pianoapp.songobject.RubyStep;

import java.util.ArrayList;

public class SongData {
	public ArrayList<RubyStep> steps;
	public ArrayList<GamePlayNote> backgroundNotes = new ArrayList<>();
	public int numNotes = 0;
	public boolean isMidi = false;
}
