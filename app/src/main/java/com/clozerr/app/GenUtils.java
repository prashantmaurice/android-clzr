package com.clozerr.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created by aravind on 7/7/15.
 */
public class GenUtils {
    private static final String TAG = "GenUtils";

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

    public static class RunManager {
        private static final String TAG = "RunManager";
        private static final String KEY_PREFIX = TAG + "-";

        public static final HashSet<String> keySet = new HashSet<>();

        private String mKey;

        public RunManager(String key) {
            mKey = KEY_PREFIX + key;
            keySet.add(mKey);
        }

        private static SharedPreferences getPreferences(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        }

        public static void initKeys(Context context) {
            keySet.clear();
            for (String key : getPreferences(context).getAll().keySet())
                if (key.startsWith(KEY_PREFIX))
                    keySet.add(key);
        }

        public static void clearKeys(Context context) {
            SharedPreferences.Editor editor = getPreferences(context).edit();
            for (String key : keySet)
                editor.remove(key);
            editor.apply();
        }

        public boolean isRunning(Context context) {
            return getPreferences(context).getBoolean(mKey, false);
        }

        public boolean signalStart(Context context) {
            if (!isRunning(context)) {
                getPreferences(context).edit().putBoolean(mKey, true).apply();
                return true;
            }
            return false;
        }

        public boolean signalStop(Context context) {
            if (isRunning(context)) {
                getPreferences(context).edit().putBoolean(mKey, false).apply();
                return true;
            }
            return false;
        }
    }
}
