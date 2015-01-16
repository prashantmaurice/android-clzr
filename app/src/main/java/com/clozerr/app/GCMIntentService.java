package com.clozerr.app;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

import static com.clozerr.app.Home.SENDER_ID;


public class GCMIntentService extends GCMBaseIntentService {

    public GCMIntentService() {
        super(SENDER_ID);
    }

    private static final String TAG = "===GCMIntentService The manual===";

    @Override
    protected void onRegistered(Context arg0, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
      // write gcm key push code here
    }


    @Override
    protected void onUnregistered(Context arg0, String arg1) {
        Log.i(TAG, "unregistered = " + arg1);
    }


    
    @SuppressWarnings("deprecation")
	@ Override
    protected void onMessage(Context context, Intent intent) {
        Log.d(TAG, "onMessage - context: " + context);

        // make notifications here

    }

    @
    Override
    protected void onError(Context arg0, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
    }

    @
    Override
    protected boolean onRecoverableError(Context context, String errorId) {
        return super.onRecoverableError(context, errorId);
    }
}