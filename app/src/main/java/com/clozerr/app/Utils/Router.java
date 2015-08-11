package com.clozerr.app.Utils;

import android.location.Location;
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

        public static String getUserDataComplete() {
            //http://api.clozerr.com/v2/user/details/get?access_token=8738307ee2b3851ae25025b830de2f73
            return getNewDefaultBuilder().path("v2/user/details/get")
                    .appendQueryParameter("access_token",getClozerrToken()).build().toString();
        }

        public static String getVendorsGifts() {
            //http://api.clozerr.com/v2/vendor/offers/rewardspage?access_token=
            return getNewDefaultBuilder().path("v2/vendor/offers/rewardspage")
                    .appendQueryParameter("access_token",getClozerrToken()).build().toString();
        }

        public static String getNearbyRestaurents(Location location, int offset, int limit) {
            return getNearbyRestaurents(location, offset, limit, null);
        }
        public static String getNearbyRestaurents(Location location, int offset, int limit, String query) {
            return getNearbyRestaurents(location,offset,limit,query,null);
        }
        public static String getNearbyRestaurents(Location location, int offset, int limit, String query, String category) {
            //"http://api.clozerr.com/v2/vendor/search/near?latitude=" + HomeActivity.lat + "&longitude=" + HomeActivity.longi + "&access_token=" + TOKEN"&offset=" + mOffset + "&limit=" + ITEMS_PER_PAGE;ttp://api.clozerr.com/v2/vendor/offers/rewardspage?access_token=
            Uri.Builder builder = getNewDefaultBuilder().path("v2/vendor/search/near")
                    .appendQueryParameter("latitude", "" + location.getLatitude())
                    .appendQueryParameter("longitude", "" + location.getLongitude())
                    .appendQueryParameter("offset", "" + offset)
                    .appendQueryParameter("access_token", getClozerrToken());
            if(limit>=0) builder.appendQueryParameter("limit", "" + limit);
            if(query!=null) builder.appendQueryParameter("name", query);
            if(category!=null) builder.appendQueryParameter("category", category);
            return builder.build().toString();
        }

    }

    public static class Myclubs{
        //http://api.clozerr.com/v2/user/favourites/list?access_token=TOKEN
        public static String getMyclubs(){
            return getNewDefaultBuilder().path("v2/user/favourites/list")
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

    public static class Content{
        public static String getFAQHtml(){
            //http://api.clozerr.com/content?key=faq
            return getNewDefaultBuilder().path("content")
                    .appendQueryParameter("key", "faq").build().toString();
        }
    }

    public static class VendorScreen {
        public static String checkInAReward(String rewardId, String vendorId, String gcmId) {
            //http://api.clozerr.com/v2/vendor/offers/checkin?access_token="+ TOKEN+"&offer_id="+model.rewardId+"&vendor_id="+model.vendorId+"&gcm_id="+ gcmIdEncoded;
            return getNewDefaultBuilder().path("v2/vendor/offers/checkin")
                    .appendQueryParameter("offer_id", rewardId)
                    .appendQueryParameter("vendor_id", vendorId)
                    .appendQueryParameter("gcm_id", gcmId)
                    .appendQueryParameter("access_token", getClozerrToken()).build().toString();
        }
    }

}


