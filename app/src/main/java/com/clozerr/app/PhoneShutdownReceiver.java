package com.clozerr.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PhoneShutdownReceiver extends BroadcastReceiver {
    private static final String TAG = "PhoneShutdownReceiver";

    public PhoneShutdownReceiver() { super(); }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "shutting down");
        GenUtils.RunManager.clearKeys(context);
    }
}
