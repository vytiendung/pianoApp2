package com.vtd.sheetmusic.midi;

import java.util.ArrayList;

/**
 * Created by Dinh on 9/5/2016.
 */
public class MidiFileWithTempoData extends MidiFile{
	public ArrayList<MidiEvent> allTempoEvents;
	public MidiFileWithTempoData(byte[] rawData, String filename) {
		super(rawData, filename);
	}

	@Override
	protected void parse(byte[] rawData) {
		allTempoEvents = new ArrayList<MidiEvent>();
		String id;
		int len;

		tracks = new ArrayList<MidiTrack>();
		trackPerChannel = false;

		MidiFileReader file = new MidiFileReader(rawData);
		id = file.ReadAscii(4);
		if (!id.equals("MThd")) {
			throw new MidiFileException("Doesn't start with MThd", 0);
		}
		len = file.ReadInt();
		if (len !=  6) {
			throw new MidiFileException("Bad MThd header", 4);
		}
		trackmode = (short) file.ReadShort();
		int num_tracks = file.ReadShort();
		quarternote = file.ReadShort();

		allevents = new ArrayList<ArrayList<MidiEvent>>();
		for (int trackNum = 0; trackNum < num_tracks; trackNum++) {
			allevents.add(ReadTrack(file));
			MidiTrack track = new MidiTrack(allevents.get(trackNum), trackNum);
			if (track.getNotes().size() > 0) {
				tracks.add(track);
			}
		}

        /* Get the length of the song in pulses */
		for (MidiTrack track : tracks) {
			MidiNote last = track.getNotes().get( track.getNotes().size()-1 );
			if (this.totalpulses < last.getStartTime() + last.getDuration()) {
				this.totalpulses = last.getStartTime() + last.getDuration();
			}
		}

        /* If we only have one track with multiple channels, then treat
         * each channel as a separate track.
         */
		if (tracks.size() == 1 && HasMultipleChannels(tracks.get(0))) {
			tracks = SplitChannels(tracks.get(0), allevents.get(tracks.get(0).trackNumber() ));
			trackPerChannel = true;
		}

		CheckStartTimes(tracks);

        /* Determine the time signature */
		int tempoCount = 0;
		long tempo = 0;
		int numer = 0;
		int denom = 0;
		for (ArrayList<MidiEvent> list : allevents) {
			for (MidiEvent mEvent : list) {
				if (mEvent.Metaevent == MetaEventTempo) {
					// Take average of all tempos
					tempo += mEvent.Tempo;
					tempoCount++;
					allTempoEvents.add(mEvent);
				}
				if (mEvent.Metaevent == MetaEventTimeSignature && numer == 0) {
					numer = mEvent.Numerator;
					denom = mEvent.Denominator;
				}
			}
		}
		if (tempo == 0) {
			tempo = 500000; /* 500,000 microseconds = 0.05 sec */
			MidiEvent tempoEvent = new MidiEvent();
			tempoEvent.Metaevent = MetaEventTempo;
			tempoEvent.Tempo = (int)tempo;
			tempoEvent.DeltaTime = 0;
			tempoEvent.StartTime = 0;
			allTempoEvents.add(tempoEvent);
		}
		else {
			tempo = tempo / tempoCount;
		}
		if (numer == 0) {
			numer = 4; denom = 4;
		}
		timesig = new TimeSignature(numer, denom, quarternote, (int)tempo);
	}
}
