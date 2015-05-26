package com.clozerr.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.Region;

import java.util.List;

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
        return new Region(REGION_ID, getUuidWithoutHyphens(uuid), null, null);
    }

    @Override
    protected void onRangedBeacons(final List<Beacon> beaconList) {
        Log.e(TAG, "Ranged; size - " + beaconList.size());
        for (Beacon beacon : beaconList) {
            Log.e(TAG, "uuid found: " + beacon.getProximityUUID());
            if (beacon.getProximityUUID().equalsIgnoreCase(uuid)) {
                // TODO Auto check-in
                putToast(getApplicationContext(), "Near this restaurant. Check in?", Toast.LENGTH_LONG);
                beaconManager.stopRanging(scanningRegion);
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
        uiThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                beaconManager.startRangingAndDiscoverDevice(scanningRegion);
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
        if (running) {
            context.stopService(new Intent(context, OneTimeBFS.class));
            PeriodicBFS.checkAndStartScan(context);
        }
    }
}
