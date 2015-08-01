package com.clozerr.app.Models;

import android.content.Context;

import com.clozerr.app.Storage.SharedPrefs;

import org.json.JSONException;

/**
 * This contains all the User data excluding kids,
 */
public class CacheMain {
    private Context mContext;
    private static CacheMain instance;
    private SharedPrefs sPrefs;
    public String name;

    public String about_us_html;

    private CacheMain(Context context) {
        mContext = context;
        pullCacheDataFromLocal();
    }
    public static CacheMain getInstance(Context context) {
        if(instance == null) {
            instance = new CacheMain(context);
        }
        return instance;
    }


    //LOCAL STORAGE ENCODERS
    public void pullCacheDataFromLocal() {
        sPrefs = SharedPrefs.getInstance(mContext);
        try {
            about_us_html = (sPrefs.cacheData.has("about_us_html"))?sPrefs.cacheData.getString("about_us_html"):"";

        } catch (JSONException e) {e.printStackTrace();}
    }
    public void saveCacheDataLocally() {
        try {
            sPrefs.cacheData.put("about_us_html", about_us_html);
        } catch (JSONException e) {e.printStackTrace();}
        sPrefs.saveCacheData();
    }


}
