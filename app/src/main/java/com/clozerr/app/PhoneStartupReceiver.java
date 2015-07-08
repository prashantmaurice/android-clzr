package com.clozerr.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by aravind on 8/7/15.
 */
public class PhoneStartupReceiver extends BroadcastReceiver {
    private static final String TAG = "PhoneStartupReceiver";

    public PhoneStartupReceiver() { super(); }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "boot done");
        GenUtils.RunManager.initKeys(context);
        GeofenceManagerService.checkAndStartService(context);
    }
}
