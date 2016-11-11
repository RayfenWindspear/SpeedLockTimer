package com.rayfenwindspear.speedlocktimer.Containers;

/**
 * Created by taeler on 11/11/16.
 */

import android.content.Context;
import android.os.Build;
import android.transition.AutoTransition;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.rayfenwindspear.speedlocktimer.Interfaces.Container;
import com.rayfenwindspear.speedlocktimer.Interfaces.ContainerView;

/**
 * BaseContainer just has a few methods that are universal to all Containers.
 * Created by taeler on 11/6/15.
 */
public abstract class BaseContainer extends FrameLayout implements Container {
    protected boolean viewsKilled;
    protected int currentView;
    protected LayoutInflater inflater;

    public BaseContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public boolean getViewsKilled() {
        return viewsKilled;
    }

    @Override
    public void switchView(View view, int nowCurrent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TransitionManager.go(new Scene(this, view), new AutoTransition());
        } else {
            this.removeAllViews();
            this.addView(view);
        }
        currentView = nowCurrent;
    }

    protected <T> T inflateView(Class T, int res) {
        T view =  (T)inflater.inflate(res, this, false);
        ((ContainerView) view).setContainer(this);
        return view;
    }

    public <T> T switchViewInflate(Class T, int nowCurrent) {
        T view = inflateView(T, nowCurrent);
        switchView((View) view, nowCurrent);
        return view;
    }

    @Override
    public int getCurrentView() {
        return currentView;
    }
}