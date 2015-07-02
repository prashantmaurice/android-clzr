package com.clozerr.app;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravind on 2/7/15.
 */
public class GeofenceTransitionsIntentService extends WakefulIntentService {

    private static final String TAG = "FenceTransitionService";

    public GeofenceTransitionsIntentService() { super(TAG); }

    @Override
    protected void doWakefulWork(Intent intent) {
        Log.e(TAG, "doing wakeful work");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceManagerService.GeofenceErrorMessages.getErrorString(this,
                                                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            sendNotification(geofenceTransitionDetails);
            Log.i(TAG, geofenceTransitionDetails);
        } else {
            Log.e(TAG, "invalid transition type " + geofenceTransition);
        }
        releaseLock();
    }

    @Override
    protected boolean isListeningAfterWork() {
        return true;
    }

    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private void sendNotification(String notificationDetails) {
        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText("Geofence notification")
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
    }
}
