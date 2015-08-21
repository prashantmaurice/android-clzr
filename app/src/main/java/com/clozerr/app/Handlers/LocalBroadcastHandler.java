package com.clozerr.app.Handlers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by maurice on 11/08/15.
 */
public class LocalBroadcastHandler {

    //Custom events that happens within the app
    public static String PROFILE_UPDATED = "PROFILE_UPDATED";
    public static String LOCATION_UPDATED = "LOCATION_UPDATED";
    public static String MYCLUBS_CHANGED = "MYCLUBS_CHANGED";
    public static String MYSTAMPS_CHANGED = "MYSTAMPS_CHANGED";

    public static void sendBroadcast(Context context, String intentCode){
        Intent intent = new Intent(intentCode);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


}
