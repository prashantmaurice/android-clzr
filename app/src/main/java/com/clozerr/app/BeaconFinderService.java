package com.clozerr.app;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by S.ARAVIND on 3/3/2015.
 */
@TargetApi(18)
public class BeaconFinderService extends Service {
    public static final long INTERVAL = 1000 * 60 * /*no. of minutes*/1; // TODO make this 10 min
    public static final long DEFAULT_DELAY = 0;
    public static final long SCAN_PERIOD = 1000 * /*no. of seconds*/40; // TODO modify as required, as low as possible
    public static final int BEACON_FOUND_LIMIT = 2; // TODO make this 3
    public static boolean hasStarted = false;

    private static boolean isBluetoothLESupported;
    private static final int NOTIFICATION_ID = 0;
    private static int beaconFoundCount = 0;

    private Timer mTimer;
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;
    private Handler mHandler;
    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private String mLastFoundBeaconAddress;
    private boolean mBeaconFound;
    //private boolean mInTheSameScan;
    private CheckForBeacons mChecker;

    @Override
    public void onCreate() {
        super.onCreate();
        hasStarted = true;
        mTimer = new Timer();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper());

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(soundUri);

        if (mBluetoothAdapter == null) {
            isBluetoothLESupported = false;
            Toast.makeText(mContext, "Sorry, but your device doesn't support Bluetooth." +
                            " Clozerr beacon-finding services won\'t work now.",
                    Toast.LENGTH_LONG).show();
        }
        else if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                isBluetoothLESupported = false;
                Toast.makeText(mContext, "Sorry, but your device doesn't support Bluetooth Low Energy." +
                                " Clozerr beacon-finding services won\'t work now.",
                        Toast.LENGTH_SHORT).show();
        }
        else isBluetoothLESupported = true;
        findBeacons();
    }

    public BeaconFinderService() {}

    private void findBeacons() {
        if (isBluetoothLESupported) {
            mLastFoundBeaconAddress = "";
            mBeaconFound = false;
            //mInTheSameScan = false;
            mChecker = new CheckForBeacons();
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mBeaconFound = true;
                            Toast.makeText(mContext, "Device found:" + device.getAddress(), Toast.LENGTH_SHORT).show();
                            if (mLastFoundBeaconAddress.equals(device.getAddress())/* && !mInTheSameScan*/) {
                                ++beaconFoundCount;
                                if (beaconFoundCount == BEACON_FOUND_LIMIT) {
                                    beaconFoundCount = 0;
                                    mLastFoundBeaconAddress = "";
                                    setNotification("You're in a restaurant. Check in with Clozerr?", null);
                                }
                            } else {
                                beaconFoundCount = 1;
                                mLastFoundBeaconAddress = device.getAddress();
                            }
                            //mInTheSameScan = true;
                            mChecker.stopScanning();
                        }
                    });
                }
            };
            mTimer.schedule(mChecker, DEFAULT_DELAY, INTERVAL);
        }
    }

    public void setNotification(CharSequence text, PendingIntent intent)
    {
        mNotificationBuilder.setContentText(text)
                .setContentIntent(intent)
                .setWhen(System.currentTimeMillis());
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    public void dismissNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class CheckForBeacons extends TimerTask {
        public void startScanning() {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
                Toast.makeText(mContext, "Enabled Bluetooth", Toast.LENGTH_SHORT).show();
            }
            mBeaconFound = false;
            // TODO use startScan() when API 21 libraries are available - this is deprecated
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }

        public void stopScanning() {
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mBluetoothAdapter.disable();
                Toast.makeText(mContext, "Disabled Bluetooth", Toast.LENGTH_SHORT).show();
                if (!mBeaconFound) {
                    beaconFoundCount = 0;
                    mLastFoundBeaconAddress = "";
                }
            }
        }

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    startScanning();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() { stopScanning(); }
                    }, SCAN_PERIOD);
                }
            });
        }
    }
}
