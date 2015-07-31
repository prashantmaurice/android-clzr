package com.clozerr.app;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;

import java.util.List;

@TargetApi(18)
public abstract class OneTimeBFS extends BeaconFinderService {

    private static final String TAG = "OTBFS";

    private static boolean running = false;

    private BeaconDBParams mBeaconDBParams;
    private Region mScanningRegion;
    private long mTimeout;
    private boolean mLogInNecessary;

    public OneTimeBFS() { super(TAG); }

    public OneTimeBFS(String name) { super(name); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mBeaconDBParams = new BeaconDBParams(intent.getIntExtra("beaconMajor", 0),
                                            intent.getIntExtra("beaconMinor", 0));
        if (mBeaconDBParams.major == 0) mBeaconDBParams.major = null;
        if (mBeaconDBParams.minor == 0) mBeaconDBParams.minor = null;
        mTimeout = intent.getLongExtra("timeout", BTStateListener.DEFAULT_TIMEOUT);
        mLogInNecessary = intent.getBooleanExtra("login", true);
        if (!running)
            running = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onRangedBeacons(List<Beacon> beacons) {
        if (!beacons.isEmpty()) {
            onRanged(beacons);
            terminate();
        }
    }

    public abstract void onRanged(List<Beacon> beacons);

    private void terminate() {
        if (running) {
            beaconManager.stopRanging(mScanningRegion);
            turnOffBluetooth(getApplicationContext());
            Log.e(TAG, "Stopped Scan");
            running = false;
//            releaseLock();//TODO : Commented to remove error ???? resolve this
        }
    }

    public static boolean isRunning() { return running; }

    @Override
    protected void doWakefulWork(Intent intent) {
        Context applicationContext = getApplicationContext();
        if (checkCompatibility(applicationContext) && checkPreferences(applicationContext, mLogInNecessary)) {
            setListener(mLogInNecessary);
            turnOnBluetooth(applicationContext);
            new BTStateListener(mTimeout) {
                @Override
                public void onBTStateReached(Context context, int state) {
                    if (state == BluetoothAdapter.STATE_ON) {
                        unregisterSelf(context);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                beaconManager.connect(new ServiceReadyCallback() {
                                    @Override
                                    public void onServiceReady() {
                                        mScanningRegion = new Region(Constants.APP_PACKAGE_NAME, getUuidWithoutHyphens(commonBeaconUUID),
                                                mBeaconDBParams.major, mBeaconDBParams.minor);
                                        beaconManager.startRangingAndDiscoverDevice(mScanningRegion);
                                        Log.e(TAG, "Started Scan");
                                    }
                                });
                            }
                        });
                    }
                }
            }.registerSelf(this);
            if (mTimeout > 0)
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        terminate();
                    }
                }, mTimeout);
        } else terminate();
    }

//    @Override
    protected boolean isListeningAfterWork() { return true; }

    public static void startOneTimeService(Context context, Class<? extends OneTimeBFS> serviceClass,
                                           BeaconDBParams beaconParams, boolean logInNecessary) {
        startOneTimeService(context, serviceClass, beaconParams, logInNecessary, BTStateListener.DEFAULT_TIMEOUT);
    }

    public static void startOneTimeService(Context context, Class<? extends OneTimeBFS> serviceClass,
                                           BeaconDBParams beaconParams, boolean logInNecessary, long timeout) {
        Intent service = new Intent(context, serviceClass);
        if (beaconParams == null)
            beaconParams = new BeaconDBParams(null, null);
        service.putExtra("beaconMajor", beaconParams.major)
                .putExtra("beaconMinor", beaconParams.minor)
                .putExtra("timeout", timeout)
                .putExtra("login", logInNecessary);
        WakefulIntentService.sendWakefulWork(context, service);
    }
}
