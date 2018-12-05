package com.vtd.pianoapp.songobject;

public class Note{
	public static final float NO_EXTEND = -1111;
	public static final float NO_VELOCITY = -1111;
	private String name;
	private float tickPlusDuration;
	private float length;
	private boolean isChord;
	private boolean isTie;
	private float extendedLength = NO_EXTEND;
	private float velocity = NO_VELOCITY;

	private boolean isForPrimary;
	private int index;

	private int fingerIndex;
	public int indexZOrder;

	private int start = -1;


	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public boolean isForPrimary() {
		return isForPrimary;
	}

	public void setForPrimary(boolean isForPrimary) {
		this.isForPrimary = isForPrimary;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getTickPlusDuration() {
		return tickPlusDuration;
	}

	public void setTickPlusDuration(float tickPlusDuration) {
		this.tickPlusDuration = tickPlusDuration;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

	public boolean isChord() {
		return isChord;
	}

	public void setChord(boolean isChord) {
		this.isChord = isChord;
	}

	public boolean isTie() {
		return isTie;
	}

	public void setTie(boolean isTie) {
		this.isTie = isTie;
	}

	public Note(String name, float tickPlusDuration, float length, boolean isChord,
	            boolean isTie) {
		this(name, tickPlusDuration, length, isChord, isTie, true);
	}


	public Note(String name, float tickPlusDuration, float length, boolean isChord,
	            boolean isTie, boolean isPrimary) {
		this(name, tickPlusDuration, length, isChord, isTie, isPrimary, 0);
	}

	public Note(String name, float tickPlusDuration, float length, boolean isChord,
	            boolean isTie, boolean isPrimary, int fingerIndex) {
		super();
		this.name = name;
		this.tickPlusDuration = tickPlusDuration;
		this.length = length;
		this.isChord = isChord;
		this.isTie = isTie;
		this.isForPrimary = isPrimary;
		this.fingerIndex = fingerIndex;
		this.indexZOrder = 0;
	}

	public int getFingerIndex(){
		return fingerIndex;
	}

	public void setFingerIndex(int fingerIndex) {
		this.fingerIndex = fingerIndex;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public float getExtendedLength() {
		return extendedLength;
	}

	public void setExtendedLength(float extendedLength) {
		this.extendedLength = extendedLength;
	}

	public float getVelocity() {
		return velocity;
	}

	public void setVelocity(float velocity) {
		this.velocity = velocity;
	}
}
