package com.clozerr.app;

import android.net.Uri;

/**
 * Created by Adarsh on 7/2/2015.
 */
public class Constants {

    public static final String SERVER_CLIENT_ID = "http://496568600186-o52fjump15ric3ct4rfoc9i73mnehu2f.apps.googleusercontent.com/";

    public static class RequestCodes {
        public static final int DETAILS_INTENT = 1000;
        public static final int RESUME_SCAN_INTENT = 1001;

        public static final int GOOGLE_SIGN_IN_ACTIVITY = 9000;
    }

    public static class FileNames {
        public static final String BEACONS = "beacons.txt";
        public static final String GEOFENCE_PARAMS = "geofenceParams.txt";
    }

    public static class Actions {

    }

    public static class SPKeys {                // Shared Preferences Keys

    }

    public static class URLBuilders {

        private static final String DEFAULT_SCHEME = "http";
        private static final String DEFAULT_AUTHORITY = "api.clozerr.com";
        
        private static Uri.Builder getNewDefaultBuilder() {
            return new Uri.Builder().scheme(DEFAULT_SCHEME).authority(DEFAULT_AUTHORITY);
        }
        
        public static final Uri.Builder ANALYTICS = getNewDefaultBuilder().path("v2/analytics/hit");
        public static final Uri.Builder AUTH_LOGIN_FACEBOOK = getNewDefaultBuilder().path("auth/login/facebook");
        public static final Uri.Builder AUTH_LOGIN_GOOGLE = getNewDefaultBuilder().path("auth/login/google");
        public static final Uri.Builder GCMREGISTRATION = getNewDefaultBuilder().path("auth/update/gcm");
        public static final Uri.Builder SUGGESTRESTAURANT = getNewDefaultBuilder().path("vendor/request");
        public static final Uri.Builder CATEGORIES = getNewDefaultBuilder().path("v2/vendor/categories/get");
        public static final Uri.Builder NEARBY = getNewDefaultBuilder().path("v2/vendor/search/near");
        public static final Uri.Builder MYCLUBS = getNewDefaultBuilder().path("v2/user/favourites/list");
        public static final Uri.Builder VENDORDETAILS = getNewDefaultBuilder().path("v2/vendor/get/details");
        public static final Uri.Builder OFFERSPAGE = getNewDefaultBuilder().path("v2/vendor/offers/offerspage");
        public static final Uri.Builder PINNEDOFFERS = getNewDefaultBuilder().path("v2/user/add/pinned");
        public static final Uri.Builder FAVORITES = getNewDefaultBuilder().path("v2/user/add/favourites");
        public static final Uri.Builder BEACON_DOWNLOAD = getNewDefaultBuilder().path("v2/vendor/beacons/all");
        public static final Uri.Builder CHECKIN = getNewDefaultBuilder().path("v2/vendor/offers/checkin");
        public static final Uri.Builder ADSOMETHINGHERE = getNewDefaultBuilder().path("v2/user/add/favourites");
        public static final Uri.Builder GEOFENCE_LIST_NEAR = getNewDefaultBuilder().path("v2/geofence/list/near");
        public static final Uri.Builder QRCODE_VALIDATE = getNewDefaultBuilder().path("v2/offers/checkin/qrcodevalidate");
    }
}
