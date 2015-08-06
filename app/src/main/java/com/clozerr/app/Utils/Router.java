package com.clozerr.app.Utils;

import android.net.Uri;

import com.clozerr.app.MainApplication;

/**
 * This contains all the routes to our backend server. This is similar to API CONTRACT given to backend team.
 * Any changes with backend API routes will only be reflected by changes in this FIle.
 */
public class Router {

    private static final String DEFAULT_SCHEME = "http";
    private static final String DEFAULT_AUTHORITY = "api.clozerr.com";
    private static final String DEFAULT_AUTHORITY_LOCAL = "localhost:3000";

    private static Uri.Builder getNewDefaultBuilder() {
        return new Uri.Builder().scheme(DEFAULT_SCHEME).authority(DEFAULT_AUTHORITY);
    }

    public static String getClozerrToken() {
        return MainApplication.getInstance().tokenHandler.clozerrtoken;
    };

    public static class Homescreen{
        public static String offerdialog(){
            return getNewDefaultBuilder().path("v2/notifications/list/since")
                    .appendQueryParameter("access_token",getClozerrToken()).build().toString();
        }

    }

    public static class User{
        public static String gcmIdUpdate(String gcmId){
            return getNewDefaultBuilder().path("auth/update/gcm")
                    .appendQueryParameter("gcm_id", gcmId)
                    .appendQueryParameter("access_token",getClozerrToken()).build().toString();
        }
    }

}


