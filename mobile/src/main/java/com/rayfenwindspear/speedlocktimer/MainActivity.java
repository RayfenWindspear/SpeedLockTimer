package com.rayfenwindspear.speedlocktimer;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.rayfenwindspear.speedlocktimer.Containers.MainContainer;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_CODE = 100;
    private LinearLayout root;
    private MainActivity tthis;
    private int currentContainer;
    private boolean firstAsk = true;
    private boolean secondBack = false;
    private Toast permissionInfoToast;
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    AsyncTask<Void, Void, Void> killTasktic;

    private MainContainer mainContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tthis = this;
        permissionInfoToast = Toast.makeText(tthis, R.string.permission_info, Toast.LENGTH_LONG);
        setContentView(R.layout.root_layout);
        root = (LinearLayout) findViewById(R.id.root);

        try {
            ((SpeedLockTimerApplication) getApplicationContext()).setMainActivity(this);
        } catch (Exception ex) {

        }
        permissionsAsk();
    }

    private void permissionsAsk() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (canCoarseGPS() && canFineGPS()) {
                finishOnCreate();
            } else {
                requestPermissions(PERMISSIONS, REQUEST_PERMISSION_CODE);
            }
        } else {
            finishOnCreate();
        }
    }

    private void finishOnCreate() {
        // this is just to preload the post-login container and view
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());

        // this is the view we start with
        mainContainer = (MainContainer) inflater.inflate(R.layout.main_container, root, false);
        root.addView(mainContainer);
        currentContainer = R.layout.main_container;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    if (firstAsk) {
                        // two longs makes 7 seconds
                        Toast.makeText(this, R.string.need_gps, Toast.LENGTH_LONG).show();
                        Toast.makeText(this, R.string.need_gps, Toast.LENGTH_LONG).show();
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (canCoarseGPS() && canFineGPS()) {
                        finishOnCreate();
                    } else {
                        if (firstAsk) {
                            firstAsk = false;
                            askAfter7();
                        } else {
                            // long+short = 5.5 seconds
                            Toast.makeText(this, R.string.quitting, Toast.LENGTH_LONG).show();
                            Toast.makeText(this, R.string.quitting, Toast.LENGTH_SHORT).show();
                            quitAfter10();
                        }
                    }
                }
                break;
        }
    }

    private boolean shouldShowCoarseGPSRationale() {
        return shouldShowRationale(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private boolean shouldShowFineGPSRationale() {
        return shouldShowRationale(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean shouldShowRationale(String perm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return !hasPermission(perm) && shouldShowRequestPermissionRationale(perm);
        }
        return false;
    }

    public boolean canCoarseGPS() {
        return hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public boolean canFineGPS() {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public boolean hasPermission(String perm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void askAfter7() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(7000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(PERMISSIONS, REQUEST_PERMISSION_CODE);
                }
                return null;
            }
        }.execute();
    }

    private void quitAfter10() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(5500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                permissionInfoToast.show();
                try {
                    Thread.sleep(4500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tthis.finish();
                System.exit(0);
                return null;
            }
        }.execute();
    }
    @Override
    public void onBackPressed() {
        boolean handled;
        switch (currentContainer) {
            default:
                handled = false;
        }
        if (!handled) {
            handleUnhandledBack();
        }
    }

    private void handleUnhandledBack() {
        if (!secondBack) {
            secondBack = true;
            Toast.makeText(this, R.string.quit, Toast.LENGTH_LONG).show();
            killTasktic = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    tthis.secondBack = false;
                    return null;
                }
            };
            killTasktic.execute();
        } else {
            killTasktic.cancel(true);
            final SpeedLockTimerApplication application = SpeedLockTimerApplication.getApplication(this);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    application.getMainActivity().finish();
                    application.getMainActivity().killUI();
                    System.exit(0);
                    return null;
                }
            }.execute();
        }
    }

    public void killUI() {
        if (mainContainer != null) {
            mainContainer.killUI();
        }
    }

    @Override
    protected void onDestroy() {
        killUI();
        super.onDestroy();
    }

    public void switchContainer(int containerId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TransitionManager.go(new Scene(root, getContainerById(containerId)), new AutoTransition());
        } else {
            root.removeAllViews();
            root.addView(getContainerById(containerId));
        }
        currentContainer = containerId;
    }

    public View getContainerById(int containerId) {
        switch (containerId) {
            case R.layout.main_container:
                if (mainContainer == null) {
                    return LayoutInflater.from(getBaseContext()).inflate(R.layout.main_container, root, false);
                }
                return mainContainer;
            default:
                return null;
        }
    }
    public MainContainer getMainContainer() {
        return mainContainer;
    }
}
