package com.clozerr.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by aravind on 7/7/15.
 */
public class GenUtils {

    private static final String TAG = "GenUtils";

    private GenUtils() {}               // prevent initialization

    public static String getCurrentTimeAsISOString() {
        TimeZone tz = TimeZone.getTimeZone("GMT+0530");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    public static Uri.Builder getClearedUriBuilder(Uri.Builder builder) {
        return builder.clearQuery();
    }

    public static Uri.Builder getDefaultAnalyticsUriBuilder(Context context, String metric) {
//        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("USER", 0);
//        String TOKEN = sharedPreferences.getString("token", "");
        String TOKEN = MainApplication.getInstance().data.userMain.token;
        String nowAsISO = getCurrentTimeAsISOString();

        Uri.Builder result = getClearedUriBuilder(Constants.URLBuilders.ANALYTICS)
                .appendQueryParameter("metric",metric)
                .appendQueryParameter("dimensions[device]", "Android API " + Build.VERSION.SDK_INT)
                .appendQueryParameter("dimensions[id]", Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID))
                .appendQueryParameter("time", nowAsISO)
                .appendQueryParameter("access_token", TOKEN);
        Location lastLocation = GeofenceManagerService.getLastLocation();
        if (lastLocation != null)
            result.appendQueryParameter("latitude", String.valueOf(lastLocation.getLatitude()))
                    .appendQueryParameter("longitude", String.valueOf(lastLocation.getLongitude()));
        return result;
    }

    public static void putAnalytics(final Context context, final String tag, final String fullAnalyticsUrl) {
        Ion.with(context).load(fullAnalyticsUrl).asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            e.printStackTrace();
                        } else {
                            Log.e(tag, "analytics url - " + fullAnalyticsUrl);
                        }
                    }
                });
    }

    public static void writeDownloadedStringToFile(Context context, String result, String fileName)
                                        throws IOException {
        FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        fileOutputStream.write(result.getBytes());
        fileOutputStream.close();
    }

    public static String readFileContentsAsString(Context context, String fileName) throws IOException {
        FileInputStream fileInputStream = context.openFileInput(fileName);
        int size = fileInputStream.available();
        byte[] dataBytes = new byte[size];
        int read = fileInputStream.read(dataBytes);
        return (read == size) ? new String(dataBytes) : null;
    }

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

    public static void putToast(final Context context, final String toastText, final int duration) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, toastText, duration).show();
            }
        });
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isFirstRun(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                .getBoolean(Constants.SPKeys.FIRST_RUN, true);
    }

    public static void updateFirstRun(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(Constants.SPKeys.FIRST_RUN, false).apply();
    }
    public static void showDebugToast(Context context, String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
