package com.clozerr.app.Utils;

import android.net.Uri;

import java.util.concurrent.TimeUnit;

/**
 * Created by Adarsh on 7/2/2015.
 */
public class Constants {

    private static final String TAG = "Constants";

    private Constants() {}          // prevent initialization

    public static final String SERVER_CLIENT_ID = "http://496568600186-o52fjump15ric3ct4rfoc9i73mnehu2f.apps.googleusercontent.com/";
    public static final String APP_PACKAGE_NAME = Constants.class.getPackage().getName();

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
        public static final String ACTION_INITIATE_DOWNLOADER = "com.clozerr.app.ACTION_INITIATE_DOWNLOADER";
        public static final String ACTION_RESUME_SCAN = "com.clozerr.app.ACTION_RESUME_SCAN";
    }

    public static class Timeouts {
        public static final long IN_STORE_INSTALL_DETECTION = TimeUnit.MILLISECONDS.convert(10L, TimeUnit.SECONDS);
    }

    /**
     * Shared Preferences keys.
     */
    public static class SPKeys {
        public static final String FIRST_RUN = APP_PACKAGE_NAME + ".FIRST_RUN";
        public static final String BLE = APP_PACKAGE_NAME + ".BLE";
        public static final String BEACON_UUID = APP_PACKAGE_NAME + ".BEACON_UUID";
        public static final String APP_DISABLE_BT = APP_PACKAGE_NAME + ".APP_DISABLE_BT";

        public static final String PREFIX_REJECT_LIST = APP_PACKAGE_NAME + ".Rejects-";
    }

    public static class Metrics {
        public static final String GEOFENCE_TRANSITION = "Geofence_Transition";
        public static final String HOME_SCREEN = "Clozerr_Home_Screen";
        public static final String IN_STORE_INSTALL = "In_Store_Install";
        public static final String BEACON_DETECTION = "Beacon_Detection";
        public static final String VENDOR_SCREEN = "Vendor Screen";

        public static final String SUFFIX_VENDOR_SCREEN = " Vendor Screen";
    }

    public static class URLBuilders {

        private static final String DEFAULT_SCHEME = "http";
        private static final String DEFAULT_AUTHORITY = "api.clozerr.com";
        private static final String DEFAULT_AUTHORITY_LOCAL = "localhost:3000";
        
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
