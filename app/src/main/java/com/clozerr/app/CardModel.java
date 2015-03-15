package com.clozerr.app;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by jun on 29/11/14.
 */
public class CardModel {

    private String imageId;
    private String franchiseId;
    private String vendorId;
    private String offerId;
    private String title;
    private String caption;
    private String offerDescription;
    private String distance;
    private Double lat;
    private Double longi;
    private String phonenumber;
    private String vendorDescription;
    private int stamps;
    private String stampString;
    public CardModel(String title, String phoneNumber,  String vendorDescription, JSONArray offers,  double lat, double longi, String imageId, String fId, String vId,int stamps) {
        this.imageId = imageId;
        this.title = title;
        if (offers.length() == 0) {
            this.caption = "No offers available";
            this.stampString = "All Offers Done";
        }
        else {
            try {
                this.caption = offers.getJSONObject(0).getString("caption");
                this.offerId = offers.getJSONObject(0).getString("_id");
                this.offerDescription = offers.getJSONObject(0).getString("description");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            this.stampString = stamps + " stamp(s)";
        }
        this.stamps=stamps;

        Location location = new Location("Vendor " + vendorId + " location");
        location.setLatitude(lat);
        location.setLongitude(longi);

        Location userLocation = new Location("User location");

        userLocation.setLatitude(Home.lat);
        userLocation.setLongitude(Home.longi);
        this.distance = String.format("%.2f", userLocation.distanceTo(location)/1000)+" km";

        this.franchiseId = fId;
        this.vendorId = vId;
        this.lat = lat;
        this.longi = longi;
        this.phonenumber = phoneNumber;
        this.vendorDescription = vendorDescription;

    }

    public String getImageId() {
        return imageId;
    }
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
    public String getCaption() {
        return caption;
    }
    public String getOfferDescription() {
        return offerDescription;
    }
    public void setDesc(String desc) {
        this.caption = desc;
    }
    public String getTitle() {
        return title;
    }
    public String getDistance() {
        return distance;
    }
    public Double getLat(){ return lat;}
    public Double getLong(){ return longi;}
    public void setTitle(String title) {
        this.title = title;
    }
    @Override
    public String toString() {
        return title + "\n" + caption;
    }

    public String getVendorId() {
        return vendorId;
    }

    public String getOfferId() {
        return offerId;
    }
    public String getPhoneNumber() {
        return phonenumber;
    }
    public String getVendorDescription() {
        return vendorDescription;
    }
    public String getStampString() {
        return stampString;
    }
    public int getStamps() {return stamps; }
}