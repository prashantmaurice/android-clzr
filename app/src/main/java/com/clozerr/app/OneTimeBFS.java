package com.clozerr.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.util.Predicate;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;

import java.util.List;

@TargetApi(18)
public class OneTimeBFS extends BeaconFinderService {

    private static final String TAG = "OTBFS";
    //private static String uuid = "";
    private static BeaconDBParams beaconDBParams;
    private static boolean running = false;

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
    }

    public static boolean isRunning() { return running; }

    /*@Override
    protected Region createRegion() {
        return new Region(REGION_ID, getUuidWithoutHyphens(CLOZERR_UUID), beaconDBParams.mMajor, beaconDBParams.mMinor);
    }*/

    @Override
    protected void onRangedBeacons(final List<Beacon> beaconList) {
        Log.e(TAG, "Ranged; size - " + beaconList.size());
        for (final Beacon beacon : beaconList) {
            BeaconDBParams params = new BeaconDBParams(beacon);
            Log.e(TAG, "found " + params.toString());
            if (params.equals(beaconDBParams)) {
                VendorParams vendorParams = VendorParams.findVendorParamsInFile(getApplicationContext(), new Predicate<VendorParams>() {
                    @Override
                    public boolean apply(VendorParams vendorParams) {
                        return vendorParams.mBeaconParams.equals(beaconDBParams);
                    }
                });
                if (vendorParams != null) {
                    String toastText = "You are close to this place. ";
                    if (vendorParams.mHasOffers)
                        toastText += "And you have rewards you can use here! Check in and use them!";
                    else if (vendorParams.mLoyaltyType.equalsIgnoreCase("sx"))
                        toastText += "Go in and get your stamps during billing!";
                    else
                        toastText += "Go in and mark your visit!";
                    putToast(getApplicationContext(), toastText, Toast.LENGTH_LONG);
                    beaconManager.stopRanging(scanningRegion);
                    turnOffBluetooth();
                    Log.e(TAG, "Stopped Scan");
                    running = false;
                    return;
                }
            }
        }
    }

    /*@Override
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
    }*/

    @Override
    protected void runService() {
        scanningRegion = new Region(REGION_ID, getUuidWithoutHyphens(CLOZERR_UUID), beaconDBParams.mMajor, beaconDBParams.mMinor);
        beaconManager.connect(new ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
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
        });
    }

    public static void checkAndStartScan(Context context, BeaconDBParams params) {
        if (params != null && isBLESupported) {
            context.stopService(new Intent(context, PeriodicBFS.class));
            beaconDBParams = params;
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
