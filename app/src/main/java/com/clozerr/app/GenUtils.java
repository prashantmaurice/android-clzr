package com.clozerr.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by aravind on 7/7/15.
 */
public class GenUtils {

    private static final String TAG = "GenUtils";

    private GenUtils() {}               // prevent initialization

    public static Uri.Builder getClearedUriBuilder(Uri.Builder builder) {
        return builder.clearQuery();
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

}
