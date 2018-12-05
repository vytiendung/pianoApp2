package com.vtd.pianoapp.common;

import com.vtd.pianoapp.util.SettingConstant;
import org.cocos2d.types.CGSize;

import java.util.Arrays;
import java.util.List;


public class Config implements Constant {
	public static final int ORIGINAL_HEIHT = 480;
	public static final int ORIGINAL_WIDTH = 800;
	public static final String BEGIN_POS = "c4";
	public static final String VERSION = "1.1";

	private static Config instance;
	public int memClass = 24;
	public boolean isNewUser = true;
	public boolean isNewToShowFullAds = true;
	public int deviceType; // 1,2,3

	/*
	 * Basic setting
	 */
	public List<String> noteList = Arrays.asList("a0", "a0m", "b0", "c1", "c1m", "d1", "d1m", "e1", "f1", "f1m", "g1",
			"g1m", "a1", "a1m", "b1", "c2",
			"c2m", "d2", "d2m", "e2", "f2", "f2m", "g2", "g2m", "a2", "a2m", "b2", "c3", "c3m", "d3", "d3m", "e3", "f3",
			"f3m", "g3", "g3m",
			"a3", "a3m", "b3", "c4", "c4m", "d4", "d4m", "e4", "f4", "f4m", "g4", "g4m", "a4", "a4m", "b4", "c5", "c5m",
			"d5", "d5m", "e5",
			"f5", "f5m", "g5", "g5m", "a5", "a5m", "b5", "c6", "c6m", "d6", "d6m", "e6", "f6", "f6m", "g6", "g6m", "a6",
			"a6m", "b6", "c7",
			"c7m", "d7", "d7m", "e7", "f7", "f7m", "g7", "g7m", "a7", "a7m", "b7", "c8");
	public int winWidth;
	public int winHeight;
	public int originalHeight = ORIGINAL_HEIHT;
	public int originalWidht = ORIGINAL_WIDTH;
	public float scaleX;
	public float scaleY;
	public String beginPosition = "c4";
	public CGSize winSize;

	/*
	 * Sound font setting
	 */

	public boolean reverbOn = true;
	private static final float DEFAULT_REVERB_ROOM_SIZE = 0.2f;
	private static final float DEFAULT_REVERB_DAMPING = 0;
	private static final float DEFAULT_REVERB_WIDTH = 0.5f;
	private static final float DEFAULT_REVERB_LEVEL = 0.9f;

	public int reverbZoomSize = Math.round(DEFAULT_REVERB_ROOM_SIZE * 100 / (SettingConstant.MAX_REVERB_ZOOM_SIZE - SettingConstant
			.MIN_REVERB_ZOOM_SIZE));
	public int reverbDamping = Math.round(DEFAULT_REVERB_DAMPING * 100 / (SettingConstant.MAX_REVERB_DAMPING - SettingConstant
			.MIN_REVERB_DAMPING));
	public int reverbWidth = Math.round(DEFAULT_REVERB_WIDTH * 200 / (SettingConstant.MAX_REVERB_WIDTH - SettingConstant
			.MIN_REVERB_WIDTH));
	public int reverbLevel = Math.round(DEFAULT_REVERB_LEVEL * 100 / (SettingConstant.MAX_REVERB_LEVEL - SettingConstant
			.MIN_REVERB_LEVEL));

	public boolean chorusOn = true;
	public int chorusVoiceCount = 0;
	public int chorusLevel = 0;
	public int chorusSpeed = 29;
	public int chorusDepth = 0;

	public int polyphonyNumber = 128;
	/*
	 * Keyboard setting
	 */
	public int keyPerScreen = 10;
	public int tmpKeyPerScreen = 10;
	public float keyHeightRatio = 0.5f;
	public float keyWidthWhite;
	public float keyHeightWhite;
	public float keyWidthBlack;
	public float keyHeightBlack;
	public float fullPianoSize;
	public float keyScaleX;
	public float keyScaleY;

	/*
	 * Sound setting
	 */
	public int sound_volume_app= 10;
	public int sound_time_of_sustain=1000;
	public int sound_beta_buffer_size=5;
	public int sound_beta_sample_rate=5;
	public boolean sound_hq_is_enable = false;
	public int sound_hq_level=5;

	/*
	 * Custom options
	 */
	public boolean isWideTouchArea;
	public boolean isShowAnimGfx = true;
	public int speedRate;

	//	public int keyWidthConfig; // normal
	public int vibrateTime = 50;
	public int decayTime;
	public boolean isVibrate;
	public boolean isHelpOn;
	public int volumePersent; // 0 ->100
	public String deviceResolution = "";
	public int deviceWidth;
	public int deviceHeight;
	public float noteAnimateRate;
//	public boolean isWeakDevice;

	/*
	 * In game setting
	 */
	public float speed;
	public int maxLevel; // sprite max level
	public String imgPath = "img/"; // image path
	public int keyPerScreenUp;
	public int keyPerScreenDown;
	public int keyWidthUpConfig;
	public int keyWidthDownConfig;
	public int noteLabelStyle;
	// song list state
	public int lastGroupIndex = -1;
	public int lastGroupIndexMySong = -1;


	// current version

	//	public String version = "1.1";

	public int lastChildIndexMySong = -1;
	public int lastChildIndexBuiltin = -1;
	public int lastCloudGroupIndex = -1;
	public int lastCloudChildIndex = -1;
	public int lastSearchGroupIndex = -1;
	public int lastSearchChildIndex = -1;
	public int lastTabIndex = 0;
	public int qualitySum = 0;

	// theme config
	// session counting
	public int sessionCount;
	public boolean ishowFeatureApp = false;
	public boolean isMagicMode = false;
	public boolean isDownloadedAllSongs = false;
	public boolean isDownloadedInstrument = false;
	// TODO: remove midiloader
	//midi game play
	public boolean isLockScreen = false;
	public boolean isPreviewDisable = false;
	public int appVersionCode;


	public boolean enableKeyPressedEffect = true;
	public int guideWhiteNoteColor;
	public int guideBlackNoteColor;

	public static Config getInstance() {
		if (instance == null) {
			instance = new Config();
		}
		return instance;
	}
}

