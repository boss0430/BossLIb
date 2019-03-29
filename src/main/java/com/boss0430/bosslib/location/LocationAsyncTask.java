package com.boss0430.bosslib.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.boss0430.bosslib.utils.Dlog;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.lang.ref.WeakReference;

/**
 * Class that Retrieve Location Data using Google Fused Location API
 * <br><br>
 * Usage<br>
 * 1. implements LocationTaskInformer from your caller class.<br>
 * 2. declare onTaskDone in your caller class.<br>
 * 3. define me like new LocationAsyncTask(mContext, this);<br>
 * 4. call executeAsyncTask<br>
 * - RESULT String in callback is <b>latitude|longitude</b>. you can change default separator(PIPE) by its set function.
 * <br>
 * @see <a href="https://stackoverflow.com/a/45500818">weak reference comes from this link</a>
 * @see <a href="https://stackoverflow.com/a/35833552">and this man was my real savior</a>
 * @since 2019 Mar 27
 * @author boss0430
 */
public class LocationAsyncTask extends AsyncTask<String, Void, String> {

    private Context mContext;

    // Hold weak reference
    private WeakReference<LocationTaskInformer> mCallback;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private boolean useLastLocation = false;
    private String separator = "|";

    private String mResult;

    private int timeoutValue = 15000;
    private int timeoutCheckInterval = 1000;

    private final static String TAG = "LocationAsyncTask";


    /*
	// ********************************************************************************************
	// Default Functions
	// ********************************************************************************************
	*/

    /**
     * Constructor.
     * @param _context context.
     * @param callback you (caller).
     */
    public LocationAsyncTask(Context _context, LocationTaskInformer callback) {
        mContext = _context;
        this.mCallback = new WeakReference<>(callback);
    }

    /**
     *
     * @param _context context.
     * @param callback you (caller).
     * @param _useLastLocation will use 'getLastLocation' value?
     */
    public LocationAsyncTask(Context _context, LocationTaskInformer callback, boolean _useLastLocation) {
        mContext = _context;
        this.mCallback = new WeakReference<>(callback);
        this.useLastLocation = _useLastLocation;
    }

    /**
     * set 'useLastLocation' value.
     * @param _useLastLocation will use 'getLastLocation' value?
     */
    public void setUseLastLocation(boolean _useLastLocation) {
        this.useLastLocation = _useLastLocation;
    }

    /**
     * When you don't want to use default separator (PIPE), use this function to change it.
     * @param _separator
     */
    public void changeSeparator(String _separator) {
        this.separator = _separator;
    }

    @Override
    protected String doInBackground(String... strings) {

        // final String result = "This is Just Test result";

        // initialize location settings.
        initLocationUpdate();

        // wait till mResult secured.
        while(TextUtils.isEmpty(mResult)) {
            // Dlog.out(TAG, "I'm waiting result of location...", Dlog.d);
        }

        // to post execute
        return mResult;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        // stop location updates
        stopLocationUpdates();

        // make sure your caller is alive.
        final LocationTaskInformer callback = mCallback.get();

        if (callback != null) {
            callback.onTaskDone(s);
        } else {
            Dlog.out(TAG, "My Caller has gone, callback unavailable", Dlog.w);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        Dlog.out(TAG, "LOCATION async task has been cancelled.", Dlog.w);
    }

    public void executeAsyncTask(String... strings) {

        // Add CountDownTimer to asyncTask
        new AsyncTaskCancelTimer(this, timeoutValue, timeoutCheckInterval, true).start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, strings);
        } else {
            this.execute(strings);
        }

    }

    /*
	// ********************************************************************************************
	// LOCATION guys
	// ********************************************************************************************
	*/

    private void initLocationUpdate() {

        Dlog.out(TAG, "LOCATION. FN_initLocationUpdate", Dlog.i);

        // Check Base Guys.

        if (fusedLocationClient == null) {
            initFusedLocationClient();
        }

        if (mLocationCallback == null) {
            createLocationCallback();
        }

        if (mLocationRequest == null) {
            createLocationRequest();
        }

    }

    /**
     * Source comes from Google developers site <br>
     * @see <a href="https://developer.android.com/training/location/retrieve-current">Google Link</a>
     * @since 2019 Mar 26
     * @author boss0430
     */
    private void initFusedLocationClient() {

        Dlog.out(TAG, "LOCATION. FN_initFusedLocationClient", Dlog.i);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Dlog.out(TAG, "Getting Location has failed because permission not granted", Dlog.e);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Dlog.out(TAG, "LOCATION : fusedLocationClient has got last location successfully.", Dlog.d);
                Dlog.out(TAG, "LOCATION : result location (may can be null) : " + location, Dlog.i);

                // Got Last Known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    Dlog.out(TAG, "LOCATION : lastLocationLatitude : " + location.getLatitude(), Dlog.i);
                    Dlog.out(TAG, "LOCATION : lastLocationLongitude : " + location.getLongitude(), Dlog.i);


                    if (useLastLocation) {
                        mResult = location.getLatitude() + separator + location.getLongitude();
                        Dlog.out(TAG, "LOCATION : mResult has been set to : " + mResult + " in getLastLocation", Dlog.i);
                    }
                }
            }
        });

    }

    /**
     * Source comes from Google developers site <br>
     * @see <a href="https://developer.android.com/training/location/receive-location-updates">Google Link</a>
     * @since 2019 Mar 26
     * @author boss0430
     */
    private void createLocationCallback() {

        Dlog.out(TAG, "LOCATION. FN_createLocationCallback", Dlog.i);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // super.onLocationResult(locationResult);
                if (locationResult == null) {
                    Dlog.out(TAG, "LOCATION : location result of LocationCallback is null", Dlog.w);
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Handle location data
                    Dlog.out(TAG, "LOCATION : Latitude of LocationCallback : " + location.getLatitude(), Dlog.i);
                    Dlog.out(TAG, "LOCATION : Longitude of LocationCallback : " + location.getLongitude(), Dlog.i);
                    mResult = location.getLatitude() + separator + location.getLongitude();
                    Dlog.out(TAG, "mResult has been set to : " + mResult + " in locationCallback", Dlog.i);
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                // super.onLocationAvailability(locationAvailability);
                Dlog.out(TAG, "LOCATION : locationAvailability.isLocationAvailable:" + locationAvailability.isLocationAvailable(), Dlog.w);
            }
        };
    }

    /**
     * Source comes from Google developers site <br>
     * @see <a href="https://developer.android.com/training/location/change-location-settings.html#java">Google Link</a>
     * @since 2019 Mar 26
     * @author boss0430
     */
    private void createLocationRequest() {

        Dlog.out(TAG, "LOCATION. FN_createLocationRequest", Dlog.i);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(mContext);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Dlog.out(TAG, "LOCATION : All location settings are satisfied. The client can initialize", Dlog.d);

                // location requests here.
                startLocationUpdates();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Dlog.out(TAG, "LOCATION : location settings failed", Dlog.d);
            }
        });

        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Dlog.out(TAG, "LOCATION : (onComplete) All location settings are satisfied.response : " + response.toString(), Dlog.d);

                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing to user a dialog.

                            try {
                                // cast to resolvable exception
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                // Show The Dialog by calling startResolutionForResult(),
                                // and may check the result in onActivityResult..
                                // but i'm asyncTask and don't want to handle onActivityResult > set requestCode to 0.
                                resolvable.startResolutionForResult((Activity) mContext, 0);
                            } catch (IntentSender.SendIntentException e1) {
                                Dlog.out(TAG, "LOCATION : SendIntentException : " + e1.toString(), Dlog.e);
                            } catch (ClassCastException e2) {
                                Dlog.out(TAG, "LOCATION : ClassCastException : " + e2.toString(), Dlog.e);
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            Dlog.out(TAG, "LOCATION : YOU CAN NOT ACCESS LOCATION INFORMATION with your configuration", Dlog.e);
                            break;
                    }
                }
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Dlog.out(TAG, "Start Location Updates has failed because permission not granted", Dlog.e);
            return;
        }

        Dlog.out(TAG, "LOCATION : right b4 requestLocationUpdates", Dlog.i);
        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /*Looper*/);
    }
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    /*
	// ********************************************************************************************
	// Cancel async task by timer (when task running permanently)
	// ********************************************************************************************
	*/

    /**
     * Watch async task and cancel it when count down has finished.
     * @see <a href="http://sjava.net/2015/03/asynctask%EC%97%90-%ED%83%80%EC%9E%84%EC%95%84%EC%9B%83timeout-%EC%B6%94%EA%B0%80%ED%95%98%EA%B8%B0/">source link</a><br>
     * @author boss0430
     * @since 2017 Mar 08
     */
    private static class AsyncTaskCancelTimer extends CountDownTimer {

        private AsyncTask asyncTask;
        private boolean interrupt;

        private AsyncTaskCancelTimer(AsyncTask asyncTask, long startTime, long interval, boolean interrupt) {
            super(startTime, interval);
            this.asyncTask = asyncTask;
            this.interrupt = interrupt;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (asyncTask == null) {
                this.cancel();
                return;
            }

            if (asyncTask.isCancelled())
                this.cancel();

            if (asyncTask.getStatus() == Status.FINISHED)
                this.cancel();
        }

        @Override
        public void onFinish() {
            if (asyncTask == null || asyncTask.isCancelled())
                return;

            try {
                if (asyncTask.getStatus() == Status.FINISHED)
                    return;

                if (asyncTask.getStatus() == Status.PENDING || asyncTask.getStatus() == Status.RUNNING) {

                    asyncTask.cancel(interrupt);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
