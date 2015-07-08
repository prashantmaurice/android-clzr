package com.clozerr.app;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
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
public class GeofenceManagerService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static final String TAG = "GeofenceManagerService";
    private static final String KEY_GEOFENCE_SERVICE_RUNNING = "com.clozerr.app.KEY_GEOFENCE_SERVICE_RUNNING";
    private static final String GEOFENCE_PARAMS_FILE_NAME = "geofenceParams.txt";
    private static final float GEOFENCE_MINIMUM_RADIUS_METERS = 100.0F;
    private static final long GEOFENCE_EXPIRATION = TimeUnit.MILLISECONDS.convert(1L, TimeUnit.DAYS);
    private static final int GEOFENCE_DWELL_TIME = (int)TimeUnit.MILLISECONDS.convert(5L, TimeUnit.SECONDS);
        // of the int type as Geofencing API accepts only int delay
    private static final GenUtils.RunManager runManager = new GenUtils.RunManager(KEY_GEOFENCE_SERVICE_RUNNING);

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
    //private static boolean running = false;

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
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "in onDestroy()");
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                getGeofencePendingIntent()
        ).setResultCallback(this);
        mGoogleApiClient.disconnect();
        checkAndStopService(this);
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

    private GeofencingRequest getGeofencingRequest() {
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

    private Location getLastLocation() {
        /*Location result;
        do {
            result = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } while (result == null);
        return result;*/
        if (mGoogleApiClient.isConnected())
            return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        else return null;
    }

    private String getGeofenceUriString() {
        Location lastLocation = getLastLocation();
        Uri.Builder listNearBuilder = GenUtils.getClearedUriBuilder(Constants.URLBuilders.GEOFENCE_LIST_NEAR);
        if (lastLocation != null) {
            Log.e(TAG, "last location - (" + lastLocation.getLatitude() + "," + lastLocation.getLongitude() + ")");
            listNearBuilder.appendQueryParameter("latitude", String.valueOf(lastLocation.getLatitude()))
                    .appendQueryParameter("longitude", String.valueOf(lastLocation.getLongitude()));
        }

        return listNearBuilder.build().toString();
    }

    private void loadURLAndAddGeofences() {
        String geofenceUriString = getGeofenceUriString();
        Ion.with(this).load(geofenceUriString).asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if (e != null) {
                            e.printStackTrace();
                        } else {
                            try {
                                JSONArray root = new JSONArray(result.toString());
                                GenUtils.writeDownloadedStringToFile(GeofenceManagerService.this,
                                        root.toString(), GEOFENCE_PARAMS_FILE_NAME);
                                for (int i = 0; i < root.length(); ++i) {
                                    JSONObject fenceObject = root.getJSONObject(i);
                                    geofenceParamsHashMap.put(fenceObject.getString("_id"),
                                            new GeofenceParams(fenceObject));
                                }
                                LocationServices.GeofencingApi.removeGeofences(
                                        mGoogleApiClient,
                                        getGeofencePendingIntent()
                                ).setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(Status status) {
                                        if (status.isSuccess())
                                            LocationServices.GeofencingApi.addGeofences(
                                                    mGoogleApiClient,
                                                    getGeofencingRequest(),
                                                    getGeofencePendingIntent()
                                            ).setResultCallback(GeofenceManagerService.this);
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
        if (runManager.signalStart(context)) {
            // download data about beacons, which will be part of this service
            // and will be running daily
            BeaconDBDownloadBaseReceiver.scheduleDownload(context);
            // start service
            context.startService(new Intent(context, GeofenceManagerService.class));
        }
    }

    public static void checkAndStopService(final Context context) {
        if (runManager.signalStop(context)) {
            BeaconDBDownloadBaseReceiver.stopDownloads(context);
            context.stopService(new Intent(context, GeofenceManagerService.class));
        }
    }

    public static void readGeofenceParamsFromFile(Context context) {
        try {
            String fileContents = GenUtils.readFileContentsAsString(context, GEOFENCE_PARAMS_FILE_NAME);
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

    @Override
    public void onConnected(Bundle bundle) {
        Log.e(TAG, "in onConnected()");
        loadURLAndAddGeofences();
        /*GeofenceParams house = geofenceParamsHashMap.get("House");
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
        Log.e(TAG, "distance from IC (m): " + String.valueOf(resultIc[0]));*/
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "in onConnectionSuspended(" + i + ")");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "in onConnectionFailed(" + connectionResult.getErrorCode() + ")");
        mGoogleApiClient.connect();
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

    public class GeofenceTransitionsIntentService extends IntentService {

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

            Location lastLocation = GeofenceManagerService.this.getLastLocation();
            SharedPreferences sharedPreferences = getSharedPreferences("USER", 0);
            String TOKEN = sharedPreferences.getString("token", "");
            TimeZone tz = TimeZone.getTimeZone("GMT+0530");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
            df.setTimeZone(tz);
            String nowAsISO = df.format(new Date());
            final Uri.Builder analytics = GenUtils.getClearedUriBuilder(Constants.URLBuilders.ANALYTICS)
                    .appendQueryParameter("metric","Clozerr_Home_Screen")
                    .appendQueryParameter("dimensions[device]", "Android API " + Build.VERSION.SDK_INT)
                    .appendQueryParameter("dimensions[id]", Settings.Secure.getString(this.getContentResolver(),
                            Settings.Secure.ANDROID_ID))
                    .appendQueryParameter("time", nowAsISO)
                    .appendQueryParameter("access_token", TOKEN);
            if (lastLocation != null)
                analytics.appendQueryParameter("latitude", String.valueOf(lastLocation.getLatitude()))
                    .appendQueryParameter("longitude", String.valueOf(lastLocation.getLongitude()));
            // TODO put more analytics about entry/exit etc and ping this URL

            for (Geofence triggeringGeofence : geofencingEvent.getTriggeringGeofences()) {
                params = geofenceParamsHashMap.get(triggeringGeofence.getRequestId());
                geofenceTypes = params.getIncludedTypes();
                Log.e(TAG, "included types - " + TextUtils.join(", ", geofenceTypes));

                switch (geofenceTransition) {
                    case Geofence.GEOFENCE_TRANSITION_ENTER:
                        Log.e(TAG, "entered " + triggeringGeofence.getRequestId());
                        if (geofenceTypes.contains(GEOFENCE_TYPE_RANGE)) {
                            PeriodicBFS.checkAndStartScan(this);
                        }
                        if (geofenceTypes.contains(GEOFENCE_TYPE_RELOAD)) {
                            GeofenceManagerService.this.loadURLAndAddGeofences();
                        }
                        if (geofenceTypes.contains(GEOFENCE_TYPE_PUSH)) {
                            // TODO push a notification to the user using the details in 'params'
                        }
                        break;
                    case Geofence.GEOFENCE_TRANSITION_EXIT:
                        Log.e(TAG, "exited " + triggeringGeofence.getRequestId());
                        if (geofenceTypes.contains(GEOFENCE_TYPE_RANGE)) {
                            PeriodicBFS.checkAndStopScan(this);
                        }
                        if (geofenceTypes.contains(GEOFENCE_TYPE_ON_EXIT)) {
                            if (geofenceTypes.contains(GEOFENCE_TYPE_RELOAD)) {
                                GeofenceManagerService.this.loadURLAndAddGeofences();
                            }
                            if (geofenceTypes.contains(GEOFENCE_TYPE_PUSH)) {
                                // TODO push a notification to the user using the details in 'params'
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

        /*private String getGeofenceTransitionDetails(
                Context context,
                int geofenceTransition,
                List<Geofence> triggeringGeofences) {

            String geofenceTransitionString = getTransitionString(geofenceTransition);

            // Get the Ids of each geofence that was triggered.
            ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
            ArrayList<String> paramsArrayList = new ArrayList<>();
            for (Geofence geofence : triggeringGeofences) {
                triggeringGeofencesIdsList.add(geofence.getRequestId());
                GeofenceManagerService.GeofenceParams params =
                        GeofenceManagerService.geofenceParamsHashMap.get(geofence.getRequestId());
                paramsArrayList.add(params.getTypeString());
            }
            String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);
            String paramsString = TextUtils.join(", ", paramsArrayList);

            return geofenceTransitionString + ": " + triggeringGeofencesIdsString + "@" + paramsString;
        }

        private void sendNotification(String notificationDetails) {
            // Get a notification builder that's compatible with platform versions >= 4
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

            String[] split = notificationDetails.split("@");

            // Define the notification settings.
            builder.setSmallIcon(R.drawable.ic_launcher)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setColor(Color.RED)
                    .setContentTitle(split[0])
                    .setContentText(split[1])
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(split[1]))
                    .setAutoCancel(true);

            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(0, builder.build());
        }

        private String getTransitionString(int transitionType) {
            switch (transitionType) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    return "Entered";
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    return "Exited";
                case Geofence.GEOFENCE_TRANSITION_DWELL:
                    return "Dwelling";
                default:
                    return "Unknown Transition";
            }
        }*/
    }
}
