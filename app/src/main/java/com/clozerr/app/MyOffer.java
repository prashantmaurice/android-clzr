package com.clozerr.app;

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

    public MyOffer(String type, String imageUrl, String optionalImageUrl, String caption,
            String description, int stamps, Boolean used, Boolean unlocked, SXOfferExtras sxOfferExtras,
                   String offerid, String vendorId, String vendorName) {
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
    }

    public MyOffer(String type, String imageUrl, String optionalImageUrl, String caption,
                   String description, int stamps, Boolean used, Boolean unlocked, SXOfferExtras sxOfferExtras,
                   String offerid) {
        this(type, imageUrl, optionalImageUrl, caption, description, stamps, used, unlocked, sxOfferExtras,
                offerid, "", "");
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
}