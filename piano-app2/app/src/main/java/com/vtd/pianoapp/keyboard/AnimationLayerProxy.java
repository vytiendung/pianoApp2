package com.vtd.pianoapp.keyboard;

import com.vtd.pianoapp.practice.Cell;

import java.util.ArrayList;

public interface AnimationLayerProxy {
	void clearChildren();

	float getY();

	void insert(Cell cell);

	void setY(float y);

	void destroyChildren(ArrayList<Cell> children);

	void setVisible(boolean isVisible);

	boolean getVisible();
}
