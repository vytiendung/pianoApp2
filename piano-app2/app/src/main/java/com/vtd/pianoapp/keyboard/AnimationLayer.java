package com.vtd.pianoapp.keyboard;

import com.vtd.pianoapp.practice.Cell;
import org.cocos2d.layers.CCLayer;

import java.util.ArrayList;


public class AnimationLayer extends CCLayer implements AnimationLayerProxy {

	@Override
	public void clearChildren() {
		removeAllChildren(true);
	}

	@Override
	public float getY() {
		return getPositionRef().y;
	}

	@Override
	public void insert(Cell cell) {
		addChild(cell);
	}

	@Override
	public void setY(float y) {
		setPosition(getPositionRef().x, y);
	}

	@Override
	public void destroyChildren(ArrayList<Cell> children) {
		for (Cell child : children) {
			removeChild(child, true);
		}
	}

	@Override
	public boolean getVisible() {
		return super.getVisible();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
	}
}
