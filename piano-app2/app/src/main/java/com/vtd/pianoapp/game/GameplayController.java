package com.vtd.pianoapp.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import com.vtd.cocos2d.FrameEventListener;
import com.vtd.pianoapp.MyApplication;
import com.vtd.pianoapp.SoundManager;
import com.vtd.pianoapp.common.Constant;
import com.vtd.pianoapp.gameUtils.PianoKeyHelper;
import com.vtd.pianoapp.keyboard.*;
import com.vtd.pianoapp.common.Config;
import com.vtd.pianoapp.practice.Cell;
import com.vtd.pianoapp.practice.CellDirector;
import com.vtd.pianoapp.songobject.GamePlayNote;
import com.vtd.pianoapp.songobject.RubyStep;
import com.vtd.pianoapp.util.NoteUtils;
import org.cocos2d.nodes.CCDirector;

import java.util.ArrayList;
import java.util.HashMap;

public class GameplayController implements FrameEventListener, PerformActionListener, KeyboardScalingObserver {
	private String TAG = "GameplayController";
	private static final float renderThreshold = 1f * Config.getInstance().winHeight;
	private static final int STATUS_ANIM_PAUSED = -1;
	private static final int STATUS_ANIM_RESUMED = -2;
	private static final int STATUS_FINISH = -3;
	private static final int NUM_FIRST_STEPS_TO_HINT = 10;

	private final ScoreLabelValues scoreLabelValues = new ScoreLabelValues();
	private final SparseArray<ArrayList<Cell>> rowsHolder = new SparseArray<>();
	public SongData songData;
	private KeyboardProxy keyboardProxy;
	private AnimationLayerProxy animLayerProxy;
	private SparseArray<Long> lastCellsFiredTimer;
	private SparseArray<StepProperty> stepProperties;
	private SparseArray<ValueAnimator> challengeScoreAnimations;
	private int currentStepIndex;
	private int renderedStepIndex;
	private int status;
	private boolean isWaitingForBgNoteToFinish;
	private boolean isWaitingForKeyReleaseToFinish;
	private float elapsedTime;
	private int currentBgNoteIndex;
	private Handler mainThreadHandler;
	private OnStepChangeListener onStepChangeListener;
	private Vibrator vibrator;
	private boolean needReCaculate = true;
	private Config config;
	private int scoreBeginTime;
	private PracticeScoreCalculator practiceScoreCalculator;
	private ChallengeScoreCalculator challengeScoreCalculator;
	private OnSongFinishListener onSongFinishListener;
	private float animationSpeed;
	private boolean isChallengeMode;
	private GamePlayControllerListener listener;

	public GameplayController(GamePlayControllerListener listener) {
		if (vibrator == null)
			vibrator = (Vibrator) MyApplication.getInstance().getSystemService(Context.VIBRATOR_SERVICE);
		config = Config.getInstance();
		mainThreadHandler = new Handler(Looper.getMainLooper());
		isChallengeMode = GameplaySetting.isChallengeMode();
		this.listener = listener;
	}

	public void setSongData(@NonNull SongData songData) {
		this.songData = songData;
		needReCaculate = true;
		currentStepIndex = 0;
	}

	@Override
	public void onEnterFrame(float dt) {
		if (!isAnimationResumed()) return;
		elapsedTime += dt * 1000f;
		float deltaDistance = dt * animationSpeed;
		animLayerProxy.setY(animLayerProxy.getY() - deltaDistance);
		checkToRenderNextRows();
		if (isChallengeMode) {
			if (checkRowReachedFirePoint(currentStepIndex + 1)) {
				playCurrentStepInChallenge();
				focusToNextStepInMainThread();
				destroyPreviousRows();
				checkToIgnoreNoteScore();
			}
			updateScoreLabels();
		} else if (checkRowReachedFirePoint(currentStepIndex)) {
			if (GameplaySetting.isAutoPlay() || !GameplaySetting.isNoteWaitingEnabled()) {
				playCurrentStep(false);
				focusToNextStepInMainThread();
				destroyPreviousRows();
			} else if (hasNextStep() || !isWaitingForKeyReleaseToFinish) {
				pauseAnimation();
			}
		}
		playBackgroundNotes();
	}

	private void updateScoreLabels() {
		synchronized (scoreLabelValues) {
			scoreLabelValues.waitForReady();
			for (String tag : scoreLabelValues.keySet()) {
				challengeScoreCalculator.increaseGainScore(tag, scoreLabelValues.get(tag));
			}
			scoreLabelValues.makeReady();
		}
	}

	private void playCurrentStepInChallenge() {
		int stepIndex = currentStepIndex;
		currentStepIndex++;
		if (!hasNextStep()) {
			waitForBackgroundNotesToFinish();
		}
		RubyStep nextStep = songData.steps.get(currentStepIndex);
		elapsedTime = nextStep.startTime;
//		long currentTime = System.currentTimeMillis();
//		ArrayList<GamePlayNote> notes = nextStep.notes;
//		for (int i = 0; i < notes.size(); i++) {
//			GamePlayNote note = notes.get(i);
//			int noteIndex = NoteUtils.keyIndexOf(note.id);
//			lastCellsFiredTimer.put(noteIndex, currentTime);
//		}
		resumeMidiBackground(currentStepIndex);
		removeKeyboardHint(stepIndex);
	}

	private void checkToIgnoreNoteScore() {
		int previousStepIndex = currentStepIndex - 1;
		StepProperty stepProperty = stepProperties.get(previousStepIndex);
		for (int i = 0; i < stepProperty.size(); i++) {
			int key = stepProperty.keyAt(i);
			if (!stepProperty.get(key).isFired) {
				print("onIgnoreNote: " + i);
				challengeScoreCalculator.onIgnoreNote();
			}
		}
	}

	private void focusToNextStepInMainThread() {
		if (currentStepIndex < songData.steps.size()) {
			glowCurrentRow();
			mainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					showKeyboardHint();
				}
			});
		} else {
			waitForBackgroundNotesToFinish();
		}
	}

	private void waitForBackgroundNotesToFinish() {
		isWaitingForBgNoteToFinish = true;
	}

	private void playBackgroundNotes() {
		if (songData.backgroundNotes != null) {
			for (int i = currentBgNoteIndex; i < songData.backgroundNotes.size(); i++) {
				GamePlayNote note = songData.backgroundNotes.get(i);
				if (elapsedTime >= note.startTime) {
					print("play bg note: id = " + note.id);
					playNoteSound(note);
					currentBgNoteIndex = i + 1;
				} else {
					break;
				}
			}
			if (isWaitingForBgNoteToFinish && currentBgNoteIndex >= songData.backgroundNotes.size()) {
				finishSong();
			}
		}
	}

	private void playCurrentStep(boolean isKeyFired) {
		try {
			int stepIndex = currentStepIndex;
			if (isKeyFired) {
				print("-------------- play note: " + stepIndex);
			}
			if (!hasNextStep()) {
				waitForKeyReleaseToFinish();
			}
			currentStepIndex++;
			stepProperties.get(stepIndex).onFired();
			RubyStep currentStep = songData.steps.get(stepIndex);
			elapsedTime = currentStep.startTime;
			long currentTime = System.currentTimeMillis();
			ArrayList<GamePlayNote> notes = currentStep.notes;
			for (int i = 0; i < notes.size(); i++) {
				GamePlayNote note = notes.get(i);
				if (isKeyFired || GameplaySetting.isAutoPlay()) {
					print("-------------- play note: " + note.name);
					playNoteSound(note);
					int noteIndex = NoteUtils.keyIndexOf(note.id);
					lastCellsFiredTimer.put(noteIndex, currentTime);
				}
			}
			if (!isChallengeMode && config.isShowAnimGfx && animLayerProxy.getVisible()) {
				keyboardProxy.updateBlinks(currentStep);
			}
			resumeMidiBackground(stepIndex);
			removeKeyboardHint(stepIndex);
			ArrayList<Cell> currentRow = rowsHolder.get(stepIndex);
			if (isKeyFired || GameplaySetting.isAutoPlay() || (!isChallengeMode && !GameplaySetting.isNoteWaitingEnabled())) {
				for (Cell cell : currentRow) {
					cell.release();
				}
			}
		} catch (Exception e) {
			print("UNKNOWN EXCEPTION:");
			e.printStackTrace();
		}
	}

	private void playNoteSound(GamePlayNote note) {
		SoundManager.getInstance().playSound(GameplaySetting.getInstrumentId(), note, Constant.DEFAULT_VOLUME);
	}

	private void calculateScorePractice(int stepIndex, float delta) {
		print("calculateScorePractice: delta = " + delta);
		float offsetTime = 1000 * delta / animationSpeed;
		long now = System.currentTimeMillis();
		if (scoreBeginTime != 0 && now > scoreBeginTime) {
			offsetTime = now - scoreBeginTime;
		}
		scoreBeginTime = 0;
		practiceScoreCalculator.scoreWithOffsetTime(stepIndex, offsetTime);
	}

	@Override
	public void onKeyPerformed(int keyIndex, int pointerId) {
		if (needToIgnoreKeyAction(keyIndex)) {
			final NoteOnFire firedNote = findFiredNote(currentStepIndex - 1, keyIndex);
			if (isChallengeMode && firedNote != null) {
				print("calculate score challenge: other cell in same row");
				calculateScoreChallenge(currentStepIndex - 1, firedNote, pointerId);
			}
			Log.i("ttt", "-------- ignored key -------");
			return;
		}
		final NoteOnFire firedNote = findFiredNote(currentStepIndex, keyIndex);
		if (isPlaying() && firedNote != null) {
			float currentRowDistance = calculateDistanceToFirePoint(currentStepIndex);
//			print("currentRowDistance = " + currentRowDistance);
			int stepIndex = currentStepIndex;
			if (GameplaySetting.hasPlayingAssistance() || isChallengeMode) {
//				print("playCurrentRow: currentStepIndex = " + currentStepIndex);
				playCurrentStep(true);
//				print("start focusToNextStep");
				focusToNextStep();
//				print("start destroyPreviousRows");
				destroyPreviousRows();
				if (isChallengeMode) {
					calculateScoreChallenge(stepIndex, firedNote, pointerId);
				} else {
					calculateScorePractice(stepIndex, Math.abs(currentRowDistance));
				}
				if (currentRowDistance > 0) {
					moveRowToFirePoint(stepIndex);
				}
			} else {
				if (firedNote.isLastNote) {
					if (!hasNextStep()) {
						waitForKeyReleaseToFinish();
					}
					currentStepIndex++;
					calculateScorePractice(stepIndex, Math.abs(currentRowDistance));
					if (currentRowDistance > 0) {
						moveRowToFirePoint(stepIndex);
					}
					RubyStep currentStep = songData.steps.get(stepIndex);
					stepProperties.get(stepIndex).onFired(currentStep.notes.indexOf(firedNote.note));
					elapsedTime = currentStep.startTime;

					resumeMidiBackground(stepIndex);
					removeKeyboardHint(stepIndex);
					ArrayList<Cell> currentRow = rowsHolder.get(stepIndex);
					for (Cell cell : currentRow) {
						cell.release();
					}

					focusToNextStep();
					destroyPreviousRows();
				} else {
					keyboardProxy.removeHint(new int[]{keyIndex});
					RubyStep currentStep = songData.steps.get(currentStepIndex);
					stepProperties.get(currentStepIndex).onFired(currentStep.notes.indexOf(firedNote.note));
				}
				playNoteSound(firedNote.note);
			}
			vibrate();
			resumeAnimation();

		} else {
//			print("not playing or firedNote == null: playKeySound: " + keyIndex);
			playKeySound(keyIndex, pointerId);
			scoreOnMiss();
		}
	}

	private boolean needToIgnoreKeyAction(int keyIndex) {
		return lastCellsFiredTimer != null && System.currentTimeMillis() - lastCellsFiredTimer.get(keyIndex, 0L) < 30;
	}

	private void calculateScoreChallenge(int stepIndex, final NoteOnFire firedNote, final int pointerId) {
		float currentRowDistance = calculateDistanceToFirePoint(stepIndex);
		float offsetTime = 1000 * currentRowDistance / animationSpeed;
		int score = challengeScoreCalculator.scoreWithOffsetTime(stepIndex, firedNote.note.id, offsetTime);
		final int[] totalGainScore = {score};
		long duration = offsetTime < 0 ? (long) (firedNote.note.duration + offsetTime) : (long) firedNote.note.duration;
		if (duration < 1) return;

		final String tag = firedNote.note.name;
		challengeScoreCalculator.onGainScore(firedNote.cell, score, offsetTime);
		ValueAnimator animation = ValueAnimator.ofInt(score, (int) firedNote.note.duration / 20);
		print("duration = " + duration);
		animation.setDuration(duration);
		animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				synchronized (scoreLabelValues) {
					totalGainScore[0] = (int) valueAnimator.getAnimatedValue();
					print("increaseGainScore: gainScore = " + totalGainScore[0]);
					scoreLabelValues.put(tag, totalGainScore[0]);
				}
			}
		});
		scoreLabelValues.put(tag, score);
		challengeScoreAnimations.put(pointerId, animation);
		animation.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				synchronized (scoreLabelValues) {
					scoreLabelValues.waitForReady();
					challengeScoreCalculator.finishGainScore(tag, totalGainScore[0]);
					challengeScoreAnimations.delete(pointerId);
					scoreLabelValues.remove(tag);
					scoreLabelValues.makeReady();
				}
			}
		});
		animation.start();
	}

	private void scoreOnMiss() {
		if (isPlaying()) {
			if (isChallengeMode) {
				challengeScoreCalculator.scoreOnMiss(currentStepIndex);
			} else {
				practiceScoreCalculator.scoreOnMiss();
			}
		}
	}

	private float calculateDistanceToFirePoint(int stepIndex) {
		float stepYCoord = stepProperties.get(stepIndex).y;
		float firePoint = KeyboardHeight.currentHeightPixels() + (-animLayerProxy.getY());
		return stepYCoord - firePoint; // can be negative
	}


	public void resumeAnimation() {
		status = STATUS_ANIM_RESUMED;
	}

	private void playKeySound(int keyIndex, int pointerId) {
		KeySoundOnPerformAction.ref().onKeyPerformed(keyIndex, pointerId);
	}

	private NoteOnFire findFiredNote(int stepIndex, int keyIndex) {
		try {
//			print("findFiredNote: stepIndex = " + stepIndex + ", keyIndex = " + keyIndex);
			if (!isPlaying() || stepIndex >= songData.steps.size()) return null;
			RubyStep step = songData.steps.get(stepIndex);
			StepProperty stepProperty = stepProperties.get(stepIndex);
			ArrayList<Cell> cells = rowsHolder.get(stepIndex);
			NoteOnFire firedNote = null;
			boolean isLastNote = true;

			ArrayList<GamePlayNote> notes = step.notes;
			for (int i = 0; i < notes.size(); i++) {
				GamePlayNote note = notes.get(i);
				int noteIndex = NoteUtils.keyIndexOf(note.id);
				NoteProperty noteProperty = stepProperty.get(i);

				if ((noteIndex == keyIndex || (Config.getInstance().isMagicMode && !isChallengeMode))
						&& (!noteProperty.isFired || isChallengeMode)) {
					firedNote = new NoteOnFire();
					firedNote.note = note;
					firedNote.cell = cells != null ? cells.get(i) : null;
				} else if (noteIndex != keyIndex && !noteProperty.isFired) {
					isLastNote = false;
				}
			}
			if (firedNote != null) {
				firedNote.isLastNote = isLastNote;
			}
			return firedNote;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void checkToRenderNextRows() {
		synchronized (rowsHolder) {
			for (int i = renderedStepIndex + 1; i < songData.steps.size(); i++) {
				if (isRowInRenderThreshold(i)) {
					renderedStepIndex = i;
					renderRow(i);
				} else {
					break;
				}
			}
			if (renderedStepIndex < currentStepIndex && hasCurrentStep()) {
				renderRow(currentStepIndex);
				renderedStepIndex = currentStepIndex;
			}
		}
	}

	private boolean isRowInRenderThreshold(int rowIndex) {
		return stepProperties.get(rowIndex).y + animLayerProxy.getY() < renderThreshold;
	}

	private void renderRow(int stepIndex) {
		ArrayList<Cell> row;
		try {
			row = rowsHolder.get(stepIndex);
		} catch (Exception e) {
			row = null;
		}
		if (row != null) {
			for (int i = 0; i < row.size(); i++) {
				row.get(i).recreate();
				createNoteProperties(stepIndex, i);
			}
		} else {
			row = new ArrayList<>();
			RubyStep rubyStep = songData.steps.get(stepIndex);
			rowsHolder.put(stepIndex, row);
			SharedKeyboardParams keyboardParams = keyboardProxy.getSharedKeyboardParams();
			float xScaleFactor = keyboardParams.whiteKeyWidth / Constant.originWhiteKeyWidth;
			for (int i = 0; i < rubyStep.notes.size(); i++) {
				GamePlayNote gamePlayNote = rubyStep.notes.get(i);
//				float width = NoteUtils.isSharpNote(gamePlayNote) ? keyboardParams.blackKeyWidth : keyboardParams.whiteKeyWidth;
				float width = NoteUtils.isSharpNote(gamePlayNote) ?
						PianoKeyHelper.getBlackKeyWidthFromWhiteKeyWidth(Constant.originWhiteKeyWidth) : Constant
						.originWhiteKeyWidth;
				float height = CellDirector.getHeightByDuration(gamePlayNote.duration);
				Cell cell = Cell.make(gamePlayNote, width, height);
				row.add(cell);
				animLayerProxy.insert(cell);
				int noteIndex = NoteUtils.keyIndexOf(gamePlayNote.id);
				float x = keyboardParams.keyPosMapping.get(noteIndex).x / xScaleFactor;
				float y = stepProperties.get(stepIndex).y;
				cell.setPosition(x, y);
				createNoteProperties(stepIndex, i);
			}
		}
	}

	private void createNoteProperties(int stepIndex, int noteIndex) {
		stepProperties.get(stepIndex).put(noteIndex, new NoteProperty());
	}

	private void calculateElapsedTime() {
		if (currentStepIndex == 0) {
			float offsetPixels = animLayerProxy.getY() - KeyboardHeight.currentHeightPixels();
			elapsedTime = -1000f * offsetPixels / animationSpeed;
		} else {
			elapsedTime = songData.steps.get(currentStepIndex).startTime;
		}
		if (songData.backgroundNotes != null) {
			currentBgNoteIndex = 0;
			for (int i = 0; i < songData.backgroundNotes.size(); i++) {
				if (songData.backgroundNotes.get(i).startTime >= elapsedTime) {
					currentBgNoteIndex = i;
					break;
				}
			}
		}
	}

	private void finishSong() {
		print("the song is finished");
		if (isSongFinished()) return;
		status = STATUS_FINISH;
		isWaitingForKeyReleaseToFinish = false;
		isWaitingForBgNoteToFinish = false;
		stopFrameEvent();
		if (onSongFinishListener != null) {
			mainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					GameplaySetting.increasePlayCount();
					onSongFinishListener.onSongFinished();
					clearView();
					songData = null;
				}
			});
		}
	}

	public void clearView() {
		if (isPlaying()) {
			status = 0;
		}
		keyboardProxy.clearHint();
		animLayerProxy.clearChildren();
	}

	private void stopFrameEvent() {
		CCDirector.sharedDirector().removeFrameEventListener(this);
	}

	public boolean isSongFinished() {
		return status == STATUS_FINISH;
	}

	private void destroyPreviousRows() {
		synchronized (rowsHolder) {
			for (int rowIndex = currentStepIndex - 2; rowIndex >= 0; rowIndex--) {
				if (rowsHolder.get(rowIndex) != null) {
					destroyRow(rowIndex);
				} else {
					break;
				}
			}
		}
	}

	private void destroyRow(int rowIndex) {
		print("destroyRow: index = " + rowIndex);
		animLayerProxy.destroyChildren(rowsHolder.get(rowIndex));
		rowsHolder.delete(rowIndex);
	}

	private void removeKeyboardHint(int stepIndex) {
		try {
			removeStepHint(songData.steps.get(stepIndex));
			if (!GameplaySetting.isGuideNotesEnabled() && stepIndex < songData.steps.size() - 1) {
				removeStepHint(songData.steps.get(stepIndex + 1));
			}
		} catch (Exception ignored) {
		}
	}

	private void removeStepHint(RubyStep step) {
		int[] keyIndexes = new int[step.notes.size()];
		for (int i = 0; i < step.notes.size(); i++) {
			int noteIndex = NoteUtils.keyIndexOf(step.notes.get(i).id);
			keyIndexes[i] = noteIndex;
		}
		keyboardProxy.removeHint(keyIndexes);
	}

	private boolean checkRowReachedFirePoint(int rowIndex) {
		return rowIndex < songData.steps.size() &&
				stepProperties.get(rowIndex).y <= KeyboardHeight.currentHeightPixels() + (-animLayerProxy.getY());
	}

	private void focusToNextStep() {
		if (hasCurrentStep()) {
			glowCurrentRow();
			showKeyboardHint();
		} else {
			waitForKeyReleaseToFinish();
		}
	}

	private void showKeyboardHint() {
		print("showKeyboardHint: " + currentStepIndex);
		if (hasCurrentStep()) {
			RubyStep currentStep = songData.steps.get(currentStepIndex);
			keyboardProxy.showHintForCurrentStep(getHintHoldersFromStep(currentStepIndex));
			if (onStepChangeListener != null) {
				long nextPulse = songData.isMidi ? (long) currentStep.startTimeTicks : (long) currentStep.startTime;
				onStepChangeListener.onStepChange(nextPulse);
			}
			if (!GameplaySetting.isGuideNotesEnabled() && hasNextStep()) {
				keyboardProxy.showHintForNextStep(getHintHoldersFromStep(currentStepIndex + 1));
			}
		}
		keyboardProxy.refreshView();
	}

	private void print(String s) {
		Log.d("ttt", s);
	}

	private ArrayList<HintHolder> getHintHoldersFromStep(int index) {
		RubyStep step = songData.steps.get(index);
		ArrayList<HintHolder> hintHolders = new ArrayList<>();
		for (int i = 0; i < step.notes.size(); i++) {
			int noteId = step.notes.get(i).id;
			int noteIndex = NoteUtils.keyIndexOf(noteId);
			int repeatTimes = countContinuousRepeatTimes(index, noteId);
			hintHolders.add(new HintHolder(noteIndex, repeatTimes));
		}
		return hintHolders;
	}

	private int countContinuousRepeatTimes(int stepIndex, int noteId) {
		int count = 1;
		for (int i = stepIndex + 1; i < songData.steps.size(); i++) {
			boolean hasNote = false;
			ArrayList<GamePlayNote> notes = songData.steps.get(i).notes;
			for (GamePlayNote note : notes) {
				if (note.id == noteId) {
					count++;
					hasNote = true;
					break;
				}
			}
			if (!hasNote) {
				break;
			}
		}
		if (stepIndex == currentStepIndex && count == 1)
			count = 0;
		return count;
	}

	private void glowCurrentRow() {
		ArrayList<Cell> currentRow = rowsHolder.get(currentStepIndex);
		if (currentRow != null) {
			for (Cell cell : currentRow) {
				cell.glow();
			}
		} else {
			Log.e("ttt", "FATAL EXCEPTION: glowCurrentRow: currentRow must be never null");
		}
	}

	private void waitForKeyReleaseToFinish() {
		isWaitingForKeyReleaseToFinish = true;
	}

	private boolean hasCurrentStep() {
		return songData != null && currentStepIndex < songData.steps.size();
	}

	private void moveRowToFirePoint(int rowIndex) {
		animLayerProxy.setY(-(stepProperties.get(rowIndex).y - KeyboardHeight.currentHeightPixels()));
	}

	private boolean hasNextStep() {
		return currentStepIndex < songData.steps.size() - 1;
	}

	public boolean hasSongData() {
		return songData != null;
	}

	private void startFrameEvent() {
		CCDirector.sharedDirector().addFrameEventListener(this);
	}

	public void pauseAnimation() {
		status = STATUS_ANIM_PAUSED;
	}

	private void destroyRowsOverRenderingThreshold() {
		synchronized (rowsHolder) {
			for (int i = renderedStepIndex; i > 0; i--) {
				if (!isRowInRenderThreshold(i)) {
					destroyRow(i);
					renderedStepIndex = i - 1;
				}
			}
		}
	}

	private boolean hasPreviousStep() {
		return currentStepIndex > 0;
	}

	private void recreateCurrentRow() {
		for (Cell cell : rowsHolder.get(currentStepIndex)) {
			cell.recreate();
		}
	}

	private void focusToPreviousStep() {
		currentStepIndex--;
		synchronized (rowsHolder) {
			renderRow(currentStepIndex);
		}
		moveRowToFirePoint(currentStepIndex);
	}

	public void startGameplay(int startIndex) {
		print("startGameplay: startIndex = " + startIndex);
		SoundManager.getInstance().refresh();
		setupScoreCalculator();
		clearView();
		if (needReCaculate)
			calculateAnimSpeed();
		startWithIndex(startIndex);
		needReCaculate = false;
		if (listener != null)
			listener.onStarted();
	}

	private void setupScoreCalculator() {
		if (isChallengeMode) {
			challengeScoreCalculator.reset();
			challengeScoreCalculator.setTotalNotes(songData.numNotes);
		} else {
			practiceScoreCalculator = PracticeScoreCalculator.getInstance();
			practiceScoreCalculator.reset();
			practiceScoreCalculator.setTotalNotes(songData.numNotes);
		}
	}

	public void calculateAnimSpeed() {
		animationSpeed = GameplaySetting.getAnimSpeed();
		print("animationSpeed = " + animationSpeed + " (pixels/s)");
	}

	private void startWithIndex(int startStepIndex) {
		if (needReCaculate)
			calculateYCoordinateForAllSteps();
		currentStepIndex = startStepIndex;
		renderedStepIndex = startStepIndex - 1;
		scoreBeginTime = 0;
		status = STATUS_ANIM_RESUMED;
		isWaitingForBgNoteToFinish = false;
		isWaitingForKeyReleaseToFinish = false;
		rowsHolder.clear();
		lastCellsFiredTimer = new SparseArray<>();
		if (isChallengeMode) {
			challengeScoreAnimations = new SparseArray<>();
		}
		moveAnimLayerToStartPoint();
		checkToRenderNextRows();
		glowCurrentRow();
		showFirstHint();
		calculateElapsedTime();
		if (startStepIndex > 0 || songData.steps.get(startStepIndex).startTime < 50)
			pauseAnimation();
		startFrameEvent();
		resumeMidiBackground(currentStepIndex);
		print("startWithIndex: " + startStepIndex + " - " + animLayerProxy.getY() + " - " + KeyboardHeight.currentHeightPixels
				());
	}

	private void showFirstHint() {
		if (hasCurrentStep()) {
			RubyStep currentStep = songData.steps.get(currentStepIndex);
			keyboardProxy.showHintForFirstStep(getHintHoldersFromStep(currentStepIndex), getKeyIndexesFromFirstSteps());
			if (onStepChangeListener != null) {
				onStepChangeListener.onStepChange((long) currentStep.startTimeTicks);
			}

			if (!GameplaySetting.isGuideNotesEnabled() && hasNextStep()) {
				keyboardProxy.showHintForNextStep(getHintHoldersFromStep(currentStepIndex + 1));
			}
		}
		keyboardProxy.refreshView();
	}

	public ArrayList<Integer> getKeyIndexesFromFirstSteps() {
		ArrayList<Integer> keyIndexes = new ArrayList<>();
		for (int i = currentStepIndex; i < currentStepIndex + NUM_FIRST_STEPS_TO_HINT; i++) {
			if (i < songData.steps.size()) {
				RubyStep step = songData.steps.get(i);
				for (GamePlayNote note : step.notes) {
					int keyIndex = NoteUtils.keyIndexOf(note);
					if (keyIndexes.indexOf(keyIndex) < 0) {
						keyIndexes.add(keyIndex);
					}
				}
			} else {
				break;
			}
		}
		return keyIndexes;
	}

	private void moveAnimLayerToStartPoint() {
		if (currentStepIndex == 0) {
			if (isChallengeMode) {
				animLayerProxy.setY(Config.getInstance().winHeight);
			} else {
				print("setY = currentHeight");
				animLayerProxy.setY(KeyboardHeight.currentHeightPixels());
			}
		} else {
			print("startPoint: " + currentStepIndex);
			moveRowToFirePoint(currentStepIndex);
		}
	}

	private void calculateYCoordinateForAllSteps() {
		stepProperties = new SparseArray<>();
		float speedInPixelsOverMillis = Config.getInstance().speed * 0.001f;
		print("speedInPixelsOverMillis = " + speedInPixelsOverMillis);
		for (int i = 0; i < songData.steps.size(); i++) {
			StepProperty stepProperty = new StepProperty();
			stepProperties.put(i, stepProperty);
			float startTimeMillis = songData.steps.get(i).startTime;
			stepProperty.y = startTimeMillis * speedInPixelsOverMillis;
		}
	}

	private void resumeMidiBackground(int stepIndex) {
		if (songData.isMidi) {
//			BackgroundMusicHandler.getInstance().seekTo((int) elapsedTime);
			int currentTicks = songData.steps.get(stepIndex).startTimeTicks;
			int nextTicks = hasNextStep() ? songData.steps.get(stepIndex + 1).startTimeTicks : currentTicks;
			print("currentTicks = " + currentTicks + ", nextTicks = " + nextTicks);
		}
	}

	public void replay() {
		if (songData != null && songData.steps != null) {
			startGameplay(0);
		}
	}

	public void cleanUp() {
		print("GameplayController: cleanUp: ");
		stopFrameEvent();
		DataTransporter.free();
		songData = null;
//		BackgroundMusicHandler.getInstance().release();
	}

	public void setKeyboardProxy(KeyboardProxy keyboardProxy) {
		this.keyboardProxy = keyboardProxy;
		keyboardProxy.setSoundOnTouch(this);
	}

	public void setAnimLayerProxy(AnimationLayerProxy animLayerProxy) {
		this.animLayerProxy = animLayerProxy;
	}

	@Override
	public void onHorizScale() {
//		if (!hasSongData()) return;
//		SharedKeyboardParams keyboardParams = keyboardProxy.getSharedKeyboardParams();
//		for (int i = 0; i < rowsHolder.size(); i++) {
//			int rowIndex = rowsHolder.keyAt(i);
//			ArrayList<Cell> row = rowsHolder.get(rowIndex);
//			for (Cell cell : row) {
//				if (NoteUtils.isSharpNote(cell.getNote())) {
//					cell.setWidth(keyboardParams.blackKeyWidth);
//				} else {
//					cell.setWidth(keyboardParams.whiteKeyWidth);
//				}
//				int keyIndex = NoteUtils.keyIndexOf(cell.getNote());
//				float x = keyboardParams.keyPosMapping.get(keyIndex).x;
//				float y = cell.getPositionRef().y;
//				cell.setPosition(x, y);
//			}
//		}
	}

	@Override
	public void onVertScale() {
		if (isPlaying() && hasCurrentStep() && stepProperties != null) {
			moveRowToFirePoint(currentStepIndex);
			pauseAnimation();
		}
	}

	public boolean isPlaying() {
		return hasSongData() && (isAnimationResumed() || isAnimationPaused());
	}

	private boolean isAnimationResumed() {
		return status == STATUS_ANIM_RESUMED;
	}

	public boolean isAnimationPaused() {
		return status == STATUS_ANIM_PAUSED;
	}

	public void showGuideNotes() {
		if (!animLayerProxy.getVisible()) {
			animLayerProxy.setVisible(true);
			keyboardProxy.refreshView();
			GameplaySetting.enableGuideNotes();
		}
	}

	public void hideGuideNotes() {
		if (animLayerProxy.getVisible()) {
			animLayerProxy.setVisible(false);
			if (isPlaying()) {
				showKeyboardHint();
			}
			GameplaySetting.disableGuideNotes();
		}
	}

	public void setOnStepChangeListener(OnStepChangeListener onStepChangeListener) {
		this.onStepChangeListener = onStepChangeListener;
	}

	@Override
	public void onKeyReleased(int keyIndex, int pointerId) {
		KeySoundOnPerformAction.ref().onKeyReleased(keyIndex, pointerId);
		if (isPlaying()) {
			if (isChallengeMode) {
				if (challengeScoreAnimations.get(pointerId) != null) {
					challengeScoreAnimations.get(pointerId).cancel();
				}
			}

			if (isWaitingForKeyReleaseToFinish) {
				finishSong();
			}
		}
	}


	public void vibrate() {
		if (Config.getInstance().isVibrate)
			vibrator.vibrate(Config.getInstance().vibrateTime);
	}

	public void onPause() {
		if (!isSongFinished()) {
			pauseAnimation();
		}
	}

	public void setOnSongFinishListener(OnSongFinishListener listener) {
		this.onSongFinishListener = listener;
	}

	public void setChallengeScoreCalculator(ChallengeScoreCalculator challengeScoreCalculator) {
		this.challengeScoreCalculator = challengeScoreCalculator;
	}

	public void removePressState() {
		keyboardProxy.removePressState();
	}

	public int getNoteIndexForStep(int stepIndex) {
		RubyStep step = songData.steps.get(stepIndex);
		for (int i = 0; i < step.notes.size(); i++) {
			int noteId = step.notes.get(i).id;
			return NoteUtils.keyIndexOf(noteId);
		}
		return -1;
	}

	public interface GamePlayControllerListener {
		void onStarted();

		void onPause();
	}

	private class StepProperty extends SparseArray<NoteProperty> {
		float y;

		void onFired() {
			for (int i = 0; i < this.size(); i++) {
				int key = this.keyAt(i);
				NoteProperty noteProperty = get(key);
				noteProperty.isFired = true;
			}
		}

		void onFired(int key) {
			NoteProperty noteProperty = get(key);
			noteProperty.isFired = true;
		}
	}

	private class NoteProperty {
		boolean isFired = false;
	}

	private class NoteOnFire {
		GamePlayNote note;
		boolean isLastNote;
		Cell cell;
	}

	private class ScoreLabelValues extends HashMap<String, Integer> {
		boolean isReady = true;

		void makeReady() {
			isReady = true;
			notifyAll();
		}

		void waitForReady() {
			while (!isReady) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			isReady = false;
		}
	}
}
