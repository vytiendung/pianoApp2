package com.vtd.pianoapp.common;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.vtd.pianoapp.MyApplication;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AppConfig {

    private static AppConfig instance;

    public static void init(Context context) {
        instance = new AppConfig(context);
    }

    public static AppConfig getInstance() {
	    if (instance == null) {
		    init(MyApplication.getInstance());
	    }
        return instance;
    }

    private Context context;

    private AppConfig(Context context) {
        this.context = context.getApplicationContext() != null ? context.getApplicationContext() : context;
    }


    public boolean checkPermission(String permission) {
        PackageManager pm = context.getPackageManager();
        if (pm.checkPermission(permission, context.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public boolean checkHasPermissionExternalStorage() {
        return this.checkPermission(WRITE_EXTERNAL_STORAGE);
    }

    public boolean checkHasPermissionReadPhoneState() {
        return this.checkPermission(READ_PHONE_STATE);
    }

    public boolean hasRequestPermission(String permission) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            for (int i = 0 ; i < pi.requestedPermissions.length; ++i) {
                if ( pi.requestedPermissions[i].equals(permission) ) {
                    return true;
                }
            }
        } catch (Exception ex) {

        }
        return false;
    }

    public boolean hasRequestPermissionExternalStorage() {
        return hasRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public boolean hasRequestPermissionReadPhoneState() {
        return hasRequestPermission(Manifest.permission.READ_PHONE_STATE);
    }
}
