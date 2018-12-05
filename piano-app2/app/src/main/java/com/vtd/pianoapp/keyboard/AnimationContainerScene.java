package com.vtd.pianoapp.keyboard;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.util.SparseArray;
import com.vtd.pianoapp.common.Config;
import com.vtd.pianoapp.common.Constant;
import com.vtd.pianoapp.game.GameplaySetting;
import com.vtd.pianoapp.gameUtils.PianoKeyHelper;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteFrame;
import org.cocos2d.nodes.CCSpriteFrameCache;
import org.cocos2d.opengl.CCGLSurfaceView;

public class AnimationContainerScene extends CCScene implements KeyboardScalingObserver, KeyboardScrollingObserver {
	final CCGLSurfaceView gameSurfaceView;
	private CCLayer rootLayer;

	private AnimationLayer noteAnimLayer;
	private CCLayer guidelineLayer;
	private Config config = Config.getInstance();
	private SharedKeyboardParams keyboardParams;
	private SparseArray<CCSprite> guidelines;

	AnimationContainerScene(Context context, SurfaceViewConfig builder) {
		super();
		gameSurfaceView = new CCGLSurfaceView(context);
		gameSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
		gameSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		CCDirector.sharedDirector().attachInView(gameSurfaceView);
		CCDirector.sharedDirector().setScreenSize(config.winWidth, config.winHeight);
		CCDirector.sharedDirector().setAnimationInterval(1.0f / 60);
		CCDirector.sharedDirector().runWithScene(this);

		CCSpriteFrameCache spriteFrameCache = CCSpriteFrameCache.sharedSpriteFrameCache();
		Log.d("ttt", "prepare to addSpriteFrames");
		spriteFrameCache.addSpriteFrames(Config.getInstance().imgPath + "guideNote.plist");
		spriteFrameCache.addSpriteFrames(Config.getInstance().imgPath + "spriteCache.plist");
		Log.d("ttt", "finish addSpriteFrames");
		rootLayer = CCLayer.node();
		addChild(rootLayer);

		rootLayer.setRelativeAnchorPoint(true);
		rootLayer.setAnchorPoint(0, 0f);
		rootLayer.setRelativeAnchorPoint(true);

		noteAnimLayer = new AnimationLayer();
		noteAnimLayer.setTag(Constant.ANIM_LAYER_TAG);
		noteAnimLayer.setVisible(GameplaySetting.isGuideNotesEnabled());
		rootLayer.addChild(noteAnimLayer);
		if (builder.getMode() == SurfaceViewConfig.CHALLENGE_MODE) {
			guidelineLayer = CCLayer.node();
			rootLayer.addChild(guidelineLayer);
			guidelines = new SparseArray<>();
		} else if (builder.getMode() == SurfaceViewConfig.PRACTICE_MODE) {
			guidelineLayer = CCLayer.node();
			rootLayer.addChild(guidelineLayer);
			guidelines = new SparseArray<>();
		}
	}

	void setKeyboardParams(SharedKeyboardParams keyboardParams) {
		this.keyboardParams = keyboardParams;
	}

	@Override
	public void onScroll(int scrollX) {
		rootLayer.setPosition(scrollX, rootLayer.getPositionRef().y);
	}

	@Override
	public void onHorizScale() {
//		float guideLineWidth = keyboardParams.blackKeyWidth * 0.75f;
//		float padding = (keyboardParams.blackKeyWidth - guideLineWidth) / 2;
//		for (int i = 0; i < config.noteList.size(); i++) {
//			CCSprite guideline = guidelines.get(i, null);
//			if (guideline != null) {
//				guideline.setScaleX(guideLineWidth/guideline.getContentSizeRef().width);
//				guideline.setPosition(keyboardParams.keyPosMapping.get(i).x + padding, guideline.getPositionRef().y);
//			}
//		}

		float scaleFactor = keyboardParams.whiteKeyWidth / Constant.originWhiteKeyWidth;
		rootLayer.setScaleX(scaleFactor);
	}

	@Override
	public void onVertScale() {
		int keyboardHeightPixels = KeyboardHeight.currentHeightPixels();
		float guidelineHeight = config.winHeight - keyboardHeightPixels;
		for (int i = 0; i < config.noteList.size(); i++) {
			CCSprite guideline = guidelines.get(i, null);
			if (guideline != null) {
				guideline.setScaleY(guidelineHeight / guideline.getContentSizeRef().height);
				guideline.setPosition(guideline.getPositionRef().x, keyboardHeightPixels);
			}
		}
	}

	void drawGuideline() {
		float scaleFactor = keyboardParams.whiteKeyWidth / Constant.originWhiteKeyWidth;
		float backKeyWidth = PianoKeyHelper.getBlackKeyWidthFromWhiteKeyWidth(Constant.originWhiteKeyWidth);
		float guideLineWidth = backKeyWidth * 0.75f;
		float padding = (backKeyWidth - guideLineWidth) / 2;
		float bottom = KeyboardHeight.currentHeightPixels();
		float guidelineHeight = config.winHeight - bottom;

		for (int i = 0; i < config.noteList.size(); i++) {
			String name = config.noteList.get(i);
			if (name.contains("m")) {
				CCSpriteFrame sf = CCSpriteFrameCache.sharedSpriteFrameCache().spriteFrameByName("key_guide.png");
				CCSprite sprite = CCSprite.sprite(sf);
				guidelines.put(i, sprite);
				sprite.setAnchorPoint(0, 0);
				sprite.setScaleX(guideLineWidth / sprite.getContentSizeRef().width);
				sprite.setScaleY(guidelineHeight / sprite.getContentSizeRef().height);
				sprite.setPosition((keyboardParams.keyPosMapping.get(i).x) / scaleFactor + padding, bottom);
				guidelineLayer.addChild(sprite);
			}
		}
	}

	AnimationLayerProxy getAnimationLayerProxy() {
		return noteAnimLayer;
	}


	public float getScrollX() {
		return rootLayer.getPositionRef().x;
	}
}
