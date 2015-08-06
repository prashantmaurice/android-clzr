package com.clozerr.app;

import org.json.JSONException;
import org.json.JSONObject;

public class MyOffer {
    /*private int stamps;
    private String caption;

    public int getStamps() {
        return stamps;
    }

    public void setStamps(int stamps) {
        this.stamps = stamps;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public MyOffer(String caption,int stamps) {

        this.caption = caption;
        this.stamps = stamps;

    }
    */
    private  String offerid;
    private String mType;
    private String mImageUrl;
    private String mOptionalImageUrl;
    private String mCaption;
    private String mDescription;
    private int mStamps;
    private Boolean mused;
    private Boolean munlocked;
    private SXOfferExtras mSXOfferExtras;
    private String mVendorId;
    private String mVendorName;
    private Boolean visitedstatus;

    public String getType() {
        return mType;
    }

    public String getOfferid(){
        return offerid;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getOptionalImageUrl() {
        return mOptionalImageUrl;
    }

    public String getCaption() {
        return mCaption;
    }

    public String getDescription() {
        return mDescription;
    }

    public int getStamps() {
        return mStamps;
    }

    public Boolean getUsedState() {return mused;}

    public Boolean getUnlocked() {return munlocked;}

    public String getVendorId() { return mVendorId; }

    public String getVendorName() { return mVendorName; }

    public Boolean getVisitedstatus() { return visitedstatus; }

    public MyOffer(String type, String imageUrl, String optionalImageUrl, String caption,
            String description, int stamps, Boolean used, Boolean unlocked, SXOfferExtras sxOfferExtras,
                   String offerid, String vendorId, String vendorName, Boolean visited) {
        this.mType = type;
        this.mImageUrl = imageUrl;
        this.mOptionalImageUrl = optionalImageUrl;
        this.mCaption = caption;
        this.mDescription = description;
        this.mStamps = stamps;
        this.mSXOfferExtras = sxOfferExtras;
        this.mused=used;
        this.munlocked=unlocked;
        this.offerid = offerid;
        this.mVendorId = vendorId;
        this.mVendorName = vendorName;
        this.visitedstatus = visited;
    }

    public MyOffer(String type, String imageUrl, String optionalImageUrl, String caption,
                   String description, int stamps, Boolean used, Boolean unlocked, SXOfferExtras sxOfferExtras,
                   String offerid, Boolean visited) {
        this(type, imageUrl, optionalImageUrl, caption, description, stamps, used, unlocked, sxOfferExtras,
                offerid, "", "",visited);
    }

    public static class SXOfferExtras {
        private int mTotalStamps;
        private double mBillAmount;

        public int getTotalStamps() {
            return mTotalStamps;
        }

        public double getBillAmount() {
            return mBillAmount;
        }

        public SXOfferExtras(int totalStamps, double billAmount) {
            this.mTotalStamps = totalStamps;
            this.mBillAmount = billAmount;
        }
    }

    public static MyOffer parseFromServer(JSONObject obj) throws JSONException {
        String caption = obj.has("caption")?obj.getString("caption"):"";
        String description = obj.getString("description");
        String type = obj.getString("type");
        String vendor_id = obj.getJSONObject("vendor").getString("_id");
        String vendorName = obj.getJSONObject("vendor").getString("name");
        String image = "";
        if(obj.has("image")) obj.getString("image");
        return new MyOffer(type, image, null, caption, description, 0, false, true, null, obj.getString("_id"), vendor_id, vendorName, false);
    }
}