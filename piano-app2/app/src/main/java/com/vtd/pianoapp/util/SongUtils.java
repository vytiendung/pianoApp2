package com.vtd.pianoapp.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
import com.vtd.pianoapp.MyApplication;
import com.vtd.pianoapp.common.Config;
import com.vtd.pianoapp.common.Constant;
import com.vtd.pianoapp.songobject.Song;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SongUtils implements Constant {
	final static String TAG = "SongUtils";
	public static String getBackgroundSongPath(String path) {
		String p = path.substring(0, path.lastIndexOf("/") + 1);
		String s = path.substring(path.lastIndexOf("/") + 1, path.length());
		int hand = Integer.valueOf(s.substring(0, s.indexOf("_")));
		s = s.substring(s.indexOf("_"), s.length());
		if (hand == Constant.RIGHT_HAND) {
			s = Constant.LEFT_HAND + s;
		} else {
			s = Constant.RIGHT_HAND + s;
		}

		String result = p + s;
		boolean isFileExists = FileUtils.isExist(MyApplication.getInstance(), result);
		return isFileExists ? result : null;
	}

	public static String decodSongData(InputStream input) {
		String result = null;
		try {
			InputStream is = input;
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
			is.close();
			String jsonString = writer.toString();

			JSONObject obj = new JSONObject(jsonString);
			JSONObject obj2 = obj.getJSONObject("ruby");
			JSONObject obj3 = obj2.getJSONObject("data");

			result = obj3.getString("base64");
			result = tryDecodeBase64(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String tryDecodeBase64(String input) {
		try {
			new JSONArray(input).getJSONArray(0);
			return input;
		} catch (Exception ignored) {
			return Base64Utils.decode(input.trim());
		}
	}


}
