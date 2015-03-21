package com.clozerr.app;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@TargetApi(18)
public class PeriodicBFS extends BeaconFinderService {

    private static final String TAG = "PBFS";

    private static final long INTERVAL = TimeUnit.MILLISECONDS.convert(1L, TimeUnit.MINUTES);
                                    // TODO make this 10 min
    private static final long SCAN_PERIOD = TimeUnit.MILLISECONDS.convert(5L, TimeUnit.SECONDS);
                                    // TODO modify as required

    private static final int PERIODIC_SCAN_BEACON_LIMIT = 3;
    private static final int NOTIFICATION_ID = 0;
    private static final HashMap<String, DeviceParams> periodicScanDeviceMap = new HashMap<>();
    private static final String valueSeparator = " ", lineSeparator = "\n";
    private static NotificationCompat.Builder notificationBuilder;
    private static NotificationManager notificationManager;
    public static boolean isRunning = false;

    public static final String mapContentsFileName = "mapContents.txt";

    private Timer mTimer;

    @Override
    public void onCreate() {
        super.onCreate();
        readHashMapFromFile();

        mTimer = new Timer();

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(soundUri);
    }

    @Override
    public void onDestroy() {
        dismissNotification();
        isRunning = false;
        if (!isScanningAllowed) {               // if this was because scanning was disallowed by settings
            periodicScanDeviceMap.clear();
            writeHashMapToFile();               // next time scan starts, start with an empty hash map
        }
        super.onDestroy();
    }

    private void readHashMapFromFile() {
        try {
            FileInputStream fileInputStream = openFileInput(mapContentsFileName);
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
            // TODO if any, read other stored parameters
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeHashMapToFile() {
        try {
            FileOutputStream fileOutputStream = openFileOutput(mapContentsFileName, Context.MODE_PRIVATE);
            JSONObject hashMapObject = new JSONObject("{}");
            for (String uuid : periodicScanDeviceMap.keySet())
                hashMapObject.put(uuid, periodicScanDeviceMap.get(uuid).mCount);
            JSONObject rootObject = new JSONObject("{}");
            rootObject.put("hashMap", hashMapObject);
            // TODO if any, put other parameters to be stored
            String fileData = rootObject.toString();
            fileOutputStream.write(fileData.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setNotification(CharSequence text, PendingIntent intent)
    {
        notificationBuilder.setContentText(text)
                .setContentIntent(intent)
                .setWhen(System.currentTimeMillis());
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void dismissNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    protected Region createRegion() {
        return new Region(REGION_UNIQUE_ID, null, null, null);  // search for multiple beacons, so no rules
    }

    @Override
    protected void onRangedBeacons(final List<Beacon> beaconList) {
        for (Beacon beacon : beaconList) {
            String uuid = beacon.getProximityUUID();    // TODO modify this if major, minor also required
            if (periodicScanDeviceMap.containsKey(uuid)) {
                if (!periodicScanDeviceMap.get(uuid).mFoundInThisScan) {
                    ++(periodicScanDeviceMap.get(uuid).mCount);
                    periodicScanDeviceMap.get(uuid).mFoundInThisScan = true;
                }
                else continue;
            }
            else {
                periodicScanDeviceMap.put(uuid, new DeviceParams(1, true));
            }
            Log.e(TAG, "count-" + periodicScanDeviceMap.get(uuid).mCount +
                    ";lim-" + PERIODIC_SCAN_BEACON_LIMIT);
            if (periodicScanDeviceMap.get(uuid).mCount == PERIODIC_SCAN_BEACON_LIMIT) {
                periodicScanDeviceMap.get(uuid).mCount = 0;
                // TODO put pending intent for CouponDetails page here, based on beacon UUID
                setNotification("You're in a restaurant. Check in with Clozerr?", null);
            }
            writeHashMapToFile();
        }
    }

    @Override
    protected void scan() {
        isRunning = true;
        mTimer.schedule(new BeaconCheckTask(), 0, INTERVAL);
    }

    public static void startScan(Context context) {
        if (!isRunning) {
            Intent service = new Intent(context, PeriodicBFS.class);

            // TODO get UUIDs of all vendors and set UUID parameters
            /*UUID[] UUIDs = new UUID[]{};
            service.putExtra("UUIDs", UUIDs);*/

            context.startService(service);
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
