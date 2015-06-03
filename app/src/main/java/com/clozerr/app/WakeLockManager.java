package com.clozerr.app;

import android.content.Context;
import android.os.PowerManager;

/**
 * Created by Aravind S on 4/23/2015.
 */
public class WakeLockManager {
    private static final String TAG = "WakeLockManager";

    private PowerManager.WakeLock mWakeLock = null;

    private void initialize(Context context, String tag) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag);
    }

    public void acquireWakeLock(Context context, String tag) {
        if (mWakeLock == null || !mWakeLock.isHeld()) {
            initialize(context, tag);
            mWakeLock.acquire();
        }
    }

    public void acquireWakeLock(Context context, String tag, long timeout) {
        if (mWakeLock == null || !mWakeLock.isHeld()) {
            initialize(context, tag);
            mWakeLock.acquire(timeout);
        }
    }

    public void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }
}
