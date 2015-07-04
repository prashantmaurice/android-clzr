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
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.lang.reflect.Field;
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
    // Convenience array of all the GEOFENCE_TYPE fields' values.
    public static final ArrayList<Integer> GEOFENCE_TYPES = new ArrayList<>();

    public static HashMap<String, GeofenceParams> geofenceParamsHashMap = new HashMap<>();
    private static GeofencingRequest geofencingRequest = null;
    private static PendingIntent geofencePendingIntent = null;

    static {
        for (Field field : GeofenceManagerService.class.getDeclaredFields())
            if (field.getName().matches("GEOFENCE_TYPE_.*") && field.getType().equals(int.class))
                try {
                    GEOFENCE_TYPES.add(field.getInt(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

        // My house.
        geofenceParamsHashMap.put("House", new GeofenceParams(new LatLng(12.9792007, 80.2617149),
                GEOFENCE_RADIUS_IN_METERS, GEOFENCE_TYPE_RANGE));

        // IITM IC.
        geofenceParamsHashMap.put("IITM IC", new GeofenceParams(new LatLng(12.9909858, 80.2427169),
                GEOFENCE_RADIUS_IN_METERS, GEOFENCE_TYPE_RANGE));

        // West Mambalam - Postal Colony 1st St.
        geofenceParamsHashMap.put("West Mambalam", new GeofenceParams(new LatLng(13.0377082,80.2175648),
                GEOFENCE_RADIUS_IN_METERS, GEOFENCE_TYPE_RANGE));
    }

    private GoogleApiClient mGoogleApiClient = null;

    @Override
    public void onCreate() {
        super.onCreate();
        //getGeofencingRequest();
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
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent == null) {
            Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
            geofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return geofencePendingIntent;
    }

    public GeofencingRequest getGeofencingRequest() {
        if (geofencingRequest == null) {
            ArrayList<Geofence> geofenceList = new ArrayList<>();
            for (HashMap.Entry<String, GeofenceParams> entry : geofenceParamsHashMap.entrySet()) {
                String id = entry.getKey();
                GeofenceParams params = entry.getValue();
                geofenceList.add(new Geofence.Builder()
                                .setRequestId(id)
                                .setCircularRegion(
                                        params.mCoordinates.latitude,
                                        params.mCoordinates.longitude,
                                        params.mRadius
                                )
                                .setExpirationDuration(GEOFENCE_EXPIRATION)
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                                    Geofence.GEOFENCE_TRANSITION_EXIT)

                                // use this if DWELL type is added
                                //.setLoiteringDelay(GEOFENCE_DWELL_TIME)
                                .build()
                );
            }
            geofencingRequest = new GeofencingRequest.Builder()
                    .addGeofences(geofenceList)

                    // TODO temporarily set this trigger; default is INITIAL_TRIGGER_DWELL
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .build();
        }
        return geofencingRequest;
    }

    public static void startService(Context context) {
        BeaconDBDownloadBaseReceiver.scheduleDownload(context);
        //wait for downloader to start service
        //context.startService(new Intent(context, GeofenceManagerService.class));
    }

    public static void stopService(Context context) {
        BeaconDBDownloadBaseReceiver.stopDownloads(context);
        context.stopService(new Intent(context, GeofenceManagerService.class));
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e(TAG, "in onConnected()");
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(this);
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            Log.e(TAG, "last location - (" + lastLocation.getLatitude() + "," + lastLocation.getLongitude() + ")");
            GeofenceParams house = geofenceParamsHashMap.get("House");
            GeofenceParams ic = geofenceParamsHashMap.get("IITM IC");
            float[] resultHome = new float[3], resultIc = new float[3];
            Location.distanceBetween(
                    lastLocation.getLatitude(), lastLocation.getLongitude(),
                    house.mCoordinates.latitude, house.mCoordinates.longitude,
                    resultHome
            );
            Location.distanceBetween(
                    lastLocation.getLatitude(), lastLocation.getLongitude(),
                    ic.mCoordinates.latitude, ic.mCoordinates.longitude,
                    resultIc
            );
            Log.e(TAG, "distance from home (m): " + String.valueOf(resultHome[0]));
            Log.e(TAG, "distance from IC (m): " + String.valueOf(resultIc[0]));
        }
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

        public ArrayList<Integer> getIncludedTypes() { return getIncludedTypes(mType); }

        public static ArrayList<Integer> getIncludedTypes(int type) {
            ArrayList<Integer> res = new ArrayList<>();
            for (Integer i : GEOFENCE_TYPES)
                if ((type & i) != 0)
                    res.add(i);
            return res;
        }

        /*public String getTypeString() { return getTypeString(mType); }

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
        }*/
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
