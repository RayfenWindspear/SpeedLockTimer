package com.rayfenwindspear.speedlocktimer.Containers;

import android.content.Context;
import android.util.AttributeSet;

import com.rayfenwindspear.speedlocktimer.R;
import com.rayfenwindspear.speedlocktimer.View.Main.MainView;


/**
 * Created by taeler on 11/11/16.
 */

public class MainContainer extends BaseContainer {
    private MainView mainView;

    public MainContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mainView = (MainView) findViewById(R.id.main_view);
        mainView.setContainer(this);
        currentView = R.layout.activity_main;
    }

    /**
     * Handles the "back stack" for the {@link MainContainer} views.
     * @return returns false to trigger the {@link com.rayfenwindspear.speedlocktimer.MainActivity} to handle the backPressed.
     */
    @Override
    public boolean onBackPressed() {
        switch(currentView) {

        }
        return false;
    }

    @Override
    public void killUI() {
        viewsKilled = true;
    }

    @Override
    public void reInitDefaultUI() {
        viewsKilled = false;
    }
}
