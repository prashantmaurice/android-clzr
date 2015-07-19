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
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.android.internal.util.Predicate;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;

import java.util.List;
import java.util.concurrent.TimeUnit;

@TargetApi(18)
public class PeriodicBFS extends BeaconFinderService {

    private static final String TAG = "PBFS";
    //private static final String ACTION_REMOVE_VENDOR = "RemoveVendor";
    //private static final String ACTION_FIRE_ALARM_SCAN = "com.clozerr.app.ACTION_FIRE_ALARM_SCAN";

    private static final long ALARM_INTERVAL = TimeUnit.MILLISECONDS.convert(90L, TimeUnit.SECONDS);
    private static final long SCAN_PERIOD = TimeUnit.MILLISECONDS.convert(15L, TimeUnit.SECONDS);
    //private static final long SCAN_PAUSE_INTERVAL = TimeUnit.MILLISECONDS.convert(60L, TimeUnit.SECONDS);
    //private static final long MAX_SCAN_RESTART_INTERVAL = ALARM_INTERVAL * 2 + SCAN_PAUSE_INTERVAL;
    private static final long MAX_SCAN_RESTART_INTERVAL = ALARM_INTERVAL * 3;
                                // interval after which alarms have to be rescheduled no matter what
                                // so it has to accommodate inexactness of alarm
    //private static final int PERIODIC_SCAN_BEACON_LIMIT = 3;
    private static final int NOTIFICATION_ID = 10;

    //private static final ConcurrentHashMap<String, DeviceParams> periodicScanDeviceMap = new ConcurrentHashMap<>();
    //private static NotificationCompat.Builder notificationBuilder = null;
    //private static NotificationManager notificationManager = null;
    //private static WakeLockManager wakeLockManager;
    //private static Bitmap NOTIFICATION_LARGE_ICON;

    public static final String ACTION_RESUME_SCAN = "com.clozerr.app.ACTION_RESUME_SCAN";

    private static Region scanningRegion = null;
    private static Integer maxRssi = null;
    private static VendorParams vendorToNotify = null;
    private static boolean running = false;
    //private static boolean pushedNotification;
    //private static boolean hasScanStarted = false;

    public PeriodicBFS() { super(TAG); }

    /*public PeriodicBFS() {
        super();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = getDefaultNotificationBuilder(this);
    }*/

    //public static final String MAP_CONTENTS_FILE_NAME = "mapContents.txt";

    //private Timer mTimer;
    //private BeaconCheckTask mCheckTask;

    /*@Override
    public void onCreate() {
        super.onCreate();
        //readHashMapFromFile();

        //mTimer = new Timer();
        //mCheckTask = new BeaconCheckTask();
        //NOTIFICATION_LARGE_ICON = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        //wakeLockManager = new WakeLockManager();

        *//*if (notificationManager == null)
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);*//*
        if (notificationBuilder == null)
            notificationBuilder = getDefaultNotificationBuilder(getApplicationContext());

    }*/

    /*@Override
    public void onDestroy() {
        Context applicationContext = getApplicationContext();
        dismissNotifications(applicationContext);
        running = false;
        mCheckTask.stopScanning();
        mTimer.cancel();
        alarmManager.cancel(getScanStarterPendingIntent(applicationContext));
        disableComponent(applicationContext, ScanStarter.class);
        if (!isScanningAllowed) {               // if this was because scanning was disallowed by settings
            periodicScanDeviceMap.clear();
            writeHashMapToFile();               // next time scan starts, start with an empty hash map
        }
        wakeLockManager.releaseWakeLock();
        super.onDestroy();
    }*/

    /*private static void enableScanStarter(Context context) {
        ComponentName receiver = new ComponentName(context, ScanStarter.class);
        context.getPackageManager().setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private static void disableScanStarter(Context context) {
        ComponentName receiver = new ComponentName(context, ScanStarter.class);
        context.getPackageManager().setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }*/

    /*private static PendingIntent getScanStarterPendingIntent(Context context) {
        enableComponent(context, ScanStarter.class);
        Intent intentToSend = new Intent(context, ScanStarter.class);
        intentToSend.setAction(ACTION_FIRE_ALARM_SCAN);
        return PendingIntent.getBroadcast(context, RequestCodes.CODE_ALARM_INTENT.code(), intentToSend,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }*/

    /*private void readHashMapFromFile() {
        try {
            FileOutputStream dummyOutputStream = openFileOutput(MAP_CONTENTS_FILE_NAME, MODE_APPEND);
                                                // create file if not created yet
            dummyOutputStream.close();
            FileInputStream fileInputStream = openFileInput(MAP_CONTENTS_FILE_NAME);
            byte[] dataBytes = new byte[fileInputStream.available()];
            fileInputStream.read(dataBytes);
            fileInputStream.close();
            String data = new String(dataBytes);
            if (!data.isEmpty()) {
                JSONObject rootObject = new JSONObject(data);
                JSONObject hashMapObject = rootObject.getJSONObject("hashMap");
                for (Iterator<String> iterator = hashMapObject.keys(); iterator.hasNext(); ) {
                    String uuid = iterator.next();
                    DeviceParams params = new DeviceParams(hashMapObject.getInt(uuid), false);
                    periodicScanDeviceMap.put(uuid, params);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeHashMapToFile() {
        try {
            FileOutputStream fileOutputStream = openFileOutput(MAP_CONTENTS_FILE_NAME, Context.MODE_PRIVATE);
            JSONObject hashMapObject = new JSONObject("{}");
            for (String uuid : periodicScanDeviceMap.keySet())
                hashMapObject.put(uuid, periodicScanDeviceMap.get(uuid).mCount);
            JSONObject rootObject = new JSONObject("{}");
            rootObject.put("hashMap", hashMapObject);
            String fileData = rootObject.toString();
            fileOutputStream.write(fileData.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private static NotificationCompat.Builder getDefaultNotificationBuilder(Context context) {
        //Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        return new NotificationCompat.Builder(context)
                //.setLargeIcon(NOTIFICATION_LARGE_ICON)
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);
    }

    private static void putAnalyticsForVendor(Context context, final VendorParams vendorParams) {
        final String analyticsURL = GenUtils.getDefaultAnalyticsUriBuilder(context, Constants.Metrics.BEACON_DETECTION)
                .appendQueryParameter("dimensions[vendor_id]", vendorParams.id)
                .appendQueryParameter("dimensions[beacon_major]", vendorParams.beaconParams.major.toString())
                .appendQueryParameter("dimensions[beacon_minor]", vendorParams.beaconParams.minor.toString())
                .build().toString();
        GenUtils.putAnalytics(context, TAG, analyticsURL);
    }

    private static void showNotificationForVendor(Context context, final VendorParams vendorParams)
    {
        Log.e(TAG, "Params for notification - " + vendorParams.beaconParams.toString());
        //putToast(context, "Params for notification - " + beaconParams.toString(), Toast.LENGTH_SHORT);
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
            String title = "Welcome to " + vendorParams.name;
            Log.e(TAG, "vendor - " + vendorParams.name);
            String contentText = "", actionText = "";
            //if (vendorParams.mHasOffers) {
                contentText = "Redeem your rewards!";
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
                                        .addParentStack(Home.class)
                                        .addNextIntent(detailIntent);
            /*PendingIntent detailPendingIntent = PendingIntent.getActivity(context,
                    RequestCodes.CODE_DETAILS_INTENT.code(),
                    detailIntent, PendingIntent.FLAG_ONE_SHOT);*/

            PendingIntent detailPendingIntent = taskStackBuilder.getPendingIntent(Constants.RequestCodes.DETAILS_INTENT, 0);

            /*Intent refuseIntent = new Intent(context, NotificationRemovalReceiver.class);
            refuseIntent.setAction(ACTION_REMOVE_VENDOR);
            refuseIntent.putExtra("uuid", vendorParams.mUUID);
            PendingIntent refusePendingIntent = PendingIntent.getBroadcast(context,
                    RequestCodes.CODE_REFUSE_INTENT.code(),
                    refuseIntent, PendingIntent.FLAG_ONE_SHOT);*/

            NotificationCompat.Builder notificationBuilder = getDefaultNotificationBuilder(context);
            notificationBuilder.setContentTitle(title)
                    .setTicker(title + " - " + contentText)
                    .setContentText(contentText)
                    .setContentIntent(detailPendingIntent)
                    //.setContentInfo("Clozerr")
                    .setWhen(System.currentTimeMillis())
                    .addAction(R.drawable.ic_action_accept, actionText, detailPendingIntent);
                    //.addAction(R.drawable.ic_refuse, "Not for this vendor", refusePendingIntent);
            ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE)).
                    notify(NOTIFICATION_ID, notificationBuilder.build());

            //pushedNotification = true;
            addToRejectList(context, vendorParams);
            //pauseScanningFor(context, SCAN_PAUSE_INTERVAL);
            /*}*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private static void showNotifications(Context context) {
        dismissNotifications(context);
        ArrayList<String> uuidList = new ArrayList<>();
        for (String uuid : periodicScanDeviceMap.keySet())
            if (periodicScanDeviceMap.get(uuid).mToBeNotified)
                uuidList.add(uuid);
        if (uuidList.size() > 0) {
            Log.e(TAG, "Setting all notifications");
            if (uuidList.size() == 1)
                showNotificationForVendor(context, uuidList.get(0));
            else
                showNotificationForVendorList(context, uuidList);
            Log.e(TAG, "Setting notifications");
            showNotificationForVendor(context, uuidList.get(0));
        }
    }*/

    /*private static void showNotificationForVendorList(Context context, ArrayList<String> uuids) {
        Log.e(TAG, "Multiple vendors, size " + uuids.size());
        dismissNotifications(context);
        try {
            String title = uuids.size() + " restaurants near you";
            String contentText = "Tap to view offers and/or check in.";
            Intent vendorListIntent = new Intent(context, VendorListActivity.class);
            vendorListIntent.putStringArrayListExtra("uuidList", uuids);
            PendingIntent vendorListPendingIntent = PendingIntent.getActivity(context,
                    RequestCodes.CODE_VENDOR_LIST_INTENT.code(),
                    vendorListIntent, PendingIntent.FLAG_ONE_SHOT);
            notificationBuilder = getDefaultNotificationBuilder(context);
            notificationBuilder.setContentTitle(title)
                    .setTicker(title)
                    .setContentText(contentText)
                    .setContentIntent(vendorListPendingIntent)
                    .setWhen(System.currentTimeMillis());
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public static void dismissNotifications(Context context) {
        ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE)).cancelAll();
    }

    /*public static void turnOffNotificationsForVendor(Context context, VendorParams vendorParams) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(vendorParams.mUUID, "don't notify");   // dummy string value
        editor.apply();
        vendorParams.mIsNotifiable = false;
        putToast(context, "Turned OFF beacon notifications for " + vendorParams.name,
                Toast.LENGTH_LONG);
        if (!areAnyVendorsNotifiable(context))
            BeaconFinderService.disallowScanning(context);
    }

    public static void turnOnNotificationsForVendor(Context context, VendorParams vendorParams) {
        boolean wereAnyVendorsNotifiable = areAnyVendorsNotifiable(context);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(vendorParams.mUUID);
        editor.apply();
        vendorParams.mIsNotifiable = true;
        putToast(context, "Turned ON beacon notifications for " + vendorParams.name,
                Toast.LENGTH_LONG);
        if (areAnyVendorsNotifiable(context) && !wereAnyVendorsNotifiable)
            BeaconFinderService.allowScanning(context);
    }

    public static void turnOnNotificationsForVendor(Context context, final String uuid) {
        turnOnNotificationsForVendor(context, VendorParams.findVendorParamsInFile(context,
                new Predicate<VendorParams>() {
                    @Override
                    public boolean apply(VendorParams vendorParams) {
                        return areUuidsEqual(uuid, vendorParams.mUUID);
                    }
                }));
    }

    public static void turnOffNotificationsForVendor(Context context, final String uuid) {
        turnOffNotificationsForVendor(context, VendorParams.findVendorParamsInFile(context,
                new Predicate<VendorParams>() {
                    @Override
                    public boolean apply(VendorParams vendorParams) {
                        return areUuidsEqual(uuid, vendorParams.mUUID);
                    }
                }));
    }

    private static boolean areAnyVendorsNotifiable(Context context) {
        if (beaconDatabase != null) {
            for (String uuid : beaconDatabase)
                if (VendorParams.isVendorWithThisUUIDNotifiable(context, uuid))
                    return true;
            return false;
        }
        else return true;
    }*/

    /*@Override
    protected Region createRegion() {
        return new Region(APP_PACKAGE_NAME, getUuidWithoutHyphens(commonBeaconUUID), null, null); // search for all beacons, so no rules on major, minor
    }*/

    @Override
    protected void onRangedBeacons(final List<Beacon> beaconList) {
        //Log.e(TAG, "Ranged; size - " + beaconList.size());
        for (Beacon beacon : beaconList) {
            //Beacon beacon = beaconList.get(i);
            //final String uuid = beacon.getProximityUUID();
            final BeaconDBParams params = new BeaconDBParams(beacon);
            int rssi = beacon.getRssi();
            //Log.e(TAG, "UUID scanned - " + beacon.getProximityUUID().toUpperCase());
            Log.e(TAG, "major - " + params.major + "; minor - " + params.minor + "; RSSI - " + rssi);
            //putToast(getApplicationContext(), "scanned", Toast.LENGTH_SHORT);
            /*if (beaconDatabase.contains(uuid.toUpperCase())) {*/
            VendorParams currentVendor = VendorParams.findVendorParamsInFile(this,
                    new Predicate<VendorParams>() {
                        @Override
                        public boolean apply(VendorParams vendorParams) {
                            //return areUuidsEqual(uuid, vendorParams.mUUID);
                            return vendorParams.beaconParams != null &&
                                    vendorParams.beaconParams.equals(params);
                        }
                    });
            if (currentVendor != null && !isRejected(getApplicationContext(), currentVendor) && rssi > currentVendor.thresholdRssi) {
                putAnalyticsForVendor(this, currentVendor);
                if (maxRssi == null || maxRssi < rssi) {
                    maxRssi = rssi;
                    vendorToNotify = currentVendor;
                }
            }
            /*if (beacon.getRssi() >= DEFAULT_THRESHOLD_RSSI) {
                *//*DeviceParams deviceParams;
                if (periodicScanDeviceMap.containsKey(uuid)) {
                    deviceParams = periodicScanDeviceMap.get(uuid);
                    if (!deviceParams.mFoundInThisScan) {
                        deviceParams.mFoundInThisScan = true;
                    } else continue;
                } else {
                    periodicScanDeviceMap.put(uuid, new DeviceParams(0, true));
                    deviceParams = periodicScanDeviceMap.get(uuid);
                }
                if (vendorParams.mPaymentType.equalsIgnoreCase("counter")) {
                    //double distance = getDistanceFromBeacon(beacon);
                    putToast(getApplicationContext(), "RSSI = " + beacon.getRssi(), Toast.LENGTH_LONG);
                    if (beacon.getRssi() >= vendorParams.thresholdRssi) {
                        //deviceParams.mCount = 0;
                        showNotificationForVendor(getApplicationContext(), uuid);
                        //deviceParams.mToBeNotified = true;
                    }
                }
                else if (vendorParams.mPaymentType.equalsIgnoreCase("gourmet")) {
                    ++(deviceParams.mCount);
                    Log.e(TAG, "count-" + deviceParams.mCount + ";lim-" + PERIODIC_SCAN_BEACON_LIMIT);
                    if (deviceParams.mCount == PERIODIC_SCAN_BEACON_LIMIT) {
                        deviceParams.mCount = 0;
                        showNotificationForVendor(getApplicationContext(), uuid);
                        //deviceParams.mToBeNotified = true;
                    }
                }
                writeHashMapToFile();*//*
                //putToast(getApplicationContext(), "Within threshold", Toast.LENGTH_SHORT);
                if (currentVendor != null) {
                    //deviceParams.mCount = 0;
                    //putToast(getApplicationContext(), "found params", Toast.LENGTH_SHORT);

                    putAnalyticsForVendor(this, currentVendor);
                    showNotificationForVendor(this, currentVendor);
                    //deviceParams.mToBeNotified = true;
                }
            }*/
            /*}*/
        }
    }

    /*@Override
    protected void scan() {
        if (!running) {
            running = true;
            //mTimer.schedule(mCheckTask, 0, ALARM_INTERVAL);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),
                                            ALARM_INTERVAL, getScanStarterPendingIntent(getApplicationContext()));
        }
    }*/

    /*@Override
    protected void runService() {
        *//*if (!running) {
            running = true;
            //mTimer.schedule(mCheckTask, 0, ALARM_INTERVAL);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),
                    ALARM_INTERVAL, getScanStarterPendingIntent(getApplicationContext()));
        }*//*
    }*/

    public void startScanning() {
        setListener(true);
        turnOnBluetooth(getApplicationContext());
        //pushedNotification = false;
        maxRssi = null;
        vendorToNotify = null;
        new BTStateChangeReceiver(SCAN_PERIOD) {
            @Override
            public void onBTStateReached(Context context, int state) {
                if (state == BluetoothAdapter.STATE_ON) {
                    unregisterSelf(context);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            beaconManager.connect(new ServiceReadyCallback() {
                                @Override
                                public void onServiceReady() {
                                    //hasScanStarted = true;
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
        /*beaconManager.connect(new ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                hasScanStarted = true;
                Log.e(TAG, "Started Scan");
                scanningRegion = new Region(APP_PACKAGE_NAME, getUuidWithoutHyphens(commonBeaconUUID), null, null);
                            // scan for all possible major & minor values, so no rules
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        beaconManager.startRangingAndDiscoverDevice(scanningRegion);
                    }
                }, BT_RECEIVER_TIMEOUT); // delay required as scanning will not work right upon enabling BT
            }
        });*/
    }

    public void stopScanning() {
        //if (hasScanStarted) {
        beaconManager.stopRanging(scanningRegion);
        turnOffBluetooth(getApplicationContext());
        Log.e(TAG, "Stopped Scan");
        //hasScanStarted = false;
        if (vendorToNotify != null) {
            showNotificationForVendor(this, vendorToNotify);
            vendorToNotify = null;
            maxRssi = null;
        }
        releaseLock();
        //}
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
                        stopScanning();
                    }
                }, SCAN_PERIOD);
            }
        });
    }

    @Override
    protected boolean isListeningAfterWork() {
        return true;
    }

    public static boolean isRunning() { return running; }

    public static void checkAndStartScan(Context context) {
        if (!running && checkCompatibility(context)) {
            running = true;
            commonBeaconUUID = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.SPKeys.BEACON_UUID, "");
            scheduleAlarms(context);
        }
    }

    public static void scheduleAlarms(Context context) {
        WakefulIntentService.scheduleAlarms(new PeriodicBFS.AlarmListener(), context);
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
            //context.stopService(new Intent(context, PeriodicBFS.class));
            running = false;
            turnOffBluetooth(context);
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove(Constants.SPKeys.APP_DISABLE_BT).commit();
            clearRejectList(context);
            WakefulIntentService.cancelAlarms(context);
            /*if (stopDownloads)
                BeaconDBDownloadBaseReceiver.stopDownloads(context);*/
        }
    }

    public static void pauseScanningFor(final Context context, long intervalMillis) {
        Log.e(TAG, "scans paused for " + intervalMillis + " ms");
        //putToast(context, "scans paused for " + intervalMillis + " ms", Toast.LENGTH_SHORT);
        long triggerTimeMillis = intervalMillis + SystemClock.elapsedRealtime();
        GenUtils.enableComponent(context, ScanResumeReceiver.class);
        Intent resumeIntent = new Intent(context, ScanResumeReceiver.class);
        resumeIntent.setAction(ACTION_RESUME_SCAN);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTimeMillis,
                PendingIntent.getBroadcast(context, Constants.RequestCodes.RESUME_SCAN_INTENT, resumeIntent, 0));
        //disallowScanning(context);
        //isScanningAllowed = false;
        isScanningPaused = true;
        //WakefulIntentService.cancelAlarms(context);
        PeriodicBFS.checkAndStopScan(context);
    }

    /*public static class ScanStarter extends WakefulBroadcastReceiver {
        private static final String TAG = "ScanStarter";
        public static boolean pushedNotification;

        private Runnable mScanningRunnable;
        private boolean hasScanStarted = false;

        public ScanStarter() {}

        public void startScanning(final Context context) {
            if (!commonBeaconUUID.isEmpty()) {
                scanningRegion = new Region(APP_PACKAGE_NAME, getUuidWithoutHyphens(commonBeaconUUID), null, null);
                                            // scan for all possible major & minor values, so no rules
                beaconManager.connect(new ServiceReadyCallback() {
                    @Override
                    public void onServiceReady() {
                        hasScanStarted = true;
                        turnOnBluetooth();
                        pushedNotification = false;
                        *//*for (DeviceParams params : periodicScanDeviceMap.values())
                            params.mFoundInThisScan *//**//*= params.mToBeNotified *//**//*= false;*//*
                        Log.e(TAG, "Started Scan");
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                beaconManager.startRangingAndDiscoverDevice(scanningRegion);
                            }
                        }, BT_RECEIVER_TIMEOUT); // delay required as scanning will not work right upon enabling BT
                    }
                });
            }
        }

        public void stopScanning(Context context) {
            if (hasScanStarted) {
                beaconManager.stopRanging(scanningRegion);
                turnOffBluetooth();
                Log.e(TAG, "Stopped Scan");
                hasScanStarted = false;
                *//*for (String uuid : periodicScanDeviceMap.keySet())
                    if (!periodicScanDeviceMap.get(uuid).mFoundInThisScan)
                        periodicScanDeviceMap.remove(uuid);*//*
                //showNotifications(context);
                //wakeLockManager.releaseWakeLock();
            }
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.e(TAG, "Received");
            if (intent.getAction() != null && intent.getAction().equals(ACTION_FIRE_ALARM_SCAN) && isScanningAllowed) {
                *//*if (wakeLockManager == null)
                    wakeLockManager = new WakeLockManager();
                wakeLockManager.acquireWakeLock(context, TAG);*//*
                //bgThreadHandler = new Handler(Looper.myLooper());
                mScanningRunnable = new Runnable() {
                            @Override
                            public void run() {
                        startScanning(context);
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                stopScanning(context);
                            }
                        }, SCAN_PERIOD);
                    }
                };
                new Handler(Looper.getMainLooper()).post(mScanningRunnable);
            }
        }
    }*/

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
            if (intent != null && intent.getAction().equals(ACTION_RESUME_SCAN)) {
                //allowScanning(context);
                //isScanningAllowed = true;
                Log.e(TAG, "scans resumed");
                isScanningPaused = false;
                //putToast(context, "scans resumed", Toast.LENGTH_SHORT);
                //WakefulIntentService.scheduleAlarms(new PeriodicBFS.AlarmListener(), context);
                PeriodicBFS.checkAndStartScan(context);
                GenUtils.disableComponent(context, ScanResumeReceiver.class);
            }
        }
    }

    /*private class DeviceParams {
        public int mCount;
        public boolean mFoundInThisScan;
        //public boolean mToBeNotified;

        public DeviceParams(int count, boolean foundInThisScan) {
            mCount = count;
            mFoundInThisScan = foundInThisScan;
            //mToBeNotified = false;
        }
    }*/

    /*public static class NotificationRemovalReceiver extends BroadcastReceiver {
        private static final String TAG = "NRReceiver";

        public NotificationRemovalReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "received");
            String uuid = intent.getStringExtra("uuid");
            turnOffNotificationsForVendor(context, uuid);
        }
    }*/
}
