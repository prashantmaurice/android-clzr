package com.clozerr.app;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.BeaconManager;
import com.jaalee.sdk.RangingListener;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by S.ARAVIND on 3/3/2015.
 */
@TargetApi(18)
public abstract class BeaconFinderService extends Service {
    private static final String TAG = "BFS";
    protected static final String REGION_UNIQUE_ID = "BeaconFinderServiceRegionUniqueID";

    // TODO get this from Settings
    protected static boolean isScanningAllowed = true;

    protected static boolean hasUserActivatedBluetooth = false;
    protected static final long SCAN_START_DELAY = TimeUnit.MILLISECONDS.convert(2L, TimeUnit.SECONDS);
                                                // TODO modify as required
    protected static BluetoothAdapter bluetoothAdapter;

    protected Handler mHandler;
    protected String[] mUUIDs;
    protected BeaconManager mBeaconManager;
    protected Region mRegion;

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = new Handler(Looper.getMainLooper());
        mBeaconManager = new BeaconManager(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO do something to get back the UUID array if not obtained here
        if (intent != null) {
            mUUIDs = (intent.hasExtra("UUIDs")) ? (String[])(intent.getExtras().get("UUIDs")) : null;
        }
        else {                                     // scan has started on closing app - null intent
            mUUIDs = null;
        }
        findBeacons();
        return START_STICKY;
    }

    protected void findBeacons() {
        if (canScanStart()) {
            //mLeScanCallback = createLeScanCallback();
            mRegion = createRegion();
            mBeaconManager.setRangingListener(new RangingListener() {
                @Override
                public void onBeaconsDiscovered(Region region, final List list) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onRangedBeacons((List<Beacon>)list);
                        }
                    });
                }
            });
            mBeaconManager.connect(new ServiceReadyCallback() {
                @Override
                public void onServiceReady() {
                    scan();
                }
            });
        }
    }

    protected abstract Region createRegion();
    protected abstract void onRangedBeacons(final List<Beacon> beaconList);
    protected abstract void scan();

    // This function is just for putting toasts, but required as work is done on a background thread
    // so if a toast is directly put, the app will crash (Toasts must be put in the UI thread)
    protected void putToast(final CharSequence text, final int duration) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, duration).show();
            }
        });
    }

    protected boolean canScanStart() {
        if (isScanningAllowed) {
            if (bluetoothAdapter == null) {
                putToast("Sorry, but your device doesn't support Bluetooth." +
                        " Clozerr beacon-finding services won\'t work now.", Toast.LENGTH_LONG);
                return false;
            }
            else if (!getApplicationContext().getPackageManager().
                        hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                putToast("Sorry, but your device doesn't support Bluetooth Low Energy." +
                        " Clozerr beacon-finding services won\'t work now.", Toast.LENGTH_LONG);
                return false;
            }
            else return true;
        }
        else return false;
    }

    protected void turnOnBluetooth() {
        hasUserActivatedBluetooth = bluetoothAdapter.isEnabled();  // check if user has already enabled BT
        if (!hasUserActivatedBluetooth)                            // disabled, so enable BT
            bluetoothAdapter.enable();
        Log.e(TAG, "BT On");
    }

    protected void turnOffBluetooth() {
        if (!hasUserActivatedBluetooth) // if user turned on BT, don't disable it as user might need it
            bluetoothAdapter.disable();
        Log.e(TAG, "BT Off");
    }

    public static void disallowScanning(Context context) {
        isScanningAllowed = false;
        if (PeriodicBFS.isRunning)
            context.stopService(new Intent(context, PeriodicBFS.class));
        else if (OneTimeBFS.isRunning)
            context.stopService(new Intent(context, OneTimeBFS.class));
    }

    public static void allowScanning(Context context) {
        isScanningAllowed = true;
        PeriodicBFS.startScan(context);
    }
}
