package com.clozerr.app;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.android.internal.util.Predicate;
import com.clozerr.app.Activities.HomeScreens.HomeActivity;
import com.clozerr.app.Utils.Constants;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;

import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

@TargetApi(18)
public class PeriodicBFS extends BeaconFinderService {

    private static final String TAG = "PBFS";

    private static long ALARM_INTERVAL = TimeUnit.MILLISECONDS.convert(120L, TimeUnit.SECONDS);
    private static long SCAN_PERIOD = TimeUnit.MILLISECONDS.convert(7L, TimeUnit.SECONDS);
    private static int SCAN_LIMIT = 20;
    //private static final long SCAN_PAUSE_INTERVAL = TimeUnit.MILLISECONDS.convert(60L, TimeUnit.SECONDS);
    //private static final long MAX_SCAN_RESTART_INTERVAL = ALARM_INTERVAL * 2 + SCAN_PAUSE_INTERVAL;
    private static final long MAX_SCAN_RESTART_INTERVAL = ALARM_INTERVAL * 3;
                                // interval after which alarms have to be rescheduled no matter what
                                // so it has to accommodate inexactness of alarm
    private static final int NOTIFICATION_ID = 10;

    private static Region scanningRegion = null;
    private static Integer maxRssi = null;
    private static VendorParams vendorToNotify = null;
    private static boolean running = false;
    private static int scanCount = 0;

    private static ArrayList<List<Beacon>> scanFrameData;


    public PeriodicBFS() { super(TAG); scanFrameData = new ArrayList<List<Beacon>>(); }

    private static NotificationCompat.Builder getDefaultNotificationBuilder(Context context) {
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);
    }

    private static void putAnalyticsForVendor(Context context, final VendorParams vendorParams) {

        String vendorId = "";


        Uri.Builder enc = GenUtils.getDefaultAnalyticsUriBuilder(context, Constants.Metrics.BEACON_DETECTION);
        String scanData = convertToDataString( scanFrameData );



        // If a notification was sent out.
        if( vendorParams != null ) {
            enc.appendQueryParameter("dimensions[vendor_id]", vendorParams.id)
            .appendQueryParameter("dimensions[beacon_major]", vendorParams.beaconParams.major.toString())
            .appendQueryParameter("dimensions[beacon_minor]", vendorParams.beaconParams.minor.toString());
        }

        enc.appendQueryParameter("dimensions[scan_data]", scanData);

        final String analyticsURL = enc.build().toString();
        GenUtils.putAnalytics(context, TAG, analyticsURL);
    }

    // Uses SFL ( Scan Frame Language ) to represent data.
    // It translates as: <FRAME>;<FRAME>;<FRAME>; ...
    // where <FRAME> is <BEACON> <BEACON> <BEACON> ...
    // where <BEACON> is <MAJOR>-<MINOR>-<RSSI>
    // This data is invaluable in understanding beacon detection
    // issues on a large scale. This will also help in configuring
    // scan frame times to optimize for a particular BLE chipset.
    private static String convertToDataString(ArrayList<List<Beacon>> scanFrameData) {
        String str = "";
        for( List<Beacon> frame : scanFrameData ){
            for( Beacon beacon : frame ) {
                str += beacon.getMajor() + "-" + beacon.getMajor() + "-" + beacon.getRssi();
                str += " ";
            }
            str += ";";
        }
        return str;
    }

    private static void showNotificationForVendor(Context context, final VendorParams vendorParams)
    {
        Log.e(TAG, "Params for notification - " + vendorParams.beaconParams.toString());
        dismissNotifications(context);
        try {

            //ArrayList<VendorParams> rootArray = VendorParams.readVendorParamsFromFile(context);
            /*for (final VendorParams vendorParams : rootArray) {
                *//*if (areUuidsEqual(vendorParams.mUUID, uuid)) {*//*
                if (vendorParams.beaconParams.equals(beaconParams)) {*/
            /*VendorParams vendorParams = VendorParams.findVendorParamsInFile(context, new Predicate<VendorParams>() {
                @Override
                public boolean apply(VendorParams vendorParams) {
                    return vendorParams.beaconParams.equals(beaconParams);
                }
            });
            if (vendorParams != null) {*/
            String title = vendorParams.notifTitle;
            Log.e(TAG, "vendor - " + vendorParams.name);
            String contentText = "", actionText = "";
            //if (vendorParams.mHasOffers) {
                contentText = vendorParams.notifMessage;
                actionText = "See rewards";
            //}
            /*else if (vendorParams.mLoyaltyType.equalsIgnoreCase("s1")) {
                contentText = "Mark your visit here!";
                actionText = "Mark visit";
            }
            else if (vendorParams.mLoyaltyType.equalsIgnoreCase("sx")) {
                contentText = "Get your stamps here during billing!";
                actionText = "Get stamps";
            }*/

            Intent detailIntent = vendorParams.getDetailsIntent(context);
            detailIntent.putExtra("from_periodic_scan", true);
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context)
                                        .addParentStack(HomeActivity.class)
                                        .addNextIntent(detailIntent);
            PendingIntent detailPendingIntent = taskStackBuilder.getPendingIntent(Constants.RequestCodes.DETAILS_INTENT, 0);

            NotificationCompat.Builder notificationBuilder = getDefaultNotificationBuilder(context);
            notificationBuilder.setContentTitle(title)
                    .setTicker(title + " - " + contentText)
                    .setContentText(contentText)
                    .setContentIntent(detailPendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .addAction(R.drawable.ic_action_accept, actionText, detailPendingIntent);
            ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE)).
                    notify(NOTIFICATION_ID, notificationBuilder.build());

            addToRejectList(context, vendorParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dismissNotifications(Context context) {
        ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE)).cancelAll();
    }

    @Override
    protected void onRangedBeacons(final List<Beacon> beaconList) {
        for (Beacon beacon : beaconList) {
            final BeaconDBParams params = new BeaconDBParams(beacon);
            int rssi = beacon.getRssi();
            Log.e(TAG, "major - " + params.major + "; minor - " + params.minor + "; RSSI - " + rssi);
            VendorParams currentVendor = VendorParams.findVendorParamsInFile(this,
                    new Predicate<VendorParams>() {
                        @Override
                        public boolean apply(VendorParams vendorParams) {
                            return vendorParams.beaconParams != null &&
                                    vendorParams.beaconParams.equals(params);
                        }
                    });
            if (currentVendor != null && !isRejected(getApplicationContext(), currentVendor) && rssi > currentVendor.thresholdRssi) {

                if (maxRssi == null || maxRssi < rssi) {
                    maxRssi = rssi;
                    vendorToNotify = currentVendor;
                }

            }
        }
        scanFrameData.add( new ArrayList<>(beaconList) );
    }

    public void startScanning() {

        // Stop scans if we hit stop scan limit.

        Log.e(TAG, "scan count: " + scanCount + " out of " + SCAN_LIMIT );
        if( scanCount > SCAN_LIMIT && SCAN_LIMIT != -1 ){
            checkAndStopScan( getApplicationContext() );
            return;
        }

        setListener(true);
        turnOnBluetooth(getApplicationContext());
        maxRssi = null;
        vendorToNotify = null;
        scanFrameData.clear();
        Log.e(TAG, "Waiting for BT State");
        new BTStateListener(SCAN_PERIOD) {
            @Override
            public void onBTStateReached(Context context, int state) {
                Log.e(TAG, "State:" +state);
                Log.e(TAG, "BT state changed");
                if (state == BluetoothAdapter.STATE_ON) {
                    unregisterSelf(context);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            beaconManager.connect(new ServiceReadyCallback() {
                                @Override
                                public void onServiceReady() {
                                    scanCount ++;
                                    Log.e(TAG, "Started Scan");
                                    scanningRegion = new Region(Constants.APP_PACKAGE_NAME, getUuidWithoutHyphens(commonBeaconUUID), null, null);
                                    // scan for all possible major & minor values, so no rules
                                    beaconManager.startRangingAndDiscoverDevice(scanningRegion);
                                }
                            });
                        }
                    });
                }
            }
        }.registerSelf(this);
    }

    public void stopScanning() {
        beaconManager.stopRanging(scanningRegion);
        turnOffBluetooth(getApplicationContext());
        Log.e(TAG, "Stopped Scan");
        putAnalyticsForVendor(this, vendorToNotify);
        if (vendorToNotify != null) {
            showNotificationForVendor(this, vendorToNotify);
            vendorToNotify = null;
            maxRssi = null;
        }
//        releaseLock();//TODO : Commented to remove error ???? resolve this
    }

    @Override
    protected void doWakefulWork(Intent intent) {


        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                startScanning();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "Stopping scans.");
                        stopScanning();
                    }
                }, SCAN_PERIOD);
            }
        });
    }

//    @Override//TODO : Commented to remove error ???? resolve this
    protected boolean isListeningAfterWork() {
        return true;
    }

    public static boolean isRunning() { return running; }

    public static void checkAndStartScan(Context context, int scanPeriod, int alarmInterval, int scanLimit, int scanOffset ) {

        SCAN_PERIOD = TimeUnit.MILLISECONDS.convert( scanPeriod, TimeUnit.SECONDS );
        ALARM_INTERVAL = TimeUnit.MILLISECONDS.convert( alarmInterval, TimeUnit.SECONDS );
        SCAN_LIMIT = scanLimit;
        long scanOffsetm = TimeUnit.MILLISECONDS.convert( scanOffset, TimeUnit.SECONDS );

        scanCount = 0;
        if (!running && checkCompatibility(context)) {
            running = true;
            commonBeaconUUID = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.SPKeys.BEACON_UUID, "");

            final Context c = context;

            Log.e( TAG, "Delaying schedule: " + scanOffset);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "Delayed Scheduling of alarms.");
                    scheduleAlarms( c );
                }
            }, scanOffsetm);

        }
    }

    public static void checkAndStartScan(Context context) {

        scanCount = 0;
        if (!running && checkCompatibility(context)) {
            running = true;
            commonBeaconUUID = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.SPKeys.BEACON_UUID, "");
            scheduleAlarms(context);
        }
    }

    public static void scheduleAlarms(Context context) {
        WakefulIntentService.scheduleAlarms(new AlarmListener(), context);
    }

    private static String getRejectListKey(VendorParams params) {
        return Constants.SPKeys.PREFIX_REJECT_LIST + params.name;
    }

    private static void addToRejectList(Context context, VendorParams params) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(getRejectListKey(params), true).commit();
    }

    private static void clearRejectList(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (String key : sharedPreferences.getAll().keySet())
            if (key.startsWith(Constants.SPKeys.PREFIX_REJECT_LIST))
                editor.remove(key);
        editor.commit();
    }

    private static boolean isRejected(Context context, VendorParams params) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(getRejectListKey(params), false);
    }

    public static void checkAndStopScan(Context context/*, boolean stopDownloads*/) {
        if (running) {
            running = false;
            turnOffBluetooth(context);
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove(Constants.SPKeys.APP_DISABLE_BT).commit();
            clearRejectList(context);
            WakefulIntentService.cancelAlarms(context);
        }
    }

    public static void pauseScanningFor(final Context context, long intervalMillis) {
        Log.e(TAG, "scans paused for " + intervalMillis + " ms");
        long triggerTimeMillis = intervalMillis + SystemClock.elapsedRealtime();
        GenUtils.enableComponent(context, ScanResumeReceiver.class);
        Intent resumeIntent = new Intent(context, ScanResumeReceiver.class);
        resumeIntent.setAction(Constants.Actions.ACTION_RESUME_SCAN);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTimeMillis,
                PendingIntent.getBroadcast(context, Constants.RequestCodes.RESUME_SCAN_INTENT, resumeIntent, 0));
        isScanningPaused = true;
        PeriodicBFS.checkAndStopScan(context);
    }

    public static class AlarmListener implements WakefulIntentService.AlarmListener {
        private static final String TAG = "PBFSAlarmListener";

        @Override
        public void scheduleAlarms(AlarmManager alarmManager, PendingIntent pendingIntent, Context context) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),
                                            ALARM_INTERVAL, pendingIntent);
        }

        @Override
        public void sendWakefulWork(Context context) {
            Log.e(TAG, "sending wakeful work");
            if (checkPreferences(context, true) &&!OneTimeBFS.isRunning() && !isScanningPaused)
                WakefulIntentService.sendWakefulWork(context, PeriodicBFS.class);
        }

        @Override
        public long getMaxAge() {
            return MAX_SCAN_RESTART_INTERVAL;
        }
    }

    public static class ScanResumeReceiver extends WakefulBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals(Constants.Actions.ACTION_RESUME_SCAN)) {
                Log.e(TAG, "scans resumed");
                isScanningPaused = false;
                PeriodicBFS.checkAndStartScan(context);
                GenUtils.disableComponent(context, ScanResumeReceiver.class);
            }
        }
    }
}
