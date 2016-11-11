package com.rayfenwindspear.speedlocktimer.Interfaces;

import android.view.View;

/**
 * Created by taeler on 11/11/16.
 */
public interface Container {

    boolean onBackPressed();

    void killUI();

    boolean getViewsKilled();

    void reInitDefaultUI();

    void switchView(View view, int nowCurrent);

    int getCurrentView();

}

