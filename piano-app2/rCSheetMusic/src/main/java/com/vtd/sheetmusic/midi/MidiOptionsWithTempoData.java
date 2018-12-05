package com.vtd.sheetmusic.midi;

import java.util.ArrayList;

/**
 * Created by Dinh on 10/5/2016.
 */
public class MidiOptionsWithTempoData extends MidiOptions {
	public ArrayList<MidiEvent> tempoEvents;
	public MidiOptionsWithTempoData(MidiFileWithTempoData midiFile) {
		super(midiFile);
		tempoEvents = midiFile.allTempoEvents;
	}
}
