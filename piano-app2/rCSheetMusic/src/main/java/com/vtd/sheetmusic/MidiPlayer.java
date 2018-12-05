/*
 * Copyright (c) 2007-2012 Madhav Vaidyanathan
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.vtd.sheetmusic.midi.MidiFile;
import com.vtd.sheetmusic.midi.MidiFileException;
import com.vtd.sheetmusic.midi.MidiOptions;


/** @class MidiPlayer
 *
 * The MidiPlayer is the panel at the top used to play the sound
 * of the midi file.  It consists of:
 *
 * - The Rewind button
 * - The Play/Pause button
 * - The Stop button
 * - The Fast Forward button
 * - The Playback speed bar
 *
 * The sound of the midi file depends on
 * - The MidiOptions (taken from the menus)
 *   Which tracks are selected
 *   How much to transpose the keys by
 *   What instruments to use per track
 * - The tempo (from the Speed bar)
 * - The volume
 *
 * The MidiFile.ChangeSound() method is used to create a new midi file
 * with these options.  The mciSendString() function is used for 
 * playing, pausing, and stopping the sound.
 *
 * For shading the notes during playback, the method
 * SheetMusic.ShadeNotes() is used.  It takes the current 'pulse time',
 * and determines which notes to shade.
 */
public class MidiPlayer  {
	final static String TAG= MidiPlayer.class.getSimpleName();
    int playstate;               /** The playing state of the Midi Player */
    public static final int stopped   = 1;     /** Currently stopped */
    public static final int playing   = 2;     /** Currently playing music */
    public static final int paused    = 3;     /** Currently paused */
    public static final int initStop  = 4;     /** Transitioning from playing to stop */
    public static final int initPause = 5;     /** Transitioning from playing to pause */
    public static final int seeking	  = 6;

    final String tempSoundFile = "playing.mid"; /** The filename to play sound from */

    MediaPlayer player;         /** For playing the audio */
    static MidiFile midifile;          /** The midi file to play */
    MidiOptions options;        /** The sound options for playing the midi file */
    double pulsesPerMsec;       /** The number of pulses per millisec */
    SheetMusic sheet;           /** The sheet music to shade while playing */
    Piano piano;                /** The piano to shade while playing */
    Handler timer;              /** Timer used to update the sheet music while playing */
    long startTime;             /** Absolute time when music started playing (msec) */
    double startPulseTime;      /** Time (in pulses) when music started playing */
    double currentPulseTime;    /** Time (in pulses) music is currently at */
    double prevPulseTime;       /** Time (in pulses) music was last at */
    Context context;            /** The context, for writing midi files */

    long speedPercent;
    boolean isEndOfSong;
    boolean onlyPlayBgTrack;
    
    /** Create a new MidiPlayer, displaying the play/stop buttons, and the
     *  speed bar.  The midifile and sheetmusic are initially null.
     */
    public MidiPlayer(Context context,long speedPercent) {
        this.context = context;
        this.midifile = null;
        this.options = null;
        this.sheet = null;
        playstate = stopped;
        startTime = SystemClock.uptimeMillis();
        startPulseTime = 0;
        currentPulseTime = 0;
        prevPulseTime = -10;
        player = new MediaPlayer();
        this.speedPercent=speedPercent+40;
        /* Initialize the timer used for playback, but don't start
         * the timer yet (enabled = false).
         */
        timer = new Handler();
        
    }


    
    public void SetPiano(Piano p) {
        piano = p;
    }

    /** The MidiFile and/or SheetMusic has changed. Stop any playback sound,
     *  and store the current midifile and sheet music.
     */
    public void SetMidiFile(MidiFile file, MidiOptions opt, SheetMusic s) {
    	

        /* If we're paused, and using the same midi file, redraw the
         * highlighted notes.
         */
        if ((file == midifile && midifile != null && playstate == paused)) {
            options = opt;
            sheet = s;
            sheet.ShadeNotes((int)currentPulseTime, (int)-1, false);

            /* We have to wait some time (200 msec) for the sheet music
             * to scroll and redraw, before we can re-shade.
             */
            timer.removeCallbacks(TimerCallback);
            timer.postDelayed(ReShade, 500);
        }
        else {
            Stop();
            midifile = file;
            options = opt;
            sheet = s;
        }
        
        //TODO: Giang
        double inverse_tempo = 1.0 / midifile.getTime().getTempo();
        double inverse_tempo_scaled = inverse_tempo * speedPercent / 100.0;
        options.tempo = (int)(1.0 / inverse_tempo_scaled);
        pulsesPerMsec = midifile.getTime().getQuarter() * (1000.0 / options.tempo);
        
    }

    /** If we're paused, reshade the sheet music and piano. */
    Runnable ReShade = new Runnable() {
      public void run() {
        if (playstate == paused || playstate == stopped) {
            sheet.ShadeNotes((int)currentPulseTime, (int)-10, false);
            piano.ShadeNotes((int)currentPulseTime, (int)prevPulseTime);
        }
      }
    };


    /** Return the number of tracks selected in the MidiOptions.
     *  If the number of tracks is 0, there is no sound to play.
     */
    private int numberTracks() {
        int count = 0;
        for (int i = 0; i < options.tracks.length; i++) {
            if (options.tracks[i] && !options.mute[i]) {
                count += 1;
            }
        }
        return count;
    }

    /** Create a new midi file with all the MidiOptions incorporated.
     *  Save the new file to playing.mid, and store
     *  this temporary filename in tempSoundFile.
     */ 
    private void CreateMidiFile() {
        double inverse_tempo = 1.0 / midifile.getTime().getTempo();
        double inverse_tempo_scaled = inverse_tempo * speedPercent / 100.0;
        // double inverse_tempo_scaled = inverse_tempo * 100.0 / 100.0;
        options.tempo = (int)(1.0 / inverse_tempo_scaled);
        pulsesPerMsec = midifile.getTime().getQuarter() * (1000.0 / options.tempo);
        Log.d(TAG, "===================== Tempo="+options.tempo+"  ===== quater="+midifile.getTime().getQuarter());
        try {
//        	String path=Environment.getExternalStorageDirectory()+"/PianistHD/tmp/"+tempSoundFile;
//        	File f=new File(path);
            FileOutputStream dest = context.openFileOutput(tempSoundFile, Context.MODE_PRIVATE);
//        	FileOutputStream dest=new FileOutputStream(f);
            midifile.ChangeSound(dest, options);
            dest.close();
            // checkFile(tempSoundFile);
        }
        catch (IOException e) {
        	e.printStackTrace();
            Toast toast = Toast.makeText(context, "Error: Unable to create MIDI file for playing.", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void checkFile(String name) {
        try {
            FileInputStream in = context.openFileInput(name);
            byte[] data = new byte[4096];
            int total = 0, len = 0;
            while (true) {
                len = in.read(data, 0, 4096);
                if (len > 0)
                    total += len;
                else
                    break;
            } 
            in.close();
            data = new byte[total];
            in = context.openFileInput(name);
            int offset = 0;
            while (offset < total) {
                len = in.read(data, offset, total - offset);
                if (len > 0)
                    offset += len;
            }
            in.close();
            MidiFile testmidi = new MidiFile(data, name); 
        }
        catch (IOException e) {
            Toast toast = Toast.makeText(context, "CheckFile: " + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }
        catch (MidiFileException e) {
            Toast toast = Toast.makeText(context, "CheckFile midi: " + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        } 
    }


    /** Play the sound for the given MIDI file */
    private void PlaySound(String filename) {
        if (player == null)
            return;
        try {
            FileInputStream input = context.openFileInput(filename);
            player.reset();
            player.setDataSource(input.getFD());
            input.close();
            player.prepare();
            player.start();
        }
        catch (IOException e) {
            Toast toast = Toast.makeText(context, "Error: Unable to play MIDI sound", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /** Stop playing the MIDI music */
    private void StopSound() {
        if (player == null)
            return;
        player.stop();
        player.reset();
    }


    /** The callback for the play button.
     *  If we're stopped or pause, then play the midi file.
     */
    public void Play() {
        if (midifile == null || sheet == null || numberTracks() == 0) {
            return;
        }
        else if (playstate == initStop || playstate == initPause || playstate == playing) {
            return;
        }
        // playstate is stopped or paused

        // Wait a little for the view to refresh, and then start playing
        timer.removeCallbacks(TimerCallback);
        timer.postDelayed(DoPlay, 1000);
    }
    
    

    Runnable DoPlay = new Runnable() {
      public void run() {
        Activity activity = (Activity)context;
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /* The startPulseTime is the pulse time of the midi file when
         * we first start playing the music.  It's used during shading.
         */
        if (options.playMeasuresInLoop) {
            /* If we're playing measures in a loop, make sure the
             * currentPulseTime is somewhere inside the loop measures.
             */
            int measure = (int)(currentPulseTime / midifile.getTime().getMeasure());
            if ((measure < options.playMeasuresInLoopStart) ||
                (measure > options.playMeasuresInLoopEnd)) {
                currentPulseTime = options.playMeasuresInLoopStart * midifile.getTime().getMeasure();
            }
            startPulseTime = currentPulseTime;
            options.pauseTime = (int)(currentPulseTime - options.shifttime);
        }
        else if (playstate == paused) {
            startPulseTime = currentPulseTime;
            options.pauseTime = (int)(currentPulseTime - options.shifttime);
        }else if(playstate==seeking){
//        	prevPulseTime=startPulseTime=currentPulseTime = midifile.getCurrentPulse();
//            prevPulseTime = currentPulseTime - midifile.getTime().getQuarter();
        }
        else {
            options.pauseTime = 0;
            startPulseTime = options.shifttime;
            currentPulseTime = options.shifttime;
            prevPulseTime = options.shifttime - midifile.getTime().getQuarter();
        }
        if(onlyPlayBgTrack)
        	createBgMidiFile();
        else
        	CreateMidiFile();
        
        sheet.ShadeNotes(-10, (int)prevPulseTime, false);
        sheet.ShadeNotes(-10, (int)currentPulseTime, false);
        piano.ShadeNotes(-10, (int)prevPulseTime);
        piano.ShadeNotes(-10, (int)currentPulseTime);
        
        playstate = playing;
        PlaySound(tempSoundFile);
        startTime = SystemClock.uptimeMillis();

        timer.removeCallbacks(TimerCallback);
        timer.removeCallbacks(ReShade);
        timer.postDelayed(TimerCallback, 100);

        sheet.ShadeNotes((int)currentPulseTime, (int)prevPulseTime, true);
        piano.ShadeNotes((int)currentPulseTime, (int)prevPulseTime);
        return;
      }
    };


    /** The callback for pausing playback.
     *  If we're currently playing, pause the music.
     *  The actual pause is done when the timer is invoked.
     */
    public void Pause() {

        Activity activity = (Activity)context;
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        if (midifile == null || sheet == null || numberTracks() == 0) {
        	//Log.d(TAG, "1. Return");
            return;
        }
        else if (playstate == playing) {
            playstate = initPause;
            //Log.d(TAG, "2. Change state to initPause");
            DoPause();
            return;
        }else if(playstate ==initPause){
        	//Log.d(TAG, "3. initPause->Stop()");
        	Stop();
        }
    }


    /** The callback for the Stop button.
     *  If playing, initiate a stop and wait for the timer to finish.
     *  Then do the actual stop.
     */
    public void Stop() {
        if (midifile == null || sheet == null || playstate == stopped) {
            return;
        }

        if (playstate == initPause || playstate == initStop || playstate == playing) {
            /* Wait for timer to finish */
            playstate = initStop;
            DoStop();
        }
        else if (playstate == paused) {
            DoStop();
        }
    }

    /** Perform the actual stop, by stopping the sound,
     *  removing any shading, and clearing the state.
     */
    void DoStop() { 
        playstate = stopped;
        timer.removeCallbacks(TimerCallback);
        sheet.ShadeNotes(-10, (int)prevPulseTime, false);
        sheet.ShadeNotes(-10, (int)currentPulseTime, false);
        piano.ShadeNotes(-10, (int)prevPulseTime);
        piano.ShadeNotes(-10, (int)currentPulseTime);
        startPulseTime = 0;
        currentPulseTime = 0;
        prevPulseTime = 0;
        midifile.setCurrentPulse((long) currentPulseTime);
        StopSound();
    }
    
    void DoPause() { 
    	playstate = initPause;
    	midifile.setCurrentPulse((long) currentPulseTime);
        timer.removeCallbacks(TimerCallback);
        StopSound();
    }

    /** Rewind the midi music back one measure.
     *  The music must be in the paused state.
     *  When we resume in playPause, we start at the currentPulseTime.
     *  So to rewind, just decrease the currentPulseTime,
     *  and re-shade the sheet music.
     */
    public void Rewind() {
        if (midifile == null || sheet == null || (playstate != paused&& playstate != seeking)) {
            return;
        }

        playstate=seeking;
        /* Remove any highlighted notes */
        sheet.ShadeNotes(-10, (int)currentPulseTime, false);
        piano.ShadeNotes(-10, (int)currentPulseTime);
   
        prevPulseTime = currentPulseTime; 
        currentPulseTime -= midifile.getTime().getMeasure();
        if (currentPulseTime < options.shifttime) {
            currentPulseTime = options.shifttime;
        }
        sheet.ShadeNotes((int)currentPulseTime, (int)prevPulseTime, false);
        piano.ShadeNotes((int)currentPulseTime, (int)prevPulseTime);
    }
    
    public void RewindOneQuater() {
        if (midifile == null || sheet == null || (playstate != paused&& playstate != seeking&&playstate!=stopped)) {
            return;
        }
        playstate=seeking;

        if(currentPulseTime<=options.shifttime){
        	Log.d(TAG, " Rewind In here 2: c="+currentPulseTime);
        	return;
        }
        
        /* Remove any highlighted notes */
        sheet.ShadeNotes(-10, (int)currentPulseTime, false);
        piano.ShadeNotes(-10, (int)currentPulseTime);
   
        
        currentPulseTime -= midifile.getTime().getQuarter();
        if (currentPulseTime < options.shifttime& currentPulseTime>options.shifttime-midifile.getTime().getQuarter()) {
            currentPulseTime = options.shifttime;
            //Log.d(TAG, " Rewind In here 1: c="+currentPulseTime);
        }
        midifile.setCurrentPulse((long) currentPulseTime);
        prevPulseTime = currentPulseTime-midifile.getTime().getQuarter(); 
        sheet.ShadeNotes((int)currentPulseTime, (int)prevPulseTime, false);
        piano.ShadeNotes((int)currentPulseTime, (int)prevPulseTime);
    }
    
    /** Fast forward the midi music by one measure.
     *  The music must be in the paused/stopped state.
     *  When we resume in playPause, we start at the currentPulseTime.
     *  So to fast forward, just increase the currentPulseTime,
     *  and re-shade the sheet music.
     */
    public void FastForward() {
        if (midifile == null || sheet == null) {
            return;
        }
        if (playstate != paused && playstate != stopped && playstate != seeking) {
            return;
        }
        playstate = seeking;

        /* Remove any highlighted notes */
        sheet.ShadeNotes(-10, (int)currentPulseTime, false);
        piano.ShadeNotes(-10, (int)currentPulseTime);
   
        prevPulseTime = currentPulseTime; 
        currentPulseTime += midifile.getTime().getMeasure();
        if (currentPulseTime > midifile.getTotalPulses()) {
            currentPulseTime -= midifile.getTime().getMeasure();
        }
        sheet.ShadeNotes((int)currentPulseTime, (int)prevPulseTime, false);
        piano.ShadeNotes((int)currentPulseTime, (int)prevPulseTime);
    }
    
    public void FastForwardOneQuater() {
        if (midifile == null || sheet == null) {
            return;
        }
        if (playstate != paused && playstate != stopped&& playstate != seeking) {
            return;
        }
        playstate = seeking;

        if(currentPulseTime >= midifile.getTotalPulses()){
        	//Log.d(TAG, " In here 2: c="+currentPulseTime);
        	return;
        }
        
        /* Remove any highlighted notes */
        sheet.ShadeNotes(-10, (int)currentPulseTime, false);
        piano.ShadeNotes(-10, (int)currentPulseTime);
   
        prevPulseTime = currentPulseTime; 
        currentPulseTime += midifile.getTime().getQuarter();
        if (currentPulseTime > midifile.getTotalPulses()&currentPulseTime < midifile.getTotalPulses()+midifile.getTime().getQuarter()) {
            currentPulseTime = midifile.getTotalPulses();
        	//Log.d(TAG, " In here 1: c="+currentPulseTime);
        }
        midifile.setCurrentPulse((long) currentPulseTime);
        sheet.ShadeNotes((int)currentPulseTime, (int)prevPulseTime, false);
        piano.ShadeNotes((int)currentPulseTime, (int)prevPulseTime);
    }
    
     
    public boolean ForwardNextPulse(long nextPulse) {
        try {
			return ForwardNextPulse(nextPulse, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return true;
    }
    
    public boolean ForwardNextPulse(long nextPulse, int d){
    	if (midifile == null || sheet == null|| (nextPulse==currentPulseTime && nextPulse!=0)) {
            return true;
        }
        if (playstate != paused && playstate != stopped&& playstate != seeking) {
            return true;
        }
        
        
        playstate = paused;

        /* Remove any highlighted notes */
        sheet.ShadeNotes(-10, (int)currentPulseTime, false);
        //piano.ShadeNotes(-10, (int)currentPulseTime);
        
        if (nextPulse > midifile.getTotalPulses()) {
        	return false; //end of song
        }
   
        prevPulseTime = currentPulseTime-d; 
        currentPulseTime = nextPulse;
        midifile.setCurrentPulse((long) currentPulseTime);
        //Log.d(TAG, " ForwardNextPulse="+currentPulseTime+" : "+midifile.getTotalPulses());
        
        sheet.ShadeNotes((int)currentPulseTime, (int)prevPulseTime, false);
        piano.ShadeNotes((int)currentPulseTime, (int)prevPulseTime);
        return true;
    }


    /** The callback for the timer. If the midi is still playing, 
     *  update the currentPulseTime and shade the sheet music.  
     *  If a stop or pause has been initiated (by someone clicking
     *  the stop or pause button), then stop the timer.
     */
    Runnable TimerCallback = new Runnable() {
      public void run() {
        if (midifile == null || sheet == null) {
            playstate = stopped;
            return;
        }
        else if (playstate == stopped || playstate == paused) {
            /* This case should never happen */
            return;
        }
        else if (playstate == initStop) {
            return;
        }
        else if (playstate == playing) {
            long msec = SystemClock.uptimeMillis() - startTime;
            prevPulseTime = currentPulseTime;
            currentPulseTime = startPulseTime + msec * pulsesPerMsec;
            midifile.setCurrentPulse((long) currentPulseTime);
            /* If we're playing in a loop, stop and restart */
            if (options.playMeasuresInLoop) {
                double nearEndTime = currentPulseTime + pulsesPerMsec*10;
                int measure = (int)(nearEndTime / midifile.getTime().getMeasure());
                if (measure > options.playMeasuresInLoopEnd) {
                    RestartPlayMeasuresInLoop();
                    return;
                }
            }

            /* Stop if we've reached the end of the song */
            if (currentPulseTime > midifile.getTotalPulses()) {
                if(piano.getListener()!=null)
                	piano.getListener().onEndOfSong();
                DoStop();
                return;
            }
            sheet.ShadeNotes((int)currentPulseTime, (int)prevPulseTime, true);
            piano.ShadeNotes((int)currentPulseTime, (int)prevPulseTime);
            timer.postDelayed(TimerCallback, 100);
            return;
        }
        else if (playstate == initPause) {
            long msec = SystemClock.uptimeMillis() - startTime;
            StopSound();

            prevPulseTime = currentPulseTime;
            currentPulseTime = startPulseTime + msec * pulsesPerMsec;
            midifile.setCurrentPulse((long) currentPulseTime);
            sheet.ShadeNotes((int)currentPulseTime, (int)prevPulseTime, false);
            piano.ShadeNotes((int)currentPulseTime, (int)prevPulseTime);
            playstate = paused;
            timer.postDelayed(ReShade, 1000);
            return;
        }
      }
    };


    /** The "Play Measures in a Loop" feature is enabled, and we've reached
     *  the last measure. Stop the sound, unshade the music, and then
     *  start playing again.
     */
    private void RestartPlayMeasuresInLoop() {
        playstate = stopped;
        piano.ShadeNotes(-10, (int)prevPulseTime);
        sheet.ShadeNotes(-10, (int)prevPulseTime, false);
        currentPulseTime = 0;
        prevPulseTime = -1;
        StopSound();
        timer.postDelayed(DoPlay, 300);
    }


	public int getPlaystate() {
		return playstate;
	}
	

	public void setPlaystate(int playstate) {
		this.playstate = playstate;
	}



	public double getPulsesPerMsec() {
		return pulsesPerMsec;
	}


	public SheetMusic getSheet() {
		return sheet;
	}



	public static MidiFile getMidifile() {
		return midifile;
	}


	public static void setMidifile(MidiFile midifile) {
		MidiPlayer.midifile = midifile;
	}



	public Piano getPiano() {
		return piano;
	}



	public boolean isEndOfSong() {
		return isEndOfSong;
	}


	public void setEndOfSong(boolean isEndOfSong) {
		this.isEndOfSong = isEndOfSong;
	}



	public void createBgMidiFile() {
		if(midifile==null||midifile.getTracks().size() <= 1) return;
		
		try {
            FileOutputStream dest = context.openFileOutput(tempSoundFile, Context.MODE_PRIVATE);
            MidiOptions mo= options.copy();
            mo.mute[midifile.getPrimaryTrack()]=true;
            midifile.ChangeSound(dest, mo);
            dest.close();
            // checkFile(tempSoundFile);
        }
        catch (IOException e) {
        	e.printStackTrace();
            Toast toast = Toast.makeText(context, "Error: Unable to create MIDI file for playing.", Toast.LENGTH_LONG);
            toast.show();
        }
	}



	public boolean isOnlyPlayBgTrack() {
		return onlyPlayBgTrack;
	}



	public void setOnlyPlayBgTrack(boolean onlyPlayBgTrack) {
		this.onlyPlayBgTrack = onlyPlayBgTrack;
	}
    
	
	
	

}


