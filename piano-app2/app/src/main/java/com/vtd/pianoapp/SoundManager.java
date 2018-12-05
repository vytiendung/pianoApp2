package com.vtd.pianoapp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.vtd.pianoapp.common.Config;
import com.vtd.pianoapp.common.Constant;
import com.vtd.pianoapp.songobject.GamePlayNote;
import com.vtd.pianoapp.songobject.Note;
import com.vtd.sheetmusic.midi.MidiFactory;
import com.vtd.sheetmusic.midi.MidiNote;

import java.io.File;
import java.util.*;

public class SoundManager implements Constant {
	static final String TAG=SoundManager.class.getSimpleName();

	public static final int MIDI_BG_TARGET=999;
	public static final String SOUND_PIANO_PATH = "sound/piano2";
	static private SoundManager _instance;
	
	
	public HashMap<String, Integer> mSoundPoolMap;
	public AudioManager mAudioManager;
	public Context mContext;
	public SparseIntArray mStreamPointerMap;
	public SparseIntArray sourceMap;
	public SparseArray<ArrayList<Integer>> targetSoundIdMap;
	public SparseArray<SparseIntArray> targetToSource;
	public SoundPool soundPool;

	private Timer timerStopSound;

	static synchronized public SoundManager getInstance() {
		if (_instance == null) {
			_instance = new SoundManager();
			_instance.initSounds();
		}
		return _instance;
	}

	public static boolean supportedNewSoundEngine() {
		int sdkInt = Build.VERSION.SDK_INT;
		return ( sdkInt >= Build.VERSION_CODES.JELLY_BEAN && sdkInt <= Build.VERSION_CODES.M );
	}

	public static boolean supportedDialogNewSoundEngine() {
		int sdkInt = Build.VERSION.SDK_INT;
		return ( supportedNewSoundEngine() && sdkInt >= Build.VERSION_CODES.LOLLIPOP );
	}

	public void initSounds() {
		mContext = MyApplication.getInstance();
		mSoundPoolMap = new HashMap<String, Integer>();
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

		mStreamPointerMap = new SparseIntArray();
		sourceMap = new SparseIntArray();
		targetToSource= new SparseArray<SparseIntArray>();
		targetSoundIdMap = new SparseArray<ArrayList<Integer>>();

		int source = 21;
		for (int i = 21; i <= 108; i++) {
			if (i % 5 == 0)
				source = i;
			sourceMap.put(i, source);
		}
		soundPool = new SoundPool(10, 3, 0);
		timerStopSound = new Timer();
	}

	public void unLoadSound(int target) {
		try{
			refresh();
			ArrayList<Integer> soundIdList = targetSoundIdMap.get(target);
			int soundID;
			if (soundIdList != null)
				for (int i = 0; i < soundIdList.size(); i++) {
					soundID=soundIdList.get(i);
					Log.d("SoundDecoder", "Unload sound = "+soundID);
					soundPool.unload(soundID);
					mSoundPoolMap.values().remove(soundID);
				}
		}catch(Exception e){
			
		}
	}

	public void addSound(String name, int SoundID) {
		mSoundPoolMap.put(name, soundPool.load(mContext, SoundID, 1));
	}
	
	private int findSourceIndex(int target, List<Integer> list){
		if(list.size()==2) return Math.abs(target-list.get(0))>Math.abs(target-list.get(1))?list.get(1):list.get(0);
		if(list.size()==1) return list.get(0);
		
		int in=list.size()/2;
		if(target==list.get(in)) return list.get(in);
		
		if(target<list.get(in)){
			return findSourceIndex(target, list.subList(0, in));
		}else{
			return findSourceIndex(target, list.subList(in,list.size()));
		}
		
	}
	
	private void genSourceMap(int instrumentId, ArrayList<Integer> noteList){
		Collections.sort(noteList);
		SparseIntArray sourceMap = new SparseIntArray();
		for (int i = 21; i <= 108; i++) {
			sourceMap.put(i, findSourceIndex(i, noteList));
		}
		targetToSource.put(instrumentId, sourceMap);
	}

	/**
	 * Find in asset folder firstly. If not found, find in gen folder 
	 * @param target
	 * @param instrumentId
	 */
	public void loadDefaultSound(int target, int instrumentId, final Runnable onLoadDone) {
		String pathInAssets = SOUND_PIANO_PATH;
		AssetFileDescriptor descriptor;
		ArrayList<Integer> loadedNote=new ArrayList<Integer>();
		try {
			targetSoundIdMap.remove(target);
			AssetManager am = mContext.getAssets();
			final String list[] = am.list(pathInAssets);
			String key;
			ArrayList<Integer> soundIdList = new ArrayList<Integer>();
			final int[] loadedCount = {0};
			soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
				@Override
				public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
					loadedCount[0]++;
					if (loadedCount[0] == list.length){
						new Handler(Looper.getMainLooper()).post(onLoadDone);
					}
				}
			});
			for (int i = 0; i < list.length; i++) {
				int soundId = -1;
				String filePath = pathInAssets + "/" + list[i];
				descriptor = am.openFd(filePath);
				key = list[i];
				key = key.substring(0, key.lastIndexOf("."));
				loadedNote.add(Integer.valueOf(key.substring(key.lastIndexOf("_")+1)));
				if(mSoundPoolMap.get(key)!=null)continue;
				soundId = soundPool.load(descriptor, 1);
				mSoundPoolMap.put(key, soundId);
				soundIdList.add(soundId);
				Log.d("SoundDecoder", "Load assets sound = " + filePath + " , " + key + " , " + soundId);
			}

			targetSoundIdMap.put(target, soundIdList);
			genSourceMap(instrumentId, loadedNote);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void loadSoundFromDir(String soundDir,int target, int instrumentId) {
		try{
			ArrayList<Integer> loadedNote=new ArrayList<Integer>();
			File dir=new File(soundDir);
			if (dir.exists()) {
				File[] files = dir.listFiles();
				if (files == null) {
					return ;
				}
				int soundId = -1;
				String key;
				String path;
				ArrayList<Integer> soundIdList = targetSoundIdMap.get(target);
				if(soundIdList==null){
					soundIdList=new ArrayList<Integer>();
					targetSoundIdMap.put(target, soundIdList);
				}
				soundIdList.clear();

				for (int i = 0; i < files.length; i++) {
					if (!files[i].isDirectory() && files[i].exists()) {
						path=files[i].getAbsolutePath();
						key=path.substring(path.lastIndexOf("/")+1, path.lastIndexOf("."));
						loadedNote.add(Integer.valueOf(key.substring(key.lastIndexOf("_")+1)));
						if(mSoundPoolMap.get(key)!=null)continue;

						soundId=soundPool.load(path, 1);

						mSoundPoolMap.put(key, soundId);
						soundIdList.add(soundId);
						Log.d("SoundDecoder", "Load sound = " + path + " , " + key + "  " + soundId);
					}
				}

				genSourceMap(instrumentId,loadedNote);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * @param soundDir
	 * @param target
	 */
	public void loadSounds(String soundDir,int target) {
		unLoadSound(MIDI_BG_TARGET);
		File dir=new File(soundDir);
		if (dir.exists()) {
			targetSoundIdMap.remove(MIDI_BG_TARGET);
			File[] files = dir.listFiles();
			if (files == null) {
				return ;
			}
			int soundId;
			String key;
			String path;
			ArrayList<Integer> soundIdList = targetSoundIdMap.get(target);
			if(soundIdList==null){
				soundIdList=new ArrayList<Integer>();
				targetSoundIdMap.put(target, soundIdList);
			}
			for (int i = 0; i < files.length; i++) {
				if (!files[i].isDirectory()) {
					path=files[i].getAbsolutePath();
					key=path.substring(path.lastIndexOf("/")+1, path.lastIndexOf("."));
					if(mSoundPoolMap.get(key)!=null)continue;
					soundId=soundPool.load(path, 1);
					mSoundPoolMap.put(key, soundId);
					soundIdList.add(soundId);
					Log.d("SoundManager", "Load sound = "+soundId+" -- key "+key);
				}
			}
		}
		
	}


	public synchronized int playSound(int instrument, int index, int pointerId, float volume) {
		int streamId = 0;
		try {
			SparseIntArray sourceMap = targetToSource.get(instrument);
			sourceMap = sourceMap == null ? this.sourceMap : sourceMap;
			int source = sourceMap.get(index);

			int step = index - source;
			float rate = (float) (1 * Math.pow(2, step / 12d));
			streamId = soundPool.play(mSoundPoolMap.get(instrument + "_" + source), volume, volume, 1, 0, rate);
			if (streamId > 0) {
				mStreamPointerMap.put(pointerId, streamId);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return streamId;
	}
	
	public synchronized int playSound(int instrument, int noteNumber, float volume) {
		Integer soundId = mSoundPoolMap.get(instrument + "_" + noteNumber);
		int streamId = 0;
		try {
			if (soundId == null) {
				SparseIntArray sourceMap = targetToSource.get(instrument);
				sourceMap = sourceMap == null ? this.sourceMap : sourceMap;
				int source = sourceMap.get(noteNumber);
				int step = noteNumber - source;
				float rate = (float) (1 * Math.pow(2, step / 12d));
				streamId = soundPool.play(mSoundPoolMap.get(instrument + "_" + source), volume, volume, 1, 0, rate);
			} else {
				streamId = soundPool.play(soundId, volume, volume, 0, 0, 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return streamId;
	}
	
	public synchronized void playSound(int instrumentId, Note note, final float volume){

			try {
				SparseIntArray sourceMap=targetToSource.get(instrumentId);
				sourceMap=sourceMap==null?this.sourceMap:sourceMap;
				int index=note.getIndex()-1+21;
				int source = sourceMap.get(index);

				int step = index - source;
				float rate = (float) (1 * Math.pow(2, step / 12d));
				final int streamId;
				streamId= soundPool.play(mSoundPoolMap.get(instrumentId + "_" + source), volume, volume, 1, 0, rate);
				if (streamId > 0) {
					final int pointerId = note.getIndex();
					mStreamPointerMap.put(pointerId, streamId);
					long duration = (long) (note.getLength() * Config.getInstance().noteAnimateRate);
					stopSoundDelay(duration, pointerId, volume);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public synchronized void playSound(int instrumentId, GamePlayNote note, float volumePercent){
		try {
			SparseIntArray sourceMap=targetToSource.get(instrumentId);
			sourceMap=sourceMap==null?this.sourceMap:sourceMap;
			int index=note.id;
			int source = sourceMap.get(index);

			int step = index - source;
			float rate = (float) (1 * Math.pow(2, step / 12d));
			final int streamId;
			streamId = soundPool.play(mSoundPoolMap.get(instrumentId + "_" + source), volumePercent, volumePercent, 1, 0, rate);
			if (streamId > 0) {
				final int pointerId=note.id +1 - 21;
				mStreamPointerMap.put(pointerId, streamId);
				stopSoundDelay((long)note.duration, pointerId, volumePercent);
				soundPool.setRate(streamId,rate);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void playSound(int instrumentId, MidiNote note, final float volume){
		try {
			Integer soundId=mSoundPoolMap.get(instrumentId + "_" + note.getNumber());
			final int streamId;
			if(soundId==null){
				SparseIntArray sourceMap=targetToSource.get(instrumentId);
				sourceMap=sourceMap==null?this.sourceMap:sourceMap;
				int index=note.getNumber();
				int source = sourceMap.get(index);

				int step = index - source;
				float rate = (float) (1 * Math.pow(2, step / 12d));
				streamId= soundPool.play(mSoundPoolMap.get(instrumentId + "_" + source), volume, volume, 1, 0, rate);

			}else{
				streamId = soundPool.play(soundId, volume, volume, 0, 0, 1);
			}
			if (streamId > 0) {
				final int pointerId=note.getNumber();
				mStreamPointerMap.put(pointerId, streamId);
				long duration = note.getDurationInMilis();
				stopSoundDelay(duration, pointerId, volume);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public synchronized void stopSound(int pointerId, float volume) {
		try {
			int streamId = -1;
			synchronized (mStreamPointerMap) {
				streamId = mStreamPointerMap.get(pointerId);
				mStreamPointerMap.delete(pointerId);
			}
			if ( streamId > 0 ) {
				fadeOutStreamID(streamId, volume);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public synchronized void stopSound(ArrayList<Integer> pil, float volume) {
		if (Config.getInstance().decayTime == DECAY_TIME_NONE) {
			return;
		}
		if(pil==null){
			return;
		}
		try {
			for(Integer i:pil){
				stopSound(i, volume);
			}
		} catch (Exception e) {

		}
	}

	public void cleanup() {
		if (timerStopSound != null) {
			timerStopSound.cancel();
			timerStopSound.purge();
			timerStopSound = null;
		}
		if (soundPool != null) {
			soundPool.release();
		}

		if (mSoundPoolMap != null) {
			mSoundPoolMap.clear();
			mSoundPoolMap = null;
		}
		
		if(mStreamPointerMap!=null) {
			mStreamPointerMap.clear();
			mStreamPointerMap = null;
		}
	}

	public void refresh() {
		try {
			if (mStreamPointerMap != null) {
				for (int i = 0; i < mStreamPointerMap.size(); i++) {
					soundPool.stop(mStreamPointerMap.get(i));
				}
				mStreamPointerMap.clear();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	

	private String doCheckAndGen(int instrumentId) {
		String path=getGenSoundPath(instrumentId, mContext);
		File dir=new File(path);
		if(!dir.exists()){
			dir.mkdirs();
			genSound(instrumentId, dir.getAbsolutePath());
		}else{

			File[] files = dir.listFiles();
			int count=0;
			String ext;
			for (int i = 0; i < files.length; i++) {
				if (!files[i].isDirectory()) {
					ext=path.substring(path.lastIndexOf("."),path.length());
					if(ext.contains("mid")){
						count++;
					}
				}
			}
			if(count!=18){
				genSound(instrumentId, dir.getAbsolutePath());
			}

		}
		return path;
	}

	private String getGenSoundPath(int instrumentId, Context context){
		String rePath = "data/data/" + context.getPackageName() + "/.gensound" + "/"+instrumentId;
		return rePath;
	}
	
	private void genSound(int instrumentId, String dir) {
		int[] note = new int[] { 21, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 105 };

		for (int i = 0; i < note.length; i++) {
			MidiFactory.createMidiFile(note[i], instrumentId, dir, 127);
		}

	}
	
	public void stopSoundDelay(final long duration, final int pointerId, final float volume) {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					stopSound(pointerId, volume);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		timerStopSound.schedule(task, duration);
	}

	public void fadeOutStreamID(final int streamID, float volume) {
		final float step = 0.1f;
		int count = 0;
		int delay;
		int decayTime = Config.getInstance().decayTime;
		while(volume > 0){
			volume -= step;
			final float streamVolume = volume;
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					soundPool.setVolume(streamID, streamVolume, streamVolume);
				}
			};
			delay = count * decayTime;
			timerStopSound.schedule(task, delay);
			++count;
		}
		delay = count * decayTime;
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if(soundPool != null)
					soundPool.stop(streamID);
			}
		};
		timerStopSound.schedule(task, delay);
	}
}