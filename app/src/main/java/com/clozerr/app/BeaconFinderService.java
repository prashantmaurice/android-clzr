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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by S.ARAVIND on 3/3/2015.
 */
@TargetApi(18)
public class BeaconFinderService extends Service {
    private static enum ScanType { PERIODIC_SCAN, ONE_TIME_SCAN };
    private static boolean isPeriodicScanRunning = false;

    private static final long INTERVAL = TimeUnit.MILLISECONDS.convert(10L, TimeUnit.MINUTES);
                                                // TODO make this 10 min
    private static final long SCAN_PERIOD = TimeUnit.MILLISECONDS.convert(3L, TimeUnit.SECONDS);
                                                // TODO modify as required
    private static final long SCAN_START_DELAY = TimeUnit.MILLISECONDS.convert(2L, TimeUnit.SECONDS);
                                                // TODO modify as required

    private static final int PERIODIC_SCAN_BEACON_LIMIT = 3;
    private static final int NOTIFICATION_ID = 0;
    private static final HashMap<UUID, DeviceParams> periodicScanDeviceMap = new HashMap<>();
    public static final String mapContentsFileName = "mapconts.txt";
    private static final String valueSeparator = " ", lineSeparator = "\n";

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
        readHashMapFromFile();
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
                .setSound(soundUri);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            mScanType = (ScanType) (intent.getExtras().get("ScanType"));
            mUUIDs = (intent.hasExtra("UUIDs")) ? (UUID[])(intent.getExtras().get("UUIDs")) : null;
        }
        else {                                     // scan has started on closing app - null intent
            mScanType = ScanType.PERIODIC_SCAN;    // default
            // TODO do something to get back the UUID array
            mUUIDs = null;
        }
        findBeacons();
        return START_STICKY;
    }

    private void readHashMapFromFile() {
        try {
            FileInputStream fileInputStream = openFileInput(mapContentsFileName);
            byte[] dataBytes = new byte[fileInputStream.available()];
            fileInputStream.read(dataBytes);
            fileInputStream.close();
            String[] hashMapData = new String(dataBytes).split(lineSeparator);
            for (String line : hashMapData) {
                String[] values = line.split(valueSeparator);
                String uuid = values[0];
                DeviceParams params = new DeviceParams(Integer.parseInt(values[1]), false);
                periodicScanDeviceMap.put(UUID.fromString(uuid), params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeHashMapToFile() {
        try {
            FileOutputStream fileOutputStream = openFileOutput(mapContentsFileName, Context.MODE_PRIVATE);
            String hashMapData = "";
            for (UUID uuid : periodicScanDeviceMap.keySet())
                // storage format: "UUID1 count1\nUUID2 count2\n" etc etc.
                hashMapData += uuid.toString() + valueSeparator +
                        periodicScanDeviceMap.get(uuid).mCount + lineSeparator;
            fileOutputStream.write(hashMapData.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findBeacons() {
        boolean isBluetoothLESupported;
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
                                // TODO Auto Check-in
                                setNotification("You're in a restaurant. Check in with Clozerr?", null);
                                mChecker.stopScanning();
                            } else if (mScanType == ScanType.PERIODIC_SCAN) {
                                UUID uuid = device.getUuids()[0].getUuid();
                                Log.i("UUIDs", device.getUuids().toString());
                                if (periodicScanDeviceMap.containsKey(uuid)) {
                                    if (!periodicScanDeviceMap.get(uuid).mFoundInThisScan) {
                                        ++(periodicScanDeviceMap.get(uuid).mCount);
                                        periodicScanDeviceMap.get(uuid).mFoundInThisScan = true;
                                    } else return;
                                } else {
                                    periodicScanDeviceMap.put(uuid, new DeviceParams(1, true));
                                }
                                Log.e("Callback", "count-" + periodicScanDeviceMap.get(uuid).mCount +
                                        ";lim-" + PERIODIC_SCAN_BEACON_LIMIT);
                                if (periodicScanDeviceMap.get(uuid).mCount == PERIODIC_SCAN_BEACON_LIMIT) {
                                    periodicScanDeviceMap.get(uuid).mCount = 0;
                                    // TODO put pending intent for CouponDetails page here, based on beacon UUID
                                    setNotification("You're in a restaurant. Check in with Clozerr?", null);
                                }
                                writeHashMapToFile();
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

    public static void stopPeriodicScan(Context context) {
        isPeriodicScanRunning = false;
        context.stopService(new Intent(context, BeaconFinderService.class));
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
        /*private BroadcastReceiver mBluetoothStateReceiver;
        private IntentFilter mBluetoothStateIntentFilter;*/
        private Runnable mScanningRunnable;
        private boolean mHasUserTurnedOnBluetooth;

        public CheckForBeacons() {
            mScanningRunnable = new Runnable() {
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
            };
            // code for checking when bluetooth is ready for scan, but not working consistently
            /*mBluetoothStateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, final Intent intent) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            final String action = intent.getAction();
                            Log.v("Broadcast called", "in service");
                            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
                            {
                                Log.v("Broadcast called", "state changed");
                                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                                if (state == BluetoothAdapter.STATE_ON) {
                                    Log.v("Broadcast called", "state on");
                                    mHandler.post(mScanningRunnable);
                                }
                            }
                        }
                    });
                }
            };
            mBluetoothStateIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);*/
        }

        public void startScanning() {
            if (mScanType == ScanType.PERIODIC_SCAN)
                for (DeviceParams params : periodicScanDeviceMap.values())
                    params.mFoundInThisScan = false;

            if (mUUIDs == null)
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            else
                mBluetoothAdapter.startLeScan(mUUIDs, mLeScanCallback);
        }

        public void stopScanning() {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            if (!mHasUserTurnedOnBluetooth) // if user turned on BT, don't disable it as user might need it
                mBluetoothAdapter.disable();
            Log.e("scan", "End of " + mScanType.toString());

            if (mScanType == ScanType.PERIODIC_SCAN)
                for (UUID uuid : periodicScanDeviceMap.keySet())
                    if (!periodicScanDeviceMap.get(uuid).mFoundInThisScan)
                        periodicScanDeviceMap.remove(uuid);
        }

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mHasUserTurnedOnBluetooth = mBluetoothAdapter.isEnabled();  // check if user has already enabled BT
                        if (!mHasUserTurnedOnBluetooth)                             // disabled, so enable BT
                            mBluetoothAdapter.enable();
                        Log.e("scan", "Start of " + mScanType.toString());

                        mHandler.postDelayed(mScanningRunnable, SCAN_START_DELAY);  // delay required as scanning will not work
                                                                                    // right upon enabling BT
                        //registerReceiver(mBluetoothStateReceiver, mBluetoothStateIntentFilter);
                    }
                });
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
