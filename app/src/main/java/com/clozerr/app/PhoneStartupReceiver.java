package com.clozerr.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PhoneStartupReceiver extends BroadcastReceiver {
    public PhoneStartupReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, BeaconFinderService.class));
    }
}
