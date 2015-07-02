package com.clozerr.app;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by aravind on 1/7/15.
 */
public class GeofenceManagerService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static final String TAG = "GeofenceManagerService";
    private static final float GEOFENCE_RADIUS_IN_METERS = 100.0F;
    private static final long GEOFENCE_EXPIRATION = TimeUnit.MILLISECONDS.convert(1L, TimeUnit.DAYS);
    private static final int GEOFENCE_DWELL_TIME = (int)TimeUnit.MILLISECONDS.convert(5L, TimeUnit.SECONDS);
        // of the int type as Geofencing API accepts only int delay

    // Geofence types
    public static final int GEOFENCE_TYPE_RANGE = 0x1;
    public static final int GEOFENCE_TYPE_RELOAD = 0x2;
    public static final int GEOFENCE_TYPE_PING = 0x4;
    public static final int GEOFENCE_TYPE_PUSH = 0x8;
    public static final int GEOFENCE_TYPE_ON_EXIT = 0x10;

    public static HashMap<String, GeofenceParams> geofenceParamsHashMap = new HashMap<>();
    private static ArrayList<Geofence> geofenceList = null;
    private static PendingIntent geofencePendingIntent = null;

    static {
        // My house.
        geofenceParamsHashMap.put("House", new GeofenceParams(new LatLng(12.9791381, 80.2617326),
                GEOFENCE_RADIUS_IN_METERS, GEOFENCE_TYPE_RANGE | GEOFENCE_TYPE_ON_EXIT));

        // IITM IC.
        geofenceParamsHashMap.put("IITM IC", new GeofenceParams(new LatLng(12.9909858, 80.2427169),
                GEOFENCE_RADIUS_IN_METERS, GEOFENCE_TYPE_RANGE));
    }

    private GoogleApiClient mGoogleApiClient = null;

    @Override
    public void onCreate() {
        super.onCreate();
        populateGeofenceList();
        buildGoogleApiClient();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mGoogleApiClient.connect();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "in onDestroy()");
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                getGeofencePendingIntent()
        ).setResultCallback(this);
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    private synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            Log.e(TAG, "building client");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        Log.e(TAG, "built client");
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent == null) {
            Log.e(TAG, "building pending intent");
            Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
            geofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        Log.e(TAG, "built pending intent");
        return geofencePendingIntent;
    }

    public void populateGeofenceList() {
        geofenceList = new ArrayList<>();
        for (HashMap.Entry<String, GeofenceParams> entry : geofenceParamsHashMap.entrySet()) {
            String id = entry.getKey();
            GeofenceParams params = entry.getValue();
            int transitionTypes = Geofence.GEOFENCE_TRANSITION_ENTER;
            if ((params.mType & GEOFENCE_TYPE_ON_EXIT) != 0)
                transitionTypes |= Geofence.GEOFENCE_TRANSITION_EXIT;
            geofenceList.add(new Geofence.Builder()
                            .setRequestId(id)
                            .setCircularRegion(
                                    params.mCoordinates.latitude,
                                    params.mCoordinates.longitude,
                                    params.mRadius
                            )
                            .setExpirationDuration(GEOFENCE_EXPIRATION)
                            .setTransitionTypes(transitionTypes)

                            // use this if DWELL type is added
                            //.setLoiteringDelay(GEOFENCE_DWELL_TIME)
                            .build()
            );
        }
        Log.e(TAG, "populated list");
    }

    public static void startService(Context context) {
        context.startService(new Intent(context, GeofenceManagerService.class));
        Log.e(TAG, "started service");
    }

    public static void stopService(Context context) {
        context.stopService(new Intent(context, GeofenceManagerService.class));
        Log.e(TAG, "stopped service");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e(TAG, "in onConnected()");
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.e(TAG, "last location - " + ((lastLocation == null) ? "null" :
                "(" + lastLocation.getLatitude() + "," + lastLocation.getLongitude() + ")"));
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                geofenceList,
                getGeofencePendingIntent()
        ).setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "in onConnectionSuspended(" + i + ")");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "in onConnectionFailed(" + connectionResult.getErrorCode() + ")");
    }

    @Override
    public void onResult(Status status) {
        Log.e(TAG, "in onResult(" + status.getStatusMessage() + ")");
        if (status.isSuccess())
            Log.e(TAG, "success");
        else if (status.isCanceled())
            Log.e(TAG, "canceled");
        else if (status.isInterrupted())
            Log.e(TAG, "interrupted");
    }

    public static class GeofenceParams {
        public LatLng mCoordinates;
        public float mRadius;
        public int mType;

        public GeofenceParams() {}

        public GeofenceParams(JSONObject geofenceObject) {}

        public GeofenceParams(LatLng coordinates, float radius, int type) {
            mCoordinates = coordinates;
            mRadius = radius;
            mType = type;
        }

        public String getTypeString() { return getTypeString(mType); }

        public static String getTypeString(int type) {
            String res = "";
            if ((type & GEOFENCE_TYPE_RANGE) != 0)
                res += "RANGE|";
            if ((type & GEOFENCE_TYPE_RELOAD) != 0)
                res += "RELOAD|";
            if ((type & GEOFENCE_TYPE_PING) != 0)
                res += "PING|";
            if ((type & GEOFENCE_TYPE_PUSH) != 0)
                res += "PUSH|";
            if ((type & GEOFENCE_TYPE_ON_EXIT) != 0)
                res += "ON_EXIT|";
            if (res.charAt(res.length() - 1) == '|')
                res = res.substring(0, res.length() - 1);
            return res;
        }
    }

    public static class GeofenceErrorMessages {

        // prevents instantiation
        private GeofenceErrorMessages() {}

        public static String getErrorString(Context context, int errorCode) {
            Resources mResources = context.getResources();
            switch (errorCode) {
                case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                    return mResources.getString(R.string.geofence_not_available);
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                    return mResources.getString(R.string.geofence_too_many_geofences);
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return mResources.getString(R.string.geofence_too_many_pending_intents);
                default:
                    return mResources.getString(R.string.unknown_geofence_error);
            }
        }
    }
}
