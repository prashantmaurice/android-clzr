package com.clozerr.app;

import android.net.Uri;

/**
 * Created by Adarsh on 7/2/2015.
 */
public class Constants {
    
    public static class URLBuilders {

        private static final String DEFAULT_SCHEME = "http";
        private static final String DEFAULT_AUTHORITY = "api.clozerr.com";
        
        private static Uri.Builder getNewDefaultBuilder() {
            return new Uri.Builder().scheme(DEFAULT_SCHEME).authority(DEFAULT_AUTHORITY);
        }
        
        public static final Uri.Builder ANALYTICS = getNewDefaultBuilder().path("v2/analytics/hit");
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
    }
}
