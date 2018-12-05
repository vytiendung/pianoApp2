package com.vtd.sheetmusic.midi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

public class MidiFactory {
	// Note lengths
	// We are working with 32 ticks to the crotchet. So
	// all the other note lengths can be derived from this
	// basic figure. Note that the longest note we can
	// represent with this code is one tick short of a
	// two semibreves (i.e., 8 crotchets)

	static final int SEMIQUAVER = 4;
	static final int QUAVER = 8;
	static final int CROTCHET = 16;
	static final int MINIM = 32;
	static final int SEMIBREVE = 64;

	// Standard MIDI file header, for one-track file
	// 4D, 54... are just magic numbers to identify the
	// headers
	// Note that because we're only writing one track, we
	// can for simplicity combine the file and track headers
	static final int header[] = new int[] { 0x4d, 0x54, 0x68, 0x64, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, // single-track
																										// format
			0x00, 0x01, // one track
			0x00, 0x10, // 16 ticks per quarter
			0x4d, 0x54, 0x72, 0x6B };

	// Standard footer
	static final int footer[] = new int[] { 0x01, 0xFF, 0x2F, 0x00 };

	// A MIDI event to set the tempo
	static final int tempoEvent[] = new int[] { 0x00, 0xFF, 0x51, 0x03, 0x0F, 0x42, 0x40 // Default
																							// 1
																							// million
																							// usec
																							// per
																							// crotchet
	};

	// A MIDI event to set the key signature. This is irrelent to
	// playback, but necessary for editing applications
	static final int keySigEvent[] = new int[] { 0x00, 0xFF, 0x59, 0x02, 0x00, // C
			0x00 // major
	};

	// A MIDI event to set the time signature. This is irrelent to
	// playback, but necessary for editing applications
	static final int timeSigEvent[] = new int[] { 0x00, 0xFF, 0x58, 0x04, 0x04, // numerator
			0x02, // denominator (2==4, because it's a power of 2)
			0x30, // ticks per click (not used)
			0x08 // 32nd notes per crotchet
	};

	// The collection of events to play, in time order

	/** Write the stored MIDI events to a file */
	public static void writeToFile(Vector<int[]>playEvents, String dir, String fileName) throws IOException {
		File f=new File(dir, fileName);
		FileOutputStream fos = new FileOutputStream(f);

		fos.write(intArrayToByteArray(header));

		// Calculate the amount of track data
		// _Do_ include the footer but _do not_ include the
		// track header

		int size = tempoEvent.length + keySigEvent.length + timeSigEvent.length + footer.length;

		for (int i = 0; i < playEvents.size(); i++)
			size += playEvents.elementAt(i).length;

		// Write out the track data size in big-endian format
		// Note that this math is only valid for up to 64k of data
		// (but that's a lot of notes)
		int high = size / 256;
		int low = size - (high * 256);
		fos.write((byte) 0);
		fos.write((byte) 0);
		fos.write((byte) high);
		fos.write((byte) low);

		// Write the standard metadata — tempo, etc
		// At present, tempo is stuck at crotchet=60
		fos.write(intArrayToByteArray(tempoEvent));
		fos.write(intArrayToByteArray(keySigEvent));
		fos.write(intArrayToByteArray(timeSigEvent));

		// Write out the note, etc., events
		for (int i = 0; i < playEvents.size(); i++) {
			fos.write(intArrayToByteArray(playEvents.elementAt(i)));
		}

		// Write the footer and close
		fos.write(intArrayToByteArray(footer));
		fos.close();
	}

	/**
	 * Convert an array of integers which are assumed to contain unsigned bytes
	 * into an array of bytes
	 */
	protected static byte[] intArrayToByteArray(int[] ints) {
		int l = ints.length;
		byte[] out = new byte[ints.length];
		for (int i = 0; i < l; i++) {
			out[i] = (byte) ints[i];
		}
		return out;
	}

	/** Store a note-on event */
	public static int[] noteOn(int delta, int note, int velocity) {
		int[] data = new int[4];
		data[0] = delta;
		data[1] = 0x90;
		data[2] = note;
		data[3] = velocity;
		return data;
//		playEvents.add(data);
	}
	
	public static int[] note10On(int delta, int note, int velocity) {
		int[] data = new int[4];
		data[0] = delta;
		data[1] = 0x99;
		data[2] = note;
		data[3] = velocity;
		return data;
//		playEvents.add(data);
	}

	/** Store a note-off event */
	public static int[] noteOff(int delta, int note) {
		int[] data = new int[4];
		data[0] = delta;
		data[1] = 0x80;
		data[2] = note;
		data[3] = 0;
		return data;
//		playEvents.add(data);
	}
	
	public static int[] note10Off(int delta, int note) {
		int[] data = new int[4];
		data[0] = delta;
		data[1] = 0x89;
		data[2] = note;
		data[3] = 0;
		return data;
//		playEvents.add(data);
	}

	/** Store a program-change event at current position */
	public static int[] progChange(int prog) {
		int[] data = new int[3];
		data[0] = 0;
		data[1] = 0xC0;
		data[2] = prog;
		return data;
//		playEvents.add(data);
	}
	
	public static int[] prog10Change(int prog) {
		int[] data = new int[3];
		data[0] = 0;
		data[1] = 0xC9;
		data[2] = prog;
		return data;
//		playEvents.add(data);
	}

	/**
	 * Store a note-on event followed by a note-off event a note length later.
	 * There is no delta value — the note is assumed to follow the previous one
	 * with no gap.
	 */
	public static Vector<int[]> noteOnOffNow(int duration, int note, int velocity) {
		Vector<int[]> v=new Vector<int[]>();
		v.add(noteOn(0, note, velocity));
		v.add(noteOff(duration, note));
		return v;
		
	}
	
	public static Vector<int[]> noteOnOff10Now(int duration, int note, int velocity) {
		Vector<int[]> v=new Vector<int[]>();
		v.add(note10On(0, note, velocity));
		v.add(note10Off(duration, note));
		return v;
	}

	public static Vector<int[]> noteSequenceFixedVelocity(int[] sequence, int velocity) {
		Vector<int[]> v=new Vector<int[]>();
		boolean lastWasRest = false;
		int restDelta = 0;
		for (int i = 0; i < sequence.length; i += 2) {
			int note = sequence[i];
			int duration = sequence[i + 1];
			if (note < 0) {
				// This is a rest
				restDelta += duration;
				lastWasRest = true;
			} else {
				// A note, not a rest
				if (lastWasRest) {
					v.add(noteOn(restDelta, note, velocity));
					v.add(noteOff(duration, note));
				} else {
					v.add(noteOn(0, note, velocity));
					v.add(noteOff(duration, note));
				}
				restDelta = 0;
				lastWasRest = false;
			}
		}
		return v;
	}
	
	
	public static int getFileIndex(int noteNumber){
		int n=noteNumber/10;
		int r=noteNumber%10;
		int index;
		if (r == 0||r==5)
			index = noteNumber;
		else {
			if (r < 5)
				index = n * 10==20?21:n*10;
			else
				index = n * 10 + 5;
		}
//		Log.d("MidiFactory", noteNumber+"---->"+index);
		return index;
	}
	
	public static boolean createPercussionFile(int noteNumber, String dir, int voulume){
		try{
			String fileName="128_"+noteNumber+".mid";
			
			Vector<int[]> v=new Vector<int[]>();
//				mf.prog10Change(1); // default drumkit
//			if(noteNumber==42||noteNumber==37||noteNumber==54||noteNumber==51)
//				v.addAll(noteOnOff10Now(MINIM, noteNumber, 50));
//			else
//				v.addAll(noteOnOff10Now(MINIM, noteNumber, 127));
			
			v.addAll(noteOnOff10Now(MINIM, noteNumber, voulume));
			writeToFile(v,dir,fileName);
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	
	public static boolean createMidiFile(int noteNumber, int instrument,String dir, int volume){
		try{
			int noteN=getFileIndex(noteNumber);
			String fileName=instrument+"_"+noteN+".mid";
			File f=new File(dir, fileName);
			if(f.exists())return true;
			
			Vector<int[]> v=new Vector<int[]>();
			v.add(progChange(instrument));
//			if(noteNumber>=41&& noteNumber<=56)
//				v.addAll(noteOnOffNow(SEMIBREVE, noteN, 50));
//			else
//				v.addAll(noteOnOffNow(SEMIBREVE, noteN, 127));
			v.addAll(noteOnOffNow(SEMIBREVE, noteN, volume));
			writeToFile(v,dir,fileName);
			
//			MidiFactory mf = new MidiFactory();
//
//			// Test 1 — play a C major chord
//
//			// Turn on all three notes at start-of-track (delta=0)
//			mf.noteOn(0, 60, 127);
//			mf.noteOn(0, 64, 127);
//			mf.noteOn(0, 67, 127);
//
//			// Turn off all three notes after one minim.
//			// NOTE delta value is cumulative — only _one_ of
//			// these note-offs has a non-zero delta. The second and
//			// third events are relative to the first
//			mf.noteOff(MINIM, 60);
//			mf.noteOff(0, 64);
//			mf.noteOff(0, 67);
//
//			// Test 2 — play a scale using noteOnOffNow
//			// We don't need any delta values here, so long as one
//			// note comes straight after the previous one
//
//			mf.noteOnOffNow(QUAVER, 60, 127);
//			mf.noteOnOffNow(QUAVER, 62, 127);
//			mf.noteOnOffNow(QUAVER, 64, 127);
//			mf.noteOnOffNow(QUAVER, 65, 127);
//			mf.noteOnOffNow(QUAVER, 67, 127);
//			mf.noteOnOffNow(QUAVER, 69, 127);
//			mf.noteOnOffNow(QUAVER, 71, 127);
//			mf.noteOnOffNow(QUAVER, 72, 127);
//
//			// Test 3 — play a short tune using noteSequenceFixedVelocity
//			// Note the rest inserted with a note value of -1
//
//			int[] sequence = new int[] { 60, QUAVER + SEMIQUAVER, 65, SEMIQUAVER, 70, CROTCHET + QUAVER, 69, QUAVER, 65, QUAVER / 3, 62, QUAVER / 3, 67,
//					QUAVER / 3, 72, MINIM + QUAVER, -1, SEMIQUAVER, 72, SEMIQUAVER, 76, MINIM, };
//
//			// What the heck — use a different instrument for a change
//			mf.progChange(10);
//
//			mf.noteSequenceFixedVelocity(sequence, 127);
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static int[] controlChange(int c , int v) {
		int[] data = new int[4];
		data[0] = 0;
		data[1] = 0xB0;
		data[2] = c;
		data[3] = v;
		return data;
	}
	
	
	public static final byte CONTROL_MOD_WHELL = 1;
	public static final byte CONTROL_VOLUME = 7;
	public static final byte CONTROL_PAN = 10;
	public static final byte CONTROL_SUSTAIN = 64;
	public static final byte CONTROL_REVERB = 91;
	public static final byte CONTROL_TREMOLO = 92;
	public static final byte CONTROL_CHORUS = 93;
	public static final byte CONTROL_DETUNE = 94;
	public static final byte CONTROL_PHASER = 95;
	
	
	public static boolean createMidiFileWithSettings(int noteNumber, int instrument,String dir, int volume, 
			int control_pan , int control_reverb,int control_chorus , int control_sustain , int control_mod_whell ,
			int control_detune , int control_phaser , int control_tremolo){
		try{
//			int noteN=getFileIndex(noteNumber);
			int noteN = noteNumber;
			String fileName=instrument+"_"+noteN+".mid";
			File f=new File(dir, fileName);
			
			Vector<int[]> v=new Vector<int[]>();
			v.add(progChange(instrument));
			v.add(controlChange(CONTROL_PAN, control_pan));
			v.add(controlChange(CONTROL_REVERB, control_reverb));
			v.add(controlChange(CONTROL_CHORUS, control_chorus));
			v.add(controlChange(CONTROL_SUSTAIN, control_sustain));
			v.add(controlChange(CONTROL_MOD_WHELL, control_mod_whell));
			v.add(controlChange(CONTROL_DETUNE, control_detune));
			v.add(controlChange(CONTROL_PHASER, control_phaser));
			v.add(controlChange(CONTROL_TREMOLO, control_tremolo));
			//
			//end
//			if(noteNumber>=41&& noteNumber<=56)
//				v.addAll(noteOnOffNow(SEMIBREVE, noteN, 50));
//			else
//				v.addAll(noteOnOffNow(SEMIBREVE, noteN, 127));
			v.addAll(noteOnOffNow(127, noteN, volume));
			writeToFile(v,dir,fileName);
			
//			MidiFactory mf = new MidiFactory();
//
//			// Test 1 — play a C major chord
//
//			// Turn on all three notes at start-of-track (delta=0)
//			mf.noteOn(0, 60, 127);
//			mf.noteOn(0, 64, 127);
//			mf.noteOn(0, 67, 127);
//
//			// Turn off all three notes after one minim.
//			// NOTE delta value is cumulative — only _one_ of
//			// these note-offs has a non-zero delta. The second and
//			// third events are relative to the first
//			mf.noteOff(MINIM, 60);
//			mf.noteOff(0, 64);
//			mf.noteOff(0, 67);
//
//			// Test 2 — play a scale using noteOnOffNow
//			// We don't need any delta values here, so long as one
//			// note comes straight after the previous one
//
//			mf.noteOnOffNow(QUAVER, 60, 127);
//			mf.noteOnOffNow(QUAVER, 62, 127);
//			mf.noteOnOffNow(QUAVER, 64, 127);
//			mf.noteOnOffNow(QUAVER, 65, 127);
//			mf.noteOnOffNow(QUAVER, 67, 127);
//			mf.noteOnOffNow(QUAVER, 69, 127);
//			mf.noteOnOffNow(QUAVER, 71, 127);
//			mf.noteOnOffNow(QUAVER, 72, 127);
//
//			// Test 3 — play a short tune using noteSequenceFixedVelocity
//			// Note the rest inserted with a note value of -1
//
//			int[] sequence = new int[] { 60, QUAVER + SEMIQUAVER, 65, SEMIQUAVER, 70, CROTCHET + QUAVER, 69, QUAVER, 65, QUAVER / 3, 62, QUAVER / 3, 67,
//					QUAVER / 3, 72, MINIM + QUAVER, -1, SEMIQUAVER, 72, SEMIQUAVER, 76, MINIM, };
//
//			// What the heck — use a different instrument for a change
//			mf.progChange(10);
//
//			mf.noteSequenceFixedVelocity(sequence, 127);
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return false;
	}

}