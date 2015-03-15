package com.clozerr.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PhoneStartupReceiver extends BroadcastReceiver {
    public PhoneStartupReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO pass all downloaded UUIDs as parameter
        //BeaconFinderService.startPeriodicScan(context);
        Log.e("startup", "at phone startup");
    }
}
