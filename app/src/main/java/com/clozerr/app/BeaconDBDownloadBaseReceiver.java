package com.clozerr.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class BeaconDBDownloadBaseReceiver extends BroadcastReceiver {
    private static final String TAG = "BDBBaseReceiver";
    private static final long MAXIMUM_ALARM_INTERVAL = TimeUnit.MILLISECONDS.convert(1L, TimeUnit.MINUTES);
    private static final long MINIMUM_ALARM_INTERVAL = TimeUnit.MILLISECONDS.convert(30L, TimeUnit.SECONDS);
    private static final long CONNECTIVITY_SCAN_PERIOD = TimeUnit.MILLISECONDS.convert(15L, TimeUnit.SECONDS);
    private static final String ACTION_FIRE_ALARM_DOWNLOAD = "com.clozerr.app.ACTION_FIRE_ALARM_DOWNLOAD";
    private static final int REDUCTION_FACTOR = 2;
    private static final int REQUEST_CODE = 1234;

    private static long alarmInterval = MAXIMUM_ALARM_INTERVAL;
    private static AlarmManager alarmManager = null;
    private static WakeLockManager wakeLockManager = null;

    private Context mContext = null;
    private Handler mHandler = null;

    public BeaconDBDownloadBaseReceiver() {}

    public BeaconDBDownloadBaseReceiver(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mContext = context;
    }

    private static PendingIntent getBDBDownloaderPendingIntent(Context context) {
        Intent intentToSend = new Intent(context, BeaconDBDownloadBaseReceiver.class);
        intentToSend.setAction(ACTION_FIRE_ALARM_DOWNLOAD);
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intentToSend, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void setNewAlarm() {
        Log.e(TAG, "setting alarm; interval - " + alarmInterval);
        PendingIntent operationIntent = getBDBDownloaderPendingIntent(mContext);
        if (alarmManager != null)
            alarmManager.cancel(operationIntent);
        else
            alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                                         alarmInterval, operationIntent);
    }

    public static boolean hasStarted() { return alarmManager != null; }

    public static void scheduleDownload(Context context) {
        if (!BeaconDBDownloadBaseReceiver.hasStarted())
            new BeaconDBDownloadBaseReceiver(context).setNewAlarm();
    }

    public static void stopDownloads(Context context) {
        if (BeaconDBDownloadBaseReceiver.hasStarted())
            alarmManager.cancel(getBDBDownloaderPendingIntent(context));
    }

    private void initiateBDBDownloader(Context context) {
        BeaconFinderService.enableComponent(context, BeaconDBDownloader.class);
        Intent initiateIntent = new Intent(context, BeaconDBDownloader.class);
        initiateIntent.setAction(BeaconDBDownloader.ACTION_INITIATE_DOWNLOADER);
        context.sendBroadcast(initiateIntent);
    }

    /*private void disableBDBDownloader(Context context) {
        ComponentName receiver = new ComponentName(context, BeaconDBDownloader.class);
        context.getPackageManager().setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }*/

    /*public static void acquireWakeLock(Context context) {
        if (wakeLockManager == null)
            wakeLockManager = new WakeLockManager();
        wakeLockManager.acquireWakeLock(context, TAG);
    }

    public static void releaseWakeLock() { wakeLockManager.releaseWakeLock(); }*/

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(ACTION_FIRE_ALARM_DOWNLOAD)) {
            mContext = context;
            mHandler = new Handler(Looper.myLooper());
            //acquireWakeLock(context);
            if (wakeLockManager == null)
                wakeLockManager = new WakeLockManager();
            wakeLockManager.acquireWakeLock(context, TAG);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "received");
                    Log.e(TAG, "enabling BDBDownloader");
                    initiateBDBDownloader(context);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!BeaconDBDownloader.isDoneDownloading()) {
                                Log.e(TAG, "Timeout; disabling BDBDownloader");
                                BeaconFinderService.disableComponent(context, BeaconDBDownloader.class);
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
                            //releaseWakeLock();
                            wakeLockManager.releaseWakeLock();
                        }
                    }, CONNECTIVITY_SCAN_PERIOD);
                }
            });
        }
    }
}
