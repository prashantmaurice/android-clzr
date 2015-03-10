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
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by S.ARAVIND on 3/3/2015.
 */
@TargetApi(18)
public class BeaconFinderService extends Service {
    private static enum ScanType { PERIODIC_SCAN, ONE_TIME_SCAN };
    private static boolean isPeriodicScanRunning = false;
    private static final long INTERVAL = 1000 * 60 * /*no. of minutes*/1/3; // TODO make this 10 min
    private static final long SCAN_PERIOD = 1000 * /*no. of seconds*/8; // TODO modify as required
    private static final long SCAN_START_DELAY = 1000 * /*no. of seconds*/2; // TODO modify as required
    private static int PERIODIC_SCAN_BEACON_LIMIT = 3;
    private static boolean isBluetoothLESupported;
    private static final int NOTIFICATION_ID = 0;
    private static HashMap<BluetoothDevice, DeviceParams> periodicScanDeviceMap = new HashMap<>();

    private Timer mTimer;
    private CheckForBeacons mChecker;
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;
    private Handler mHandler;
    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private ScanType mScanType;
    private UUID[] mUUIDs;

    @Override
    public void onCreate() {
        super.onCreate();
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
                // TODO set sound
                /*.setSound(soundUri)*/;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            if (intent.getExtras() != null)
                mScanType = (ScanType) (intent.getExtras().get("ScanType"));
        }
        else mScanType = ScanType.PERIODIC_SCAN;    // default
        // TODO set this to the array of UUIDs returned
        mUUIDs = /*(intent.hasExtra("UUIDs")) ? (UUID[])(intent.getExtras().get("UUIDs")) : null*/
                null;
        findBeacons();
        return START_STICKY;
    }

    public BeaconFinderService() {}

    private void findBeacons() {
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
        if (isBluetoothLESupported) {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (mScanType == ScanType.ONE_TIME_SCAN) {
                                // TODO DON'T put pending intent for CouponDetails page here. Put something else.
                                setNotification("You're in a restaurant. Check in with Clozerr?", null);
                                mChecker.stopScanning();
                            }
                            else if (mScanType == ScanType.PERIODIC_SCAN) {
                                if (periodicScanDeviceMap.containsKey(device)) {
                                    if (!periodicScanDeviceMap.get(device).foundInThisScan) {
                                        ++(periodicScanDeviceMap.get(device).count);
                                        periodicScanDeviceMap.get(device).foundInThisScan = true;
                                    }
                                }
                                else {
                                    periodicScanDeviceMap.put(device, new DeviceParams(1, true));
                                }
                                Log.e("Callback", "count-"+ periodicScanDeviceMap.get(device).count+";lim-"+ PERIODIC_SCAN_BEACON_LIMIT);
                                if (periodicScanDeviceMap.get(device).count == PERIODIC_SCAN_BEACON_LIMIT) {
                                    periodicScanDeviceMap.get(device).count = 0;
                                    // TODO put pending intent for CouponDetails page here, based on beacon UUID/Address
                                    setNotification("You're in a restaurant. Check in with Clozerr?", null);
                                }
                            }
                        }
                    });
                }
            };
            mChecker = new CheckForBeacons();
            if (mScanType == ScanType.PERIODIC_SCAN) {
                isPeriodicScanRunning = true;
                mTimer.schedule(mChecker, 0, INTERVAL);
            }
            else if (mScanType == ScanType.ONE_TIME_SCAN) {
                mTimer.schedule(mChecker, 0);
            }
        }
    }

    private void setNotification(CharSequence text, PendingIntent intent)
    {
        mNotificationBuilder.setContentText(text)
                .setContentIntent(intent)
                .setWhen(System.currentTimeMillis());
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void dismissNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void startPeriodicScan(Context context) {
        if (!isPeriodicScanRunning) {
            Intent service = new Intent(context, BeaconFinderService.class);
            service.putExtra("ScanType", ScanType.PERIODIC_SCAN);
            context.startService(service);
        }
    }

    public static void startOneTimeScan(Context context, UUID[] serviceUUIDs) {
        Intent service = new Intent(context, BeaconFinderService.class);
        service.putExtra("ScanType", ScanType.ONE_TIME_SCAN);
        service.putExtra("UUIDs", serviceUUIDs);
        context.stopService(new Intent(context, BeaconFinderService.class));
        isPeriodicScanRunning = false;
        context.startService(service);
    }

    // ALTERNATE
    public static void stopOneTimeScan(Context context) {
        context.stopService(new Intent(context, BeaconFinderService.class));
    }

    // ALTERNATE
    @Override
    public void onDestroy() {
        mChecker.stopScanning();
        mTimer.cancel();
        super.onDestroy();
    }

    private class CheckForBeacons extends TimerTask {
        public void startScanning() {
            if (mScanType == ScanType.PERIODIC_SCAN)
                for (DeviceParams params : periodicScanDeviceMap.values())
                    params.foundInThisScan = false;

            if (mUUIDs == null)
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            else
                mBluetoothAdapter.startLeScan(mUUIDs, mLeScanCallback);
        }

        public void stopScanning() {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mBluetoothAdapter.disable();
            Log.e("scan", "Disabled bluetooth-" + mScanType.toString());

            if (mScanType == ScanType.PERIODIC_SCAN)
                for (BluetoothDevice device : periodicScanDeviceMap.keySet())
                    if (!periodicScanDeviceMap.get(device).foundInThisScan)
                        periodicScanDeviceMap.remove(device);
        }

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.enable();
                    }
                    Log.e("scan", "Enabled bluetooth-" + mScanType.toString());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startScanning();
                            // ALTERNATE (just the if condition alone)
                            if (mScanType == ScanType.PERIODIC_SCAN)
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        stopScanning();
                                    }
                                }, SCAN_PERIOD);
                        }
                    }, SCAN_START_DELAY);
                }
            });
        }
    }

    private class DeviceParams {
        public int count;
        public boolean foundInThisScan;

        public DeviceParams(int count, boolean foundInThisScan) {
            this.count = count;
            this.foundInThisScan = foundInThisScan;
        }
    }
}
