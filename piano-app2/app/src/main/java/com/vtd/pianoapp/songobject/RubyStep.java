package com.vtd.pianoapp.songobject;


import java.util.ArrayList;

public class RubyStep {
	public ArrayList<GamePlayNote> notes;
	public float startTime, duration;
	public int index;
	public int startTimeTicks;
	public RubyStep() {
	}
	public RubyStep(RubyStep step) {
		this.notes = (ArrayList<GamePlayNote>) step.notes.clone();
		this.startTime = step.startTime;
		this.duration = step.duration;
		this.index = step.index;
		this.startTimeTicks = step.startTimeTicks;
	}

}
