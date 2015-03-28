package com.clozerr.app;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.Region;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@TargetApi(18)
public class PeriodicBFS extends BeaconFinderService {

    private static final String TAG = "PBFS";

    private static final long INTERVAL = TimeUnit.MILLISECONDS.convert(20L, TimeUnit.SECONDS);
                                    // TODO make this 10 min
    private static final long SCAN_PERIOD = TimeUnit.MILLISECONDS.convert(6L, TimeUnit.SECONDS);
                                    // TODO modify as required

    private static final int PERIODIC_SCAN_BEACON_LIMIT = 3;
    private static final int NOTIFICATION_ID = 0;
    private static final HashMap<String, DeviceParams> periodicScanDeviceMap = new HashMap<>();
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
        dismissNotification(getApplicationContext());
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
            FileInputStream fileInputStream = openFileInput(MAP_CONTENTS_FILE_NAME);
            byte[] dataBytes = new byte[fileInputStream.available()];
            fileInputStream.read(dataBytes);
            fileInputStream.close();
            JSONObject rootObject = new JSONObject(new String(dataBytes));
            JSONObject hashMapObject = rootObject.getJSONObject("hashMap");
            for (Iterator<String> iterator = hashMapObject.keys(); iterator.hasNext(); ) {
                String uuid = iterator.next();
                DeviceParams params = new DeviceParams(hashMapObject.getInt(uuid), false);
                periodicScanDeviceMap.put(uuid, params);
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
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(soundUri);
    }

    private static void setNotificationForVendor(String uuid, Context context)
    {
        Log.e(TAG, "UUID for notification - " + uuid);
        dismissNotification(context);
        try {
            FileInputStream fileInputStream = context.openFileInput(UUIDDownloader.UUID_FILE_NAME);
            byte[] dataBytes = new byte[fileInputStream.available()];
            fileInputStream.read(dataBytes);
            // TODO change if JSON changes
            JSONArray rootArray = new JSONArray(new String(dataBytes));
            Log.e(TAG, "root - " + rootArray.toString());
            VendorParams vendorParams = null;
            for (int i = 0; i < rootArray.length(); ++i) {
                try {
                    vendorParams = new VendorParams(rootArray.getJSONObject(i));
                    if (vendorParams.mUUID.equalsIgnoreCase(uuid)) break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String title = "Clozerr - " + vendorParams.mName;
            String contentText = vendorParams.mNextOfferCaption;
            Intent detailIntent = new Intent(context, CouponDetails.class);
            detailIntent.putExtra("vendor_id", vendorParams.mVendorID);
            detailIntent.putExtra("offer_id", vendorParams.mNextOfferID);
            detailIntent.putExtra("offer_caption", vendorParams.mNextOfferCaption);
            detailIntent.putExtra("offer_text", vendorParams.mNextOfferDescription);
            detailIntent.putExtra("from_periodic_scan", true);
            PendingIntent notificationIntent = PendingIntent.getActivity(context, 1234,
                                                                         detailIntent,
                                                                         PendingIntent.FLAG_ONE_SHOT);
            notificationBuilder.setContentTitle(title)
                               .setContentText(contentText)
                               .setContentIntent(notificationIntent)
                               .setAutoCancel(true)
                               .setWhen(System.currentTimeMillis())
                               .addAction(R.drawable.ic_action_accept, "Check In", notificationIntent);
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dismissNotification(Context context) {
        notificationManager.cancel(NOTIFICATION_ID);
        notificationBuilder = getDefaultNotificationBuilder(context);
    }

    @Override
    protected Region createRegion() {
        return new Region(REGION_UNIQUE_ID, null, null, null);  // search for multiple beacons, so no rules
    }

    @Override
    protected void onRangedBeacons(final List<Beacon> beaconList) {
        Log.e(TAG, "Ranged");
        for (Beacon beacon : beaconList) {
            String uuid = beacon.getProximityUUID();    // TODO modify this if major, minor also required
            if (uuidDatabase.contains(uuid.toUpperCase())) {
                if (periodicScanDeviceMap.containsKey(uuid)) {
                    if (!periodicScanDeviceMap.get(uuid).mFoundInThisScan) {
                        ++(periodicScanDeviceMap.get(uuid).mCount);
                        periodicScanDeviceMap.get(uuid).mFoundInThisScan = true;
                    } else continue;
                } else {
                    periodicScanDeviceMap.put(uuid, new DeviceParams(1, true));
                }
                Log.e(TAG, "count-" + periodicScanDeviceMap.get(uuid).mCount +
                        ";lim-" + PERIODIC_SCAN_BEACON_LIMIT);
                if (periodicScanDeviceMap.get(uuid).mCount == PERIODIC_SCAN_BEACON_LIMIT) {
                    periodicScanDeviceMap.get(uuid).mCount = 0;
                    setNotificationForVendor(uuid, getApplicationContext());
                }
                writeHashMapToFile();
            }
        }
    }

    @Override
    protected void scan() {
        running = true;
        mTimer.schedule(mCheckTask, 0, INTERVAL);
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
                    mHandler.postDelayed(new Runnable() {
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
                params.mFoundInThisScan = false;
            Log.e(TAG, "Started Scan");
            mHandler.postDelayed(new Runnable() {
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
        }

        @Override
        public void run() {
            if (uuidDatabase != null)
                mHandler.post(mScanningRunnable);
        }
    }

    private class DeviceParams {
        public int mCount;
        public boolean mFoundInThisScan;

        public DeviceParams(int count, boolean foundInThisScan) {
            mCount = count;
            mFoundInThisScan = foundInThisScan;
        }
    }
}
