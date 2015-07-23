package com.clozerr.app;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by aravind on 18/7/15.
 */
public abstract class BTStateListener extends BroadcastReceiver {
    private static final String TAG = "BTStateListener";
    public static final long DEFAULT_TIMEOUT = -1;

    private long mTimeout;
    private boolean mIsRegistered;

    public BTStateListener() { this(DEFAULT_TIMEOUT); }

    public BTStateListener(long timeout) {
        super();
        mTimeout = timeout;
        mIsRegistered = false;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent != null && intent.getAction() != null && intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            onBTStateReached(context, intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR));
        }
    }

    public void registerSelf(final Context context) {
        if (!mIsRegistered) {
            mIsRegistered = true;
            context.getApplicationContext().registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            onBTStateReached(context, BluetoothAdapter.getDefaultAdapter().getState());
            if (mTimeout >= 0) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        unregisterSelf(context);
                    }
                }, mTimeout);
            }
        }
    }

    public void unregisterSelf(Context context) {
        if (mIsRegistered) {
            mIsRegistered = false;
            context.getApplicationContext().unregisterReceiver(this);
        }
    }

    public abstract void onBTStateReached(Context context, int state);
}
