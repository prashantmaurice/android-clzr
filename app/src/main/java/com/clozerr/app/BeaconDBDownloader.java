package com.clozerr.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

public class BeaconDBDownloader extends BroadcastReceiver {
    private static final String TAG = "BDBDownloader";
    //private static final String DOWNLOAD_URL = "http://api.clozerr.com/v2/vendor/beacons/all?access_token=";
    public static final String BEACONS_FILE_NAME = "beacons.txt";
    public static final String ACTION_INITIATE_DOWNLOADER = "com.clozerr.app.ACTION_INITIATE_DOWNLOADER";
    private static boolean isDownloadDone = false;

    public BeaconDBDownloader() {
        isDownloadDone = false;
    }

    private static void downloadBeaconDB(final Context context) {
        String token = context.getSharedPreferences("USER", 0).getString("token", "");
        final String url = GenUtils.getClearedUriBuilder(Constants.URLBuilders.BEACON_DOWNLOAD)
                            .appendQueryParameter("access_token", token)
                            .build().toString();
        Log.e(TAG, "downloading; url - " + url);
        Ion.with(context).load(url).asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null)
                            e.printStackTrace();
                        else {
                            try {
                                String s = result.toString();
                                GenUtils.writeDownloadedStringToFile(context, s, BEACONS_FILE_NAME);
                                BeaconFinderService.commonBeaconUUID = new JSONObject(s).getString("UUID");
                                PreferenceManager.getDefaultSharedPreferences(context).edit().
                                        putString(BeaconFinderService.KEY_BEACON_UUID, BeaconFinderService.commonBeaconUUID).apply();
                                Log.e(TAG, "downloaded");
                                isDownloadDone = true;
                                GenUtils.disableComponent(context, BeaconDBDownloader.class);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Intent nextTrialIntent = new Intent(context, BeaconDBDownloader.class);
                                nextTrialIntent.setAction(ACTION_INITIATE_DOWNLOADER);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(nextTrialIntent);
                            }
                        }
                    }
                });
    }

    public static boolean isDoneDownloading() { return isDownloadDone; }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null &&
            (intent.getAction().equals(ACTION_INITIATE_DOWNLOADER) ||
                (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)))
            ) {
            Log.e(TAG, "received");
            if (GenUtils.isNetworkAvailable(context))
                if (!isDownloadDone)
                    downloadBeaconDB(context);
                else
                    Log.e(TAG, "already downloaded");
            else
                Log.e(TAG, "Network unavailable");
        }
    }
}
