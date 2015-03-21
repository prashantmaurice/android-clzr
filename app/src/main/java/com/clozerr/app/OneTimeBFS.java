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
    public static boolean isRunning = false;

    @Override
    public void onDestroy() {
        isRunning = false;
        super.onDestroy();
    }

    @Override
    protected Region createRegion() {
        // TODO set the UUID to the region
        if (mUUIDs != null)
            return new Region(REGION_UNIQUE_ID, mUUIDs[0], null, null);
        else
            return new Region(REGION_UNIQUE_ID, null, null, null);
    }

    @Override
    protected void onRangedBeacons(final List<Beacon> beaconList) {
        for (Beacon beacon : beaconList) {
            // TODO put condition to check if this is the beacon for the vendor
            if (mUUIDs != null && beacon.getProximityUUID().equalsIgnoreCase(mUUIDs[0])) {
                putToast("Near this restaurant. Check in?", Toast.LENGTH_LONG);
                mBeaconManager.stopRanging(mRegion);
                turnOffBluetooth();
                Log.e(TAG, "Stopped Scan");
                isRunning = false;
                return;
            }
        }
    }

    @Override
    protected void scan() {
        turnOnBluetooth();
        Log.e(TAG, "Started Scan");
        isRunning = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBeaconManager.startRangingAndDiscoverDevice(mRegion);
            }
        }, SCAN_START_DELAY); // delay required as scanning will not work right upon enabling BT
    }

    public static void startScan(Context context, String[] UUIDs) {
        context.stopService(new Intent(context, PeriodicBFS.class));
        Intent service = new Intent(context, OneTimeBFS.class);
        service.putExtra("UUIDs", UUIDs);
        context.startService(service);
    }

    public static void stopScan(Context context) {
        context.stopService(new Intent(context, OneTimeBFS.class));
    }
}
