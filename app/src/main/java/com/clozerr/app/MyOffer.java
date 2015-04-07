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
    private String mType;
    private String mImageUrl;
    private String mOptionalImageUrl;
    private String mCaption;
    private String mDescription;
    private int mStamps;
    private SXOfferExtras mSXOfferExtras;

    public String getType() {
        return mType;
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

    public MyOffer(String type, String imageUrl, String optionalImageUrl, String caption,
            String description, int stamps, SXOfferExtras sxOfferExtras) {
        this.mType = type;
        this.mImageUrl = imageUrl;
        this.mOptionalImageUrl = optionalImageUrl;
        this.mCaption = caption;
        this.mDescription = description;
        this.mStamps = stamps;
        this.mSXOfferExtras = sxOfferExtras;
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