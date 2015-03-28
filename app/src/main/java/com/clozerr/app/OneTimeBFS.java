package com.clozerr.app;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.Region;

import java.util.List;
import java.util.UUID;

@TargetApi(18)
public class OneTimeBFS extends BeaconFinderService {

    private static final String TAG = "OTBFS";
    private static String uuid = "";
    private static boolean running = false;

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
    }

    public static boolean isRunning() { return running; }

    @Override
    protected Region createRegion() {
        String regionUUID = "";
        for (char c : uuid.toCharArray())
            if (c != '-') regionUUID += String.valueOf(c);
        Log.e(TAG, "uuids - " + uuid + " & " + regionUUID);
        return new Region(REGION_UNIQUE_ID, regionUUID, null, null);
    }

    @Override
    protected void onRangedBeacons(final List<Beacon> beaconList) {
        for (Beacon beacon : beaconList) {
            if (beacon.getProximityUUID().equalsIgnoreCase(uuid)) {
                // TODO Auto check-in
                putToast("Near this restaurant. Check in?", Toast.LENGTH_LONG);
                mBeaconManager.stopRanging(mRegion);
                turnOffBluetooth();
                Log.e(TAG, "Stopped Scan");
                running = false;
                return;
            }
        }
    }

    @Override
    protected void scan() {
        turnOnBluetooth();
        Log.e(TAG, "Started Scan");
        running = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBeaconManager.startRangingAndDiscoverDevice(mRegion);
            }
        }, SCAN_START_DELAY); // delay required as scanning will not work right upon enabling BT
    }

    public static void checkAndStartScan(Context context, String vendorUuid) {
        if (vendorUuid != null && !vendorUuid.isEmpty() && isBLESupported) {
            context.stopService(new Intent(context, PeriodicBFS.class));
            uuid = vendorUuid;
            context.startService(new Intent(context, OneTimeBFS.class));
        }
    }

    public static void checkAndStopScan(Context context) {
        if (running)
            context.stopService(new Intent(context, OneTimeBFS.class));
    }
}
