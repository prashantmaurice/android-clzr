package com.clozerr.app;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.util.Log;

import java.io.FileOutputStream;

import static com.clozerr.app.AsyncGet.isNetworkAvailable;

public class UUIDDownloader extends BroadcastReceiver {
    private static final String TAG = "UUIDDownloader";
    private static final String DOWNLOAD_URL = "http://api.clozerr.com/vendor/get/all/uuid?access_token=";
    public static final String UUID_FILE_NAME = "UUIDs.txt";
    public static final String ACTION_INITIATE_DOWNLOADER = "InitiateDownloader";
    private static boolean isDownloadDone = false;

    public UUIDDownloader() {
        isDownloadDone = false;
    }

    private static void downloadUUIDs(final Context context) {
        String token = context.getSharedPreferences("USER", 0).getString("token", "");
        final String url = DOWNLOAD_URL + token;
        Log.e(TAG, "downloading; url - " + url);
        new AsyncGet(context, DOWNLOAD_URL + token, new AsyncGet.AsyncResult() {
            @Override
            public void gotResult(String s) {
                Log.e(TAG, "results - " + s);
                try {
                    FileOutputStream fileOutputStream = context.openFileOutput(UUID_FILE_NAME, Context.MODE_PRIVATE);
                    fileOutputStream.write(s.getBytes());
                    fileOutputStream.close();
                    isDownloadDone = true;
                    Log.e(TAG, "disabled UUIDDownloader");
                    ComponentName receiver = new ComponentName(context, UUIDDownloader.class);
                    context.getPackageManager().setComponentEnabledSetting(receiver,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                } catch (Exception e) {
                    e.printStackTrace();
                    Intent nextTrialIntent = new Intent(context, UUIDDownloader.class);
                    nextTrialIntent.setAction(ACTION_INITIATE_DOWNLOADER);
                    context.sendBroadcast(nextTrialIntent);
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
            if (isNetworkAvailable(context))
                if (!isDownloadDone)
                    downloadUUIDs(context);
                else
                    Log.e(TAG, "already downloaded");
            else
                Log.e(TAG, "Network unavailable");
        }
    }
}
