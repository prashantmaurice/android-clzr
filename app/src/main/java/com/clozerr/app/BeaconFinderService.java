package com.clozerr.app;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by S.ARAVIND on 3/3/2015.
 */
@TargetApi(18)
public abstract class BeaconFinderService extends Service {
    private static final String TAG = "BFS";
    protected static final String REGION_UNIQUE_ID = "BeaconFinderServiceRegionUniqueID";
    protected static final long SCAN_START_DELAY = TimeUnit.MILLISECONDS.convert(2L, TimeUnit.SECONDS);
    public static final String ACTION_UPDATE_UUID_DATABASE = "UpdateUUIDDatabase";

    protected static boolean isBLESupported = true;
    // TODO get isScanningAllowed from Settings
    protected static boolean isScanningAllowed = true;
    protected static boolean hasUserActivatedBluetooth = false;

    protected static BluetoothAdapter bluetoothAdapter;
    protected static ArrayList<String> uuidDatabase = null;

    protected Handler mHandler;
    //protected String[] mUUIDs;
    protected BeaconManager mBeaconManager;
    protected Region mRegion;
    //protected Boolean mIsWaitingForUpdate = false;

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
        findBeacons();
        return START_STICKY;
    }

    protected void findBeacons() {
        if (canScanStart()) {
            mRegion = createRegion();
            mBeaconManager.setRangingListener(new RangingListener() {
                @Override
                public void onBeaconsDiscovered(Region region, final List list) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onRangedBeacons((List<Beacon>) list);
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
                isBLESupported = false;
                return false;
            }
            else if (!getApplicationContext().getPackageManager().
                        hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                putToast("Sorry, but your device doesn't support Bluetooth Low Energy." +
                        " Clozerr beacon-finding services won\'t work now.", Toast.LENGTH_LONG);
                isBLESupported = false;
                return false;
            }
            else {
                isBLESupported = true;
                getApplicationContext().registerReceiver(new UUIDUpdateReceiver(),
                        new IntentFilter(ACTION_UPDATE_UUID_DATABASE));
                UUIDDownloadBaseReceiver.scheduleDownload(getApplicationContext());
                if (uuidDatabase == null) {
                    try {
                        readUUIDsFromFile(getApplicationContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }
        else return false;
    }

    protected void readUUIDsFromFile(Context context) throws IOException, JSONException {
        try {
            FileInputStream fileInputStream = context.openFileInput(UUIDDownloader.UUID_FILE_NAME);
            byte[] dataBytes = new byte[fileInputStream.available()];
            fileInputStream.read(dataBytes);
            fileInputStream.close();
            // TODO change format if JSON changes
            JSONArray rootArray = new JSONArray(new String(dataBytes));
            uuidDatabase = new ArrayList<String>();
            for (int i = 0; i < rootArray.length(); ++i)
                if (rootArray.getJSONObject(i).getJSONArray("UUID").length() > 0)
                    uuidDatabase.add(rootArray.getJSONObject(i).getJSONArray("UUID").getString(0));
        } catch (Exception e) {
            if (e instanceof FileNotFoundException)
                readUUIDsFromFile(context);
            else throw e;
        }
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
        if (PeriodicBFS.isRunning())
            context.stopService(new Intent(context, PeriodicBFS.class));
        else if (OneTimeBFS.isRunning())
            context.stopService(new Intent(context, OneTimeBFS.class));
    }

    public static void allowScanning(Context context) {
        isScanningAllowed = true;
        PeriodicBFS.checkAndStartScan(context);
    }

    protected static class VendorParams {
        public String mName;
        public String mUUID;
        public String mVendorID;
        public String mNextOfferID;
        public String mNextOfferCaption;
        public String mNextOfferDescription;

        public VendorParams(JSONObject object) throws JSONException {
            Log.e(TAG, "object - " + object.toString());
            mName = object.getString("name");
            mUUID = (object.getJSONArray("UUID").length() > 0) ?
                        object.getJSONArray("UUID").getString(0) : "";
            mVendorID = object.getString("_id");
            JSONObject nextOffer = (object.getJSONArray("offers_qualified").length()) > 0 ?
                                    object.getJSONArray("offers_qualified").getJSONObject(0) : null;
            mNextOfferID = (nextOffer == null) ? "" : nextOffer.getString("_id");
            mNextOfferCaption = (nextOffer == null) ? "" : nextOffer.getString("caption");
            mNextOfferDescription = (nextOffer == null) ? "" : nextOffer.getString("description");
        }
    }

    public class UUIDUpdateReceiver extends BroadcastReceiver {
        private static final String TAG = "UUIDUpdateReceiver";
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.e(TAG, "received");
            if (intent.getAction() != null && intent.getAction().equals(ACTION_UPDATE_UUID_DATABASE)) {
                try {
                    readUUIDsFromFile(context);
                    /*if (BeaconFinderService.this.mIsWaitingForUpdate) {
                        *//*synchronized (BeaconFinderService.this.mIsWaitingForUpdate) {
                            BeaconFinderService.this.mIsWaitingForUpdate = false;
                            BeaconFinderService.this.mIsWaitingForUpdate.notify();
                        }*//*
                        BeaconFinderService.this.mIsWaitingForUpdate = false;
                        BeaconFinderService.this.notify();
                    }*/
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    context.unregisterReceiver(this);
                }
            }
        }
    }
}
