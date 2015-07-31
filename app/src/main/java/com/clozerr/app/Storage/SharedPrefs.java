package com.clozerr.app.Storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.clozerr.app.Utils.Logg;

import org.json.JSONException;
import org.json.JSONObject;


public class SharedPrefs {

    private static SharedPrefs instance;
    private Context mContext;
    SharedPreferences prefs_trackers,prefs_apply;
    String TAG = "SHAREDPREFS";

    public enum PrefsTypes{ TRACKERS}//all different bundles of prefs stored
    public final static String PREF_TRACKERS = "trackedPrefs";


    //ALL DATA STORED
    public JSONObject userData = new JSONObject();

    private SharedPrefs(Context context) {
        this.mContext = context;
        refillPrefs();
    }
    public static SharedPrefs getInstance(Context context) {
        if(instance == null) instance = new SharedPrefs(context);
        return instance;
    }

    /**-----------------------EXPOSED FUNCTIONS(use these to save data)-------------------------------*/

    //USER DATA
    public void saveUserData() {
        Logg.d(TAG, "saved all UserData in Prefs");
        Logg.d("SAVE PREFS userData :=====", userData.toString());
        SharedPreferences.Editor editor = prefs_trackers.edit();
        editor.putString(Str.userData, userData.toString());
        editor.apply();
    }





    /**-----------------------COMMMON FUNCTIONS(dont bother about these)-------------------------------*/
    private void refillPrefs(){
        Logg.e(TAG,"refill preferences called..!");
        prefs_trackers = getSharedPreferencesFor(PREF_TRACKERS);
        prefs_trackers.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                Logg.e(TAG,"prefs changed:"+s);
            }
        });
        try {
            userData = new JSONObject(getString(PrefsTypes.TRACKERS, Str.userData,"{}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private SharedPreferences getSharedPreferencesFor(String prefName) {
        return mContext.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }
    public String getString(PrefsTypes type, String variable, String defaultValue){
        String value = "";
        switch (type){
            case TRACKERS:value = prefs_trackers.getString(variable,defaultValue); break;
        }
        return value;
    }
    public boolean getBoolean(PrefsTypes type, String variable, boolean defaultValue){
        boolean value = false;
        switch (type){
            case TRACKERS:value = prefs_trackers.getBoolean(variable,defaultValue); break;
        }
        return value;
    }
    public Long getLong(PrefsTypes type, String variable, Long defaultValue){
        Long value = 0L;
        switch (type){
            case TRACKERS:value = prefs_trackers.getLong(variable, defaultValue); break;
        }
        return value;
    }

    //Holder for all strings used
    public static class Str{
        static String userData = "userData";
    }


}
