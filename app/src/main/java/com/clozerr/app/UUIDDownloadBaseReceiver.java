package com.clozerr.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class UUIDDownloadBaseReceiver extends BroadcastReceiver {
    private static final String TAG = "UUIDBaseReceiver";
    private static final long MAXIMUM_ALARM_INTERVAL = TimeUnit.MILLISECONDS.convert(1L, TimeUnit.MINUTES);
                                        // TODO change to 1 day
    private static final long MINIMUM_ALARM_INTERVAL = TimeUnit.MILLISECONDS.convert(30L, TimeUnit.SECONDS);
                                        // TODO change to 1 hr or so
    private static final long CONNECTIVITY_SCAN_PERIOD = TimeUnit.MILLISECONDS.convert(15L, TimeUnit.SECONDS);
                                        // TODO change to 15 min or so
    private static final String ACTION_FIRE_ALARM_DOWNLOAD = "FireAlarmDownload";
    private static final int REDUCTION_FACTOR = 2;
    private static final int REQUEST_CODE = 1234;

    private static long alarmInterval = MAXIMUM_ALARM_INTERVAL;
    private static AlarmManager alarmManager = null;
    private static WakeLockManager wakeLockManager = null;

    private Context mContext = null;
    private Handler mHandler = null;

    public UUIDDownloadBaseReceiver() {}

    public UUIDDownloadBaseReceiver(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mContext = context;
    }

    private static PendingIntent getUUIDDownloaderPendingIntent(Context context) {
        Intent intentToSend = new Intent(context, UUIDDownloadBaseReceiver.class);
        intentToSend.setAction(ACTION_FIRE_ALARM_DOWNLOAD);
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intentToSend, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void setNewAlarm() {
        Log.e(TAG, "setting alarm; interval - " + alarmInterval);
        PendingIntent operationIntent = getUUIDDownloaderPendingIntent(mContext);
        if (alarmManager != null)
            alarmManager.cancel(operationIntent);
        else
            alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                                         alarmInterval, operationIntent);
    }

    public static boolean hasStarted() { return alarmManager != null; }

    public static void scheduleDownload(Context context) {
        if (!UUIDDownloadBaseReceiver.hasStarted())
            new UUIDDownloadBaseReceiver(context).setNewAlarm();
    }

    public static void stopDownloads(Context context) {
        if (UUIDDownloadBaseReceiver.hasStarted())
            alarmManager.cancel(getUUIDDownloaderPendingIntent(context));
    }

    private void enableUUIDDownloader(Context context) {
        ComponentName receiver = new ComponentName(context, UUIDDownloader.class);
        context.getPackageManager().setComponentEnabledSetting(receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        Intent initiateIntent = new Intent(context, UUIDDownloader.class);
        initiateIntent.setAction(UUIDDownloader.ACTION_INITIATE_DOWNLOADER);
        context.sendBroadcast(initiateIntent);
    }

    private void disableUUIDDownloader(Context context) {
        ComponentName receiver = new ComponentName(context, UUIDDownloader.class);
        context.getPackageManager().setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    public static void acquireWakeLock(Context context) { wakeLockManager.acquireWakeLock(context, TAG); }

    public static void releaseWakeLock() { wakeLockManager.releaseWakeLock(); }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(ACTION_FIRE_ALARM_DOWNLOAD)) {
            mContext = context;
            mHandler = new Handler(Looper.myLooper());
            wakeLockManager = new WakeLockManager();
            wakeLockManager.acquireWakeLock(context, TAG);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "received");
                    Log.e(TAG, "enabling UUIDDownloader");
                    enableUUIDDownloader(context);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!UUIDDownloader.isDoneDownloading()) {
                                Log.e(TAG, "Timeout; disabling UUIDDownloader");
                                disableUUIDDownloader(context);
                                if (alarmInterval > MINIMUM_ALARM_INTERVAL) {
                                    alarmInterval /= REDUCTION_FACTOR;
                                    if (alarmInterval < MINIMUM_ALARM_INTERVAL)
                                        alarmInterval = MINIMUM_ALARM_INTERVAL;
                                    setNewAlarm();
                                }
                            }
                            else {
                                Log.e(TAG, "Download for this session completed successfully.");
                                if (alarmInterval < MAXIMUM_ALARM_INTERVAL) {
                                    alarmInterval = MAXIMUM_ALARM_INTERVAL;
                                    setNewAlarm();
                                }
                            }
                            wakeLockManager.releaseWakeLock();
                        }
                    }, CONNECTIVITY_SCAN_PERIOD);
                }
            });
        }
    }
}
