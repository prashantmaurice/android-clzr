package com.clozerr.app;

import android.location.Location;

import org.json.JSONArray;

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
    private String distanceString;
    private double distance;
    private Double lat;
    private Double longi;
    private String phonenumber;
    private String vendorDescription;
    private int stamps;
    private JSONArray offers;
    private String stampString;
    private boolean active;

    public CardModel(String title, String phonenumber,  String vendorDescription, JSONArray offers,  double lat, double longi, String imageId, String fId, String vId,int stamps, String caption, Boolean active) {
        this.imageId = imageId;
        this.title = title;
        this.offers = offers;
        this.stamps = stamps;
        /*if (offers.length() == 0) {
            this.caption = "No offers available";
            this.stampString = "All Offers Done";
        }
        else {
            try {
                this.caption = offers.getJSONObject(0).getString("caption");
                this.offerId = offers.getJSONObject(0).getString("_id");
                this.offerDescription = offers.getJSONObject(0).getString("description");
            } catch (JSONException e) {
                //e.printStackTrace();
            }
            this.stampString = stamps + " stamp(s)";
        }*/
        this.caption = caption;
        this.active = active;
        Location location = new Location("Vendor " + vendorId + " location");
        location.setLatitude(lat);
        location.setLongitude(longi);

        Location userLocation = new Location("User location");

        userLocation.setLatitude(MainApplication.getInstance().location.getLatitude());
        userLocation.setLongitude(MainApplication.getInstance().location.getLongitude());
        //distance = userLocation.distanceTo(location) / 1000;
        this.distanceString = this.caption;
        /*if (lat > 0.0 && longi > 0.0)
            this.distanceString = String.format("%.2f", distance) + " km";
        else
            this.distanceString = "";*/


        this.franchiseId = fId;
        this.vendorId = vId;
        this.lat = lat;
        this.longi = longi;
        this.phonenumber = phonenumber;
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
    public double getDistance() { return distance; }
    public String getDistanceString() {
        return distanceString;
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
    public String getPhonenumber() {
        return phonenumber;
    }
    public String getVendorDescription() {
        return vendorDescription;
    }
    public String getStampString() {
        return stampString;
    }
    public int getMaxStamps(){
        int i=0;
        try {
            for(i = 0 ; i < offers.length() ; i++){
                String cap=offers.getJSONObject(i).getString("caption");
            }
        } catch (Exception e) {

            e.printStackTrace();
            return i;
        }
        return i;
    }
    public int getStamps() {return stamps; }

    public boolean getActive() {
        return active;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}