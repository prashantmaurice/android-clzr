package com.clozerr.app;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.Region;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@TargetApi(18)
public class PeriodicBFS extends BeaconFinderService {

    private static final String TAG = "PBFS";
    private static final String ACTION_REMOVE_VENDOR = "RemoveVendor";

    private static final long INTERVAL = TimeUnit.MILLISECONDS.convert(20L, TimeUnit.SECONDS);
                                    // TODO make this 10 min
    private static final long SCAN_PERIOD = TimeUnit.MILLISECONDS.convert(6L, TimeUnit.SECONDS);
                                    // TODO modify as required
    private static final int DIALOG_VIEW_ID = 0; // TODO change to R.layout.dialog_vendor_list
    private static enum RequestCodes {
        CODE_DETAILS_INTENT(1234),
        CODE_REFUSE_INTENT(1235);

        private int mCode;

        private RequestCodes(int code) { mCode = code; }
        public int code() { return mCode; }
    }

    private static final int PERIODIC_SCAN_BEACON_LIMIT = 1;
    private static final int NOTIFICATION_ID = 0;
    private static final ConcurrentHashMap<String, DeviceParams> periodicScanDeviceMap = new ConcurrentHashMap<>();
    private static NotificationCompat.Builder notificationBuilder;
    private static NotificationManager notificationManager;
    private static boolean running = false;

    public static final String MAP_CONTENTS_FILE_NAME = "mapContents.txt";

    private Timer mTimer;
    private BeaconCheckTask mCheckTask;

    @Override
    public void onCreate() {
        super.onCreate();
        readHashMapFromFile();

        mTimer = new Timer();
        mCheckTask = new BeaconCheckTask();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = getDefaultNotificationBuilder(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        dismissNotifications(getApplicationContext());
        running = false;
        mCheckTask.stopScanning();
        mTimer.cancel();
        if (!isScanningAllowed) {               // if this was because scanning was disallowed by settings
            periodicScanDeviceMap.clear();
            writeHashMapToFile();               // next time scan starts, start with an empty hash map
        }
        super.onDestroy();
    }

    private void readHashMapFromFile() {
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
    }

    private static NotificationCompat.Builder getDefaultNotificationBuilder(Context context) {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notif_logo)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(soundUri);
    }

    private static void setNotificationForVendor(String uuid, Context context)
    {
        Log.e(TAG, "UUID for notification - " + uuid);
        dismissNotifications(context);
        try {
            ArrayList<VendorParams> rootArray = VendorParams.readVendorParamsFromFile(context);
            for (final VendorParams vendorParams : rootArray) {
                if (areUuidsEqual(vendorParams.mUUID, uuid)) {
                    String title = "Clozerr - " + vendorParams.mName;
                    Log.e(TAG, "vendor - " + vendorParams.mName);
                    final String contentText = vendorParams.mNextOfferCaption;

                    Intent detailIntent = vendorParams.getDetailsIntent(context);
                    detailIntent.putExtra("from_periodic_scan", true);
                    PendingIntent detailPendingIntent = PendingIntent.getActivity(context,
                            RequestCodes.CODE_DETAILS_INTENT.code(),
                            detailIntent, PendingIntent.FLAG_ONE_SHOT);

                    Intent refuseIntent = new Intent(ACTION_REMOVE_VENDOR);
                    PendingIntent refusePendingIntent = PendingIntent.getBroadcast(context,
                            RequestCodes.CODE_REFUSE_INTENT.code(),
                            refuseIntent, PendingIntent.FLAG_ONE_SHOT);
                    context.registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            // TODO modify files
                            putToast(context, "Turned off beacon notifications for " + vendorParams.mName,
                                    Toast.LENGTH_LONG);
                            notificationManager.cancel(NOTIFICATION_ID);
                            context.unregisterReceiver(this);
                        }
                    }, new IntentFilter(ACTION_REMOVE_VENDOR));

                    notificationBuilder = getDefaultNotificationBuilder(context);
                    notificationBuilder.setContentTitle(title)
                            .setContentText(contentText)
                            .setContentIntent(detailPendingIntent)
                            .setAutoCancel(true)
                            .setWhen(System.currentTimeMillis())
                            .addAction(R.drawable.ic_action_accept, "Check in", detailPendingIntent);
                            //.addAction(R.drawable.ic_refuse, "Turn off for this vendor", refusePendingIntent);
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setNotifications(Context context) {
        dismissNotifications(context);
        ArrayList<String> uuidList = new ArrayList<>();
        for (String uuid : periodicScanDeviceMap.keySet())
            if (periodicScanDeviceMap.get(uuid).mToBeNotified)
                uuidList.add(uuid);
        if (uuidList.size() > 0) {
            Log.e(TAG, "Setting all notifications");
            /*if (uuidList.size() == 1)
                setNotificationForVendor(uuidList.get(0), context);
            else
                showDialogForList(context, uuidList);*/
            setNotificationForVendor(uuidList.get(0), context);
        }
    }

    private static void showDialogForList(Context context, ArrayList<String> uuids) {

    }

    public static void dismissNotifications(Context context) {
        notificationManager.cancelAll();
        notificationBuilder = getDefaultNotificationBuilder(context);
    }

    @Override
    protected Region createRegion() {
        return new Region(REGION_UNIQUE_ID, null, null, null);  // search for multiple beacons, so no rules
    }

    @Override
    protected void onRangedBeacons(final List<Beacon> beaconList) {
        Log.e(TAG, "Ranged; size - " + beaconList.size());
        for (int i = 0; i < beaconList.size(); ++i) {
            Beacon beacon = beaconList.get(i);
            String uuid = beacon.getProximityUUID();    // TODO modify this if major, minor also required
            Log.e(TAG, "UUID scanned - " + uuid.toUpperCase());
            if (uuidDatabase.contains(uuid.toUpperCase())) {
                DeviceParams deviceParams = null;
                if (periodicScanDeviceMap.containsKey(uuid)) {
                    deviceParams = periodicScanDeviceMap.get(uuid);
                    if (!deviceParams.mFoundInThisScan) {
                        ++(deviceParams.mCount);
                        deviceParams.mFoundInThisScan = true;
                    } else continue;
                } else {
                    periodicScanDeviceMap.put(uuid, new DeviceParams(1, true));
                    deviceParams = periodicScanDeviceMap.get(uuid);
                }
                Log.e(TAG, "count-" + deviceParams.mCount +
                        ";lim-" + PERIODIC_SCAN_BEACON_LIMIT);
                if (deviceParams.mCount == PERIODIC_SCAN_BEACON_LIMIT) {
                    deviceParams.mCount = 0;
                    deviceParams.mToBeNotified = true;
                    //setNotificationForVendor(uuid, getApplicationContext(), NOTIFICATION_ID + i);
                }
                writeHashMapToFile();
            }
        }
    }

    @Override
    protected void scan() {
        if (!running) {
            running = true;
            mTimer.schedule(mCheckTask, 0, INTERVAL);
        }
    }

    public static boolean isRunning() { return running; }

    public static void checkAndStartScan(Context context) {
        if (!running && isBLESupported) {
            context.startService(new Intent(context, PeriodicBFS.class));
        }
    }

    private class BeaconCheckTask extends TimerTask {
        private Runnable mScanningRunnable;

        public BeaconCheckTask() {
            mScanningRunnable = new Runnable() {
                @Override
                public void run() {
                    startScanning();
                    uiThreadHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stopScanning();
                        }
                    }, SCAN_PERIOD);
                }
            };
        }

        public void startScanning() {
            turnOnBluetooth();
            for (DeviceParams params : periodicScanDeviceMap.values())
                params.mFoundInThisScan = params.mToBeNotified = false;
            Log.e(TAG, "Started Scan");
            uiThreadHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBeaconManager.startRangingAndDiscoverDevice(mRegion);
                }
            }, SCAN_START_DELAY); // delay required as scanning will not work right upon enabling BT
        }

        public void stopScanning() {
            mBeaconManager.stopRanging(mRegion);
            turnOffBluetooth();
            Log.e(TAG, "Stopped Scan");

            for (String uuid : periodicScanDeviceMap.keySet())
                if (!periodicScanDeviceMap.get(uuid).mFoundInThisScan)
                    periodicScanDeviceMap.remove(uuid);
            setNotifications(getApplicationContext());
        }

        @Override
        public void run() {
            readUUIDsFromFile(getApplicationContext());
            if (uuidDatabase != null)
                uiThreadHandler.post(mScanningRunnable);
        }
    }

    private class DeviceParams {
        public int mCount;
        public boolean mFoundInThisScan;
        public boolean mToBeNotified;

        public DeviceParams(int count, boolean foundInThisScan) {
            mCount = count;
            mFoundInThisScan = foundInThisScan;
            mToBeNotified = false;
        }
    }
}
