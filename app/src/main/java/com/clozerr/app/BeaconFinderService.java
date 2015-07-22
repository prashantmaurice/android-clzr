package com.clozerr.app;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.bluetooth.BluetoothAdapter;
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

/**
 * Created by S.ARAVIND on 3/3/2015.
 */
@TargetApi(18)
public abstract class BeaconFinderService extends WakefulIntentService {
    private static final String TAG = "BFS";

    public static final int DEFAULT_THRESHOLD_RSSI = -110;

    protected static String commonBeaconUUID = "";
    protected static boolean isBLESupported = true;
    protected static boolean isScanningAllowed = true;
    protected static boolean isScanningPaused = false;
    protected static boolean isUserLoggedIn = false;
    protected static BluetoothAdapter bluetoothAdapter;
    protected static AlarmManager alarmManager = null;
    protected static BeaconManager beaconManager = null;

    public BeaconFinderService() {
        super(TAG);
    }

    public BeaconFinderService(String name) { super(name); }

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

    protected void setListener(final boolean logInNecessary) {
        beaconManager.setRangingListener(new RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List list) {
                Log.e(TAG, "Beacons discovered: " + list.size());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (checkPreferences(getApplicationContext(), logInNecessary))
                            onRangedBeacons((List<Beacon>) list);
                    }
                });
            }
        });
    }

    protected abstract void onRangedBeacons(final List<Beacon> beaconList);

    protected static String getUuidWithoutHyphens(String uuidWithHyphens) {
        String resultUuid = "";
        for (char c : uuidWithHyphens.toCharArray())
            if (c != '-') resultUuid += String.valueOf(c);
        return resultUuid;
    }

    protected static boolean checkCompatibility(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!sharedPreferences.contains(Constants.SPKeys.BLE)) {
            if (BluetoothAdapter.getDefaultAdapter() == null) {     // IntentService used, so
                                                                    // bluetoothAdapter may not have been initialized
                GenUtils.putToast(context,
                        "Sorry, but your device doesn't support Bluetooth." +
                                " Clozerr beacon-finding services won\'t work now.",
                        Toast.LENGTH_LONG);
                isBLESupported = false;
                return false;
            }
            else if (!context.getPackageManager().
                    hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                GenUtils.putToast(context,
                        "Sorry, but your device doesn't support Bluetooth Low Energy." +
                                " Clozerr beacon-finding services won\'t work now.",
                        Toast.LENGTH_LONG);
                isBLESupported = false;
                return false;
            }
            else {
                isBLESupported = true;
            }
            sharedPreferences.edit().putBoolean(Constants.SPKeys.BLE, isBLESupported).apply();
        }
        return isBLESupported;
    }

    protected static boolean checkPreferences(Context context, boolean logInNecessary) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        isScanningAllowed = sharedPreferences.getBoolean(context.getResources().getString(R.string.beacon_detection), true);
        commonBeaconUUID = sharedPreferences.getString(Constants.SPKeys.BEACON_UUID, "");
        sharedPreferences = context.getSharedPreferences("USER", 0);
        isUserLoggedIn = !sharedPreferences.getString("token", "").isEmpty();
        if (logInNecessary)
            return (isUserLoggedIn && isScanningAllowed && !commonBeaconUUID.isEmpty());
        else
            return (isScanningAllowed && !commonBeaconUUID.isEmpty());
    }

    protected static void turnOnBluetooth(Context context) {
        Log.e(TAG, "Turning on bluetooth");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean shouldAppDeactivateBluetooth =
                preferences.getBoolean(Constants.SPKeys.APP_DISABLE_BT, false) || !bluetoothAdapter.isEnabled();
                                                // check if it's this app that has to disable BT
                                                // stored in (and read from) preferences to account for restart of
                                                // process after OS/user kills app
        Log.e(TAG, "App Should Deactivate Bluetooth: " + shouldAppDeactivateBluetooth);
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(Constants.SPKeys.APP_DISABLE_BT, shouldAppDeactivateBluetooth).apply();
        if (!bluetoothAdapter.isEnabled()) {                            // disabled, so enable BT
            bluetoothAdapter.enable();
        }
    }

    protected static void turnOffBluetooth(Context context) {
        Log.e(TAG,"Turning Off bluetooth");
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.SPKeys.APP_DISABLE_BT, false)) {
            // if app did not turn on BT, don't disable it as user might need it
            Log.e(TAG,"App has to turn on bluetooth");
            bluetoothAdapter.disable();
        }
    }

    /*public static void disallowScanning(Context context) {
        isScanningAllowed = false;
        PeriodicBFS.checkAndStopScan(context*//*, true*//*);
        OneTimeBFS.checkAndStopScan(context);
        Log.e(TAG, "scans blocked");
    }

    public static void allowScanning(Context context) {
        isScanningAllowed = true;
        PeriodicBFS.checkAndStartScan(context);
        Log.e(TAG, "scans allowed");
    }*/

    protected static class VendorParams {
        public String name;
        public BeaconDBParams beaconParams;
        public String id;
        public int thresholdRssi;

        public VendorParams(JSONObject object) throws JSONException {
            name = object.getString("name");
            if (object.has("beacons") && object.getJSONObject("beacons").has("major"))
                beaconParams = new BeaconDBParams(object.getJSONObject("beacons"));
            else beaconParams = null;
            id = object.getString("_id");
            thresholdRssi = (object.has("thresh")) ? object.getInt("thresh") : DEFAULT_THRESHOLD_RSSI;
        }

        public Intent getDetailsIntent(Context context) {
            Intent detailIntent = new Intent(context, VendorActivity.class);
            detailIntent.putExtra("vendor_id", id);
            return detailIntent;
        }

        public static ArrayList<VendorParams> readVendorParamsFromFile(Context context) {
            ArrayList<VendorParams> result = null;
            try {
                String fileContents = GenUtils.readFileContentsAsString(context, Constants.FileNames.BEACONS);
                JSONObject rootObject = new JSONObject(fileContents);
                commonBeaconUUID = rootObject.getString("UUID");
                JSONArray rootArray = rootObject.getJSONArray("vendors");
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
    }

    public static class BeaconDBParams {
        public Integer major;
        public Integer minor;

        public BeaconDBParams(JSONObject object) throws JSONException {
            major = object.getInt("major");
            minor = object.getInt("minor");
        }

        public BeaconDBParams(Beacon beacon) {
            major = beacon.getMajor();
            minor = beacon.getMinor();
        }

        public BeaconDBParams(Integer major, Integer minor) {
            this.major = major;
            this.minor = minor;
        }

        public boolean equals(BeaconDBParams other) {
            return other != null && major.equals(other.major) && minor.equals(other.minor);
        }

        public String toString() {
            return "major:" + major + ";minor:" + minor;
        }
    }

}
