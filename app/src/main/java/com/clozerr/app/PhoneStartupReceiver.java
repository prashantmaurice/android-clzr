package com.clozerr.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PhoneStartupReceiver extends BroadcastReceiver {
    public PhoneStartupReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        //BeaconFinderService.startPeriodicScan(context, false);
        //PeriodicBFS.startScan(context);
        Log.e("startup", "at phone startup");
    }
}
