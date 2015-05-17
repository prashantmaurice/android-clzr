package com.clozerr.app;

import android.annotation.TargetApi;
import android.app.Service;
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
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.BeaconManager;
import com.jaalee.sdk.RangingListener;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    protected static boolean isBLESupported = true;
    protected static boolean isScanningAllowed = true;
    protected static boolean hasUserActivatedBluetooth = false;
    protected static boolean isUserLoggedIn = false;

    protected static BluetoothAdapter bluetoothAdapter;
    protected static ArrayList<String> uuidDatabase = null;
    protected static Handler uiThreadHandler;

    protected static BeaconManager beaconManager;
    protected static Region scanningRegion;

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        uiThreadHandler = new Handler(Looper.getMainLooper());
        beaconManager = new BeaconManager(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*if (intent == null)
            PeriodicBFS.checkAndStartScan(getApplicationContext());
        else
            findBeacons();
        return START_STICKY;*/
        stopSelf();
        return START_NOT_STICKY;
    }

    protected void findBeacons() {
        if (canScanStart()) {
            scanningRegion = createRegion();
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
            });
        }
    }

    protected abstract Region createRegion();
    protected abstract void onRangedBeacons(final List<Beacon> beaconList);
    protected abstract void scan();

    // This function is just for putting toasts, but required as work is done on a background thread
    // so if a toast is directly put, the app will crash (Toasts must be put in the UI thread)
    protected static void putToast(final Context context, final CharSequence text, final int duration) {
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

    protected static boolean areUuidsEqual(String uuid1, String uuid2) {
        return (getUuidWithoutHyphens(uuid1).equalsIgnoreCase(getUuidWithoutHyphens(uuid2)));
    }

    protected boolean canScanStart() {
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
                UUIDDownloadBaseReceiver.scheduleDownload(getApplicationContext());
                if (uuidDatabase == null)
                        readUUIDsFromFile(getApplicationContext());
                return true;
            }
        }
        else return false;
    }

    protected static void readUUIDsFromFile(Context context) {
        try {
            FileOutputStream dummyOutputStream = context.openFileOutput(UUIDDownloader.UUID_FILE_NAME, MODE_APPEND);
                                                // for creating the file if not present
            dummyOutputStream.close();
            FileInputStream fileInputStream = context.openFileInput(UUIDDownloader.UUID_FILE_NAME);
            byte[] dataBytes = new byte[fileInputStream.available()];
            fileInputStream.read(dataBytes);
            fileInputStream.close();
            // TODO change format if JSON changes
            String data = new String(dataBytes);
            if (!data.isEmpty()) {
                JSONArray rootArray = new JSONArray(data);
                uuidDatabase = new ArrayList<String>();
                for (int i = 0; i < rootArray.length(); ++i)
                    try {
                        if (rootArray.getJSONObject(i).getJSONArray("UUID").length() > 0)
                            uuidDatabase.add(rootArray.getJSONObject(i).getJSONArray("UUID").getString(0));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        public boolean mIsNotifiable;
        public String mPaymentType;

        public VendorParams(Context context, JSONObject object) throws JSONException {
            Log.e(TAG, "object - " + object.toString());
            mName = object.getString("name");
            mUUID = (object.getJSONArray("UUID").length() > 0) ?
                        object.getJSONArray("UUID").getString(0).toLowerCase() : "";
            mVendorID = object.getString("_id");
            JSONObject nextOffer = (object.getJSONArray("offers_qualified").length()) > 0 ?
                                    object.getJSONArray("offers_qualified").getJSONObject(0) : null;
            mNextOfferID = (nextOffer == null) ? "" : nextOffer.getString("_id");
            mNextOfferCaption = (nextOffer == null) ? "" : nextOffer.getString("caption");
            mNextOfferDescription = (nextOffer == null) ? "" : nextOffer.getString("description");
            mIsNotifiable = isVendorWithThisUUIDNotifiable(context, mUUID);
            //mPaymentType = object.getString("paymentType");
            mPaymentType = "counter";
        }

        public Intent getDetailsIntent(Context context) {
            Intent detailIntent = new Intent(context, CouponDetails.class);
            detailIntent.putExtra("vendor_id", mVendorID);
            detailIntent.putExtra("offer_id", mNextOfferID);
            detailIntent.putExtra("offer_caption", mNextOfferCaption);
            detailIntent.putExtra("offer_text", mNextOfferDescription);
            return detailIntent;
        }

        public static ArrayList<VendorParams> readVendorParamsFromFile(Context context) {
            ArrayList<VendorParams> result = null;
            try {
                FileInputStream fileInputStream = context.openFileInput(UUIDDownloader.UUID_FILE_NAME);
                byte[] dataBytes = new byte[fileInputStream.available()];
                fileInputStream.read(dataBytes);
                JSONArray rootArray = new JSONArray(new String(dataBytes));
                Log.e(TAG, "root - " + rootArray.toString());
                VendorParams vendorParams;
                result = new ArrayList<VendorParams>();
                for (int i = 0; i < rootArray.length(); ++i) {
                    vendorParams = null;
                    try {
                        vendorParams = new VendorParams(context, rootArray.getJSONObject(i));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        if (vendorParams != null) result.add(vendorParams);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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

        public static boolean isVendorWithThisUUIDNotifiable(Context context, String uuid) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            return !preferences.contains(uuid);
        }
    }
}
