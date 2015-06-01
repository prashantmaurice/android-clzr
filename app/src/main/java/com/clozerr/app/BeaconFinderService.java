package com.clozerr.app;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.util.Predicate;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.BeaconManager;
import com.jaalee.sdk.RangingListener;
import com.jaalee.sdk.Region;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by S.ARAVIND on 3/3/2015.
 */
@TargetApi(18)
public abstract class BeaconFinderService extends Service {
    private static final String TAG = "BFS";
    protected static final String REGION_ID = "com.clozerr.app";
    protected static final String ACTION_RESUME_SCAN = "com.clozerr.app.ACTION_RESUME_SCAN";
    protected static final long SCAN_START_DELAY = TimeUnit.MILLISECONDS.convert(2L, TimeUnit.SECONDS);
    protected static final int THRESHOLD_RSSI = -100;

    protected static String CLOZERR_UUID = "";
    protected static boolean isBLESupported = true;
    protected static boolean isScanningAllowed = true;
    protected static boolean hasUserActivatedBluetooth = false;
    protected static boolean isUserLoggedIn = false;

    protected enum RequestCodes {
        CODE_ALARM_INTENT(1000),
        CODE_DETAILS_INTENT(1234),
        CODE_REFUSE_INTENT(1235),
        CODE_VENDOR_LIST_INTENT(1236),
        CODE_RESUME_SCAN_INTENT(1237);

        private int mCode;

        RequestCodes(int code) { mCode = code; }
        public int code() { return mCode; }
    }

    protected static BluetoothAdapter bluetoothAdapter;
    protected static ArrayList<BeaconDBParams> beaconDatabase = null;
    protected static Handler uiThreadHandler;
    protected static AlarmManager alarmManager;
    protected static BeaconManager beaconManager;
    protected static Region scanningRegion;

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        beaconManager = new BeaconManager(getApplicationContext());
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            PeriodicBFS.checkAndStartScan(getApplicationContext());
        else
            findBeacons();
        return START_STICKY;
        /*stopSelf();
        return START_NOT_STICKY;*/
    }

    @Override
    public void onDestroy() {
        turnOffBluetooth();
        alarmManager = null;
        super.onDestroy();
    }

    protected void findBeacons() {
        uiThreadHandler = new Handler(Looper.getMainLooper());
        if (canServiceRun()) {
            /*scanningRegion = createRegion();
            beaconManager.setRangingListener(new RangingListener() {
                @Override
                public void onBeaconsDiscovered(Region region, final List list) {
                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onRangedBeacons((List<Beacon>) list);
                        }
                    });
                }
            });
            beaconManager.connect(new ServiceReadyCallback() {
                @Override
                public void onServiceReady() {
                    scan();
                }
            });*/
            beaconManager.setRangingListener(new RangingListener() {
                @Override
                public void onBeaconsDiscovered(Region region, final List list) {
                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onRangedBeacons((List<Beacon>) list);
                        }
                    });
                }
            });
            runService();
        }
    }

    /*protected abstract Region createRegion();
    protected abstract void onRangedBeacons(final List<Beacon> beaconList);
    protected abstract void scan();*/

    protected abstract void onRangedBeacons(final List<Beacon> beaconList);
    protected abstract void runService();

    // This function is just for putting toasts, but required as work is done on a background thread
    // so if a toast is directly put, the app will crash (Toasts must be put in the UI thread)
    protected static void putToast(final Context context, final CharSequence text, final int duration) {
        uiThreadHandler = new Handler(Looper.getMainLooper());
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, duration).show();
            }
        });
    }

    protected static String getUuidWithoutHyphens(String uuidWithHyphens) {
        String resultUuid = "";
        for (char c : uuidWithHyphens.toCharArray())
            if (c != '-') resultUuid += String.valueOf(c);
        return resultUuid;
    }

    /*protected static boolean areUuidsEqual(String uuid1, String uuid2) {
        return (getUuidWithoutHyphens(uuid1).equalsIgnoreCase(getUuidWithoutHyphens(uuid2)));
    }*/

    protected boolean canServiceRun() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isScanningAllowed = sharedPreferences.getBoolean(getResources().getString(R.string.beacon_detection), true);
        sharedPreferences = getSharedPreferences("USER", 0);
        isUserLoggedIn = !sharedPreferences.getString("token", "").isEmpty();
        if (isScanningAllowed && isUserLoggedIn) {
            if (bluetoothAdapter == null) {
                putToast(getApplicationContext(),
                        "Sorry, but your device doesn't support Bluetooth." +
                        " Clozerr beacon-finding services won\'t work now.",
                        Toast.LENGTH_LONG);
                isBLESupported = false;
                return false;
            }
            else if (!getApplicationContext().getPackageManager().
                        hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                putToast(getApplicationContext(),
                        "Sorry, but your device doesn't support Bluetooth Low Energy." +
                        " Clozerr beacon-finding services won\'t work now.",
                        Toast.LENGTH_LONG);
                isBLESupported = false;
                return false;
            }
            else {
                isBLESupported = true;
                BeaconDBDownloadBaseReceiver.scheduleDownload(getApplicationContext());
                return true;
            }
        }
        else return false;
    }

    /*protected static void readBeaconDBFromFile(Context context) throws IOException, JSONException {
        FileOutputStream dummyOutputStream = context.openFileOutput(BeaconDBDownloader.BEACONS_FILE_NAME, MODE_APPEND);
                                            // for creating the file if not present
        dummyOutputStream.close();
        FileInputStream fileInputStream = context.openFileInput(BeaconDBDownloader.BEACONS_FILE_NAME);
        byte[] dataBytes = new byte[fileInputStream.available()];
        fileInputStream.read(dataBytes);
        fileInputStream.close();
        String data = new String(dataBytes);
        if (!data.isEmpty()) {
            JSONObject rootObject = new JSONObject(data);
            CLOZERR_UUID = rootObject.getString("UUID");
            JSONArray rootArray = rootObject.getJSONArray("vendors");
            beaconDatabase = new ArrayList<>();
            for (int i = 0; i < rootArray.length(); ++i)
                try {
                *//*if (rootArray.getJSONObject(i).getJSONArray("UUID").length() > 0)
                    beaconDatabase.add(rootArray.getJSONObject(i).getJSONArray("UUID").getString(0));*//*
                    JSONObject beaconObject = rootArray.getJSONObject(i).has("beacons") ?
                            rootArray.getJSONObject(i).getJSONObject("beacons") : null;
                    if (beaconObject != null && beaconObject.has("major"))
                        beaconDatabase.add(new BeaconDBParams(beaconObject));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        }
    }*/

    public static void enableComponent(Context context, Class componentClass) {
        ComponentName component = new ComponentName(context, componentClass);
        context.getPackageManager().setComponentEnabledSetting(component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public static void disableComponent(Context context, Class componentClass) {
        ComponentName component = new ComponentName(context, componentClass);
        context.getPackageManager().setComponentEnabledSetting(component,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    protected static void turnOnBluetooth() {
        hasUserActivatedBluetooth = bluetoothAdapter.isEnabled();  // check if user has already enabled BT
        if (!hasUserActivatedBluetooth)                            // disabled, so enable BT
            bluetoothAdapter.enable();
        Log.e(TAG, "BT On");
    }

    protected static void turnOffBluetooth() {
        if (!hasUserActivatedBluetooth) // if user turned on BT, don't disable it as user might need it
            bluetoothAdapter.disable();
        Log.e(TAG, "BT Off");
    }

    public static void disallowScanning(Context context) {
        isScanningAllowed = false;
        PeriodicBFS.checkAndStopScan(context);
        OneTimeBFS.checkAndStopScan(context);
        Log.e(TAG, "scans blocked");
    }

    public static void allowScanning(Context context) {
        isScanningAllowed = true;
        PeriodicBFS.checkAndStartScan(context);
        Log.e(TAG, "scans allowed");
    }

    public static void pauseScanningFor(final Context context, long intervalMillis) {
        Log.e(TAG, "scans paused for " + intervalMillis + " ms");
        putToast(context, "scans paused for " + intervalMillis + " ms", Toast.LENGTH_SHORT);
        long triggerTimeMillis = intervalMillis + SystemClock.elapsedRealtime();
        enableComponent(context, ScanResumeReceiver.class);
        Intent resumeIntent = new Intent(context, ScanResumeReceiver.class);
        resumeIntent.setAction(ACTION_RESUME_SCAN);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTimeMillis,
                PendingIntent.getBroadcast(context, RequestCodes.CODE_RESUME_SCAN_INTENT.code(), resumeIntent, 0));
        //disallowScanning(context);
        isScanningAllowed = false;
    }

    protected static class VendorParams {
        public String mName;
        //public String mUUID;
        public BeaconDBParams mBeaconParams;
        public String mVendorID;
        /*public String mNextOfferID;
        public String mNextOfferCaption;
        public String mNextOfferDescription;*/
        //public boolean mIsNotifiable;
        public boolean mHasOffers;
        //public String mPaymentType;
        public String mLoyaltyType;
        public int mThresholdRssi;

        public VendorParams(/*Context context, */JSONObject object) throws JSONException {
            mName = object.getString("name");
            /*mUUID = (object.getJSONArray("UUID").length() > 0) ?
                        object.getJSONArray("UUID").getString(0).toLowerCase() : "";*/
            if (object.has("beacons") && object.getJSONObject("beacons").has("major"))
                mBeaconParams = new BeaconDBParams(object.getJSONObject("beacons"));
            else mBeaconParams = null;
            mVendorID = object.getString("_id");
            /*JSONObject nextOffer = (object.getJSONArray("offers_qualified").length()) > 0 ?
                                    object.getJSONArray("offers_qualified").getJSONObject(0) : null;
            mNextOfferID = (nextOffer == null) ? "" : nextOffer.getString("_id");
            mNextOfferCaption = (nextOffer == null) ? "" : nextOffer.getString("caption");
            mNextOfferDescription = (nextOffer == null) ? "" : nextOffer.getString("description");*/
            //mIsNotifiable = /*!mNextOfferID.isEmpty() && isVendorWithThisUUIDNotifiable(context, mUUID)*/true;
            mHasOffers = object.getBoolean("hasOffers");
            mLoyaltyType = (object.has("settings") && object.getJSONObject("settings").getBoolean("sxEnabled")) ?
                    "SX" : "S1";
            //mPaymentType = object.getString("paymentType");
            //mPaymentType = "counter";
            mThresholdRssi = THRESHOLD_RSSI;
        }

        public Intent getDetailsIntent(Context context) {
            Intent detailIntent = new Intent(context, VendorActivity.class);
            detailIntent.putExtra("vendor_id", mVendorID);
            /*detailIntent.putExtra("offer_id", mNextOfferID);
            detailIntent.putExtra("offer_caption", mNextOfferCaption);
            detailIntent.putExtra("offer_text", mNextOfferDescription);*/
            return detailIntent;
        }

        public static ArrayList<VendorParams> readVendorParamsFromFile(Context context) {
            ArrayList<VendorParams> result = null;
            try {
                FileInputStream fileInputStream = context.openFileInput(BeaconDBDownloader.BEACONS_FILE_NAME);
                byte[] dataBytes = new byte[fileInputStream.available()];
                fileInputStream.read(dataBytes);
                JSONObject rootObject = new JSONObject(new String(dataBytes));
                JSONArray rootArray = rootObject.getJSONArray("vendors");
                Log.e(TAG, "root - " + rootArray.toString());
                VendorParams vendorParams;
                result = new ArrayList<>();
                for (int i = 0; i < rootArray.length(); ++i) {
                    vendorParams = null;
                    try {
                        vendorParams = new VendorParams(/*context, */rootArray.getJSONObject(i));
                    } catch (Exception ex) {
                        //ex.printStackTrace();
                    } finally {
                        if (vendorParams != null) result.add(vendorParams);
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
            return result;
        }

        public static VendorParams findVendorParamsInFile(Context context, Predicate<VendorParams> paramsPredicate) {
            ArrayList<VendorParams> rootArray = readVendorParamsFromFile(context);
            for (VendorParams params : rootArray)
                if (paramsPredicate.apply(params))
                    return params;
            return null;
        }

        /*public static boolean isVendorWithThisUUIDNotifiable(Context context, String uuid) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            return !preferences.contains(uuid);
        }*/
    }

    public static class BeaconDBParams {
        public int mMajor;
        public int mMinor;

        public BeaconDBParams(JSONObject object) throws JSONException {
            mMajor = object.getInt("major");
            mMinor = object.getInt("minor");
        }

        public BeaconDBParams(Beacon beacon) {
            mMajor = beacon.getMajor();
            mMinor = beacon.getMinor();
        }

        public BeaconDBParams(int major, int minor) {
            mMajor = major;
            mMinor = minor;
        }

        public boolean equals(BeaconDBParams other) {
            if (other == null) return false;
            return mMajor == other.mMajor && mMinor == other.mMinor;
        }

        public String toString() {
            return "major: " + mMajor + "; minor: " + mMinor;
        }
    }

    public static class ScanResumeReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction().equals(ACTION_RESUME_SCAN)) {
                    //allowScanning(context);
                    isScanningAllowed = true;
                    Log.e(TAG, "scans resumed");
                    putToast(context, "scans resumed", Toast.LENGTH_SHORT);
                    disableComponent(context, ScanResumeReceiver.class);
                }
            }
    }
}
