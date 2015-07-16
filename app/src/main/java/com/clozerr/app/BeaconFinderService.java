package com.clozerr.app;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.util.Predicate;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.BeaconManager;
import com.jaalee.sdk.RangingListener;
import com.jaalee.sdk.Region;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by S.ARAVIND on 3/3/2015.
 */
@TargetApi(18)
public abstract class BeaconFinderService extends WakefulIntentService {
    private static final String TAG = "BFS";

    public static final String REGION_ID = "com.clozerr.app";
    public static final String KEY_BLE = "com.clozerr.app.KEY_BLE";
    public static final String KEY_BEACON_UUID = "com.clozerr.app.KEY_BEACON_UUID";
    public static final String KEY_APP_DISABLE_BT = "com.clozerr.app.KEY_APP_DISABLE_BT";
    public static final long BT_RECEIVER_TIMEOUT = TimeUnit.MILLISECONDS.convert(3L, TimeUnit.SECONDS);
    public static final int THRESHOLD_RSSI = -100;

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

    protected static String commonBeaconUUID = "";
    protected static boolean isBLESupported = true;
    protected static boolean isScanningAllowed = true;
    protected static boolean isScanningPaused = false;
    protected static boolean isUserLoggedIn = false;
    protected static BluetoothAdapter bluetoothAdapter;
    protected static AlarmManager alarmManager = null;
    protected static BeaconManager beaconManager = null;
    protected static Region scanningRegion = null;

    public BeaconFinderService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (bluetoothAdapter == null)
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (beaconManager == null)
            beaconManager = new BeaconManager(getApplicationContext());
        if (alarmManager == null)
            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //setListener();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            PeriodicBFS.checkAndStartScan(getApplicationContext());
        else
            findBeacons();
        return START_STICKY;
        *//*stopSelf();
        return START_NOT_STICKY;*//*
    }*/

    /*@Override
    public void onDestroy() {
        turnOffBluetooth();
        alarmManager = null;
        super.onDestroy();
    }*/

    /*protected void findBeacons() {
        //bgThreadHandler = new Handler(Looper.myLooper());
        if (canServiceRun()) {
            beaconManager.setRangingListener(new RangingListener() {
                @Override
                public void onBeaconsDiscovered(Region region, final List list) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            onRangedBeacons((List<Beacon>) list);
                        }
                    });
                }
            });
            runService();
        }
    }*/

    protected void setListener() {
        beaconManager.setRangingListener(new RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List list) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        onRangedBeacons((List<Beacon>) list);
                    }
                });
            }
        });
    }

    /*protected void connectServiceAndStartRanging() {
        beaconManager.connect(new ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                onManagerConnected();
                scanningRegion = setScanningRegion();
                beaconManager.startRangingAndDiscoverDevice(scanningRegion);
            }
        });
    }

    protected abstract Region setScanningRegion();
    protected abstract void onManagerConnected();*/
    protected abstract void onRangedBeacons(final List<Beacon> beaconList);
    //protected abstract void runService();

    // This function is just for putting toasts, but required as work is done on a background thread
    // so if a toast is directly put, the app will crash (Toasts must be put in the UI thread)
    protected static void putToast(final Context context, final CharSequence text, final int duration) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
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

    /*protected boolean canServiceRun() {
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
                commonBeaconUUID = sharedPreferences.getString(KEY_BEACON_UUID, "");
                return true;
            }
        }
        else return false;
    }*/

    protected static boolean checkCompatibility(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!sharedPreferences.contains(KEY_BLE)) {
            if (BluetoothAdapter.getDefaultAdapter() == null) {     // IntentService used, so
                                                                    // bluetoothAdapter may not have been initialized
                putToast(context,
                        "Sorry, but your device doesn't support Bluetooth." +
                                " Clozerr beacon-finding services won\'t work now.",
                        Toast.LENGTH_LONG);
                isBLESupported = false;
                return false;
            }
            else if (!context.getPackageManager().
                    hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                putToast(context,
                        "Sorry, but your device doesn't support Bluetooth Low Energy." +
                                " Clozerr beacon-finding services won\'t work now.",
                        Toast.LENGTH_LONG);
                isBLESupported = false;
                return false;
            }
            else {
                isBLESupported = true;
                /*BeaconDBDownloadBaseReceiver.scheduleDownload(getApplicationContext());
                commonBeaconUUID = sharedPreferences.getString(KEY_BEACON_UUID, "");*/
            }
            sharedPreferences.edit().putBoolean(KEY_BLE, isBLESupported).apply();
        }
        return isBLESupported;
    }

    protected static boolean checkPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        isScanningAllowed = sharedPreferences.getBoolean(context.getResources().getString(R.string.beacon_detection), true);
        sharedPreferences = context.getSharedPreferences("USER", 0);
        isUserLoggedIn = !sharedPreferences.getString("token", "").isEmpty();
        return (!isUserLoggedIn || isScanningAllowed);
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
            commonBeaconUUID = rootObject.getString("UUID");
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

    protected static void turnOnBluetooth(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean shouldAppDeactivateBluetooth =
                preferences.getBoolean(KEY_APP_DISABLE_BT, false) || !bluetoothAdapter.isEnabled();
                                                // check if it's this app that has to disable BT
                                                // stored in (and read from) preferences to account for restart of
                                                // process after OS/user kills app
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(KEY_APP_DISABLE_BT, shouldAppDeactivateBluetooth).apply();
        if (!bluetoothAdapter.isEnabled()) {                            // disabled, so enable BT
            /*context.registerReceiver(new BTStateChangeReceiver() {
                @Override
                public void onBluetoothOK() {
                    connectServiceAndStartRanging();
                }
            }, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));*/
            bluetoothAdapter.enable();
        }
    }

    protected static void turnOffBluetooth(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_APP_DISABLE_BT, false))
                            // if app did not turn on BT, don't disable it as user might need it
            bluetoothAdapter.disable();
    }

    public static void disallowScanning(Context context) {
        isScanningAllowed = false;
        PeriodicBFS.checkAndStopScan(context/*, true*/);
        OneTimeBFS.checkAndStopScan(context);
        Log.e(TAG, "scans blocked");
    }

    public static void allowScanning(Context context) {
        isScanningAllowed = true;
        PeriodicBFS.checkAndStartScan(context);
        Log.e(TAG, "scans allowed");
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
        //public boolean mHasOffers;
        //public String mPaymentType;
        //public String mLoyaltyType;
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
            /*mHasOffers = object.getBoolean("hasOffers");
            mLoyaltyType = (object.has("settings") && object.getJSONObject("settings").getBoolean("sxEnabled")) ?
                    "SX" : "S1";*/
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
                String fileContents = GenUtils.readFileContentsAsString(context, BeaconDBDownloader.BEACONS_FILE_NAME);
                JSONObject rootObject = new JSONObject(fileContents);
                commonBeaconUUID = rootObject.getString("UUID");
                JSONArray rootArray = rootObject.getJSONArray("vendors");
                //Log.e(TAG, "root - " + rootArray.toString());
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
            return other != null && mMajor == other.mMajor && mMinor == other.mMinor;
        }

        public String toString() {
            return "major:" + mMajor + ";minor:" + mMinor;
        }
    }

    public abstract class BTStateChangeReceiver extends BroadcastReceiver {

        private long mTimeout;

        public BTStateChangeReceiver(long timeout) {
            super();
            mTimeout = timeout;
        }

        @Override
        public void onReceive(final Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        context.unregisterReceiver(BTStateChangeReceiver.this);
                    }
                }, mTimeout);
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    onBluetoothOK();
                    context.unregisterReceiver(this);
                }
            }
        }

        public abstract void onBluetoothOK();
    }
}
