package com.rayfenwindspear.speedlocktimer;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.AutoTransition;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.rayfenwindspear.speedlocktimer.Containers.MainContainer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    public static final String TAG = MainActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

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

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private Location lastLocation;
    private Location lastActionLocation;

    private long startTime = 0L;

    private Handler customHandler = new Handler();

    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    // long speedBarrier = 60*1000; // in meters/h 60km/h was for old ingress
    long speedBarrier = 15*1000; // in meters/h 15km/h is for HPWU


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

    public class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
            Log.i(TAG, "Speed barrier adjusted to: "+parent.getItemAtPosition(pos).toString()+" kph");
            speedBarrier = Integer.parseInt(parent.getItemAtPosition(pos).toString())*1000;
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        MenuItem item = menu.findItem(R.id.barrier_spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

        List<String> list = new ArrayList<String>();
        for (int i=3; i<=70; i++) {
            list.add(String.format("%d",i));
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                R.layout.my_spinner, list);

        spinner.setAdapter(dataAdapter); // set the adapter to provide layout of rows and content
        spinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());
        spinner.setSelection(12); // default to 15km/h, which is item 12
        return super.onCreateOptionsMenu(menu);
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

        // setup location stuff
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2 * 1000)        // 2 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


        // this is the view we start with
        mainContainer = (MainContainer) inflater.inflate(R.layout.main_container, root, false);
        root.addView(mainContainer);
        currentContainer = R.layout.main_container;

        Log.i(TAG, "finished onCreate");
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

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    // location API stuff

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "onConnected");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.i(TAG, "no permissions");
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            Log.i(TAG, "location was null");
        }
        else {
            handleNewLocation(location);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        Log.i(TAG, "onConnectionFailed");
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location) {
        Log.i(TAG, location.toString());

        lastLocation = location;

        if (lastActionLocation != null) {
            double currentLat = location.getLatitude();
            double currentLon = location.getLongitude();
            double lastLat = lastActionLocation.getLatitude();
            double lastLon = lastActionLocation.getLongitude();

            double dist = getDist(currentLat,currentLon,lastLat,lastLon);
            mainContainer.getMainView().updateDistance(dist);
            mainContainer.getMainView().updateSpeed(getKPH(
                    lastActionLocation.getLatitude(),
                    lastActionLocation.getLongitude(),
                    lastLocation.getLatitude(),
                    lastLocation.getLongitude()
            ));
            //mainContainer.getMainView().setTimerValue(calculateBubbleTimer());
        }

        mainContainer.getMainView().updateAccuracy(Math.round(location.getAccuracy()));

    }

    // gets distance between two points in meters
    public static double getDist(double lat1, double lon1, double lat2, double lon2) {
        double R = 6373e3; // radius of the earth in meters
        double lat1rad = Math.toRadians(lat1);
        double lat2rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2-lat1);
        double deltaLon = Math.toRadians(lon2-lon1);

        double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
                Math.cos(lat1rad) * Math.cos(lat2rad) *
                        Math.sin(deltaLon/2) * Math.sin(deltaLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double d = R * c;
        return d;
    }

    public static double convertFeet(double dist) {
        return dist * 3.28084;
    }

    // timer stuff

    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;

            mainContainer.getMainView().setTimerValue(calculateBubbleTimer());
            customHandler.postDelayed(this, 10);
        }

    };

    public double getKPH() {
        return getKPH(
                lastActionLocation.getLatitude(),
                lastActionLocation.getLongitude(),
                lastLocation.getLatitude(),
                lastLocation.getLongitude()
        );
    }

    public double getKPH(double lat1, double lon1, double lat2, double lon2) {
        double meters = getDist(lat1,lon1,lat2,lon2);
        double km = meters/1000;
        double seconds = timeInMilliseconds / (double)1000;
        double hours = seconds / (60*60);
        return km/hours;
    }

    // timer is calculated by getting a ratio of kph/barrier
    // this value represents how many "barriers" ahead we are.
    // if > 1, multiply time by ratio-1 because bubble has already travelled 1
    public long calculateBubbleTimer() {
        double ratio = getKPH()/((float)speedBarrier/1000);
        if (ratio > 1) {
            // in order to maintain precision, multiply by 10^y, which should preserve y decimal points.
            // then take the final number and divide by 10^y to get the end value
            double y = 5; // pow takes a
            long tenPowY = Math.round(Math.pow(10,y));
            long pr = Math.round((ratio-1) * tenPowY);
            long pv = timeInMilliseconds * pr;
            return pv/tenPowY;
        } else {
            return 0;
        }
    }

    ////////////////////
    // Click Handlers //
    ////////////////////

    public void setLastActionLocation(View view) {
        mainContainer.getMainView().updateDistance(0);
        mainContainer.getMainView().updateSpeed(0);
        if (lastLocation != null) {
            lastActionLocation = lastLocation;
            Log.i(TAG, "lastActionLocation: " + lastActionLocation.toString());

            timeSwapBuff = 0;
            customHandler.removeCallbacks(updateTimerThread);

            startTime = SystemClock.uptimeMillis();
            customHandler.postDelayed(updateTimerThread, 0);
        } else {
            Log.i(TAG, "LastLocation was null");
        }
    }

}
