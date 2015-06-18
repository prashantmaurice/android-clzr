package com.clozerr.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.util.Predicate;
import com.commonsware.cwac.wakeful.WakefulIntentService;
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

    /*public OneTimeBFS() {
        super();
    }*/

    /*@Override
    public void onDestroy() {
        if (running) {
            running = false;
            beaconManager.stopRanging(scanningRegion);
            turnOffBluetooth();
        }
        super.onDestroy();
    }*/

    public static boolean isRunning() { return running; }

    /*@Override
    protected Region createRegion() {
        return new Region(REGION_ID, getUuidWithoutHyphens(CLOZERR_UUID), beaconDBParams.mMajor, beaconDBParams.mMinor);
    }*/

    @Override
    protected void onRangedBeacons(final List<Beacon> beaconList) {
        //Log.e(TAG, "Ranged; size - " + beaconList.size());
        for (final Beacon beacon : beaconList) {
            BeaconDBParams params = new BeaconDBParams(beacon);
            Log.e(TAG, "found " + params.toString());
            if (params.equals(beaconDBParams)) {
                VendorParams vendorParams = VendorParams.findVendorParamsInFile(getApplicationContext(), new Predicate<VendorParams>() {
                    @Override
                    public boolean apply(VendorParams vendorParams) {
                        return beaconDBParams.equals(vendorParams.mBeaconParams);
                    }
                });
                if (vendorParams != null) {
                    String toastText;
                    if (vendorParams.mHasOffers)
                        toastText = "Redeem your rewards here!";
                    else if (vendorParams.mLoyaltyType.equalsIgnoreCase("sx"))
                        toastText = "Get your stamps during billing!";
                    else
                        toastText = "Mark your visit!";
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
        bgThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                beaconManager.startRangingAndDiscoverDevice(scanningRegion);
            }
        }, SCAN_START_DELAY); // delay required as scanning will not work right upon enabling BT
    }*/

    /*@Override
    protected void runService() {
        scanningRegion = new Region(REGION_ID, getUuidWithoutHyphens(CLOZERR_UUID), beaconDBParams.mMajor, beaconDBParams.mMinor);
        beaconManager.connect(new ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                turnOnBluetooth();
                Log.e(TAG, "Started Scan");
                running = true;
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        beaconManager.startRangingAndDiscoverDevice(scanningRegion);
                    }
                }, SCAN_START_DELAY); // delay required as scanning will not work right upon enabling BT
            }
        });
    }*/

    @Override
    protected void doWakefulWork(Intent intent) {
        /*beaconManager.connect(new ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                turnOnBluetooth();
                Log.e(TAG, "Started Scan");
                running = true;
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        beaconManager.startRangingAndDiscoverDevice(scanningRegion);
                    }
                }, SCAN_START_DELAY); // delay required as scanning will not work right upon enabling BT
            }
        });*/
        setListener();
        turnOnBluetooth();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                beaconManager.connect(new ServiceReadyCallback() {
                    @Override
                    public void onServiceReady() {
                        Log.e(TAG, "Started Scan");
                        running = true;
                        scanningRegion = new Region(REGION_ID, getUuidWithoutHyphens(CLOZERR_UUID), beaconDBParams.mMajor, beaconDBParams.mMinor);
                        beaconManager.startRangingAndDiscoverDevice(scanningRegion);
                    }
                });
            }
        }, SCAN_START_DELAY); // delay required as scanning will not work right upon enabling BT
    }

    @Override
    protected boolean isListeningAfterWork() {
        return false;
    }

    public static void checkAndStartScan(Context context, BeaconDBParams params) {
        if (!running && params != null && checkCompatibility(context) && checkPreferences(context)) {
            //context.stopService(new Intent(context, PeriodicBFS.class));
            //WakefulIntentService.cancelAlarms(context);
            PeriodicBFS.checkAndStopScan(context, false);
            beaconDBParams = params;
            //context.startService(new Intent(context, OneTimeBFS.class));
            WakefulIntentService.sendWakefulWork(context, OneTimeBFS.class);
        }
    }

    public static void checkAndStopScan(Context context) {
        if (running) {
            running = false;
            beaconManager.stopRanging(scanningRegion);
            turnOffBluetooth();
            PeriodicBFS.checkAndStartScan(context);
        }
    }
}
