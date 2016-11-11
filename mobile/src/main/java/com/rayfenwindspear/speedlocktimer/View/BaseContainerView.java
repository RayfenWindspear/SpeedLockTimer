package com.rayfenwindspear.speedlocktimer.View;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.rayfenwindspear.speedlocktimer.Interfaces.Container;
import com.rayfenwindspear.speedlocktimer.Interfaces.ContainerView;

/**
 * Created by taeler on 11/11/16.
 */

public abstract class BaseContainerView extends LinearLayout implements ContainerView {
    protected Container container;
    public BaseContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setContainer(Container container) {
        this.container = container;
    }

    @Override
    public Container getContainer() {
        return container;
    }
}
