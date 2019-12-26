package com.rayfenwindspear.speedlocktimer.View.Main;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

import com.rayfenwindspear.speedlocktimer.MainActivity;
import com.rayfenwindspear.speedlocktimer.R;
import com.rayfenwindspear.speedlocktimer.View.BaseContainerView;

/**
 * Created by taeler on 11/11/16.
 */

public class MainView extends BaseContainerView {
    public static final String TAG = MainView.class.getSimpleName();
    private Button startButton;
    //private Button pauseButton;
    private TextView timerValue;
    private TextView accuracy;
    private TextView distance;
    private TextView speed;

    private boolean displayFeet = true;



    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        timerValue = (TextView) findViewById(R.id.timerValue);
        accuracy = (TextView) findViewById(R.id.accuracy);
        distance = (TextView) findViewById(R.id.distance);
        speed = (TextView) findViewById(R.id.speed);

        startButton = (Button) findViewById(R.id.startButton);

//        startButton.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View view) {
//                startTime = SystemClock.uptimeMillis();
//                customHandler.postDelayed(updateTimerThread, 0);
//            }
//        });

//        pauseButton = (Button) findViewById(R.id.pauseButton);
//
//        pauseButton.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View view) {
//
//                timeSwapBuff += timeInMilliseconds;
//                customHandler.removeCallbacks(updateTimerThread);
//
//
//            }
//        });
    }

    public void setTimerValue(long timeinmillis) {
        int secs = (int) (timeinmillis / 1000);
        int mins = secs / 60;
        secs = secs % 60;
        int milliseconds = (int) (timeinmillis % 1000);
        timerValue.setText("" + mins + ":"
                + String.format("%02d", secs) + ":"
                + String.format("%03d", milliseconds));
    }

    public void updateAccuracy(int i) {
        if (displayFeet) {
            accuracy.setText(String.format("Acc: %3d ft", Math.round(i*3.28084)));
        } else {
            accuracy.setText(String.format("Acc: %3d m", i));
        }
    }

    public void updateDistance(double dist) {
        if (displayFeet) {
            dist = MainActivity.convertFeet(dist);
            if (dist < 5280) {
                distance.setText(String.format("Dist: %4d ft", Math.round(dist)));
            } else {
                double miles = (float)dist/(float)5280;
                distance.setText(String.format("Dist: %4.1f mi", miles));
            }
        } else {
            if (dist < 1000) {
                distance.setText(String.format("Dist: %4d m", Math.round(dist)));
            } else {
                double km = (float)dist/(float)5280;
                distance.setText(String.format("Dist: %4.1f km", km));
            }
        }
    }

    public void updateSpeed(double spd) {
        if (displayFeet) {
            speed.setText(String.format("Spd: %3.1f mph", spd*0.621371));
        } else {
            speed.setText(String.format("Spd: %3.1f kph", spd));
        }
    }
}
