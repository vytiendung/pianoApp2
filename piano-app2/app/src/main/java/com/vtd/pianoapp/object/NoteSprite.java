package com.vtd.pianoapp.object;

import com.vtd.pianoapp.common.Config;
import com.vtd.pianoapp.common.Constant;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.nodes.*;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor3B;

import static com.vtd.pianoapp.common.Constant.SMALL;


public class NoteSprite extends CCLayer {
	private final static int maxKeyPerScreen = Constant.MAX_KEY_NUM;
	private static final String TAG = "NoteSprite";
	private static Config config = Config.getInstance();
	private final static float maxWidth = Math.round(config.winSize.width / maxKeyPerScreen) * 50 / 100;
	private static float scl = ((float) config.winSize.width / 12) / 4 / CCLabel.makeLabel("AA", "DroidSans", 18)
			.getContentSize().width; // use 12 key as standard to calculate label+ rect size
	private static float height_threshold = 0;
	private static float baseScale = 0;
	public int delta_height_check = 30;
	public int delta_y_for_glow = 0;
	boolean isBigSize = true;
	CCLabel fingerNumber;
	private CCSprite sprite;
	private CCNode fingerGroup;
	private float posY = 0;
	private float deltaY = 0;

	public static NoteSprite getNoteSpriteByFrameName(CCSpriteFrameCache spriteFrameCache, String frameName) {
		if (height_threshold == 0) {
			findHeightThreshold();
		}
		NoteSprite noteSprite = new NoteSprite();
		CCSpriteFrame sf = spriteFrameCache.spriteFrameByName(frameName);
		CCSprite sprite = CCSprite.sprite(sf);
		sprite.setStringTag(frameName);
		noteSprite.addChild(sprite);
		noteSprite.sprite = sprite;
		if (frameName.contains("_tiny")) {
			noteSprite.isBigSize = false;
		}
		if (frameName.contains("_glow")) {
			noteSprite.delta_y_for_glow = (config.deviceType == SMALL ? 15 : 22) * 2;
			noteSprite.delta_height_check = 70;
		}
		return noteSprite;
	}

	private static void findHeightThreshold() {
		CCSprite temp = CCSprite.sprite(config.imgPath + "finger_bg.png");
		baseScale = maxWidth / temp.getContentSizeRef().width;
		height_threshold = temp.getContentSizeRef().height * baseScale;
	}

	public void createFingerNumber(int fingerIndex, float scaleX, float scaleY) {
		if (fingerIndex != 0) {
			if (fingerGroup != null) {
				updateFingerIndexLabel(fingerIndex);
			} else {
				createFingerIndexGroup(fingerIndex, scaleX, scaleY);
			}
		} else {
			hideFingerIndexIfNeed();
		}
	}

	private void updateFingerIndexLabel(int fingerIndex) {
		fingerGroup.setVisible(true);
		fingerNumber.setString(((Integer) fingerIndex).toString());
	}

	private void createFingerIndexGroup(int fingerIndex, float scaleX, float scaleY) {
		if (isBigSize) {
			if (checkCanUseBigSize()) {
				createBigFingerNumber(fingerIndex);
			} else {
				createSmallFingerNumber(fingerIndex);
			}
		} else {
			createSmallFingerNumber(fingerIndex);
		}
		updateFingerNumberScaleX(scaleX);
		updateFingerNumberScaleY(scaleY);
	}

	private boolean checkCanUseBigSize() {
		boolean canFitHeight = height_threshold * 1.0 + delta_y_for_glow * sprite.getScaleY() < sprite.getContentSize()
				.height * sprite.getScaleY();
		boolean canFitWidth = maxWidth < sprite.getContentSize().width * sprite.getScaleX();
		return canFitHeight && canFitWidth;
	}

	private void createBigFingerNumber(int fingerIndex) {
		fingerGroup = CCNode.node();
		CCSprite bkg = CCSprite.sprite(config.imgPath + "finger_bg.png");
		fingerNumber = CCLabel.makeLabel(((Integer) fingerIndex).toString(), "DroidSans", 18);
		float scaleBkg = maxWidth / bkg.getContentSizeRef().width;
		fingerNumber.setScale(scl);
		bkg.setScale(scaleBkg);
		fingerGroup.addChild(bkg);
		fingerGroup.addChild(fingerNumber);
		float height = bkg.getContentSizeRef().height;
		posY = 0;
		if (checkCanPutFingerWithMaximumGap(scaleBkg, height)) {
			deltaY = -height * scaleBkg * (float) 0.8 - delta_y_for_glow / 2 * sprite.getScaleY();
		} else if (checkCanPutFingerWithGap(scaleBkg, height)) {
			deltaY = -height * scaleBkg * (float) 0.65 - delta_y_for_glow / 2 * sprite.getScaleY();
		} else {
			deltaY = -height * scaleBkg * (float) 0.5 - delta_y_for_glow / 2 * sprite.getScaleY();
		}
		addChild(fingerGroup);
		fingerGroup.setPosition(0, posY - deltaY);
	}

	private boolean checkCanPutFingerWithGap(float scaleBkg, float height) {
		return (sprite.getContentSize().height - delta_y_for_glow) * sprite.getScaleY() > height * scaleBkg * 1.3;
	}

	private boolean checkCanPutFingerWithMaximumGap(float scaleBkg, float height) {
		return (sprite.getContentSize().height - delta_y_for_glow) * sprite.getScaleY() > height * scaleBkg * 1.6;
	}

	private void createSmallFingerNumber(int fingerIndex) {
		fingerGroup = CCNode.node();
		CCSprite bkg = CCSprite.sprite(config.imgPath + "finger_bubble.png");
		fingerNumber = CCLabel.makeLabel(((Integer) fingerIndex).toString(), "DroidSans", 18);
		float scaleBkg = maxWidth / bkg.getContentSizeRef().width;
		fingerNumber.setColor(ccColor3B.ccBLACK);
		fingerNumber.setScale(scl);
		bkg.setScale(scaleBkg * 7 / 5);
		fingerGroup.addChild(bkg);
		fingerGroup.addChild(fingerNumber);
		fingerNumber.setPosition(0, 10 * scaleBkg);
		posY = sprite.getContentSizeRef().height * sprite.getScaleY();
		deltaY = delta_y_for_glow / 3 * 2 * scaleBkg;
		addChild(fingerGroup);
		fingerGroup.setPosition(0, posY - deltaY);
	}

	public void updateFingerNumberScaleX(float parentScaleX) {
		if (fingerGroup != null) {
			fingerGroup.setScaleX(1 / parentScaleX);
		}
	}

	public void updateFingerNumberScaleY(float parentScaleY) {
		if (fingerGroup != null) {
			fingerGroup.setScaleY(1 / parentScaleY);
			fingerGroup.setPosition(0, posY - deltaY / parentScaleY);
		}
	}

	private void hideFingerIndexIfNeed() {
		if (fingerGroup != null) {
			fingerGroup.setVisible(false);
		}
	}

	public void setColor(ccColor3B color3B) {
		sprite.setColor(color3B);
	}

	@Override
	public void setScale(float scaleX, float scaleY) {
		sprite.setScale(scaleX, scaleY);
	}

	@Override
	public CGSize getContentSizeRef() {
		return sprite.getContentSizeRef();
	}

	@Override
	public float getScaleX() {
		return sprite.getScaleX();
	}

	@Override
	public void setScaleX(float sx) {
		sprite.setScaleX(sx);
	}

	@Override
	public float getScaleY() {
		return sprite.getScaleY();
	}

	@Override
	public void setScaleY(float sy) {
		sprite.setScaleY(sy);
	}

	@Override
	public void setAnchorPoint(float x, float y) {
		if (sprite != null) {
			sprite.setAnchorPoint(x, y);
		} else {
			super.setAnchorPoint(x, y);
		}
	}
}
