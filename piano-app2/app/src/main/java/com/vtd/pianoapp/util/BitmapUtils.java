package com.vtd.pianoapp.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import com.vtd.pianoapp.MyApplication;
import com.vtd.pianoapp.ui.BitmapFromPlist;
import com.vtd.pianoapp.ui.BitmapInfoProperty;
import org.cocos2d.config.ccMacros;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.utils.GeometryUtil;
import org.cocos2d.utils.PlistParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;

public class BitmapUtils {

	public static HashMap<String, BitmapInfoProperty> getBitmapInfoProperties(String plistPath) {

		InputStream in;
		try {
			in = MyApplication.getInstance().getAssets().open(plistPath);
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}

		HashMap<String, Object> dictionary = PlistParser.parse(in);
		@SuppressWarnings("unchecked")
		HashMap<String, Object> metadataDict = (HashMap<String, Object>) dictionary.get("metadata");
		@SuppressWarnings("unchecked")
		HashMap<String, Object> framesDict = (HashMap<String, Object>) dictionary.get("frames");

		int format = 0;

		// get the format
		if (metadataDict != null)
			format = (Integer) metadataDict.get("format");

		// check the format
		if (!(format >= 0 && format <= 3)) {
			ccMacros.CCLOGERROR("BitmapUtils", "Unsupported Zwoptex plist file format.");
		}


		HashMap<String, BitmapInfoProperty> infos = new HashMap<>();

		// add real frames
		for (Entry<String, Object> frameDictEntry : framesDict.entrySet()) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> frameDict = (HashMap<String, Object>) frameDictEntry.getValue();

			BitmapInfoProperty bInfo = null;
			if (format == 0) {
				float x = ((Number) frameDict.get("x")).floatValue();
				float y = ((Number) frameDict.get("y")).floatValue();
				float w = ((Number) frameDict.get("width")).floatValue();
				float h = ((Number) frameDict.get("height")).floatValue();

				// create frame
				bInfo = new BitmapInfoProperty(new Rect((int) x, (int) y, (int) (x + w), (int) (y + h)), false);

			} else if (format == 1 || format == 2) {
				CGRect frame = GeometryUtil.CGRectFromString((String) frameDict.get("frame"));
				boolean rotated = false;

				// rotation
				if (format == 2)
					rotated = (Boolean) frameDict.get("rotated");

				// create frame
				bInfo = new BitmapInfoProperty(new Rect((int) frame.origin.x, (int) frame.origin.y, (int) (frame.size.width + frame
						.origin.x), (int) (frame.size.height + frame.origin.y)), rotated);
			} else if (format == 3) {
				// get values
				CGSize spriteSize = GeometryUtil.CGSizeFromString((String) frameDict.get("spriteSize"));
				CGRect textureRect = GeometryUtil.CGRectFromString((String) frameDict.get("textureRect"));
				boolean textureRotated = (Boolean) frameDict.get("textureRotated");

				// create frame
				bInfo = new BitmapInfoProperty(new Rect((int) textureRect.origin.x, (int) textureRect.origin.y, (int) (spriteSize
						.width + textureRect.origin.x), (int) (spriteSize.height + textureRect.origin.y)), textureRotated);
			}

			// add sprite frame
			infos.put(frameDictEntry.getKey(), bInfo);
		}
		return infos;
	}

	private static Bitmap getBitmapFromAsset(String path) {
		try {
			InputStream imgStream = MyApplication.getInstance().getAssets().open(path);
			Bitmap bitmap = BitmapFactory.decodeStream(imgStream, null, null);
			imgStream.close();
			return bitmap;
		} catch (IOException ignored) {
			return null;
		}
	}

	public static BitmapFromPlist getBitmapFromPlist(String plistFilePath) {
		String imageFilePath = plistFilePath.replace(".plist", ".png");
		return new BitmapFromPlist(getBitmapFromAsset(imageFilePath), getBitmapInfoProperties(plistFilePath));
	}
}
