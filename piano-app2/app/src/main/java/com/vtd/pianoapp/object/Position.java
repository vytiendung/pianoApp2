package com.vtd.pianoapp.object;

public class Position {
	public float x;
	public float y;
	
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	
	public Position(){
		
	}
	
	public Position(float x, float y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	
	
}
