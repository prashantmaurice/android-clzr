package com.clozerr.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
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
public class BeaconFinderService extends Service {
    public static final long INTERVAL = 1000 * 60 * /*no. of minutes*/1;
    public static final long DEFAULT_DELAY = 0;
    public static final long DEFAULT_DISABLE_DELAY = 5000;
    public static final int BEACON_FOUND_LIMIT = 3;
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
        if (isBluetoothLESupported)
            mTimer.schedule(new CheckForBeacons(), DEFAULT_DELAY, INTERVAL);
    }

    public void setNotification(CharSequence text, PendingIntent intent)
    {
        mNotificationBuilder.setContentText(text)
                .setContentIntent(intent)
                .setWhen(System.currentTimeMillis());
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
        //mNotificationBuilder.setContentTitle(getResources().getString(R.string.app_name));
    }

    public void dismissNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class CheckForBeacons extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.enable();
                        Toast.makeText(mContext, "Enabled Bluetooth", Toast.LENGTH_SHORT).show();
                    }
                    /* TODO check for beacons in between, instead of this random test
                    *  If the beacon finding task takes a bit of time, remove the new Timer
                    *  and put the inside code in this run() itself.
                    */
                    if (Math.random() > 0.5) {
                        ++beaconFoundCount;
                        if (beaconFoundCount == BEACON_FOUND_LIMIT) {
                            beaconFoundCount = 0;
                            setNotification("You're near a restaurant. Check in with Clozerr?", null);
                        }
                    }
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mBluetoothAdapter.isEnabled()) {
                                        mBluetoothAdapter.disable();
                                        Toast.makeText(mContext, "Disabled Bluetooth", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }, DEFAULT_DISABLE_DELAY);
                }
            });
        }
    }
}
