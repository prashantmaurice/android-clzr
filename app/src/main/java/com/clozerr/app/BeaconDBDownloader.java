package com.clozerr.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.FileOutputStream;

import static com.clozerr.app.AsyncGet.isNetworkAvailable;

public class BeaconDBDownloader extends BroadcastReceiver {
    private static final String TAG = "BDBDownloader";
    private static final String DOWNLOAD_URL = "http://api.clozerr.com/v2/vendor/beacons/all?access_token=";
    public static final String BEACONS_FILE_NAME = "beacons.txt";
    public static final String ACTION_INITIATE_DOWNLOADER = "com.clozerr.app.ACTION_INITIATE_DOWNLOADER";
    private static boolean isDownloadDone = false;

    public BeaconDBDownloader() {
        isDownloadDone = false;
    }

    private static void downloadBeaconDB(final Context context) {
        String token = context.getSharedPreferences("USER", 0).getString("token", "");
        final String url = DOWNLOAD_URL + token;
        Log.e(TAG, "downloading; url - " + url);
        new AsyncGet(context, DOWNLOAD_URL + token, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                //Log.e(TAG, "results - " + s);
                if (s != null && !s.isEmpty()) {
                    try {
                        FileOutputStream fileOutputStream = context.openFileOutput(BEACONS_FILE_NAME, Context.MODE_PRIVATE);
                        fileOutputStream.write(s.getBytes());
                        fileOutputStream.close();
                        BeaconFinderService.commonBeaconUUID = new JSONObject(s).getString("UUID");
                        PreferenceManager.getDefaultSharedPreferences(context).edit().
                                putString(BeaconFinderService.KEY_BEACON_UUID, BeaconFinderService.commonBeaconUUID).apply();
                        //BeaconDBDownloadBaseReceiver.releaseWakeLock();
                        Log.e(TAG, "downloaded");
                        //PeriodicBFS.scheduleAlarms(context);
                        context.startService(new Intent(context, GeofenceManagerService.class));
                        isDownloadDone = true;
                        BeaconFinderService.disableComponent(context, BeaconDBDownloader.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Intent nextTrialIntent = new Intent(context, BeaconDBDownloader.class);
                        nextTrialIntent.setAction(ACTION_INITIATE_DOWNLOADER);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(nextTrialIntent);
                    }
                }
            }
        }, false);
    }

    public static boolean isDoneDownloading() { return isDownloadDone; }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null &&
            (intent.getAction().equals(ACTION_INITIATE_DOWNLOADER) ||
                (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)))
            ) {
            Log.e(TAG, "received");
            if (isNetworkAvailable(context))
                if (!isDownloadDone)
                    downloadBeaconDB(context);
                else
                    Log.e(TAG, "already downloaded");
            else
                Log.e(TAG, "Network unavailable");
        }
    }
}
