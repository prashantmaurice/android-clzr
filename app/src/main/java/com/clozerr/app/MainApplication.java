package com.clozerr.app;

import android.app.Application;
import android.content.SharedPreferences;
import android.location.Location;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.clozerr.app.Handlers.TokenHandler;
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
    public TokenHandler tokenHandler;
    public Location location = new Location("Location");
    boolean mBound = false;
    RequestQueue queue;
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

        //set default location
        location.setLatitude(13);
        location.setLongitude(80.2);

        sharedPreferences = getSharedPreferences("Clozerr", MODE_PRIVATE);//will be deprecated soon
        data = Data.getInstance(this);//this gets all the data from preferances and Db
        queue = Volley.newRequestQueue(this);
        tokenHandler = TokenHandler.getInstance(this);
    }

    public synchronized static MainApplication getInstance() {
        return sInstance;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public RequestQueue getRequestQueue() {
        return queue;
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
