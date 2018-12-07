package com.vtd.pianoapp.practice;


import android.util.Log;
import com.vtd.pianoapp.common.Constant;
import com.vtd.pianoapp.common.UserConfig;
import com.vtd.pianoapp.songobject.GamePlayNote;
import com.vtd.pianoapp.common.Config;
import com.vtd.pianoapp.util.NoteUtils;
import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteFrame;
import org.cocos2d.nodes.CCSpriteFrameCache;

public class Cell extends CCNode {
	private static final String NOTE_FRAME_NAME_TINY = "guide_note_tiny.png";
	private static final String NOTE_FRAME_NAME_SMALL = "guide_note_small.png";
	private static final String NOTE_FRAME_NAME_DEFAULT = "guide_note.png";
	private static final String NEW_STATE = "NEW";
	private static final String GLOW_STATE = "GLOW";
	private static final String RELEASE_STATE = "RELEASE";

	private int border;
	private float mWidth, mHeight; // width & height without border
	private CCSprite sprite;
	private GamePlayNote gamePlayNote;
	private String state;

	public static Cell make(GamePlayNote gamePlayNote) {
		float width = NoteUtils.isSharpNote(gamePlayNote) ? Config.getInstance().keyWidthBlack : Config.getInstance().keyWidthWhite;
		float height = CellDirector.getHeightByDuration(gamePlayNote.duration);
		return new Cell(gamePlayNote, width, height);
	}

	public static Cell make(GamePlayNote gamePlayNote, float width, float height) {
		return new Cell(gamePlayNote, width, height);
	}

	private Cell(GamePlayNote gamePlayNote, float width, float height) {
		super();
		this.gamePlayNote = gamePlayNote;
		this.mWidth = width;
		this.mHeight = height;
		draw();
	}

	private void draw() {
		state = NEW_STATE;
		String frameName = frameNameByDuration(gamePlayNote.duration);
		border = 0;
		sprite = createSprite(frameName);
		setWidth(mWidth);
		setHeight(mHeight);
		sprite.setPosition(0f, 0f);
		if (NoteUtils.isSharpNote(gamePlayNote)) {
			sprite.setColor(UserConfig.getInstance().getBlackNoteGuideColor());
		} else {
			sprite.setColor(UserConfig.getInstance().getWhiteNoteGuideColor());
		}
		addChild(sprite);
	}

	private static String frameNameByDuration(float duration) {
		String frameName;
		if (duration < 200) {
			frameName = NOTE_FRAME_NAME_TINY;
		} else if (duration < 700) {
			frameName = NOTE_FRAME_NAME_SMALL;
		} else {
			frameName = NOTE_FRAME_NAME_DEFAULT;
		}
		return frameName;
	}

	private static CCSprite createSprite(String frameName) {
		CCSpriteFrame sf = CCSpriteFrameCache.sharedSpriteFrameCache().spriteFrameByName(frameName);
		CCSprite sprite = CCSprite.sprite(sf);
		sprite.setAnchorPoint(0f, 0f);
		return sprite;
	}

	public void setWidth(float width) {
		mWidth = width;
		sprite.setScaleX(width / (sprite.getContentSizeRef().width - border));
		sprite.setPosition(-sprite.getScaleX() * border / 2, sprite.getPositionRef().y);
	}

	public void setHeight(float height) {
		mHeight = height;
		sprite.setScaleY(height / (sprite.getContentSizeRef().height - border));
		sprite.setPosition(sprite.getPositionRef().x, -sprite.getScaleY() * border / 2);
	}

	public void glow() {
		if (GLOW_STATE.equals(state)) return;
		removeAllChildren(true);
		state = GLOW_STATE;
		String frameName = frameNameByDuration(gamePlayNote.duration);
		border = (Config.getInstance().deviceType == Constant.SMALL ? 15 : 22) * 2;
		sprite = createSprite(buildGlowingFrameName(frameName));
		setWidth(mWidth);
		setHeight(mHeight);
		if (NoteUtils.isSharpNote(gamePlayNote)) {
			sprite.setColor(UserConfig.getInstance().getBlackNoteGuideColor());
		} else {
			sprite.setColor(UserConfig.getInstance().getWhiteNoteGuideColor());
		}
		addChild(sprite);
	}

	private static String buildGlowingFrameName(String frameName) {
		return frameName.replace(".png", "_glow.png");
	}

	public void release() {
		if (RELEASE_STATE.equals(state)) return;
		removeAllChildren(true);
		state = RELEASE_STATE;
		String frameName = frameNameByDuration(gamePlayNote.duration);
		border = 0;
		sprite = createSprite(frameName);
		setWidth(mWidth);
		setHeight(mHeight);
		sprite.setPosition(0f, 0f);
		addChild(sprite);
	}

	public float getWidth() {
		return mWidth;
	}

	public float getHeight() {
		return mHeight;
	}

	public void recreate() {
		if (NEW_STATE.equals(state)) return;
		removeAllChildren(true);
		draw();
	}

	public GamePlayNote getNote() {
		return gamePlayNote;
	}
}
