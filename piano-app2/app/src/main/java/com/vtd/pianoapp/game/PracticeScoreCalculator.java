package com.vtd.pianoapp.game;


public class PracticeScoreCalculator {
	private static final int ONTIME_THRESHOLD = 500;

	private static final ScoreTimingRule[] scoreTimingRules = new ScoreTimingRule[]{
			new ScoreTimingRule(200, 5),
			new ScoreTimingRule(400, 4),
			new ScoreTimingRule(600, 3),
			new ScoreTimingRule(800, 2),
			new ScoreTimingRule(-1, 1)
	};

	private static PracticeScoreCalculator instance;
	private int missCount;
	private int totalScore;
	private float totalOnTimeAccuracy;
	private int hitCount;
	private int totalNotes = 1;
	private int currentStepIndex;

	public static PracticeScoreCalculator getInstance() {
		if (instance == null) {
			instance = new PracticeScoreCalculator();
		}
		return instance;
	}

	private PracticeScoreCalculator() {
	}

	public void reset() {
		missCount = 0;
		totalScore = 0;
		totalOnTimeAccuracy = 0;
		hitCount = 0;
		currentStepIndex = 0;
	}

	public void scoreWithOffsetTime(int stepIndex, float offsetTime) {
		if (currentStepIndex <= stepIndex) {
			currentStepIndex = stepIndex;
			int scoreGain = scoreTimingRules[4].score;
			for (ScoreTimingRule rule : scoreTimingRules) {
				if (offsetTime < rule.maxOffsetTime) {
					scoreGain = rule.score;
					break;
				}
			}
			totalScore += scoreGain;
			calculateOnTime(offsetTime);
		}
	}

	private void calculateOnTime(float offsetTime) {
		totalOnTimeAccuracy += Math.max(1 - offsetTime / ONTIME_THRESHOLD, 0f);
		hitCount++;
	}

	public void scoreOnMiss() {
		missCount++;
	}

	public float getOnTimePercent() {
		if (hitCount == 0) return 0f;
		return (float) Math.floor(totalOnTimeAccuracy * 1000 / hitCount) / 10f;
	}

	public int getMissCount() {
		return missCount;
	}

	public int getTotalScore() {
		return totalScore;
	}

	public int getHitCount() {
		return hitCount;
	}

	public int getRewardRuby() {
		int ruby = 15;

		if (totalScore < 250) {
			ruby = 4;
		} else if (totalScore < 500) {
			ruby = 7;
		} else if (totalScore < 700) {
			ruby = 10;
		}
		return ruby;
	}

	public int getTotalNotes() {
		return totalNotes;
	}

	public void setTotalNotes(int totalNotes) {
		this.totalNotes = totalNotes;
	}

	private static class ScoreTimingRule {
		int maxOffsetTime;
		int score;

		ScoreTimingRule(int maxOffsetTime, int score) {
			this.maxOffsetTime = maxOffsetTime;
			this.score = score;
		}
	}

}
