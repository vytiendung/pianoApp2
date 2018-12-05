package com.vtd.pianoapp.gameUtils;

/**
 * Created by Dinh on 27/4/2016.
 */
public class ResultScoreHelper {
	private int hitCount;
	private int missCount;
	private int totalHit;
	private int totalScore;

	public int getHitCount() {
		return hitCount;
	}

	public int getTotalHit() {
		return totalHit;
	}

	public int getTotalScore() {
		return totalScore;
	}

	public int getMissCount() {
		return missCount;
	}

	public ResultScoreHelper() {
		hitCount = 0;
		missCount = 0;
		totalHit = 0;
		totalScore = 0;
	}

	public void resetAll(){
		hitCount = 0;
		missCount = 0;
		totalHit = 0;
		totalScore = 0;
	}

	public void plusTotalScoreByOne(){
		totalScore++;
	}

	public void resetTotalScore(){
		totalScore = 0;
	}

	public void calculateScoreWithTime(float delta, long waitTime) {
		totalScore++;
		totalHit++;
		if (waitTime > 0) {
			long deltaTime = System.currentTimeMillis() - waitTime;
			if (deltaTime < 50) {
				hitCount++;
			} else if (deltaTime < 100) {
				hitCount++;
			} else if (deltaTime < 200) {
				missCount++;
			} else {
				missCount++;
			}
		} else {
			if (delta < 20) {
				hitCount++;
			} else if (delta < 35) {
				hitCount++;
			} else if (delta < 50) {
				missCount++;
			} else {
				missCount++;
			}
		}
	}

	public void calculateScoreWhenHit(){
		totalScore++;
		totalHit++;
		hitCount++;
	}

	public void calculateScoreWhenMiss(){
		totalScore++;
		totalHit++;
		missCount++;
	}
}
