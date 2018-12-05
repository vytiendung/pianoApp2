package com.vtd.pianoapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import com.vtd.pianoapp.MyApplication;
import com.vtd.pianoapp.common.AppConfig;
import com.vtd.pianoapp.common.Constant;

import java.io.File;
import java.math.BigDecimal;

public class CommonUtils implements Constant {
	private static final String TAG = "CommonUtils";

	/*
	 * Check SD card
	 */

	public static boolean checkSDCard() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		return mExternalStorageAvailable & mExternalStorageWriteable & AppConfig.getInstance().checkHasPermissionExternalStorage();
	}


	public static void setAudioVolumne(AudioManager audio) {

		int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		if ((float) currentVolume / max > 0.9f)
			audio.setStreamVolume(AudioManager.STREAM_MUSIC,
					(int) (0.9 * audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
	}


	public static float round(double d, int decimalPlace) {
		BigDecimal bd = new BigDecimal(d);
		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd.floatValue();
	}

	public static long bytesAvailable(File f) {
		try {
			StatFs stat = new StatFs(f.getPath());
			long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
			return bytesAvailable;

		} catch (IllegalArgumentException e) {
			return 0;
		}
	}

	public static void saveBooleanSetting(Context mContext, String pKey, boolean pValue) {
		if (mContext == null)
			return ;
		Editor mEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
		mEditor.putBoolean(pKey, pValue);
		mEditor.commit();
	}

	public static boolean getBooleanSetting(Context mContext, String pKey, boolean mDefaultValue) {
		if (mContext == null)
			return mDefaultValue;
		SharedPreferences mSharePreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean mValue = mSharePreferences.getBoolean(pKey, mDefaultValue);
		return mValue;
	}


	public static void saveStringSetting(Context mContext, String pKey, String pValue) {
		if (mContext == null)
			return ;
		Editor mEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
		mEditor.putString(pKey, pValue);
		mEditor.commit();
	}

	public static String getStringSetting(Context mContext, String pKey, String mDefaultValue) {
		if (mContext == null)
			return mDefaultValue;
		SharedPreferences mSharePreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		String mValue = mSharePreferences.getString(pKey, mDefaultValue);
		return mValue;
	}

	public static void saveLongSetting(Context mContext, String pKey, long pValue) {
		if(mContext==null)
			return;
		Editor mEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
		mEditor.putLong(pKey, pValue);
		mEditor.commit();
	}

	public static Long getLongSetting(Context mContext, String pKey, long mDefaultValue) {
		if (mContext == null)
			return mDefaultValue;
		SharedPreferences mSharePreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		long mValue = mSharePreferences.getLong(pKey, mDefaultValue);
		return mValue;
	}

	public static void saveIntSetting(Context mContext, String pKey, int pValue) {
		if(mContext==null)
			return;
		Editor mEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
		mEditor.putInt(pKey, pValue);
		mEditor.commit();
	}

	public static int getIntSetting(Context mContext, String pKey, int mDefaultValue) {
		if (mContext == null)
			return mDefaultValue;
		SharedPreferences mSharePreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		int mValue = mSharePreferences.getInt(pKey, mDefaultValue);
		return mValue;
	}







	public static float convertDpToPixel(Context context, float dp) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}

	public static float convertDpToPixel(float dp) {
		Resources resources = MyApplication.getInstance().getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}




	public static String convertToUnicode(String str) {
		try {
			StringBuffer ostr = new StringBuffer();

			for (int i = 0; i < str.length(); i++) {
				char ch = str.charAt(i);

				if ((ch >= 0x0020) && (ch <= 0x007e)) // Does the char need to be converted to unicode?
				{
					ostr.append(ch); // No.
				} else // Yes.
				{
					ostr.append("\\u"); // standard unicode format.
					String hex = Integer.toHexString(str.charAt(i) & 0xFFFF); // Get hex value of the char.
					for (int j = 0; j < 4 - hex.length(); j++)
						// Prepend zeros because unicode requires 4 digits
						ostr.append("0");
					ostr.append(hex.toLowerCase()); // standard unicode format.
													// ostr.append(hex.toLowerCase(Locale.ENGLISH));
				}
			}
			return (new String(ostr)); // Return the stringbuffer cast as a string.
		} catch (Exception e) {

		}
		return str;
	}



	public static void saveFloatSetting(Context mContext, String pKey, float pValue) {
		if(mContext==null)
			return;
		Editor mEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
		mEditor.putFloat(pKey, pValue);
		mEditor.commit();
	}

	public static void saveFloatSetting(String pKey, float pValue) {
		final Context mContext = MyApplication.getInstance().getApplicationContext();
		if (mContext == null)
			return;
		Editor mEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
		mEditor.putFloat(pKey, pValue);
		mEditor.commit();
	}

	public static float getFloatSetting(Context mContext, String pKey, float mDefaultValue) {
		if (mContext == null)
			return mDefaultValue;
		SharedPreferences mSharePreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		float mValue = mSharePreferences.getFloat(pKey, mDefaultValue);
		return mValue;
	}

	public static float getFloatSetting(String pKey, float mDefaultValue) {
		final Context mContext = MyApplication.getInstance().getApplicationContext();
		if (mContext == null)
			return mDefaultValue;
		SharedPreferences mSharePreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		float mValue = mSharePreferences.getFloat(pKey, mDefaultValue);
		return mValue;
	}


	public static void saveIntSetting(String pKey, int pValue) {
		final Context context = MyApplication.getInstance().getApplicationContext();
		if(context==null)
			return;
		Editor mEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		mEditor.putInt(pKey, pValue);
		mEditor.commit();
	}

	public static int getIntSetting(String pKey, int mDefaultValue) {
		final Context mContext = MyApplication.getInstance().getApplicationContext();
		if (mContext == null)
			return mDefaultValue;
		SharedPreferences mSharePreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		int mValue = mSharePreferences.getInt(pKey, mDefaultValue);
		return mValue;
	}

	public static void saveBooleanSetting( String pKey, boolean pValue) {
		final Context mContext = MyApplication.getInstance().getApplicationContext();
		if (mContext == null)
			return ;
		Editor mEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
		mEditor.putBoolean(pKey, pValue);
		mEditor.commit();
	}
	public static boolean getBooleanSetting(String pKey, boolean mDefaultValue) {
		final Context mContext = MyApplication.getInstance().getApplicationContext();
		if (mContext == null)
			return mDefaultValue;
		SharedPreferences mSharePreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean mValue = mSharePreferences.getBoolean(pKey, mDefaultValue);
		return mValue;
	}

	public static void saveLongSetting(String pKey, long pValue) {
		final Context mContext = MyApplication.getInstance().getApplicationContext();
		if (mContext == null)
			return;
		Editor mEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
		mEditor.putLong(pKey, pValue);
		mEditor.commit();
	}

	public static Long getLongSetting(String pKey, long mDefaultValue) {
		final Context mContext = MyApplication.getInstance().getApplicationContext();
		if (mContext == null)
			return mDefaultValue;
		SharedPreferences mSharePreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		long mValue = mSharePreferences.getLong(pKey, mDefaultValue);
		return mValue;
	}

	public static void saveStringSetting(String pKey, String pValue) {
		final Context mContext = MyApplication.getInstance().getApplicationContext();
		if (mContext == null)
			return;
		Editor mEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
		mEditor.putString(pKey, pValue);
		mEditor.commit();
	}

	public static String getStringSetting(String pKey, String mDefaultValue) {
		final Context mContext = MyApplication.getInstance().getApplicationContext();
		if (mContext == null)
			return mDefaultValue;
		SharedPreferences mSharePreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		String mValue = mSharePreferences.getString(pKey, mDefaultValue);
		return mValue;
	}

	public static Point getDisplaySize(Display display) {
		Point size = null;
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
			size = new Point();
			display.getRealSize(size);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2 ) {
			display.getSize(size);
		} else {
			size = new Point(display.getWidth(), display.getHeight());
		}
		return size;
	}


	public static void rotateImageView(ImageView imageView, float fromDeltaDegree, float toDeltaDegree){
		RotateAnimation rotate = new RotateAnimation(fromDeltaDegree, toDeltaDegree,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotate.setInterpolator(new LinearInterpolator());
		rotate.setFillAfter(true);
		imageView.startAnimation(rotate);
	}

	public static void fillColorForImageView(ImageView img, Drawable drawable, int color){
		Drawable tmp = DrawableCompat.wrap(drawable);
		DrawableCompat.setTint(tmp, color);
		DrawableCompat.setTintMode(tmp, PorterDuff.Mode.MULTIPLY);
		img.setImageDrawable(tmp);
	}


	public static boolean isLowerThanLollipop(){
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
	}

	public static void setRoundImage(Context context, ImageView imageView, Drawable drawable){
		Bitmap bmp = ((BitmapDrawable)drawable).getBitmap();
		RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bmp);
		roundDrawable.setCircular(true);
		imageView.setImageDrawable(roundDrawable);
	}

	public static void setRoundImage(ImageView imageView, Bitmap bmp){
		RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory
				.create(MyApplication.getInstance().getResources(), bmp);
		roundDrawable.setCircular(true);
		imageView.setImageDrawable(roundDrawable);
	}
}
