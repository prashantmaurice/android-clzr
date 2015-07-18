package com.clozerr.app;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by aravind on 1/7/15.
 */
public class GeofenceManagerService extends Service {

    private static final String TAG = "GeofenceManagerService";
    //private static final String KEY_GEOFENCE_SERVICE_RUNNING = "com.clozerr.app.KEY_GEOFENCE_SERVICE_RUNNING";
    private static final float GEOFENCE_MINIMUM_RADIUS_METERS = 100.0F;
    private static final long GEOFENCE_EXPIRATION = TimeUnit.MILLISECONDS.convert(1L, TimeUnit.DAYS);
    //private static final int GEOFENCE_DWELL_TIME = (int)TimeUnit.MILLISECONDS.convert(5L, TimeUnit.SECONDS);
        // of the int type as Geofencing API accepts only int delay
    private static final int NOTIFICATION_ID = 100;

    public static final HashMap<String, GeofenceParams> geofenceParamsHashMap = new HashMap<>();
    // Geofence types; all of them must start with "GEOFENCE_TYPE_" as the array holding them needs that
    public static final int GEOFENCE_TYPE_RANGE = 0x1;
    public static final int GEOFENCE_TYPE_RELOAD = 0x2;
    public static final int GEOFENCE_TYPE_PING = 0x4;
    public static final int GEOFENCE_TYPE_PUSH = 0x8;
    public static final int GEOFENCE_TYPE_ON_EXIT = 0x10;
    // Convenience array of all the GEOFENCE_TYPE fields' values.
    public static final ArrayList<Integer> GEOFENCE_TYPES = new ArrayList<>();

    // HashMap storing details of all geofences to be tracked at any point of time.
    private static GeofencingRequest geofencingRequest = null;
    private static PendingIntent geofencePendingIntent = null;
    private static boolean running = false;

    static {
        for (Field field : GeofenceManagerService.class.getDeclaredFields())
            if (field.getName().matches("GEOFENCE_TYPE_.*") && field.getType().equals(int.class))
                try {
                    GEOFENCE_TYPES.add(field.getInt(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

        /*// My house.
        geofenceParamsHashMap.put("House", new GeofenceParams(new LatLng(12.9792007, 80.2617149),
                GEOFENCE_MINIMUM_RADIUS_METERS, GEOFENCE_TYPE_RANGE));

        // IITM IC.
        geofenceParamsHashMap.put("IITM IC", new GeofenceParams(new LatLng(12.9909858, 80.2427169),
                GEOFENCE_MINIMUM_RADIUS_METERS, GEOFENCE_TYPE_RANGE));

        // West Mambalam - Postal Colony 1st St.
        geofenceParamsHashMap.put("West Mambalam", new GeofenceParams(new LatLng(13.0377082,80.2175648),
                GEOFENCE_MINIMUM_RADIUS_METERS, GEOFENCE_TYPE_RANGE));*/
    }

    private static GoogleApiClient googleApiClient = null;

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
        googleApiClient.connect();
        return START_REDELIVER_INTENT;
    }

    /*@Override
    public void onDestroy() {
        Log.e(TAG, "in onDestroy()");
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                getGeofencePendingIntent(this)
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Log.e(TAG, "in onResult(" + status.getStatusMessage() + ") - for onDestroy removal of geofences");
                if (status.isSuccess())
                    Log.e(TAG, "success");
                else if (status.isCanceled())
                    Log.e(TAG, "canceled");
                else if (status.isInterrupted())
                    Log.e(TAG, "interrupted");
            }
        });
        googleApiClient.disconnect();
        checkAndStopService(this);
        super.onDestroy();
    }*/

    private synchronized void buildGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Log.e(TAG, "in onConnected()");
                            loadURLAndAddGeofences(GeofenceManagerService.this);
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            Log.e(TAG, "in onConnectionSuspended(" + i + ")");
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            Log.e(TAG, "in onConnectionFailed(" + connectionResult.getErrorCode() + ")");
                            googleApiClient.connect();
                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private static PendingIntent getGeofencePendingIntent(Context context) {
        if (geofencePendingIntent == null) {
            Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
            geofencePendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return geofencePendingIntent;
    }

    private static GeofencingRequest getGeofencingRequest() {
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
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .build();
        }
        return geofencingRequest;
    }

    private static Location getLastLocation() {
        /*Location result;
        do {
            result = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        } while (result == null);
        return result;*/
        if (googleApiClient.isConnected())
            return LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        else return null;
    }

    private static String getGeofenceUriString() {
        Location lastLocation = getLastLocation();
        Uri.Builder listNearBuilder = GenUtils.getClearedUriBuilder(Constants.URLBuilders.GEOFENCE_LIST_NEAR);
        if (lastLocation != null) {
            Log.e(TAG, "last location - (" + lastLocation.getLatitude() + "," + lastLocation.getLongitude() + ")");
            listNearBuilder.appendQueryParameter("latitude", String.valueOf(lastLocation.getLatitude()))
                    .appendQueryParameter("longitude", String.valueOf(lastLocation.getLongitude()));
        }

        return listNearBuilder.build().toString();
    }

    private static void loadURLAndAddGeofences(final Context context) {
        String geofenceUriString = getGeofenceUriString();
        Log.e(TAG, "fence url - " + geofenceUriString);
        Ion.with(context).load(geofenceUriString).asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if (e != null) {
                            e.printStackTrace();
                            loadURLAndAddGeofences(context);
                        } else {
                            try {
                                JSONArray root = new JSONArray(result.toString());
                                GenUtils.writeDownloadedStringToFile(context,
                                        root.toString(), Constants.FileNames.GEOFENCE_PARAMS);
                                for (int i = 0; i < root.length(); ++i) {
                                    JSONObject fenceObject = root.getJSONObject(i);
                                    geofenceParamsHashMap.put(fenceObject.getString("_id"),
                                            new GeofenceParams(fenceObject));
                                }
                                LocationServices.GeofencingApi.removeGeofences(
                                        googleApiClient,
                                        getGeofencePendingIntent(context)
                                ).setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(Status status) {
                                        if (status.isSuccess())
                                            LocationServices.GeofencingApi.addGeofences(
                                                    googleApiClient,
                                                    getGeofencingRequest(),
                                                    getGeofencePendingIntent(context)
                                            ).setResultCallback(new ResultCallback<Status>() {
                                                @Override
                                                public void onResult(Status status) {
                                                    Log.e(TAG, "in onResult(" + status.getStatusMessage() + ")" +
                                                    " - for adding geofences");
                                                    if (status.isSuccess())
                                                        Log.e(TAG, "success");
                                                    else if (status.isCanceled())
                                                        Log.e(TAG, "canceled");
                                                    else if (status.isInterrupted())
                                                        Log.e(TAG, "interrupted");
                                                }
                                            });
                                        else Log.e(TAG, "error : " + status.getStatusMessage());
                                    }
                                });
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
    }

    public static void checkAndStartService(final Context context) {
        if (/*runManager.signalStart(context)*/!running) {
            running = true;
            // download data about beacons, which will be part of this service
            // and will be running daily
            BeaconDBDownloadBaseReceiver.scheduleDownload(context);

            context.startService(new Intent(context, GeofenceManagerService.class));
        }
    }

    public static void checkAndStopService(final Context context) {
        if (/*runManager.signalStop(context)*/running) {
            running = false;
            BeaconDBDownloadBaseReceiver.stopDownloads(context);

            context.stopService(new Intent(context, GeofenceManagerService.class));
        }
    }

    public static void readGeofenceParamsFromFile(Context context) {
        try {
            String fileContents = GenUtils.readFileContentsAsString(context, Constants.FileNames.GEOFENCE_PARAMS);
            geofenceParamsHashMap.clear();
            JSONArray root = new JSONArray(fileContents);
            for (int i = 0; i < root.length(); ++i) {
                JSONObject fenceObject = root.getJSONObject(i);
                geofenceParamsHashMap.put(fenceObject.getString("_id"), new GeofenceParams(fenceObject));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*@Override
    public void onConnected(Bundle bundle) {
        Log.e(TAG, "in onConnected()");
        loadURLAndAddGeofences(this);
        *//*GeofenceParams house = geofenceParamsHashMap.get("House");
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
        Log.e(TAG, "distance from IC (m): " + String.valueOf(resultIc[0]));*//*
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "in onConnectionSuspended(" + i + ")");
        //googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "in onConnectionFailed(" + connectionResult.getErrorCode() + ")");
        googleApiClient.connect();
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
    }*/

    public static class GeofenceParams {
        public LatLng mCoordinates;
        public float mRadius;
        public int mType;
        public JSONObject mParams;

        public GeofenceParams() {}

        public GeofenceParams(JSONObject fenceObject) throws JSONException{
            float radius = (float) fenceObject.getDouble("radius");
            mRadius = (radius > GEOFENCE_MINIMUM_RADIUS_METERS) ? radius : GEOFENCE_MINIMUM_RADIUS_METERS;

            JSONArray locationArray = fenceObject.getJSONArray("location");
            mCoordinates = new LatLng(locationArray.getDouble(0), locationArray.getDouble(1));

            mType = fenceObject.getInt("type");

            mParams = fenceObject.has("params") ? fenceObject.getJSONObject("params") : null;
        }

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

    public static class GeofenceTransitionsIntentService extends IntentService {

        private static final String TAG = "FenceTransitionService";

        public GeofenceTransitionsIntentService() { super(TAG); }

        @Override
        protected void onHandleIntent(Intent intent) {
            Log.e(TAG, "handling intent");
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                String errorMessage = GeofenceErrorMessages.getErrorString(this,
                                                        geofencingEvent.getErrorCode());
                Log.e(TAG, errorMessage);
                return;
            }

            int geofenceTransition = geofencingEvent.getGeofenceTransition();
            GeofenceParams params = null;
            ArrayList<Integer> geofenceTypes = null;

            readGeofenceParamsFromFile(this);

            Location lastLocation = GeofenceManagerService.getLastLocation();
            SharedPreferences sharedPreferences = getSharedPreferences("USER", 0);
            String TOKEN = sharedPreferences.getString("token", "");
            TimeZone tz = TimeZone.getTimeZone("GMT+0530");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
            df.setTimeZone(tz);
            String nowAsISO = df.format(new Date());
            final Uri.Builder analytics = GenUtils.getClearedUriBuilder(Constants.URLBuilders.ANALYTICS)
                    .appendQueryParameter("metric","Geofence_Transition")
                    .appendQueryParameter("dimensions[device]", "Android API " + Build.VERSION.SDK_INT)
                    .appendQueryParameter("dimensions[id]", Settings.Secure.getString(this.getContentResolver(),
                            Settings.Secure.ANDROID_ID))
                    .appendQueryParameter("time", nowAsISO)
                    .appendQueryParameter("access_token", TOKEN);
            if (lastLocation != null)
                analytics.appendQueryParameter("dimensions[latitude]", String.valueOf(lastLocation.getLatitude()))
                    .appendQueryParameter("dimensions[longitude]", String.valueOf(lastLocation.getLongitude()));

            for (Geofence triggeringGeofence : geofencingEvent.getTriggeringGeofences()) {
                params = geofenceParamsHashMap.get(triggeringGeofence.getRequestId());
                geofenceTypes = params.getIncludedTypes();
                Log.e(TAG, "included types - " + TextUtils.join(", ", geofenceTypes));

                switch (geofenceTransition) {
                    case Geofence.GEOFENCE_TRANSITION_ENTER:
                        Log.e(TAG, "entered " + triggeringGeofence.getRequestId());
                        analytics.appendQueryParameter("dimensions[geofence]", "entered " + triggeringGeofence.getRequestId());
                        final String urlEntry = analytics.build().toString();
                        Ion.with(this).load(urlEntry).asString()
                                .setCallback(new FutureCallback<String>() {
                                    @Override
                                    public void onCompleted(Exception e, String result) {
                                        if (e != null)
                                            e.printStackTrace();
                                        else
                                            Log.e(TAG, urlEntry);
                                    }
                                });
                        if (geofenceTypes.contains(GEOFENCE_TYPE_RANGE)) {
                            PeriodicBFS.checkAndStartScan(this);
                        }
                        if (geofenceTypes.contains(GEOFENCE_TYPE_RELOAD)) {
                            GeofenceManagerService.loadURLAndAddGeofences(this);
                        }
                        if (geofenceTypes.contains(GEOFENCE_TYPE_PUSH)) {
                            setupNotification(this, params);
                        }
                        break;
                    case Geofence.GEOFENCE_TRANSITION_EXIT:
                        Log.e(TAG, "exited " + triggeringGeofence.getRequestId());
                        analytics.appendQueryParameter("dimensions[geofence]", "exited " + triggeringGeofence.getRequestId());
                        final String urlExit = analytics.build().toString();
                        Ion.with(this).load(urlExit).asString()
                                .setCallback(new FutureCallback<String>() {
                                    @Override
                                    public void onCompleted(Exception e, String result) {
                                        if (e != null)
                                            e.printStackTrace();
                                        else
                                            Log.e(TAG, urlExit);
                                    }
                                });
                        if (geofenceTypes.contains(GEOFENCE_TYPE_RANGE)) {
                            PeriodicBFS.checkAndStopScan(this);
                        }
                        if (geofenceTypes.contains(GEOFENCE_TYPE_ON_EXIT)) {
                            if (geofenceTypes.contains(GEOFENCE_TYPE_RELOAD)) {
                                GeofenceManagerService.loadURLAndAddGeofences(this);
                            }
                            if (geofenceTypes.contains(GEOFENCE_TYPE_PUSH)) {
                                setupNotification(this, params);
                            }
                        }
                        break;
                    case Geofence.GEOFENCE_TRANSITION_DWELL:
                        Log.e(TAG, "dwelling in " + triggeringGeofence.getRequestId());
                        break;
                    default:
                        Log.e(TAG, "unknown transition for " + triggeringGeofence.getRequestId());
                        break;
                }
            }
            /*
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                // Get the geofences that were triggered. A single event can trigger multiple geofences.
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                // Get the transition details as a String.
                String geofenceTransitionDetails = getGeofenceTransitionDetails(
                        this,
                        geofenceTransition,
                        triggeringGeofences
                );

                sendNotification(geofenceTransitionDetails);
                Log.e(TAG, geofenceTransitionDetails);
            } else {
                Log.e(TAG, "invalid transition type " + geofenceTransition);
            }
            */
        }

        private void notify( String title, String content ){
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon( R.drawable.ic_launcher )
                            .setContentTitle( title )
                            .setContentText( content )
                            .setDefaults(NotificationCompat.DEFAULT_ALL);
            Intent resultIntent = new Intent(this, Home.class);
            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(Home.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }

        @SuppressWarnings("deprecation")
        protected void setupNotification(Context context, GeofenceParams params) {
            Log.e(TAG, "setting up");
            try {
                String type = params.mParams.has("type") ? params.mParams.getString("type") : "";
                String message = "", title = "";
                // make notifications here
                switch (type) {
                    case "STANDARD":
                        message = params.mParams.getString("message");
                        title = params.mParams.getString("title");
                        notify(title, message);
                        break;
                    case "REVIEW":
                        String checkin_id = params.mParams.getString("checkin_id");
                        String vendor_id = params.mParams.getString("vendor_id");
                        message = params.mParams.getString("message");
                        title = params.mParams.getString("title");
                        notifyreview(title, message, checkin_id, vendor_id);
                        break;
                    default: break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void notifyreview(String title, String message, String checkin_id, String vendor_id) {
            //Uri sound_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            //builder.setSound(alarmSound);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon( R.drawable.ic_launcher )
                            .setContentTitle( title )
                            .setContentText( message )
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setAutoCancel( true );

            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, VendorActivity.class);
            resultIntent.putExtra("from_notify_review",true);
            resultIntent.putExtra("checkin_id",checkin_id);
            resultIntent.putExtra("vendor_id", vendor_id);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(Home.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            //int mId = 1234;
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }
}
