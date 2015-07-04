package com.clozerr.app;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Created by Adarsh on 7/2/2015.
 */
public class Constants {
      public static class URLBuilders {
          public static final Uri.Builder ANALYTICS = new Uri.Builder()
                  .scheme("http").authority("api.clozerr.com").path("v2/analytics/hit");
          public static final Uri.Builder GCMREGISTRATION = new Uri.Builder()
                  .scheme("http").authority("api.clozerr.com").path("auth/update/gcm");
          public static final Uri.Builder SUGGESTRESTAURANT = new Uri.Builder()
                  .scheme("http").authority("api.clozerr.com").path("vendor/request");
          public static final Uri.Builder CATEGORIES = new Uri.Builder()
                  .scheme("http").authority("api.clozerr.com").path("v2/vendor/categories/get");
          public static final Uri.Builder NEARBY = new Uri.Builder()
                  .scheme("http").authority("api.clozerr.com").path("v2/vendor/search/near");
          public static final Uri.Builder MYCLUBS = new Uri.Builder()
                  .scheme("http").authority("api.clozerr.com").path("v2/user/favourites/list");
          public static final Uri.Builder VENDORDETAILS = new Uri.Builder()
                  .scheme("http").authority("api.clozerr.com").path("v2/vendor/get/details");
          public static final Uri.Builder OFFERSPAGE = new Uri.Builder()
                  .scheme("http").authority("api.clozerr.com").path("v2/vendor/offers/offerspage");
          public static final Uri.Builder PINNEDOFFERS = new Uri.Builder()
                  .scheme("http").authority("api.clozerr.com").path("v2/user/add/pinned");
          public static final Uri.Builder FAVORITES = new Uri.Builder()
                  .scheme("http").authority("api.clozerr.com").path("v2/user/add/favourites");
          public static final Uri.Builder BEACONDOWNLOAD = new Uri.Builder()
                  .scheme("http").authority("api.clozerr.com").path("v2/vendor/beacons/all");
          public static final Uri.Builder CHECKIN = new Uri.Builder()
                  .scheme("http").authority("api.clozerr.com").path("v2/vendor/offers/checkin");
          public static final Uri.Builder ADSOMETHINGHERE = new Uri.Builder()
                  .scheme("http").authority("api.clozerr.com").path("v2/user/add/favourites");
      }
}
