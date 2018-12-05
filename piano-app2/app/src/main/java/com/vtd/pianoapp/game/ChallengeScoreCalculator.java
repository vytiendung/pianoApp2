package com.vtd.pianoapp.game;


import com.vtd.pianoapp.practice.Cell;

import java.util.ArrayList;

public class ChallengeScoreCalculator {
	public static final int MAX_DELAY_TIME_TO_GET_ONTIME = 500;
	private static final int MAX_DELAY_TIME_GET_ONTIME_PERFECT = 100;
	private final ScoreOnBeganTouchRule[] scoreOnBeganTouchRules = new ScoreOnBeganTouchRule[]{
			new ScoreOnBeganTouchRule(100, 5, EnumAccuracy.PERECT),
			new ScoreOnBeganTouchRule(200, 4, EnumAccuracy.GREAT),
			new ScoreOnBeganTouchRule(300, 3, EnumAccuracy.GOOD),
			new ScoreOnBeganTouchRule(400, 2, EnumAccuracy.NOT_BAD),
			new ScoreOnBeganTouchRule(-1, 1, EnumAccuracy.BAD)
	};

	private final ComboRule[] comboRules = new ComboRule[]{
			new ComboRule(60, 4),
			new ComboRule(30, 3),
			new ComboRule(10, 2),
			new ComboRule(0, 1)
	};

	private final RewardRubyRule[] rewardRubyRules = new RewardRubyRule[]{
			new RewardRubyRule(2500, 5),
			new RewardRubyRule(2000, 4),
			new RewardRubyRule(1500, 3),
			new RewardRubyRule(1000, 2),
			new RewardRubyRule(500, 1)
	};

	private final int maxHoldingScore = 5;

	private int currentScore, totalNotes, wrongNoteCount;
	private int comboGain, comboChain;
	private int lastMissedStepIndex = -1;

	private boolean isComboGainEnabled;
	private int currentStepIndex;
	private ArrayList<Integer> playedNoteId;
	private float timeOffset;
	private ScoreListener listener;
	private int numNoteHit, numNotePlayed;
	private float totalOntime;

	public ChallengeScoreCalculator(ScoreListener listener) {
		this.listener = listener;
	}

	public void reset() {
		currentScore = 0;
		wrongNoteCount = 0;
		comboGain = 1;
		comboChain = 0;
		lastMissedStepIndex = -1;
		currentStepIndex = -1;
		playedNoteId = new ArrayList<>();
		timeOffset = 0;
		numNoteHit = 0;
		numNotePlayed = 0;
		totalOntime = 0;
	}

	public int scoreWithOffsetTime(int stepIndex, int noteId, float timeOffset) {
		if (timeOffset < 0)
			timeOffset *= -1;
		boolean willCalculateScore = stepIndex > currentStepIndex || playedNoteId.indexOf(noteId) == -1;
		if (stepIndex > currentStepIndex) {
			playedNoteId.clear();
			playedNoteId.add(noteId);
		} else {
			playedNoteId.add(noteId);
		}
		if (willCalculateScore) {
			this.currentStepIndex = stepIndex;
			this.timeOffset = timeOffset;
			numNoteHit++;
			numNotePlayed++;
			if (stepIndex == lastMissedStepIndex) {
				wrongNoteCount++;
			}
			int gainScore = comboGain * scoreOnBeganTouchRules[4].score;
			for (ScoreOnBeganTouchRule scoreOnBeganTouchRule : scoreOnBeganTouchRules) {
				if (timeOffset < scoreOnBeganTouchRule.maxOffsetTime) {
					gainScore = comboGain * scoreOnBeganTouchRule.score;
					break;
				}
			}
//			finishGainScore(gainScore);
			if (isComboGainEnabled) {
				increaseComboChain();
			}
			calOntime(timeOffset);
			return gainScore;
		}
		return 0;
	}

//	public int onBeganTouchOtherNoteInCurrentStep(int noteId) {
//		if (playedNoteId.indexOf(noteId) == -1) {
//			playedNoteId.add(noteId);
//			numNoteHit++;
//			int gainScore = scoreOnBeganTouchRules[4].score;
//			for (ScoreOnBeganTouchRule scoreOnBeganTouchRule : scoreOnBeganTouchRules) {
//				if (timeOffset < scoreOnBeganTouchRule.maxOffsetTime) {
//					gainScore += comboGain * scoreOnBeganTouchRule.score;
//					break;
//				}
//			}
//			finishGainScore(gainScore);
//			if (isComboGainEnabled) {
//				increaseComboChain();
//			}
//			calOntime(timeOffset);
//			return gainScore;
//		}
//		return 0;
//	}

	public void calOntime(float delayTime) {
		if (delayTime < MAX_DELAY_TIME_GET_ONTIME_PERFECT) {
			totalOntime += 100;
		} else if (delayTime <= MAX_DELAY_TIME_TO_GET_ONTIME) {
			totalOntime += 100 * (1 - (delayTime - MAX_DELAY_TIME_GET_ONTIME_PERFECT) /
					(MAX_DELAY_TIME_TO_GET_ONTIME - MAX_DELAY_TIME_GET_ONTIME_PERFECT));
		}
	}

	private void increaseComboChain() {
		comboChain++;
		for (ComboRule comboRule : comboRules) {
			if (comboChain >= comboRule.chain) {
				comboGain = comboRule.gain;
				break;
			}
		}
		notifyOnComboUpdated();
	}

	private void notifyOnComboUpdated() {
		if (listener != null) {
			listener.onComboUpdated(comboChain, comboGain);
		}
	}

	public void scoreOnMiss(int currentStepIndex) {
		onMiss();
		if (currentStepIndex != lastMissedStepIndex) {
			lastMissedStepIndex = currentStepIndex;
		}
	}

	private void onMiss() {
		if (isComboGainEnabled) {
			resetComboChain();
		}
	}

	private void resetComboChain() {
		comboChain = 0;
		comboGain = 1;
		notifyOnComboUpdated();
	}

	public void onIgnoreNote() {
		onMiss();
		numNotePlayed++;
	}

	public int getScoreHoldTime(float holdingTimePercent) {
		return (int) (comboGain * maxHoldingScore * Math.min(holdingTimePercent, 1));
	}

	public void onNoteEnded(float holdingTimePercent) {
//		finishGainScore((int) (comboGain * maxHoldingScore * Math.min(holdingTimePercent, 1)));

	}

	public void finishGainScore(String tag, int amount) {
		currentScore += amount;
		if (listener != null) {
			listener.onScoreUpdated(currentScore);
			listener.removeScoreLabel(tag);
		}
	}

	public int getFinalScore() {
		return currentScore;
//		return (int) (currentScore * (1 - (float)(wrongNoteCount + totalNotes - numNoteHit)/ totalNotes));
	}

	public int getHitCount() {
		return numNoteHit;
	}

	public int getOnTime(int notePlayed) {
		return Math.round(totalOntime / notePlayed);
	}

	public int getRubyReward() {
		int reward = 1;
		for (RewardRubyRule mark : rewardRubyRules) {
			if (getCurrentScore() >= mark.score) {
				reward = mark.reward;
				break;
			}
		}
		return reward;
	}

	public int getCurrentScore() {
		return currentScore;
	}

	public int getWrongNoteCount() {
		return wrongNoteCount;
	}

	public void enableComboGain() {
		this.isComboGainEnabled = true;
	}

	public void disableComboGain() {
		this.isComboGainEnabled = false;
	}

	public void onGainScore(Cell cell, int score, float offsetTime) {
		if (listener != null) {
			listener.onGainScore(cell, score, getAccuracy(offsetTime));
		}
	}

	public EnumAccuracy getAccuracy(float delayTime) {
		if (delayTime < 0)
			delayTime *= -1;
		EnumAccuracy accuracy = scoreOnBeganTouchRules[4].accuracy;
		for (ScoreOnBeganTouchRule scoreOnBeganTouchRule : scoreOnBeganTouchRules) {
			if (delayTime < scoreOnBeganTouchRule.maxOffsetTime) {
				accuracy = scoreOnBeganTouchRule.accuracy;
				break;
			}
		}
		return accuracy;
	}

	public void increaseGainScore(String tag, int gainScore) {
		if (listener != null) {
			listener.increaseGainScore(tag, gainScore);
		}
	}

	public int getTotalNotes() {
		return totalNotes;
	}

	public void setTotalNotes(int numNotes) {
		totalNotes = numNotes;
	}

	public int getOnTimePercent() {
		return Math.round(totalOntime / numNotePlayed);
	}

	public enum EnumAccuracy {
		BAD,
		NOT_BAD,
		GOOD,
		GREAT,
		PERECT
	}

	public interface ScoreListener {
		void onScoreUpdated(int score);

		void onComboUpdated(int comboChain, int comboGain);

		void onGainScore(Cell cell, int score, EnumAccuracy accuracy);

		void increaseGainScore(String tag, int gainScore);

		void removeScoreLabel(String tag);
	}

	private class ScoreOnBeganTouchRule {
		int maxOffsetTime;
		int score;
		EnumAccuracy accuracy;

		ScoreOnBeganTouchRule(int maxOffsetTime, int score, EnumAccuracy accuracy) {
			this.maxOffsetTime = maxOffsetTime;
			this.score = score;
			this.accuracy = accuracy;
		}
	}

	private class ComboRule {
		int gain, chain;

		ComboRule(int chain, int gain) {
			this.chain = chain;
			this.gain = gain;
		}
	}

	private class RewardRubyRule {
		int score;
		int reward;

		RewardRubyRule(int score, int reward) {
			this.score = score;
			this.reward = reward;
		}
	}
}
