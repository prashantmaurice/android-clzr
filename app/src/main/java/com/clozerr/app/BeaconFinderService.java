package com.clozerr.app;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by S.ARAVIND on 3/3/2015.
 */
public class BeaconFinderService extends Service {
    public static final long INTERVAL = 1000 * 60 * /*no. of minutes*/1;
    public static final long DEFAULT_DELAY = 0;
    private static boolean isBluetoothLESupported;

    private Timer mTimer;
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mTimer = new Timer();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper());

        if (mBluetoothAdapter == null) {
            // TODO device doesn't support Bluetooth
            isBluetoothLESupported = false;
            Toast.makeText(mContext, "Sorry, but your device doesn't support Bluetooth.",
                    Toast.LENGTH_LONG).show();
        }
        else {
            if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                // TODO device doesn't support Bluetooth LE
                isBluetoothLESupported = false;
                Toast.makeText(mContext, "Sorry, but your device doesn't support Bluetooth Low Energy.",
                        Toast.LENGTH_SHORT).show();
            }
            else isBluetoothLESupported = true;
        }
        findBeacons();
    }

    public BeaconFinderService() {}

    private void findBeacons() {
        if (isBluetoothLESupported)
            mTimer.schedule(new CheckForBeacons(), DEFAULT_DELAY, INTERVAL);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class CheckForBeacons extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!mBluetoothAdapter.isEnabled()) {
                        Toast.makeText(mContext, "Enabling Bluetooth...", Toast.LENGTH_SHORT).show();
                        mBluetoothAdapter.enable();
                        Toast.makeText(mContext, "Enabled Bluetooth", Toast.LENGTH_SHORT).show();
                    }
                    // TODO check for beacons in between
                    Toast.makeText(mContext, "Disabling Bluetooth...", Toast.LENGTH_SHORT).show();
                    mBluetoothAdapter.disable();
                    Toast.makeText(mContext, "Disabled Bluetooth", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static abstract class OnBeaconFindCallback {
        public abstract void onFoundBeacon();
    }
}
