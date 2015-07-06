package com.clozerr.app;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;

/**
 * Created by aravind on 2/7/15.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "FenceTransitionService";

    public GeofenceTransitionsIntentService() { super(TAG); }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(TAG, "handling intent");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceManagerService.GeofenceErrorMessages.getErrorString(this,
                                                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        // TODO check for possibilities that this may need to check multiple geofences
        Geofence triggeringGeofence = geofencingEvent.getTriggeringGeofences().get(0);
        GeofenceManagerService.GeofenceParams params =
                GeofenceManagerService.geofenceParamsHashMap.get(triggeringGeofence.getRequestId());
        ArrayList<Integer> geofenceTypes = params.getIncludedTypes();
        Log.e(TAG, "included types - " + TextUtils.join(", ", geofenceTypes));

        switch (geofenceTransition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.e(TAG, "entered " + triggeringGeofence.getRequestId());
                if (geofenceTypes.contains(GeofenceManagerService.GEOFENCE_TYPE_RANGE)) {
                    PeriodicBFS.checkAndStartScan(this);
                }
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.e(TAG, "exited " + triggeringGeofence.getRequestId());
                if (geofenceTypes.contains(GeofenceManagerService.GEOFENCE_TYPE_RANGE)) {
                    PeriodicBFS.checkAndStopScan(this);
                }
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.e(TAG, "dwelling in " + triggeringGeofence.getRequestId());
                break;
            default:
                Log.e(TAG, "unknown transition for " + triggeringGeofence.getRequestId());
                break;
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
