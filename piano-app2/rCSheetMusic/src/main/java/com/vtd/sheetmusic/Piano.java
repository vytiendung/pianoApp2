/*
 * Copyright (c) 2009-2011 Madhav Vaidyanathan
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 */

package com.vtd.sheetmusic;

import java.util.ArrayList;

import android.util.Log;

import com.vtd.sheetmusic.midi.MidiFile;
import com.vtd.sheetmusic.midi.MidiNote;
import com.vtd.sheetmusic.midi.MidiOptions;
import com.vtd.sheetmusic.midi.MidiTrack;

/**
 * @class Piano
 * 
 *        The Piano Control is the panel at the top that displays the piano, and
 *        highlights the piano notes during playback. The main methods are:
 * 
 *        SetMidiFile() - Set the Midi file to use for shading. The Midi file is
 *        needed to determine which notes to shade.
 * 
 *        ShadeNotes() - Shade notes on the piano that occur at a given pulse
 *        time.
 * 
 */
public class Piano {
	static final String TAG = Piano.class.getSimpleName();

	public interface PianoNotesListener {
		public void onChangeNotes(ArrayList<MidiNote> primaryNoteList, ArrayList<MidiNote> secondaryNoteList, final long nextPulse,
								  final long delayInMilis, SheetMusic sheet);
		public void onEndOfSong();
	}

	private ArrayList<MidiNote> notes;
	/** The Midi notes for shading */
	private int maxShadeDuration;
	/** The maximum duration we'll shade a note for */
	private boolean useTwoColors;
	private PianoNotesListener listener;
	private long nextPulse;
	private MidiPlayer player;
	private int playTrack;
	private int noteIndex;
	private long maxPulse;

	/** Create a new Piano. */
	public Piano(PianoNotesListener listener, int playTrack) {
		this.listener = listener;
		this.playTrack = playTrack;
	}

	/**
	 * Set the MidiFile to use. Save the list of midi notes. Each midi note
	 * includes the note Number and StartTime (in pulses), so we know which
	 * notes to shade given the current pulse time.
	 */
	public void SetMidiFile(MidiFile midifile, MidiOptions options, MidiPlayer player) {
		if (midifile == null) {
			notes = null;
			useTwoColors = false;
			maxPulse=nextPulse = 0;
			return;
		}
		this.player = player;
		ArrayList<MidiTrack> tracks = midifile.ChangeMidiNotes(options);
		MidiTrack track = MidiFile.CombineToSingleTrack(tracks);
		notes = track.getNotes();

		maxShadeDuration = midifile.getTime().getQuarter() * 2;
		maxPulse=midifile.getTotalPulses()+1;
		/*
		 * We want to know which track the note came from. Use the 'channel'
		 * field to store the track.
		 */
		for (int tracknum = 0; tracknum < tracks.size(); tracknum++) {
			MidiTrack midiTrack = tracks.get(tracknum);
			for (MidiNote note : midiTrack.getNotes()) {
				note.setChannel(tracknum);
				note.setInstrument(midiTrack.getInstrument());
			}
		}

		/*
		 * When we have exactly two tracks, we assume this is a piano song, and
		 * we use different colors for highlighting the left hand and right hand
		 * notes.
		 */
		useTwoColors = false;
		if (tracks.size() == 2) {
			useTwoColors = true;
		}

	}

	/**
	 * Find the MidiNote with the startTime closest to the given time. Return
	 * the index of the note. Use a binary search method.
	 */
	private int FindClosestStartTime(int pulseTime) {
		int left = 0;
		int right = notes.size() - 1;

		while (right - left > 1) {
			int i = (right + left) / 2;
			if (notes.get(left).getStartTime() == pulseTime)
				break;
			else if (notes.get(i).getStartTime() <= pulseTime)
				left = i;
			else
				right = i;
		}
		while (left >= 1 && (notes.get(left - 1).getStartTime() == notes.get(left).getStartTime())) {
			left--;
		}
		return left;
	}

	/**
	 * Return the next StartTime that occurs after the MidiNote at offset i,
	 * that is also in the same track/channel.
	 */
	private int NextStartTimeSameTrack(int i) {
		int start = notes.get(i).getStartTime();
		int end = notes.get(i).getEndTime();
		int track = notes.get(i).getChannel();

		while (i < notes.size()) {
			if (notes.get(i).getChannel() != track) {
				i++;
				continue;
			}
			if (notes.get(i).getStartTime() > start) {
				return notes.get(i).getStartTime();
			}
			end = Math.max(end, notes.get(i).getEndTime());
			i++;
		}
		return end;
	}

	/**
	 * Return the next StartTime that occurs after the MidiNote at offset i. If
	 * all the subsequent notes have the same StartTime, then return the largest
	 * EndTime.
	 */
	private int NextStartTime(int i) {
		int start = notes.get(i).getStartTime();
		int end = notes.get(i).getEndTime();

		while (i < notes.size()) {
			if (notes.get(i).getStartTime() > start) {
				return notes.get(i).getStartTime();
			}
			end = Math.max(end, notes.get(i).getEndTime());
			i++;
		}
		return end;
	}

	/**
	 * Find the Midi notes that occur in the current time. Shade those notes on
	 * the piano displayed. Un-shade the those notes played in the previous
	 * time.
	 */
	public boolean ShadeNotes(int currentPulseTime, int prevPulseTime) {
		ArrayList<MidiNote> primaryNotes = new ArrayList<MidiNote>();
		ArrayList<MidiNote> secondaryNotes = new ArrayList<MidiNote>();

		if (notes == null || notes.size() == 0) {
			return false;
		}

		/*
		 * Loop through the Midi notes. Unshade notes where StartTime <=
		 * prevPulseTime < next StartTime Shade notes where StartTime <=
		 * currentPulseTime < next StartTime
		 */
		int lastShadedIndex = FindClosestStartTime(prevPulseTime - maxShadeDuration * 2);
		// Log.d(TAG, " Last shade index="+lastShadedIndex+
		// "  m="+(prevPulseTime - maxShadeDuration *
		// 2)+"  cur="+currentPulseTime+"  pre="+prevPulseTime);
		double pulsesPerMsec = player.getPulsesPerMsec();
		MidiNote note;
		int end=0,start=0;
		nextPulse=maxPulse;
		for (int i = lastShadedIndex; i < notes.size(); i++) {
			note = notes.get(i);
			start = note.getStartTime();
			end = note.getEndTime();
			// int notenumber = note.getNumber();
			int nextStart = NextStartTime(i);
			int nextStartTrack = NextStartTimeSameTrack(i);
			end = Math.max(end, nextStartTrack);
			end = Math.min(end, start + maxShadeDuration - 1);

			// Log.d(TAG," start="+start);
			/* If we've past the previous and current times, we're done. */
			if ((start > prevPulseTime) && (start > currentPulseTime)) {
				nextPulse = start;
				noteIndex = i;
				 //Log.d(TAG, " Case 1  start="+start);
				break;
			}

			/* If shaded notes are the same, we're done */
			if ((start <= currentPulseTime) && (currentPulseTime < nextStart) && (currentPulseTime < end) && (start <= prevPulseTime)
					&& (prevPulseTime < nextStart) && (prevPulseTime < end)) {
				nextPulse = end;
				noteIndex = i;
				 //Log.d(TAG, " Case 2 ");
				break;
			}

			/* If the note is in the current time, shade it */
			if ((start <= currentPulseTime) && (currentPulseTime < end) && (prevPulseTime < start)) {
				// Log.d(TAG,
				// " number="+note.getNumber()+" start="+start+"   currentPulse="+currentPulseTime+"   previousPulse="+prevPulseTime+"   end="+end
				// +"  ppm="+player.getPulsesPerMsec());
				note.setDurationInMilis((int) (note.getDuration() / pulsesPerMsec));
				if (notes.get(i).getChannel() != this.playTrack) {
					// ShadeOneNote(bufferCanvas, notenumber, shade2);
					// Log.d(TAG, " add secd note = "+note.getNumber());
					secondaryNotes.add(note);
				} else {
					// ShadeOneNote(bufferCanvas, notenumber, shade1);
					// Log.d(TAG, " add pri note = "+note.getNumber());
					primaryNotes.add(note);
				}
			}

		}
		if (listener != null) {
			//Log.d(TAG, "Next pulse="+nextPulse);
				long delayInMilis = (long) ((nextPulse - currentPulseTime) / pulsesPerMsec);
//				 Log.d(TAG,
//				 " next pulse = "+nextPulse+"   delayInMilis="+delayInMilis);
				if(delayInMilis>0)
					listener.onChangeNotes(primaryNotes, secondaryNotes, nextPulse, delayInMilis,player.getSheet());
				if (primaryNotes.size() > 0)
					return true;
		}

		return false;
	}

	public int getPlayTrack() {
		return playTrack;
	}

	public void setPlayTrack(int playTrack) {
		this.playTrack = playTrack;
	}

	public void shadeFirstNotes(long atPulse) {
		Log.d(TAG, "Init shade note at pulse=" + atPulse);
		if (atPulse > 0) {
			player.playstate = MidiPlayer.seeking;
			player.ForwardNextPulse(atPulse, 1);
		} else {
			if (notes != null && notes.size() != 0) {
				this.noteIndex = 0;
				player.playstate = MidiPlayer.seeking;
				player.ForwardNextPulse(notes.get(0).getStartTime(), 1);
			}
		}
	}

	public PianoNotesListener getListener() {
		return listener;
	}

	public void setListener(PianoNotesListener listener) {
		this.listener = listener;
	}
	
	

//	public void shadeNextNotes() {
//		
//		
//		int index = this.noteIndex;
//		if (notes == null || notes.size() == 0 || index > notes.size() - 1)
//			return;
//
//		int currentPulseTime = notes.get(index).getStartTime();
//		int prevPulseTime = notes.get(index - 1).getStartTime();
//
//		SheetMusic sheet = player.getSheet();
//		sheet.ShadeNotes(currentPulseTime, prevPulseTime, true);
//
//		MidiNote note;
//		ArrayList<MidiNote> primaryNotes = new ArrayList<MidiNote>();
//		ArrayList<MidiNote> secondaryNotes = new ArrayList<MidiNote>();
//		double pulsesPerMsec = player.getPulsesPerMsec();
//		for (int i = index; i < notes.size(); i++) {
//			note = notes.get(i);
//			int start = note.getStartTime();
//			Log.d(TAG, " note number = "+note.getNumber());
//			if (start != currentPulseTime) {
//				nextPulse = start;
//				this.noteIndex = i;
//				break;
//			}
//
//			note.setDurationInMilis((int) (note.getDuration() / pulsesPerMsec));
//			if (notes.get(i).getChannel() != this.playTrack) {
//				secondaryNotes.add(note);
//			} else {
//				primaryNotes.add(note);
//			}
//
//		}
//
//		if (listener != null) {
//			if (primaryNotes.size() > 0 || secondaryNotes.size() > 0) {
//				long delayInMilis = (long) ((nextPulse - currentPulseTime) / pulsesPerMsec);
//				Log.d(TAG, " next pulse = " + nextPulse + "   delayInMilis=" + delayInMilis+"  index="+this.noteIndex);
//				listener.onChangeNotes(primaryNotes, secondaryNotes, nextPulse, delayInMilis);
//				if (this.noteIndex == notes.size() - 1) {
//					// end of song
//					Log.d(TAG, "===================================End of song "+notes.size());
//					player.setEndOfSong(true);
//				}
//			}
//		}
//	}

}
