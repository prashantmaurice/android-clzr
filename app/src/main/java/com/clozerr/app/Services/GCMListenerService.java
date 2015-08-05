package com.clozerr.app.Services;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class GCMListenerService extends GcmListenerService {

    public GCMListenerService() {
    }

    private static final String TAG = "NotificationListenerSvc";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String key = data.getString("key");
        String value = data.getString("value");
        String enum_text = data.getString("enum_text");
        String extra = data.getString("extra");
        Log.i(TAG, "From: " + from);
        Log.i(TAG, "Message: " + message);

        /**
         * Process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
    }
    // [END receive_message]
}
