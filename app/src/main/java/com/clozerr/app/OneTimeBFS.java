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
    protected BluetoothAdapter.LeScanCallback createLeScanCallback() {
        return new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto Check-in
                        putToast("Near this restaurant. Check in?", Toast.LENGTH_LONG);
                        bluetoothAdapter.stopLeScan(mLeScanCallback);
                        turnOffBluetooth();
                        Log.e(TAG, "Stopped Scan");
                        isRunning = false;
                    }
                });
            }
        };
    }

    @Override
    protected void scan() {
        turnOnBluetooth();
        Log.e(TAG, "Started Scan");
        isRunning = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mUUIDs == null)
                    bluetoothAdapter.startLeScan(mLeScanCallback);
                else
                    bluetoothAdapter.startLeScan(mUUIDs, mLeScanCallback);
            }
        }, SCAN_START_DELAY); // delay required as scanning will not work right upon enabling BT
    }

    public static void startScan(Context context, UUID[] UUIDs) {
        context.stopService(new Intent(context, PeriodicBFS.class));
        Intent service = new Intent(context, OneTimeBFS.class);
        service.putExtra("UUIDs", UUIDs);
        context.startService(service);
    }

    public static void stopScan(Context context) {
        context.stopService(new Intent(context, OneTimeBFS.class));
    }
}
