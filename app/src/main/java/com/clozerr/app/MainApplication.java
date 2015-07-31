package com.clozerr.app;

import android.app.Application;
import android.content.SharedPreferences;

import com.clozerr.app.Storage.Data;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

/**
 * Created by Maurice on 19/05/15.
 */
public class MainApplication extends Application {

    //private static final String PROPERTY_ID = "UA-60919025-1";UA-59529735-1

    public static int GENERAL_TRACKER = 0;
    private static MainApplication sInstance;
    SharedPreferences sharedPreferences;
    public Data data;
    boolean mBound = false;

    public enum TrackerName {
        APP_TRACKER,
        GLOBAL_TRACKER,
        ECOMMERCE_TRACKER,
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();






    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        sharedPreferences = getSharedPreferences("Tinystep", MODE_PRIVATE);//will be deprecated soon
        data = Data.getInstance(this);//this gets all the data from preferances and Db
    }

    public synchronized static MainApplication getInstance() {
        return sInstance;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            Tracker t = analytics.newTracker(R.xml.app_tracker);

            //t.enableAdvertisingIdCollection(true);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }




}
