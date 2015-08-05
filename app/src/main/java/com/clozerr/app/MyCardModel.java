package com.clozerr.app;

import android.location.Location;

import com.clozerr.app.Activities.HomeScreens.HomeActivity;

/**
 * Created by jun on 29/11/14.
 */
public class MyCardModel {

    private String imageId;
    private String franchiseId;
    private String vendorId;
    private String offerId;
    private String title;
    private int stamps;
    private String distance;

    private Location location;

    public MyCardModel(String title, int stamps, double lat, double longi, String imageId, String fId, String vId) {
        this.imageId = imageId;
        this.title = title;
        this.stamps=stamps;
        this.location = new Location("Vendor " + vendorId + " location");
        this.location.setLatitude(lat);
        this.location.setLongitude(longi);

        Location userLocation = new Location("User location");
        userLocation.setLatitude(Double.valueOf(HomeActivity.lat));
        userLocation.setLongitude(Double.valueOf(HomeActivity.longi));
        this.distance=userLocation.distanceTo(this.location)+" km";

        this.franchiseId = fId;
        this.vendorId = vId;

    }

    public String getImageId() {
        return imageId;
    }
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
    public String getStamps() {
        return stamps+"";
    }

    public String getTitle() {
        return title;
    }
    public String getDistance() {
        return distance;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getOfferId() { return offerId; }

    public String getVendorId() {
        return vendorId;
    }


}