package com.rayfenwindspear.speedlocktimer;


import android.app.Application;
import android.content.Context;
import android.provider.Settings;

import com.rayfenwindspear.speedlocktimer.Util.SecurePreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by taeler on 11/11/16.
 */

public class SpeedLockTimerApplication extends Application {
    private boolean firstRun;
    private SecurePreferences preferences;
    private MainActivity mainActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        // androidID + a string is a good enough "secret key". we aren't storing super secret stuff anyway.
        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        androidId += "speedlocktimer";
        preferences = new SecurePreferences(this, "speedlocktimer-prefs", androidId, true);
        String frun = preferences.getString("firstRun");
        if (frun == null || frun.equals("")) {
            firstRun = true;
        } else {
            firstRun = false;
        }
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public static SpeedLockTimerApplication getApplication(Context context) {
        return ((SpeedLockTimerApplication) context.getApplicationContext());
    }

    public boolean isFirstRun() {
        return firstRun;
    }

    public SecurePreferences getPreferences() {
        return preferences;
    }
}
