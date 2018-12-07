package com.vtd.pianoapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.vtd.pianoapp.R;
import com.vtd.pianoapp.common.Constant;
import com.vtd.pianoapp.game.*;
import com.vtd.pianoapp.keyboard.KeyboardHeight;
import com.vtd.pianoapp.keyboard.KeyboardRootView;
import com.vtd.pianoapp.keyboard.KeyboardWidth;
import com.vtd.pianoapp.keyboard.SurfaceViewConfig;
import com.vtd.pianoapp.practice.Cell;
import com.vtd.pianoapp.util.CommonUtils;
import org.cocos2d.nodes.CCNode;

import java.util.ArrayList;

public class GamePlayActivity extends AppCompatActivity implements Constant, View.OnClickListener, OnSongFinishListener {

	private static final String TAG = "ttt";
	private String songPath = "";
	private String songTitle = "";
	private String songAuthor = "";

	private KeyboardRootView keyboardRootView;
	private GameplayController gameplayController;
	private ChallengeScoreCalculator scoreCalculator;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setLayoutNoMenuBar();
		setContentView(R.layout.gameplay_activity);
		getChallengeSongData();
		initGameplayController();
		initView();
		initSurfaceView();
		keyboardRootView.postDelayed(new Runnable() {
			@Override
			public void run() {
				keyboardRootView.scrollToKey("c4");
				startGame();
			}
		},0);
	}

	public void startGame(){
		gameplayController.setSongData(SongLoader.loadBuildInSongData(songPath));
		gameplayController.startGameplay(0);
		onStartGameplay();
	}

	private void initSurfaceView() {
		Log.d(TAG, "initSurfaceView: ");
		keyboardRootView = (KeyboardRootView) findViewById(R.id.keyboard_root);
		SurfaceViewConfig builder = new SurfaceViewConfig();
		builder.setMode(SurfaceViewConfig.CHALLENGE_MODE);
		keyboardRootView.requestUI(this, builder);
		gameplayController.setKeyboardProxy(keyboardRootView.getKeyboardProxy());
		gameplayController.setAnimLayerProxy(keyboardRootView.getAnimLayerProxy());
		keyboardRootView.addOnScaleListener(gameplayController);
	}

	private void initGameplayController() {
		gameplayController = new GameplayController(new GameplayController.GamePlayControllerListener() {
			@Override
			public void onStarted() {
			}

			@Override
			public void onPause() {

			}
		});
		gameplayController.setOnSongFinishListener(this);

		scoreCalculator = new ChallengeScoreCalculator(new ChallengeScoreCalculator.ScoreListener() {
			@Override
			public void onScoreUpdated(int score) {
				setScore(score);
			}

			@Override
			public void onComboUpdated(int comboChain, int comboGain) {
				setCombo(comboGain, comboChain);
			}

			@Override
			public void onGainScore(Cell cell, int score, ChallengeScoreCalculator.EnumAccuracy accuracy) {
//				float xScaleFactor = KeyboardWidth.whiteKeyWidthPixels() / Constant.originWhiteKeyWidth;
//				float padding = xScaleFactor * cell.getWidth() / 2;
//				float x = (cell.getPositionRef().x * xScaleFactor + keyboardRootView.getAnimationScene().getScrollX()) + padding;
//				float y = KeyboardHeight.currentHeightPixels() + cell.getHeight() + CommonUtils.convertDpToPixel(12);
			}

			@Override
			public void increaseGainScore(String tag, int gainScore) {
			}

			@Override
			public void removeScoreLabel(String tag) {
			}
		});
		scoreCalculator.enableComboGain();
		gameplayController.setChallengeScoreCalculator(scoreCalculator);
	}

	public void setScore(final int score) {
		Log.d(TAG, "setScore: score" + score);
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				tvScoreValue.setText(String.valueOf(score));
//			}
//		});
	}

	public void setCombo(final int combo, final int chain) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
			}
		});
	}

	private void getChallengeSongData() {
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey("songPath"))
				songPath = bundle.getString("songPath");
			if (bundle.containsKey("songTitle"))
				songTitle = bundle.getString("songTitle");
			if (bundle.containsKey("songAuthor"))
				songAuthor = bundle.getString("songAuthor");
		}
		songPath = "song/1_Brahms_Lullaby.ruby";
	}

	private void setLayoutNoMenuBar() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_DITHER, WindowManager.LayoutParams.FLAG_DITHER);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	private void initView() {
		ImageView imgPause = findViewById(R.id.imgPause);
		setupSongInfoView();
		imgPause.setOnClickListener(this);
	}

	private void setupSongInfoView() {

	}

	private void onStartGameplay() {
		scoreCalculator.reset();
		setScore(0);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		gameplayController.cleanUp();
	}

	private void goBackChallengeActivity() {
		Intent data = new Intent();
		try {
			if (gameplayController.isSongFinished()) {
				int score = scoreCalculator.getFinalScore();
				Log.d("", "goBackChallengeActivity: score" + score);
				data.putExtra("score", score);
			}
		} catch (Exception ignored) {
		}
		setResult(Activity.RESULT_OK, data);
		finish();
	}

	@Override
	public void onBackPressed() {
		try {
			pauseGameplay();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		pauseGameplay();
	}

	private void pauseGameplay() {
		if (!gameplayController.isAnimationPaused()) {
			showDialogPaused(scoreCalculator.getHitCount(), scoreCalculator.getTotalNotes(), scoreCalculator.getOnTimePercent());
			gameplayController.pauseAnimation();
		}
	}

	public void showDialogPaused(int noteHit, int numNote, int onTime) {
//		DialogPauseIngameChallenge dialogPaused = DialogPauseIngameChallenge.newInstance();
//		Bundle bundle = new Bundle();
//		bundle.putInt(StringUtils.SCORE_NOTE_HIT, noteHit);
//		bundle.putInt(StringUtils.SCORE_ON_TIME, onTime);
//		bundle.putInt(StringUtils.MAX_NOTE_HIT, numNote);
//		dialogPaused.setArguments(bundle);
//		dialogPaused.show(getSupportFragmentManager(), StringUtils.TAG_DIALOG_PAUSED);
	}

	public void resumeGameplay() {
		gameplayController.resumeAnimation();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.imgPause:
				pauseGameplay();
				break;
			default:
				Log.d(TAG, "onClick: onClick");
				break;
		}
	}


	public void restartChallenge() {
		Log.d(TAG, "restart challenge");
		gameplayController.replay();
		onStartGameplay();
	}

	public void quitGame() {
		goBackChallengeActivity();
	}

	@Override
	public void onSongFinished() {
		onGameComplete();
	}

	public void onGameComplete() {
		int noteHit = scoreCalculator.getHitCount();
		int numNote = scoreCalculator.getTotalNotes();
		int onTime = scoreCalculator.getOnTimePercent();
		int totalScore = scoreCalculator.getFinalScore();
		int bonus = scoreCalculator.getRubyReward();
		showDialogComplete(noteHit, numNote, onTime, totalScore, bonus);
	}

	private void showDialogComplete(final int noteHit, final int numNote, final int onTime, final int totalScore, final int
			bonus) {
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				Bundle args = new Bundle();
//				args.putFloat(ResultDialog.EXTRA_ONTIME, onTime);
//				args.putInt(ResultDialog.EXTRA_MISS, 0);
//				args.putInt(ResultDialog.EXTRA_HIT, noteHit);
//				args.putInt(ResultDialog.EXTRA_TOTAL_SCORE, totalScore);
//				args.putInt(ResultDialog.EXTRA_TOTAL_NOTE, numNote);
//
//				args.putInt(ResultDialog.EXTRA_SCORE_FACTOR, bonus);
//				args.putInt(ResultDialog.PLAY_MODE, ResultDialog.PLAY_MODE_CHALLENGE);
//				CommonUtils.crashlyticLog(TAG, "show result dialog");
//				ResultDialog resultDialog = ResultDialog.newInstance();
//				resultDialog.setArguments(args);
//				resultDialog.show(getSupportFragmentManager(), StringUtils.TAG_DIALOG_COMPLETE);
//			}
//		});

	}
}
